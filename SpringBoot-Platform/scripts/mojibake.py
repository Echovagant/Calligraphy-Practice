#!/usr/bin/env python3
"""
Detect and repair UTF-8-read-as-GBK mojibake in Java sources.

Background
----------
Source files were UTF-8, got decoded as GBK (lossy), then re-encoded as UTF-8.
The GBK decoder consumes two bytes at a time.  Whenever the trail byte fell
below 0x40 the pair was invalid and the decoder replaced the pair with '?'.

Each '?' therefore stands for exactly two lost original bytes:

  * a GBK lead byte L (0x81..0xFE), and
  * the ASCII trail byte T (0x00..0x3F) that followed it.

Because T was ASCII, the damage falls into four classes:

  A  the CJK run itself is intact -> reversible:
     run.encode('gbk').decode('utf-8')
  B  T was '"'                    -> the string literal lost its closing quote
  C  T was '\\n'                  -> a // comment swallowed the statement that
     followed it
  D  T was anything else          -> the byte cannot be recovered, a human
     has to decide

Telling a damage marker from real code
--------------------------------------
A '?' produced by the accident always follows a non-ASCII character: the
decoder had just emitted a double byte character and then hit an invalid
lead byte.  A '?' that follows an ASCII character is ordinary Java -- a
ternary operator, a regex quantifier -- and is left strictly alone.

Deciding whether rule B applies
-------------------------------
A closing quote only has to be added when the string literal really lost
it.  The trail byte is a single ASCII byte, so a line either lost a quote
or it did not, and the quote counts tell which: the damaged line holds one
quote fewer than the original.  For a '?' inside a string literal the site
that closed the literal is the one where the number of quotes *after* it
is even while the number *before* it is odd.

The '?' also ate the last byte of the CJK character in front of it, so
that character stays broken.  Rule A cannot restore it: the run is one
byte short.  Such lines are reported to a human.

Usage
-----
  python scripts/mojibake.py selftest
  python scripts/mojibake.py detect  [--root DIR]
  python scripts/mojibake.py repair  [--root DIR] [--apply] [--report FILE]
"""
import argparse
import io
import os
import re
import sys

CJK_RUN = re.compile(r"[\u3000-\u9fff\uff00-\uffef]+")
# A mojibake run, and the text it decodes back to, must both look like real
# CJK text.  Besides the CJK block the fullwidth forms (U+FF00..U+FFEF) and
# the kana and Bopomofo ranges show up in mojibake, because the GBK decoder
# produced them out of the bytes of the original text.  ASCII letters belong
# to the set as well: the run "\u934d\u5b72" decodes back to "\u548cS", the
# "S" of "Setter", and "\u5a23\u8bf2\u59de\u7f02\u54c4\u76af\u9428\u5227" decodes
# back to "\u6dfb\u52a0\u7f3a\u5c11\u7684g".
MOJIBAKE_TEXT = re.compile(
    r"^[\u3000-\u9fff\u3040-\u30ff\u3100-\u312f\uff00-\uffef"
    r"A-Za-z\u2018\u2019\u201c\u201d\u3001\u3002\uff01\uff0c"
    r"\uff1a\uff1b\uff08\uff09\u00b7\u2014\-\s]+$"
)
# a '?' immediately followed by one of these means a swallowed code statement
CODE_AFTER_Q = re.compile(
    r"^\s*(?:java\.|com\.|org\.|this\.|new\s|return\s|private\s|public\s"
    r"|protected\s|static\s|final\s|int\s|long\s|String\s|boolean\s|void\s"
    r"|List<|Map<|Optional<|@|import\s|package\s)"
)
# A '?' that swallowed the newline of a comment is followed by the
# indentation of the next source line and then by code.  The indentation is
# what tells the two cases apart: when the '?' ate an ordinary character the
# text behind it continues the same line and is not indented at all.
INDENTED_CODE = re.compile(r"^[ \t]{2,}[A-Za-z_$@{}]")

SRC_DIRS = ("src/main/java", "src/main/resources")
EXTENSIONS = (".java", ".sql", ".properties")

# lexical states
CODE = "code"
STRING = "string"
CHAR = "char"
LINE_COMMENT = "line_comment"
BLOCK_COMMENT = "block_comment"


def quote_positions(line):
    """Indices of the unescaped '"' characters of `line`."""
    out = []
    i = 0
    while i < len(line):
        if line[i] == "\\":
            i += 2
            continue
        if line[i] == '"':
            out.append(i)
        i += 1
    return out


def quotes_balanced(line):
    """True when `line` holds an even number of unescaped '"' characters."""
    return len(quote_positions(line)) % 2 == 0


def has_unbalanced_quotes(line):
    """True when `line` holds an odd number of unescaped '"' characters."""
    return not quotes_balanced(line)


def guarded_roundtrip(run):
    """Reverse the transform for a run that lost no bytes; None if not reversible."""
    try:
        fixed = run.encode("gbk").decode("utf-8")
    except (UnicodeEncodeError, UnicodeDecodeError):
        return None
    if fixed == run:
        return None
    # both sides must look like real CJK text, else it is a false positive
    if not MOJIBAKE_TEXT.match(fixed) or not MOJIBAKE_TEXT.match(run):
        return None
    # a round trip that lands in the C1 range (U+0080..U+00FF) is a false
    # positive: U+8DEF decodes back to U+00B7, U+805F to U+0085, U+807D to
    # U+00A0, ...  Keep this file pure ASCII so the GBK console can never
    # choke on a stray byte.
    for ch in fixed:
        if 0x80 <= ord(ch) <= 0xFF:
            return None
    return fixed


def restore_runs(text):
    """Rule A: restore every lossless CJK run."""
    return CJK_RUN.sub(lambda m: guarded_roundtrip(m.group(0)) or m.group(0), text)


def is_truncated_mojibake(run):
    """True when `run` is a mojibake run whose last character lost a byte.

    Such a run decodes as UTF-8 up to its last byte and then runs out of
    data.  A run of genuine Chinese fails differently: its GBK form is not
    a UTF-8 prefix at all.
    """
    try:
        raw = run.encode("gbk", "replace")
    except UnicodeEncodeError:
        return False
    try:
        raw.decode("utf-8")
    except UnicodeDecodeError as exc:
        return exc.reason == "unexpected end of data"
    return False


def damaged_runs(text):
    """The CJK runs of `text` that rule A cannot restore."""
    return [m.group(0) for m in CJK_RUN.finditer(text)
            if guarded_roundtrip(m.group(0)) is None]


def damage_sites(line):
    """Indices of the '?' characters that are corruption markers.

    A '?' that follows an ASCII character is ordinary Java -- a ternary
    operator or a regex quantifier -- and is never a damage marker.
    """
    sites = []
    for i, ch in enumerate(line):
        if ch != "?" or i == 0:
            continue
        if ord(line[i - 1]) < 0x80:
            continue
        sites.append(i)
    return sites


def scan_line(line, in_block):
    """Lex one line of Java.

    Returns (states, newline_sites, in_block_after).  `states` labels every
    character with the state it sits inside.  `newline_sites` lists the
    '?' characters that swallowed the newline which ended a comment: the
    text behind them is code again, so the scan continues there.
    """
    states = []
    newline_sites = []
    i = 0
    n = len(line)
    while i < n:
        c = line[i]
        if in_block:
            states.append(BLOCK_COMMENT)
            if c == "*" and i + 1 < n and line[i + 1] == "/":
                states.append(BLOCK_COMMENT)
                in_block = False
                i += 2
                continue
            i += 1
            continue
        if c == "/" and i + 1 < n and line[i + 1] == "/":
            # a line comment runs to the end of the line, but a '?' in it
            # may have swallowed the newline that ended it
            j = i
            while j < n:
                if line[j] == "?" and j > 0 and ord(line[j - 1]) >= 0x80:
                    tail = line[j + 1:]
                    if CODE_AFTER_Q.match(tail) or INDENTED_CODE.match(tail):
                        newline_sites.append(j)
                        states.extend([LINE_COMMENT] * (j - i + 1))
                        i = j + 1
                        j = i
                        continue
                states.append(LINE_COMMENT)
                j += 1
            break
        if c == "/" and i + 1 < n and line[i + 1] == "*":
            states.append(BLOCK_COMMENT)
            states.append(BLOCK_COMMENT)
            in_block = True
            i += 2
            continue
        if c == '"':
            states.append(STRING)
            i += 1
            while i < n:
                d = line[i]
                states.append(STRING)
                if d == "\\" and i + 1 < n:
                    states.append(STRING)
                    i += 2
                    continue
                if d == '"':
                    i += 1
                    break
                i += 1
            continue
        if c == "'":
            states.append(CHAR)
            i += 1
            while i < n:
                d = line[i]
                states.append(CHAR)
                if d == "\\" and i + 1 < n:
                    states.append(CHAR)
                    i += 2
                    continue
                if d == "'":
                    i += 1
                    break
                i += 1
            continue
        states.append(CODE)
        i += 1
    return states, newline_sites, in_block


def _insert_quotes(line, sites):
    """Return `line` with a '"' inserted behind each site of `sites`."""
    out = []
    for i, ch in enumerate(line):
        out.append(ch)
        if i in sites:
            out.append('"')
    return "".join(out)


def _choice_is_consistent(text, string_sites, chosen):
    """True when `chosen` is a consistent set of quote eating sites.

    A site that ate a closing quote closes the string literal it sits in,
    so the quote inserted behind it must be the one that ends the literal.
    Every other site of the same literal must stay inside a string.
    """
    repaired = _insert_quotes(text, chosen)
    states, _, _ = scan_line(repaired, False)
    # no string may be left open at the end of the line
    if states and states[-1] == STRING:
        return False
    # every chosen site must sit inside a string literal
    for s in chosen:
        if states[s] != STRING:
            return False
    # every site that did not eat a quote must stay inside a string
    for s in string_sites:
        if s in chosen:
            continue
        if states[s] != STRING:
            return False
    return True


def select_quote_sites(text, string_sites):
    """Pick the sites that ate a closing quote.

    A line lost at most one quote per '?', so the candidates are few.  The
    set is accepted when exactly one of them yields a line whose string
    literals are all closed and whose remaining sites still sit inside a
    string.  An ambiguous line is left alone and reported to a human.
    """
    if not string_sites:
        return []
    found = None
    for mask in range(1 << len(string_sites)):
        chosen = [string_sites[i] for i in range(len(string_sites))
                  if mask >> i & 1]
        if _choice_is_consistent(text, string_sites, chosen):
            if found is not None:
                return None
            found = chosen
    return found or []


def _repair_segment(text, in_block):
    """Repair one line segment.  Returns (repaired, [unresolved_offsets])."""
    states, newline_sites, _ = scan_line(text, in_block)
    sites = damage_sites(text)
    if not sites:
        return text, []
    string_sites = [s for s in sites if states[s] == STRING]
    chosen = select_quote_sites(text, string_sites)
    if string_sites and chosen is None:
        # ambiguous: leave the whole segment for a human
        return text, sites
    out = []
    unresolved = []
    for i, ch in enumerate(text):
        if i in chosen:
            out.append('"')
            continue
        if ch == "?" and i in sites:
            out.append(ch)
            unresolved.append(i)
            continue
        out.append(ch)
    return "".join(out), unresolved


def repair_line(line, lexer):
    """Apply rules B and C to one line.

    Returns (repaired_text, damaged) where `damaged` is True when the line
    still carries damage that a human has to look at.
    """
    restored = restore_runs(line)
    sites = damage_sites(restored)
    if not sites:
        # No damage marker, but the line can still hold a CJK run whose last
        # character lost a byte.  That one has to reach the report as well.
        return restored, _segment_damaged(restored)
    states, newline_sites, in_block = scan_line(restored, lexer.in_block_comment)

    # split the line at the recovered newlines; the '?' becomes the newline
    segments = []
    prev = 0
    for s in newline_sites:
        segments.append((restored[prev:s], lexer.in_block_comment))
        prev = s + 1
        # behind a '//' comment the text is code again, a block comment goes on
        if states[s] != BLOCK_COMMENT:
            lexer.in_block_comment = False
    segments.append((restored[prev:], lexer.in_block_comment))

    out = []
    unresolved = []
    damaged = False
    offset = 0
    for text, in_block in segments:
        piece, unres = _repair_segment(text, in_block)
        out.append(piece)
        unresolved.extend(o + offset for o in unres)
        offset += len(piece) + 1
        if damage_sites(text) or _segment_damaged(text):
            damaged = True
    if newline_sites:
        # a '?' that swallowed a newline leaves the CJK run in front of it
        # one byte short, so the text stays broken
        damaged = True
    return "\n".join(out), damaged


def _segment_damaged(text):
    """True when `text` holds a CJK run that lost its last byte."""
    for m in CJK_RUN.finditer(text):
        if is_truncated_mojibake(m.group(0)):
            return True
    return False


def java_files(root):
    for rel_dir in SRC_DIRS:
        base = os.path.join(root, rel_dir)
        if not os.path.isdir(base):
            continue
        for dirpath, dirnames, filenames in os.walk(base):
            dirnames[:] = [d for d in dirnames if d != "target"]
            for fn in sorted(filenames):
                if fn.endswith(EXTENSIONS):
                    yield os.path.join(dirpath, fn)


def process_text(text):
    """Returns (new_text, needs_human).

    `needs_human` holds one entry per output line that still carries
    damage: (line_after_repair, line_before_repair, text).
    """
    lexer = LexerState()
    out_lines = []
    needs_human = []
    for line_no, line in enumerate(text.splitlines(), 1):
        repaired, damaged = repair_line(line, lexer)
        pieces = repaired.split("\n")
        for idx, piece in enumerate(pieces):
            out_lines.append(piece)
            if damaged and _piece_damaged(piece):
                needs_human.append((len(out_lines), line_no, piece))
    new_text = "\n".join(out_lines) + ("\n" if text.endswith("\n") else "")
    return new_text, needs_human


def _piece_reason(piece):
    """Why a piece still needs a human: 'marker', 'truncated-cjk' or None."""
    if damage_sites(piece):
        return "marker"
    for m in CJK_RUN.finditer(piece):
        if is_truncated_mojibake(m.group(0)):
            return "truncated-cjk"
    return None


def _piece_damaged(piece):
    """True when `piece` still carries damage a human has to look at."""
    return _piece_reason(piece) is not None


class LexerState:
    """The block comment state carried from one line to the next."""

    def __init__(self):
        self.in_block_comment = False


def cmd_detect(args):
    root = args.root
    total_runs = 0
    total_sites = 0
    affected = []
    for path in java_files(root):
        with open(path, "rb") as fh:
            text = fh.read().decode("utf-8", errors="replace")
        runs = sum(1 for m in CJK_RUN.finditer(text) if guarded_roundtrip(m.group(0)))
        sites = sum(len(damage_sites(line)) for line in text.splitlines())
        if runs or sites:
            total_runs += runs
            total_sites += sites
            affected.append((os.path.relpath(path, root), runs, sites))
    affected.sort(key=lambda x: (-(x[1] + x[2]), x[0]))
    print(f"files with mojibake  : {len(affected)}")
    print(f"reversible CJK runs   : {total_runs}")
    print(f"damage markers        : {total_sites}")
    for rel, runs, sites in affected[:40]:
        print(f"  runs={runs:4d} marks={sites:4d}  {rel}")
    return 0 if not affected else 1


def cmd_repair(args):
    root = args.root
    report = io.StringIO()
    report.write("# Mojibake repair report\n\n")
    report.write("Line numbers refer to the tree AFTER `repair --apply` rewrote it.\n")
    report.write("The line number before the repair is given in brackets.\n")
    report.write("Paths are relative to the root and use forward slashes.\n\n")
    report.write("Every listed line still needs a human to look at it:\n\n")
    report.write("- `[marker]` the line still holds a `?` damage marker;\n")
    report.write("- `[truncated-cjk]` the CJK run in the line lost its last byte,\n")
    report.write("  so the character before the damage stays broken.\n\n")
    changed_files = 0
    unresolved_total = 0
    reasons = {"marker": 0, "truncated-cjk": 0}
    skipped = []
    pending = []
    for path in java_files(root):
        with open(path, "rb") as fh:
            raw = fh.read()
        try:
            text = raw.decode("utf-8")
        except UnicodeDecodeError as exc:
            rel = os.path.relpath(path, root).replace(os.sep, "/")
            skipped.append((rel, str(exc)))
            continue
        new_text, needs_human = process_text(text)
        rel = os.path.relpath(path, root).replace(os.sep, "/")
        if new_text != text:
            changed_files += 1
            pending.append((path, new_text))
        for line_after, line_before, snippet in needs_human:
            unresolved_total += 1
            reason = _piece_reason(snippet)
            reasons[reason] += 1
            report.write(f"UNRESOLVED {rel}:{line_after} [{reason}]\n")
            report.write(f"    {snippet.strip()[:200]}  [was line {line_before}]\n\n")
    if skipped:
        report.write("SKIPPED (not valid UTF-8, left untouched)\n")
        for rel, why in skipped:
            report.write(f"    {rel}: {why}\n")
        report.write("\n")
    if args.apply:
        write_all(pending)
    report.write(f"\nfiles changed    : {changed_files}\n")
    report.write(f"unresolved lines : {unresolved_total}\n")
    report.write(f"  marker         : {reasons['marker']}\n")
    report.write(f"  truncated-cjk  : {reasons['truncated-cjk']}\n")
    if skipped:
        report.write(f"skipped files    : {len(skipped)}\n")
    if args.report:
        with open(args.report, "w", encoding="utf-8") as fh:
            fh.write(report.getvalue())
    print(f"files changed    : {changed_files}")
    print(f"unresolved lines : {unresolved_total}")
    print(f"  marker         : {reasons['marker']}")
    print(f"  truncated-cjk  : {reasons['truncated-cjk']}")
    if skipped:
        print(f"skipped files    : {len(skipped)}")
    print("APPLIED" if args.apply else "DRY RUN (add --apply to write)")
    return 0


def write_all(pending):
    """Write every file, so a failure cannot leave a half written tree.

    The content of every file is written to a temporary file first; only
    when all of them are on disk are they moved into place.
    """
    staged = []
    try:
        for path, text in pending:
            tmp = path + ".mojibake.tmp"
            with open(tmp, "wb") as fh:
                fh.write(text.encode("utf-8"))
            staged.append((tmp, path))
        for tmp, path in staged:
            os.replace(tmp, path)
    except OSError:
        for tmp, _ in staged:
            if os.path.exists(tmp):
                os.remove(tmp)
        raise


def cmd_selftest(args):
    failures = []

    def check(condition, message):
        if not condition:
            failures.append(message)

    # 1. normal Chinese words must not be taken for mojibake
    for word in ("\u804c\u4f4d", "\u72b6\u6001", "\u7528\u6237"):
        check(guarded_roundtrip(word) is None,
              f"false positive on {word!r}")

    # 2. rule A restores a damaged run (fixed sample, no repository state)
    run = "\u9429\u7a3f\u53e7\u95b0\u5d87\u7586\u9359\u509b\u669f"
    fixed = "\u76f8\u5173\u914d\u7f6e\u53c2\u6570"
    check(guarded_roundtrip(run) == fixed,
          f"rule A failed: {guarded_roundtrip(run)!r}")
    check(restore_runs(run) == fixed, "rule A restore_runs failed")

    # 3. rule A must reject a round trip that lands in the C1 range
    for word in ("\u805f", "\u807d", "\u8def"):
        candidate = None
        try:
            candidate = word.encode("gbk").decode("utf-8")
        except (UnicodeEncodeError, UnicodeDecodeError):
            candidate = None
        check(candidate is not None
              and any(0x80 <= ord(c) <= 0xFF for c in candidate),
              f"the C1 sample {word!r} no longer lands in the C1 range")
        check(guarded_roundtrip(word) is None,
              f"C1 guard failed on {word!r} -> {candidate!r}")

    # 4. rule C: a // comment that swallowed a statement
    line = "private int a = 1; // 7\u6fb6?    private String header = \"X\";"
    repaired, _ = repair_line(line, LexerState())
    check(repaired.split("\n")[-1].strip() == 'private String header = "X";',
          f"rule C failed: {repaired!r}")
    check(repaired.count("\n") == 1, f"rule C made no newline: {repaired!r}")

    # 5. rule B: a string literal that lost its closing quote
    line = '@Size(max = 200, message = "\u6807\u9898\u957f\u5ea6\u4e0d\u80fd\u8d85\u8fc7200\u4e2a\u5b57\u7b26?)'
    repaired, _ = repair_line(line, LexerState())
    check(quotes_balanced(repaired), f"rule B failed: {repaired!r}")
    check(repaired.count('"') == line.count('"') + 1,
          f"rule B added the wrong number of quotes: {repaired!r}")

    # 6. rule B must not fire when the literal is already closed
    line = 'new X("\u9422\u3126\u57db\u6d93\u5d85\u74e8\u9366? " + userId);'
    repaired, _ = repair_line(line, LexerState())
    check(quotes_balanced(repaired),
          f"rule B added a quote to a closed literal: {repaired!r}")
    check(repaired.count('"') == line.count('"'),
          f"rule B changed the quote count: {repaired!r}")

    # 7. a ternary operator and a regex quantifier stay untouched
    for line in ('logger.debug("jwt: {}", jwt != null ? "***" : null);',
                 '@Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])", message = "x")'):
        repaired, _ = repair_line(line, LexerState())
        check(repaired == line, f"legitimate '?' was modified: {repaired!r}")

    # 8. a '?' inside a string literal must not gain a newline
    line = '@Mapping(target = "f", expression = "java(x != null ? y : z)")'
    repaired, _ = repair_line(line, LexerState())
    check(repaired == line, f"rule C fired inside a string: {repaired!r}")

    # 9. a '?' that follows an ASCII character is never a damage marker
    check(damage_sites("a ? b") == [], "an ASCII '?' was taken for damage")
    check(damage_sites("\u6d4b? b") == [1], "a damage marker was missed")

    # 10. a run that lost its last byte is reported as damaged
    check(is_truncated_mojibake("\u9422\u3126\u57db\u6d93\u5d85\u74e8\u9366"),
          "a truncated run was not detected")

    # 11. the selftest must not depend on the state of the repository
    text = '    @Size(max = 200, message = "\u6807\u9898\u957f\u5ea6\u4e0d\u80fd\u8d85\u8fc7200\u4e2a\u5b57\u7b26?)'
    out, needs = process_text(text)
    check(quotes_balanced(out), f"the repaired line is unbalanced: {out!r}")
    check(len(needs) == 0,
          f"a fully repaired line was still reported: {needs!r}")

    # 12. a line whose CJK run lost a byte stays in the report
    text = '    @Size(max = 50, message = "\u93c2\u56e6\u6b22\u9422\u3129\u20ac\u65c8\u66b1\u6434\ufe3f\u7b09\u9473\u501f\u79f4\u6769?0\u6d63?)'
    out, needs = process_text(text)
    check(len(needs) == 1, f"a broken line was not reported: {needs!r}")

    # 13. a line that only holds a truncated run (no '?' at all) is reported too
    text = " * @param name \u6434\u65c2\u6564\u935a\u5d87"
    out, needs = process_text(text)
    check(len(needs) == 1, f"a truncated-only line was not reported: {needs!r}")
    check(needs and needs[0][2] == text,
          f"the reported line was altered: {needs!r}")
    check(needs and needs[0][2] and _piece_reason(needs[0][2]) == "truncated-cjk",
          f"the truncated line was not classified: {needs!r}")

    # 14. a correct Chinese line is never reported
    text = " * \u7528\u6237\u540d\u4e0d\u80fd\u4e3a\u7a7a"
    out, needs = process_text(text)
    check(len(needs) == 0, f"a correct Chinese line was reported: {needs!r}")

    # 15. rule C: a '?' that swallowed the newline of a comment is followed by
    #     the indentation of the next line, whatever statement starts there
    line = "        // \u68c0\u67e5\u7528\u6237\u540d\u662f\u5426\u5b58\u5728?        if (repo.existsByUsername(name)) {"
    repaired, _ = repair_line(line, LexerState())
    check(repaired.count("\n") == 1, f"rule C made no newline: {repaired!r}")
    check(repaired.split("\n")[1].strip().startswith("if (repo.existsByUsername"),
          f"rule C split at the wrong place: {repaired!r}")

    # 16. a '?' that ate an ordinary character stays on its line
    line = "        // \u8bc4\u5206\u8303\u56f4?1-10"
    repaired, _ = repair_line(line, LexerState())
    check(repaired == line, f"a same-line '?' was split: {repaired!r}")

    # 17. the guard must see the mojibake classes it used to miss: a run
    #     that holds a fullwidth form, and a run that decodes back to a
    #     word with an ASCII letter in it
    for run, expected in (
            ("\u7ee0\uff04\u608a", "\u7ba1\u7406"),
            ("\u7459\uff46\u703d", "\u89e3\u6790"),
            ("\u5a23\u8bf2\u59de\u7f02\u54c4\u76af\u9428\u5227",
             "\u6dfb\u52a0\u7f3a\u5c11\u7684g"),
            ("\u935c\u5b72", "\u548cS")):
        check(guarded_roundtrip(run) == expected,
              f"the widened guard missed {run!r}: {guarded_roundtrip(run)!r}")

    if failures:
        for f in failures:
            print("FAIL:", f)
        return 1
    print("selftest: all cases passed")
    return 0


def main():
    ap = argparse.ArgumentParser(description=__doc__)
    ap.add_argument("command", choices=["detect", "repair", "selftest"])
    ap.add_argument("--root", default=".")
    ap.add_argument("--apply", action="store_true")
    ap.add_argument("--report")
    args = ap.parse_args()
    if args.command == "detect":
        return cmd_detect(args)
    if args.command == "repair":
        return cmd_repair(args)
    return cmd_selftest(args)


if __name__ == "__main__":
    sys.exit(main())

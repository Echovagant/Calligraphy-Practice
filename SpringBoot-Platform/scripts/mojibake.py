#!/usr/bin/env python3
"""
Detect and repair UTF-8-read-as-GBK mojibake in Java sources.

Background
----------
Source files were UTF-8, got decoded as GBK (lossy), then re-encoded as UTF-8.
GBK trail bytes below 0x40 are invalid, so any ASCII byte in that range that
followed a GBK lead byte was replaced by '?'. That single marker explains all
four damage classes:

  A  lossless CJK run  -> reversible: run.encode('gbk').decode('utf-8')
  B  '?' ate a '"'     -> a string literal lost its closing quote
  C  '?' ate a '\\n'   -> a // comment swallowed the statement on the next line
  D  '?' ate an ASCII char (digit etc.) -> needs a byte-level search

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
CJK_ONLY = re.compile(
    r"^[\u3000-\u9fff\u2018\u2019\u201c\u201d\u3001\u3002\uff01\uff0c"
    r"\uff1a\uff1b\uff08\uff09\u00b7\u2014\-\s]+$"
)
# a '?' immediately followed by one of these means a swallowed code statement
CODE_AFTER_Q = re.compile(
    r"^\s*(?:java\.|com\.|org\.|this\.|new\s|return\s|private\s|public\s"
    r"|protected\s|static\s|final\s|int\s|long\s|String\s|boolean\s|void\s"
    r"|List<|Map<|Optional<|@|import\s|package\s)"
)
# a '?' followed by one of these means a swallowed closing quote / delimiter
QUOTE_AFTER_Q = re.compile(r"^\s*[),;:'\"}\]]")

SRC_DIRS = ("src/main/java", "src/main/resources")
EXTENSIONS = (".java", ".sql", ".properties")


def odd_quotes(line):
    n, i = 0, 0
    while i < len(line):
        if line[i] == "\\":
            i += 2
            continue
        if line[i] == '"':
            n += 1
        i += 1
    return n % 2 == 0


def has_odd_quotes(line):
    return not odd_quotes(line)


def guarded_roundtrip(run):
    """Reverse the transform for a run that lost no bytes; None if not reversible."""
    try:
        fixed = run.encode("gbk").decode("utf-8")
    except (UnicodeEncodeError, UnicodeDecodeError):
        return None
    if fixed == run:
        return None
    # both sides must look like real CJK text, else it is a false positive
    if not CJK_ONLY.match(fixed) or not CJK_ONLY.match(run):
        return None
    return fixed


def restore_runs(text):
    """Rule A: restore every lossless CJK run."""
    return CJK_RUN.sub(lambda m: guarded_roundtrip(m.group(0)) or m.group(0), text)


def recover_single_char(line, qpos):
    """Find the character the '?' at qpos stands for.

    Encode the line to GBK, put each byte value in that slot, and keep the
    substitutions that make the whole line valid UTF-8.
    """
    try:
        head_b = line[:qpos].encode("gbk")
        tail_b = line[qpos + 1:].encode("gbk")
    except UnicodeEncodeError:
        return None
    results = []
    for b in range(0x00, 0x100):
        try:
            text = (head_b + bytes([b]) + tail_b).decode("utf-8")
        except UnicodeDecodeError:
            continue
        results.append(text)
    if not results:
        return None
    if len(results) == 1:
        return results[0]
    balanced = [t for t in results if odd_quotes(t)]
    if len(balanced) == 1:
        return balanced[0]
    return None  # ambiguous -> a human has to choose


def resolve_question_marks(line):
    """Rules B/C/D. Returns (repaired_line, [unresolved_offsets])."""
    out = []
    unresolved = []
    i = 0
    while i < len(line):
        ch = line[i]
        if ch != "?":
            out.append(ch)
            i += 1
            continue
        rest = line[i + 1:]
        if CODE_AFTER_Q.match(rest):
            out.append("\n")          # rule C: the '?' ate a newline
            i += 1
            continue
        if QUOTE_AFTER_Q.match(rest):
            out.append('"')            # rule B: the '?' ate a closing quote
            i += 1
            continue
        cand = recover_single_char(line, i)
        if cand is None:
            unresolved.append(i)
            out.append("?")
            i += 1
        else:
            out.append(cand)
            i += 1
    return "".join(out), unresolved


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
    """Returns (new_text, unresolved_lines)."""
    out_lines = []
    unresolved = []
    for lineno, line in enumerate(text.splitlines(), 1):
        restored = restore_runs(line)
        repaired, unres = resolve_question_marks(restored)
        if unres:
            unresolved.append((lineno, repaired))
        out_lines.append(repaired)
    new_text = "\n".join(out_lines) + ("\n" if text.endswith("\n") else "")
    return new_text, unresolved


def cmd_detect(args):
    root = args.root
    total_runs = 0
    total_odd = 0
    affected = []
    for path in java_files(root):
        with open(path, "rb") as fh:
            text = fh.read().decode("utf-8", errors="replace")
        runs = sum(1 for m in CJK_RUN.finditer(text) if guarded_roundtrip(m.group(0)))
        odd = sum(1 for l in text.splitlines() if has_odd_quotes(l))
        if runs or odd:
            total_runs += runs
            total_odd += odd
            affected.append((os.path.relpath(path, root), runs, odd))
    affected.sort(key=lambda x: (-x[1], -x[2]))
    print(f"files with mojibake  : {len(affected)}")
    print(f"reversible CJK runs   : {total_runs}")
    print(f"lines with odd quotes : {total_odd}")
    for rel, runs, odd in affected[:40]:
        print(f"  runs={runs:4d} odd={odd:4d}  {rel}")
    return 0 if not affected else 1


def cmd_repair(args):
    root = args.root
    report = io.StringIO()
    report.write("# Mojibake repair report\n\n")
    changed_files = 0
    unresolved_total = 0
    for path in java_files(root):
        with open(path, "rb") as fh:
            text = fh.read().decode("utf-8")
        new_text, unresolved = process_text(text)
        if new_text == text:
            continue
        changed_files += 1
        if args.apply:
            with open(path, "wb") as fh:
                fh.write(new_text.encode("utf-8"))
        rel = os.path.relpath(path, root)
        for lineno, line in unresolved:
            unresolved_total += 1
            report.write(f"UNRESOLVED {rel}:{lineno}\n    {line.strip()[:200]}\n\n")
    report.write(f"\nfiles changed    : {changed_files}\n")
    report.write(f"unresolved lines : {unresolved_total}\n")
    if args.report:
        with open(args.report, "w", encoding="utf-8") as fh:
            fh.write(report.getvalue())
    print(f"files changed    : {changed_files}")
    print(f"unresolved lines : {unresolved_total}")
    print("APPLIED" if args.apply else "DRY RUN (add --apply to write)")
    return 0


def cmd_selftest(args):
    failures = []

    # 1. false positives must not be flagged
    for word in ("\u804c\u4f4d", "\u72b6\u6001", "\u7528\u6237"):
        if guarded_roundtrip(word) is not None:
            failures.append(f"false positive on {word!r}")

    # 2. a real mangled run from the repo must be reversible
    sample = None
    for path in java_files(args.root):
        with open(path, "rb") as fh:
            text = fh.read().decode("utf-8", errors="replace")
        for m in CJK_RUN.finditer(text):
            if guarded_roundtrip(m.group(0)):
                sample = m.group(0)
                break
        if sample:
            break
    if sample is None:
        failures.append("no mangled run found in the repository")
    else:
        fixed = guarded_roundtrip(sample)
        if fixed is None or not CJK_ONLY.match(fixed):
            failures.append(f"roundtrip failed for {sample!r}")

    # 3. rule C: a // comment that swallowed a statement
    line = "// Builder \u7eb7?    public static class Builder {"
    fixed, unres = resolve_question_marks(line)
    if "public static class Builder {" not in fixed.split("\n")[-1]:
        failures.append(f"rule C failed: {fixed!r}")
    if unres:
        failures.append(f"rule C left unresolved: {unres}")

    # 4. rule B: a string literal that lost its closing quote
    line = '@Size(max = 200, message = "\u6807\u9898\u957f\u5ea6\u4e0d\u80fd\u8d85\u8fc7200\u4e2a\u5b57\u7b26?)'
    fixed, unres = resolve_question_marks(line)
    if has_odd_quotes(fixed):
        failures.append(f"rule B failed: {fixed!r}")
    if unres:
        failures.append(f"rule B left unresolved: {unres}")

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

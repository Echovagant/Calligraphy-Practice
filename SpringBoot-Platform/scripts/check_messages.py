#!/usr/bin/env python3
"""Check that JSR-303 validation messages agree with their annotation limits.

A repaired message is only correct if the number it mentions matches the
limit in the annotation, e.g.

    @Size(max = 200, message = "标题长度不能超过200个字符")
    ^ 200 must appear in the message

The report is written to a UTF-8 file rather than printed: residual mojibake
still contains characters such as U+20AC that the Windows GBK console cannot
encode, and printing them raises UnicodeEncodeError and aborts the scan.

Usage:
    python scripts/check_messages.py [--report FILE] [--quiet]
"""
import argparse
import os
import re
import sys

BASE = os.path.join(os.path.dirname(os.path.abspath(__file__)), "..", "src", "main", "java")
SIZE_RE = re.compile(r"@Size\s*\(\s*(?:min\s*=\s*(\d+)\s*,\s*)?max\s*=\s*(\d+)\s*,\s*message\s*=\s*\"([^\"]*)\"")
RANGE_RE = re.compile(r"@Size\s*\(\s*min\s*=\s*(\d+)\s*,\s*max\s*=\s*(\d+)\s*,\s*message\s*=\s*\"([^\"]*)\"")
NUM_RE = re.compile(r"\d+")


def check(path):
    """Return [(lineno, line, why)] for messages that disagree with their limit."""
    problems = []
    with open(path, encoding="utf-8") as fh:
        for lineno, line in enumerate(fh, 1):
            m = RANGE_RE.search(line)
            if m:
                lo, hi, msg = m.group(1), m.group(2), m.group(3)
                nums = set(NUM_RE.findall(msg))
                if lo not in nums or hi not in nums:
                    problems.append((lineno, line.strip(), "expected %s and %s in message" % (lo, hi)))
                continue
            m = SIZE_RE.search(line)
            if m:
                hi, msg = m.group(2), m.group(3)
                if hi not in set(NUM_RE.findall(msg)):
                    problems.append((lineno, line.strip(), "expected %s in message" % hi))
    return problems


def main():
    ap = argparse.ArgumentParser(description=__doc__)
    ap.add_argument("--report", default="message-check-report.txt")
    ap.add_argument("--quiet", action="store_true")
    args = ap.parse_args()

    report = []
    total = 0
    for dirpath, dirnames, filenames in os.walk(BASE):
        dirnames[:] = [d for d in dirnames if d != "target"]
        for fn in sorted(filenames):
            if not fn.endswith(".java"):
                continue
            path = os.path.join(dirpath, fn)
            for lineno, line, why in check(path):
                total += 1
                report.append("%s:%d: %s" % (os.path.relpath(path, BASE), lineno, why))
                report.append("    " + line[:180])
    report.append("")
    report.append("problems: %d" % total)

    with open(args.report, "w", encoding="utf-8") as fh:
        fh.write("\n".join(report))
    if not args.quiet:
        print("problems: %d  (report written to %s)" % (total, args.report))
    return 1 if total else 0


if __name__ == "__main__":
    sys.exit(main())

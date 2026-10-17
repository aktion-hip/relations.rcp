#!/usr/bin/env python3
"""Checks the Eclipse IDE output folders (bundles/*/bin) for compiler error stubs.

When the IDE compiles a source file while the bundle's plug-in classpath can't be
resolved (e.g. while MANIFEST.MF or the target platform is being changed), JDT still
writes a class file whose methods throw "Unresolved compilation problems". Such stubs
survive until the project is cleaned and only show up at runtime, typically as a
NoClassDefFoundError for an unqualified class name (e.g. 'Shell').

Usage (from the repository root): python3 tools/check-ide-output.py
Exit code 0 if no stubs are found, 1 otherwise. Fix: Project > Clean in Eclipse.
"""
import glob
import os
import sys

MARKER = b"Unresolved compilation problem"


def main():
    hits = []
    checked = 0
    for bin_dir in sorted(glob.glob(os.path.join("bundles", "*", "bin"))):
        bundle = os.path.basename(os.path.dirname(bin_dir))
        for root, _, files in os.walk(bin_dir):
            for name in files:
                if not name.endswith(".class"):
                    continue
                path = os.path.join(root, name)
                checked += 1
                with open(path, "rb") as class_file:
                    if MARKER in class_file.read():
                        hits.append((bundle, path))
    for bundle, path in hits:
        print(f"STUB {bundle}: {path}")
    print(f"{checked} class file(s) checked, {len(hits)} compiler error stub(s) found")
    if hits:
        print("Fix: resolve the errors shown in Eclipse's Problems view, then Project > Clean.")
    return 1 if hits else 0


if __name__ == "__main__":
    sys.exit(main())

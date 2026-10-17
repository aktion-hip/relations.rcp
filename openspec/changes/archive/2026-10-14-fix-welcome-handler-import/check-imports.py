#!/usr/bin/env python3
"""Checks that every <imports> elementId of the bundles' e4 model fragments matches
exactly one element of the application model Relations.e4xmi.

ModelAssembler resolves fragment imports by elementId only and takes the first match,
so an id shared by e.g. a menu item and a command breaks the application start.

Usage (from the repository root): python3 openspec/changes/fix-welcome-handler-import/check-imports.py
Exit code 0 if all imports are unique, 1 otherwise.
"""
import glob
import sys
import xml.etree.ElementTree as ET

APP_MODEL = "bundles/org.elbe.relations/Relations.e4xmi"
XSI_TYPE = "{http://www.w3.org/2001/XMLSchema-instance}type"


def element_ids(path):
    """Maps elementId -> list of element descriptions in the model at path."""
    ids = {}
    for elem in ET.parse(path).iter():
        eid = elem.get("elementId")
        if eid:
            ids.setdefault(eid, []).append(elem.get(XSI_TYPE) or elem.tag)
    return ids


def main():
    app_ids = element_ids(APP_MODEL)
    failures = 0
    for fragment in sorted(glob.glob("bundles/*/*.e4xmi")):
        if fragment.replace("\\", "/") == APP_MODEL:
            continue
        for imp in ET.parse(fragment).getroot().findall("imports"):
            eid = imp.get("elementId")
            matches = app_ids.get(eid, [])
            if len(matches) != 1:
                failures += 1
                print(f"AMBIGUOUS {fragment}: import {imp.get(XSI_TYPE)} '{eid}' "
                      f"matches {len(matches)} elements {matches}")
            else:
                print(f"ok        {fragment}: '{eid}'")
    print(f"{failures} ambiguous import(s)")
    return 1 if failures else 0


if __name__ == "__main__":
    sys.exit(main())

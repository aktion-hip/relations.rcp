# Proposal

## Why

When Relations is launched from the Eclipse IDE, creating the handler for
Help > About fails:

```
Unable to create class 'org.elbe.relations.handlers.AboutHandler' from bundle '215'
InjectionException: NoClassDefFoundError: Shell
  Caused by: ClassNotFoundException: Shell cannot be found by org.elbe.relations_2.5.0.qualifier
```

The class name `Shell` has no package, which shows that the problem is in the compiled
class, not in the source. Findings (2026-10-14):

- `AboutHandler.java` is correct. It imports `org.eclipse.swt.widgets.Shell`, and its
  diff against `HEAD` contains line-ending changes only.
- The IDE output `bundles/org.elbe.relations/bin/` contains **two** class files that
  Eclipse's compiler wrote as error stubs ("Unresolved compilation problems") at 12:56:
  - `handlers/AboutHandler.class`: `Execute`, `Named`, `IServiceConstants`, `Shell` and
    `IEclipseContext` could not be resolved, and `AboutDialog.open()` was undefined.
  - `internal/about/AboutDialog.class`: `Composite` could not be resolved.

  At that moment the bundle's whole plug-in classpath was unavailable: SWT, JFace and the
  e4 annotations. It coincides with editing these two files during the bump to 2.5.0
  (`MANIFEST.MF` was saved at 13:05). The other 646 classes in `bin/` are intact, and no
  other bundle is affected.
- A clean offline Tycho build of the current tree (`mvn -B -o clean verify`, 2.5.0)
  succeeds, and its `AboutHandler`/`AboutDialog` classes are correct. The exported product
  never had the problem.

The IDE does not recompile these two files on its own, because their sources haven't
changed since. The stubs therefore persist until the project is cleaned. Nothing currently
warns that the IDE output contains such stubs. They only surface at runtime, as a
misleading `NoClassDefFoundError`.

## What Changes

- Remove the two stale error stubs and rebuild `org.elbe.relations` in the IDE with a
  clean build, after making sure the target platform and `MANIFEST.MF` resolve.
- Add a small guard script, `tools/check-ide-output.py`. It scans every
  `bundles/*/bin/**/*.class` for compiler error stubs and exits non-zero, listing them,
  when it finds any. It is run by hand before launching from the IDE. There is no CI.
- No change to Java sources, manifests, the product or features.

## Capabilities

### New Capabilities
<!-- none -->

### Modified Capabilities
<!-- none. Help > About behaves as before once the class is compiled correctly. This is a
     build-artifact defect plus developer tooling, so the change sets skip_specs: true. -->

## Impact

- Local IDE output only (`bundles/org.elbe.relations/bin/`, which is git-ignored).
- New tracked file `tools/check-ide-output.py` (new top-level `tools/` directory). It is
  not part of the Tycho reactor (only `bundles`, `features`, `products` are modules), so
  the build is unaffected.
- Verification: the guard script reports 0 stubs, and Help > About opens the dialog in an
  IDE launch.

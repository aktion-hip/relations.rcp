# Tasks

## 1. Guard script

- [x] 1.1 Create `tools/check-ide-output.py` (design D2). It scans `bundles/*/bin/**/*.class` for `Unresolved compilation problem`, prints each hit with its bundle, and exits 1 on hits and 0 otherwise. Verify by running it from the repository root against the current tree: it must report exactly `org.elbe.relations/bin/org/elbe/relations/handlers/AboutHandler.class` and `.../internal/about/AboutDialog.class` and exit 1.

## 2. Remove the stale stubs

- [x] 2.1 Delete the two stub class files from `bundles/org.elbe.relations/bin/` (the paths from 1.1). Verify that `tools/check-ide-output.py` now exits 0.
- [ ] 2.2 (Maintainer, in Eclipse) Open `target-platform/target-platform.target` and choose *Reload Target Platform*. Check that the Problems view shows no errors for `org.elbe.relations` (in particular none in `MANIFEST.MF`), then run **Project > Clean…** on `org.elbe.relations`. Verify that `tools/check-ide-output.py` still exits 0 after the IDE rebuild and that `bin/org/elbe/relations/handlers/AboutHandler.class` exists again with a new timestamp.

## 3. Verification

- [ ] 3.1 (Maintainer) Launch Relations from the IDE. Verify that the log no longer contains `Unable to create class 'org.elbe.relations.handlers.AboutHandler'` and that Help > About opens the About dialog.
- [x] 3.2 Run `mvn -B -o clean verify` and verify BUILD SUCCESS (the new `tools/` folder does not affect the reactor). Then restore the `pom.tycho` files the build deletes (`git checkout -- bundles/pom.tycho features/pom.tycho products/pom.tycho`).

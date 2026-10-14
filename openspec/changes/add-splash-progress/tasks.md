# Tasks

## 1. Messages

- [x] 1.1 Add the six `splash.progress.*` keys (design D2 table) to `bundles/org.elbe.relations/src/RelationsMessages.properties` and `RelationsMessages_de.properties`. Match the existing style: German umlauts as `\u00XX` escapes, CRLF line endings. Verify that both files contain all six keys (`grep -c "^splash.progress" ` gives 6 in each) and that the German draft is flagged for the maintainer to review (design: Open Questions).

## 2. Splash progress class

- [x] 2.1 Add `org.elbe.relations.internal.utility.SplashProgress` (design D1). It obtains the splash shell via `org.eclipse.equinox.launcher.splash.handle` + `Shell.internal_new`, and is a no-op without a handle or on failure. It reads `startupProgressRect`/`startupMessageRect`/`startupForegroundColor` from `Platform.getProduct()` with the current values as fallback, creates `ProgressBar` + `Label` (background mode `SWT.INHERIT_FORCE`), and provides `step(String messageKey)` (advance, set the translated text, `update()`), `runHidden(Runnable)` (D4) and an idempotent `dispose()`. Verify that it compiles in the build.
- [x] 2.2 In `SplashProgress`, register an `org.eclipse.osgi.service.runnable.StartupMonitor` service with `Constants.SERVICE_RANKING = Integer.MAX_VALUE`. Its `applicationRunning()` unregisters itself and calls `dispose()`, and `update()` is a no-op (design D3). Add `org.eclipse.osgi.service.runnable` to `Import-Package` in `bundles/org.elbe.relations/META-INF/MANIFEST.MF`. Verify the build resolves the import (BUILD SUCCESS).

## 3. Lifecycle integration

- [x] 3.1 In `RelationsLifeCycle.initializeApp`, create `SplashProgress` first, keep it in a field, set it into the context, and call `step` at positions 1–4 of the design D2 table. Wrap the existing `MessageDialog.openError` in `splashProgress.runHidden(...)`. Verify by code review that the order matches the table and that no step is inside a conditional branch (every startup passes all six steps).
- [x] 3.2 Call step 5 at the start of `@ProcessAdditions` and step 6 at the end of `@ProcessRemovals`. Then call `splashProgress.dispose()` in the existing `APP_STARTUP_COMPLETE` subscriber (`subscribeTest`) as the safety net (D3). Verify with `mvn -B -o clean verify` (BUILD SUCCESS). Afterwards restore the `pom.tycho` files the build deletes (`git checkout -- bundles/pom.tycho features/pom.tycho products/pom.tycho`).

## 4. Cleanup

- [x] 4.1 Delete `bundles/org.elbe.relations/src/org/elbe/relations/internal/utility/RelationsSplashHandler.java` (design D5). Verify that `grep -rn "RelationsSplashHandler\|org.eclipse.ui.splash" bundles/org.elbe.relations --exclude-dir=bin --exclude-dir=target` returns nothing and that the build still succeeds.

## 5. Manual verification in the launched product

The build does not exercise startup, so these are checked by hand in the exported product on Windows (Linux/macOS if available).

- [ ] 5.1 Normal start with an existing workspace. Verify that the bar and message appear in the bottom strip of the splash image, that the messages run through the six phases in order, that the bar only moves forward, and that nothing remains after the main window opens.
- [ ] 5.2 First start on a fresh workspace (creates the embedded DB). Verify that the progress shows and startup completes.
- [ ] 5.3 Force a re-index (create the reindex marker in the catalog) and start. Verify that "Checking search index…" stays visible while indexing runs.
- [ ] 5.4 Start with `-nl de`. Verify that all messages are in German.
- [ ] 5.5 Start with `-nosplash`. Verify a normal start with no errors in `<workspace>/.metadata/.log`.
- [ ] 5.6 Provoke the database error dialog (embedded config missing). Verify that the dialog is visible above or without the splash, can be confirmed, and that startup continues.

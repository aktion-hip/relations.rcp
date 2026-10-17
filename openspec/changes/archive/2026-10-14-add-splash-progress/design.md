# Design

## Context

See proposal.md (Why). Facts verified against the platform sources in the target
(`org.eclipse.e4.ui.workbench.swt` 0.18.0, `org.eclipse.ui.workbench` 3.138.0):

- **Startup sequence in `E4Application.start`:** `getApplicationDisplay()` creates the
  `Display` **before** `createE4Workbench`. The latter calls the lifecycle hooks
  `@PostContextCreate` (E4Application l.233), then assembles the model (fragments), then
  `@ProcessAdditions` and `@ProcessRemovals` (l.268–269). After that,
  `workbench.createAndRunUI` renders the windows. In `PartRenderingEngine.run` (l.1093ff.),
  once all windows are built, it calls `IApplicationContext.applicationRunning()`, which
  tears down the native splash, and then posts `APP_STARTUP_COMPLETE`.
- **There is no event loop before rendering.** Everything up to `applicationRunning()`
  runs synchronously on the UI thread, so widgets on the splash only repaint when told to
  (`update()`).
- **How 3.x does it:** `WorkbenchPlugin.getSplashShell` reads the system property
  `org.eclipse.equinox.launcher.splash.handle` and wraps the launcher's native window
  with `Shell.internal_new(display, handle)`. The workbench registers an OSGi
  `org.eclipse.osgi.service.runnable.StartupMonitor` with ranking `Integer.MAX_VALUE`.
  Its `applicationRunning()` disposes the splash handler and the wrapping shell, before
  the launcher's own monitor closes the native window.
- **Relations today:** the product defines `startupProgressRect=2,286,450,12`,
  `startupMessageRect=15,263,450,20` and `startupForegroundColor=FFFFFF`. `splash.png` is
  453×303, so the bar is the bottom strip and the message sits just above it. These
  properties and the unregistered `RelationsSplashHandler` (an `AbstractSplashHandler`)
  are dead under `E4Application`.
- **Startup work in `RelationsLifeCycle.initializeApp` (`@PostContextCreate`):**
  1. context services
  2. `EmbeddedCatalogHelper.cleanUp`
  3. DB open or create (`DbEmbeddedCreateHandler` on first run; `MessageDialog.openError`
     if the config is missing)
  4. `EventStoreChecker`
  5. `dataService.loadData`
  6. `reindexChecked`, which may rebuild the Lucene index

  `@ProcessAdditions` restores the browsers and fixes key bindings, and `@ProcessRemovals`
  adds the p2 repository.

## Goals / Non-Goals

**Goals:**
- Reuse the launcher's native splash window and the product's existing rectangles and
  colour.
- No dependency on 3.x workbench classes at runtime.

**Non-Goals:**
- Time-based or estimated progress. The bar moves only on real phase changes.

## Decisions

### D1. Pure SWT overlay on the native splash shell, not `BasicSplashHandler`

A new class `org.elbe.relations.internal.utility.SplashProgress` does the following:

- obtains the splash shell exactly as `WorkbenchPlugin.getSplashShell` does (system
  property → `Shell.internal_new(display, Long.parseLong(handle))`). If there is no
  handle, no display, or the parse fails, it becomes a **no-op instance**, so
  `-nosplash` is unaffected.
- reads `startupProgressRect`, `startupMessageRect` and `startupForegroundColor` from
  `Platform.getProduct()`. It falls back to `2,286,450,12` / `15,263,450,20` / `FFFFFF`,
  the current product values.
- creates a `ProgressBar` (`SWT.HORIZONTAL | SWT.SMOOTH`) and a `Label` (transparent
  background via `SWT.INHERIT_FORCE` background mode on the splash shell) at those bounds.
- `step(String messageKey)` advances the bar by one unit (maximum = number of phases),
  sets the translated message, and calls `splashShell.update()` so it repaints without an
  event loop.

**Alternative: subclass `org.eclipse.ui.splash.BasicSplashHandler`.** That would give the
same look for free, but loading any `org.eclipse.ui.workbench` class activates that bundle
lazily (`WorkbenchPlugin.start`), and Relations deliberately avoids running 3.x workbench
code. `BasicSplashHandler` also pulls in `StartupThreading` internals. Rejected. The SWT
version is about 100 lines.

**Alternative: a separate SWT shell (own progress window).** It would sit on top of or
next to the native splash, with two windows and z-order issues. Rejected.

### D2. Phase hooks in `RelationsLifeCycle`

`initializeApp` creates the `SplashProgress` first (`Display.getCurrent()` exists, see
Context), stores it in a field and puts it into the application context (so that later
hooks and, if needed, other code can reach it). Steps:

| # | Hook / position | Message key (`RelationsMessages`) | en | de |
|---|---|---|---|---|
| 1 | start of `initializeApp` | `splash.progress.prepare` | Preparing application… | Applikation wird vorbereitet… |
| 2 | before the DB block | `splash.progress.database` | Opening database… | Datenbank wird geöffnet… |
| 3 | before `dataService.loadData` | `splash.progress.data` | Loading data… | Daten werden geladen… |
| 4 | before `reindexChecked` | `splash.progress.index` | Checking search index… | Suchindex wird geprüft… |
| 5 | start of `@ProcessAdditions` | `splash.progress.browsers` | Restoring browsers… | Browser werden wiederhergestellt… |
| 6 | end of `@ProcessRemovals` | `splash.progress.ui` | Building user interface… | Benutzeroberfläche wird aufgebaut… |

Six equal steps, so the bar fills one sixth per phase. Rendering (after step 6) is the
last visible phase. The messages are read with `RelationsMessages.getString`, the same
mechanism the lifecycle already uses. The language therefore follows the existing
language handling, with the default bundle as the English fallback.

### D3. Teardown via an Equinox `StartupMonitor`

`SplashProgress` registers a `StartupMonitor` service (ranking `Integer.MAX_VALUE`, like
3.x) through the bundle context. In `applicationRunning()` it unregisters itself, disposes
the bar and label, and disposes the wrapping shell. The launcher's monitor then closes the
native window, as in 3.x. `applicationRunning()` is invoked from
`PartRenderingEngine.run` on the UI thread, so no synchronisation is needed. `update()`
(called by the framework while bundles start) is a no-op.

As a safety net, the lifecycle also calls `SplashProgress.dispose()` from an
`APP_STARTUP_COMPLETE` subscriber. That is idempotent and guarded by `isDisposed()`.

### D4. Dialogs during startup

`SplashProgress.runHidden(Runnable)` sets the splash shell invisible, runs the runnable
(the existing `MessageDialog.openError` in `initializeApp`), and makes the shell visible
again. This is the only dialog in the startup path. `DbEmbeddedCreateHandler` and
`reindexChecked` open none.

### D5. Remove `RelationsSplashHandler`

It is unregistered, uses the 3.x API and would never run. It is deleted rather than
adapted.

## Risks / Trade-offs

- [`Shell.internal_new` is SWT-internal API] → it is the documented approach used by the
  3.x workbench itself, in the same target. Wrapped in `try/catch (RuntimeException |
  SWTError)` → no-op on failure.
- [Windows/GTK/Cocoa differences in painting on the launcher window] → bounds come from
  the product rectangles and a `ProgressBar` is a native child control. Manual check on
  Windows is required, and on Linux/macOS if available.
- [Label background over the splash image: native labels may not be transparent on every
  platform] → `setBackgroundMode(SWT.INHERIT_FORCE)` on the splash shell plus the splash
  image as background. If a platform still paints a solid box, the fallback is to draw the
  text in a `PaintListener` on the shell. That is an implementation detail.
- [Progress is coarse when `reindexChecked` takes minutes] → the message names the phase
  (spec: long phase stays visible). A finer-grained bar is out of scope.
- [Relying on Equinox to call all `StartupMonitor` services in ranking order] → this is
  the same contract the 3.x workbench depends on. The `APP_STARTUP_COMPLETE` safety net
  (D3) catches a missed call.

## Migration Plan

None. No data, preferences or product changes. Rollback means reverting the
`org.elbe.relations` changes.

## Open Questions

- German wording of the six messages. The table above is a draft for the maintainer to
  confirm.

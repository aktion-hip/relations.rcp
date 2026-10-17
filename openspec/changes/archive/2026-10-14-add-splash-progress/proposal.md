# Proposal

## Why

Relations can take noticeably long to start. It opens or creates the embedded Derby
database, loads data, may rebuild the search index and restores the browsers. During all
of that the user only sees a static `splash.png`, with no sign of whether the application
is working or hung.

The product was clearly meant to show progress. The `org.elbe.relations` product defines
`startupProgressRect`, `startupMessageRect` and `startupForegroundColor`, and a
`RelationsSplashHandler` with a `ProgressBar` exists in the code. Neither takes effect:

- Relations runs on `E4Application`. Splash handlers (`org.eclipse.ui.splashHandlers`)
  and those product properties are only evaluated by the Eclipse 3.x `Workbench`, which
  Relations never starts.
- `RelationsSplashHandler` is not registered anywhere and is based on that same 3.x API.

## What Changes

- Show a **progress bar with a status message** on the splash screen, inside the areas the
  product already defines (`startupProgressRect` = bottom strip of `splash.png`,
  `startupMessageRect` just above it, in `startupForegroundColor`).
- The bar is **determinate** and advances through the real startup phases. Each phase shows
  a short message, for example "Opening database…", "Checking search index…" or
  "Restoring browsers…".
- The messages are available in **English and German**.
- The progress display disappears with the splash screen when the main window is ready.
  Without a splash screen (e.g. `-nosplash`, or a launch configuration without one),
  startup works as before.
- Remove the dead `RelationsSplashHandler`.

## Non-goals

- Changing the splash image itself.
- Progress for the platform's own bundle start-up before Relations code runs. Nothing in
  Relations executes at that point.
- Showing progress in the main window (e.g. for re-indexing that continues after startup).

## Capabilities

### New Capabilities
- `startup-splash`: what the splash screen shows while Relations starts. This covers the
  progress bar, the phase messages, their languages, when the display ends, and how it
  behaves without a splash screen.

### Modified Capabilities
<!-- none -->

## Impact

- `bundles/org.elbe.relations`:
  - a new small SWT-only splash progress class in `internal/utility`
  - progress calls from `RelationsLifeCycle` (`@PostContextCreate`, `@ProcessAdditions`,
    `@ProcessRemovals`)
  - new `RelationsMessages[_de].properties` keys
  - `MANIFEST.MF` gains `Import-Package: org.eclipse.osgi.service.runnable`
  - `RelationsSplashHandler.java` is deleted
- No changes to the product, the features, the data model or the database.
- Platform behaviour relied on: the launcher's splash window handle
  (`org.eclipse.equinox.launcher.splash.handle`) and the Equinox `StartupMonitor` service.
  The 3.x workbench uses both the same way.
- Verification: manual, in the launched product on Windows. The build does not exercise
  startup.

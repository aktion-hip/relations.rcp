# Design

## Context

See proposal.md (Why) for the failure. The facts that shape the fix:

- `ModelAssembler.resolveImports` resolves each fragment `<imports>` entry by **elementId
  only**, taking the first matching element of the application model. It does not check
  that the type matches the import's `xsi:type`.
- `Relations.e4xmi` reuses Eclipse command ids as menu-item ids: `org.eclipse.ui.help.quickStartAction`
  (menu item and command) and `org.eclipse.ui.help.helpContents` (menu item, handler and
  command). Only the first is imported by a fragment today. A scan of all fragment imports
  (`defaultBrowser`, `browser.finder`, `intro`) found that the welcome import is the only
  ambiguous one.
- The model is persisted (`workbench.xmi`). Once a workspace has been run, the main menu
  comes from the persisted model, not from `Relations.e4xmi`. Edits to `Relations.e4xmi`
  therefore do not reach existing workspaces.
- Programmatic handler activation through `EHandlerService` is already used in the code
  base (`FinderBrowserPart`, `ItemEditPart`, `RelationsSearcher`).

## Goals / Non-Goals

**Goals:**
- Relations starts again, on clean and on existing workspaces, and Help > Welcome opens
  the welcome page.
- The fix stays inside `org.elbe.relations.intro`.

**Non-Goals:**
- Renaming the duplicated ids in `Relations.e4xmi`. That is latent hygiene and is not
  needed once nothing imports them (see Risks).

## Decisions

### D1. Activate the handler through `EHandlerService` instead of a model handler

`WelcomeAddon`, in its existing `APP_STARTUP_COMPLETE` method and before the first-start
check, calls:

```java
handlerService.activateHandler("org.eclipse.ui.help.quickStartAction", new OpenWelcomeHandler());
```

The handler service is injected (`EHandlerService`, application context).
`OpenWelcomeHandler` stays unchanged. Its `@Execute` parameters (`MApplication`,
`EModelService`, `EPartService`) are injected from the active context at execution time.
The command id becomes a constant in `WelcomeConstants`.

`Welcome.e4xmi` loses the `<imports>` element and the `handlers` fragment. The part
descriptor and the addon fragments stay.

Activating at `APP_STARTUP_COMPLETE` instead of in `@PostConstruct` makes sure ours is the
most recent activation for that command in the application context. It comes after the
`HandlerProcessingAddon` has processed model handlers, including a classless
`org.elbe.relations.handler.intro` that may still sit in workspaces persisted before
`add-welcome-page`. Before startup completes, the menu cannot be used anyway.

**Alternatives considered:**
- *Give the menu item in `Relations.e4xmi` a unique id* (e.g. `org.elbe.relations.menu.help.welcome`).
  This fixes clean workspaces only. Persisted models keep the old id and would still crash.
  Rejected as the sole fix.
- *A new command owned by the intro fragment, with its own Help menu contribution.*
  This would need a second menu item, plus either removing the host item or leaving a dead
  duplicate. It is more model surgery for the same result.
- *Import with an `xpath:` or typed lookup.* Not supported by `ModelAssembler` for imports.

### D2. Guard script for import ambiguity

Add `openspec/changes/fix-welcome-handler-import/check-imports.py`. It lists every
`<imports elementId>` in `bundles/*/*.e4xmi` (except the host model) and counts the
elements in `Relations.e4xmi` carrying that id. It exits non-zero when a count is not 1.
It is run by hand; there is no CI. On archive, it is moved or not at the maintainer's
discretion.

## Risks / Trade-offs

- [The old persisted classless handler `org.elbe.relations.handler.intro` still competes
  in upgraded workspaces] → D1 activates later, so ours wins. Verify on a workspace from
  the previous release (`add-welcome-page` task 6.7).
- [Help > Welcome is disabled until startup completes] → not observable. The window is not
  usable before then.
- [`org.eclipse.ui.help.helpContents` remains a duplicated id] → harmless while nothing
  imports it. The guard script flags it as soon as a fragment does.

## Migration Plan

None. No data or preferences are involved. Rollback means reverting the changed files in `org.elbe.relations.intro`.

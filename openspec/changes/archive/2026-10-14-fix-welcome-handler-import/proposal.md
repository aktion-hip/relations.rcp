# Proposal

## Why

With the `add-welcome-page` change in place, Relations does not start. Model assembly
aborts with:

```
ClassCastException: HandledMenuItemImpl cannot be cast to MCommand
  at HandlerImpl.eSet ← ModelAssembler.resolveImports ← … ← E4Application.loadApplicationModel
```

The cause is in `org.elbe.relations.intro/Welcome.e4xmi`. Its handler references the
command through an `<imports>` element with elementId `org.eclipse.ui.help.quickStartAction`.
In `Relations.e4xmi` that id belongs to **two** elements: the Help > Welcome
`HandledMenuItem` (line 125) and the `Command` (line 435). `ModelAssembler.resolveImports`
takes the first element with a matching id. Because the main menu sits inside the window,
which comes before `commands` in the model, that is the menu item. It is then assigned to
`MHandler.command`, which fails.

Every start fails, with or without a clean workspace, so the application cannot be used
at all. It also means none of the manual checks for `add-welcome-page` (its section 6) could
run yet.

## What Changes

- Remove the command import and the handler fragment from `Welcome.e4xmi`.
- Register the Help > Welcome handler **in code**: `WelcomeAddon` activates
  `OpenWelcomeHandler` for the command id `org.eclipse.ui.help.quickStartAction` through
  `EHandlerService`. Lookup by command id goes through the command service, not the model
  element tree, so it cannot pick up the menu item. It also works with models persisted
  by earlier runs, which keep the menu item's old id. Renaming the menu item in
  `Relations.e4xmi` would not reach those models.
- Add a repeatable check (a small script under the change, run by hand) confirming that
  every `<imports>` elementId in the bundles' `*.e4xmi` fragments matches exactly one
  element in `Relations.e4xmi`. This class of clash is invisible to the build.

Behaviour stays as specified by `add-welcome-page` (capability `welcome-page`). This change
only fixes how that behaviour is wired.

## Capabilities

### New Capabilities
<!-- none -->

### Modified Capabilities
<!-- none. The required behaviour (application starts; Help > Welcome opens the page) is
     already specified in the pending add-welcome-page change's `welcome-page` spec. This is
     an implementation defect, so the change sets skip_specs: true. -->

## Impact

- `bundles/org.elbe.relations.intro`: `Welcome.e4xmi` (two elements removed),
  `WelcomeAddon.java` (handler activation), `WelcomeConstants.java` (command id),
  `META-INF/MANIFEST.MF` (+ import `org.eclipse.e4.core.commands`).
- No changes to `org.elbe.relations`, the product or the features.
- Relation to `add-welcome-page`: that change is still open. Its task 5.1 (handler in the
  fragment) is superseded by this fix, and its manual checks (section 6) become
  runnable once this is applied.
- Verification: the build cannot detect this (`mvn verify` runs no tests and does not
  assemble the model). It must be checked by launching the product.

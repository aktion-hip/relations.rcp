# Tasks

## 1. Guard against ambiguous fragment imports

- [x] 1.1 Write `openspec/changes/fix-welcome-handler-import/check-imports.py` (design D2). Verify that it exits non-zero and names `org.eclipse.ui.help.quickStartAction` against the current tree (it reproduces the defect), and that it reports every other import as unique.

## 2. Replace the model handler with programmatic activation

- [x] 2.1 Remove the `<imports>` element `_welcomeImportCommand` and the `handlers` fragment `_welcomeHandlerFragment` from `bundles/org.elbe.relations.intro/Welcome.e4xmi`, keeping the descriptor and addon fragments. Verify that the file is well-formed and that `check-imports.py` now exits 0.
- [x] 2.2 Add `COMMAND_WELCOME = "org.eclipse.ui.help.quickStartAction"` to `WelcomeConstants`, and add `org.eclipse.e4.core.commands` to `Import-Package` in the intro `MANIFEST.MF`. Verify this together with 2.3 in the build.
- [x] 2.3 In `WelcomeAddon.appStartupComplete`, inject `EHandlerService` and call `activateHandler(WelcomeConstants.COMMAND_WELCOME, new OpenWelcomeHandler())` before the first-start check (design D1). Verify with `mvn -B clean verify` (BUILD SUCCESS, intro jar contains the classes), then restore the `pom.tycho` files the build deletes (`git checkout -- bundles/pom.tycho features/pom.tycho products/pom.tycho`).

## 3. Manual verification in the launched product

The build does not assemble the e4 model, so these steps can only be checked by launching the product (Windows).

- [x] 3.1 Start with a fresh workspace (`-data <new dir>`). Verify that the application starts without the `ClassCastException` (check the console and `<workspace>/.metadata/.log`) and that the welcome page appears.
- [x] 3.2 In the same workspace, choose Help > Welcome after closing the page. Verify that the page opens. Then restart and verify that the menu still works with the now persisted model.
- [x] 3.3 Start with a workspace persisted by the previous release, which still contains the classless `org.elbe.relations.handler.intro`. Verify that startup succeeds and that Help > Welcome opens the page (design D1 risk).
- [x] 3.4 Once 3.1–3.3 pass, continue with the manual checks of `add-welcome-page` (its section 6), and record in that change's `tasks.md` that its task 5.1 handler was replaced by this fix.

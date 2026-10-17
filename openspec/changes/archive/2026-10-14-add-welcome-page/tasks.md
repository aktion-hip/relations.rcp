# Tasks

## 1. Remove the non-functional Universal Intro wiring

- [x] 1.1 In `bundles/org.elbe.relations.intro`: delete `src/org/elbe/relations/intro/IntroHandler.java` and `intro/introContent.xml`, and remove both `org.eclipse.ui.intro*` extensions from `plugin.xml`. Verify that `grep -rn "org.eclipse.ui.intro" bundles/org.elbe.relations.intro --exclude-dir=target --exclude-dir=bin` returns nothing.
- [x] 1.2 In `bundles/org.elbe.relations`: delete `handlers/OpenIntroHandler.java` and `utility/IntroDisplayStartup.java`, remove the `org.eclipse.ui.startup` extension (`org.elbe.relations.startup`) from `plugin.xml`, and remove the three `org.eclipse.ui.intro*` lines from `plugin_customization.ini`. Verify with `grep -rn "IntroDisplayStartup\|OpenIntroHandler\|intro.universal" bundles/org.elbe.relations --exclude-dir=bin --exclude-dir=target` (expected: no hits).
- [x] 1.3 Delete the dangling handler `_O8VwIDDNEfGl_dCuvLkdnQ` (`org.elbe.relations.handler.intro`) from `Relations.e4xmi`. Keep the Help > Welcome menu item and command `_4iC8EDDGEfGl_dCuvLkdnQ`. Verify the XML is well-formed (`xmllint --noout`) and that the command is still referenced by the menu item.
- [x] 1.4 Remove `org.eclipse.ui.intro` from `Require-Bundle` in `bundles/org.elbe.relations/META-INF/MANIFEST.MF`. Verify that the host bundle compiles (`mvn -pl bundles/org.elbe.relations -am compile` or a clean Eclipse build with no errors).
- [x] 1.5 Remove `<intro introId="org.eclipse.ui.intro.universal"/>` from `products/relations.rcp.product/relations.rcp.product`. Verify the product file is well-formed.

## 2. Rebuild the intro bundle skeleton

- [x] 2.1 Rewrite `org.elbe.relations.intro/META-INF/MANIFEST.MF` as in design D8: drop the `Require-Bundle` entries for `org.eclipse.ui*`, add the `Import-Package` list, `Require-Bundle: org.eclipse.help.base`, `Bundle-Localization: OSGI-INF/l10n/bundle`, `Bundle-ActivationPolicy: lazy`, and `Bundle-Name: %bundle.name`. Verify that the bundle resolves in the Eclipse target (no manifest errors).
- [x] 2.2 Add `OSGI-INF/l10n/bundle.properties` and `bundle_de.properties` with `bundle.name`, `part.welcome.label` ("Welcome" / "Willkommen"), the fallback text and the help-link label. Verify that both files contain the same key set (`diff <(cut -d= -f1 …) <(…)`).
- [x] 2.3 Copy `relations48.gif` and `RelationWare.png` from `org.elbe.relations/icons/` to `org.elbe.relations.intro/welcome/img/`, and update `build.properties` `bin.includes` to `plugin.xml, META-INF/, ., Welcome.e4xmi, OSGI-INF/, icons/, welcome/`. Verify after a build that the jar in `target/` contains these paths (`unzip -l`).

## 3. Welcome content (en + de)

- [x] 3.1 Write `welcome/welcome.html` and `welcome/css/welcome.css` from `introOverview.xml`, `introFirststeps.xml` and `introWhatsNew.xml`. Use three sections, the Relations logo, help links as `relations-welcome://help?topic=/org.elbe.relations.help/html/gettingstarted/index.html` and `…/html/whatsNew/index.html`, and a "Start working" link to `relations-welcome://close`. Verify that the file opens correctly in a desktop browser (styles and images load, no placeholder text).
- [x] 3.2 Write `welcome/welcome_de.html` as the German counterpart (same folder, see design D6), with the same structure and links. Verify that the link targets are identical to the English file (`grep -o 'relations-welcome://[^"]*'` on both, then diff), and flag the wording for the maintainer to review (design: Open Questions).
- [x] 3.3 Delete `intro/` (`introOverview.xml`, `introFirststeps.xml`, `introWhatsNew.xml`, `extensions.xml`, `css/relations.css`) now that its content is ported. Verify that no file references `intro/` anymore.

## 4. Java implementation

- [x] 4.1 Add `org.elbe.relations.intro.WelcomeConstants` with the part descriptor id, preference node/key (`welcome.shown`), stack id `relations.views`, and URL scheme. Verify it compiles.
- [ ] 4.2 Add a help launcher helper (`ensureWebappRunning` + `Program.launch` of `index.jsp?topic=…`) as in design D2. Verify manually: calling it with the getting-started topic opens that topic in the system browser.
- [ ] 4.3 Add the `WelcomePart` POJO (`@PostConstruct`): an SWT `Browser` loading `welcome/welcome[_<lang>].html` through `FileLocator` (design D6), a `LocationListener` dispatching `help` / `close` / external links with `doit = false`, and the `SWTError` fallback composite of design D7. Verify by opening the part (task 6) and by forcing the fallback branch once (temporarily throw in the try) to see the label + link.
- [x] 4.4 Add a `WelcomeOpener` helper (find existing part → activate; otherwise create from descriptor, add to `relations.views`, then `showPart` ACTIVATE) and `OpenWelcomeHandler` (`@Execute` delegates to it). Verify that Help > Welcome opens the page, and that a second invocation activates the same part instead of opening a second one.
- [x] 4.5 Add `WelcomeAddon`. In `@PostConstruct` it removes any persisted welcome part, and on `APP_STARTUP_COMPLETE` it checks and sets + flushes the `welcome.shown` preference, and opens the page via `UISynchronize.asyncExec` (design D5). Verify through the first-start and restart checks in section 6.

## 5. Model fragment and packaging

- [x] 5.1 Create `Welcome.e4xmi` with the part descriptor (closeable, `removeOnHide` tag, `%part.welcome.label`, icon), the handler for imported command `org.eclipse.ui.help.quickStartAction`, and the addon, all with stable `elementId`s. Register it in `plugin.xml` under `org.eclipse.e4.workbench.model`. Verify the XML is well-formed and that the imported command elementId matches `Relations.e4xmi`.
  - Note: the handler and command import from 5.1 broke startup (the elementId is shared with the Help > Welcome menu item). They were replaced by programmatic activation in `fix-welcome-handler-import`.
- [x] 5.2 Add `org.elbe.relations.intro` to `features/relations.rcp.feature/feature.xml`. Verify with `mvn clean verify` from the root that the product builds and that the materialised product contains `plugins/org.elbe.relations.intro_*`.
- [ ] 5.3 Check that German and English l10n are complete for all new user-facing strings (bundle.properties pair, HTML pair). The existing `command.show.welcome` keys in the host are reused. Verify by running the product with `-nl de` and checking that no `%key` or English text shows on the page, tab or menu.

## 6. Manual verification in the launched product

The build runs no tests, so each of these is checked by hand in an exported or launched product, on Windows at least.

- [x] 6.1 First start: with a fresh workspace (`-data <new dir>`), start Relations. Verify that the welcome page opens on top of the browser area after startup, that it shows all three sections and the logo, and that the DB connection works normally.
- [x] 6.2 Restart the same workspace twice. Verify the page does not open and the previously active browser is shown both times (also covers duplicate fragment elements: Help > Welcome still opens exactly one page).
- [ ] 6.3 Open via Help > Welcome, then exit without closing it, and restart. Verify the page is not restored.
- [ ] 6.4 Click First Steps and What's New. Verify that each opens the correct help topic in the system browser and that the welcome page stays unchanged. Click "Start working" and verify the page closes and the browser is visible.
- [ ] 6.5 Crash case: on a fresh workspace, let the page appear, kill the process, and restart. Verify the page does not reappear.
- [ ] 6.6 Start with `-nl de`. Verify that the page, tab label and menu entry are in German. Start with `-nl fr` and verify the English fallback.
- [x] 6.7 Upgrade case: start with a workspace created by the previous release. Verify the page appears exactly once.

## 7. Follow-up (not part of this change)

- [x] 7.1 Record a follow-up: assess removing `org.eclipse.ui.intro`, `org.eclipse.ui.intro.universal` and `org.eclipse.ui.intro.quicklinks` from `relations.add.feature`, together with the placeholder `org.elbe.relations.help/intro/introContent.xml`, after a cold target resolve. The Help > Welcome menu icon in `Relations.e4xmi` still points to `platform:/plugin/org.eclipse.ui.intro/icons/welcome16.png` and must be re-pointed first. Verify that the note exists in the change's archive summary or as an issue.

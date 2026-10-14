# Design

## Context

See proposal.md (Why) for the motivation. The current state that shapes the approach:

- **Pure e4.** The product application is `org.eclipse.e4.ui.workbench.swt.E4Application`
  and the model is `Relations.e4xmi`. No 3.x `Workbench` is created, so
  `PlatformUI.getWorkbench()` and `IIntroManager` are unusable. This rules out the
  `org.eclipse.ui.intro` framework, including `CustomizableIntroPart` and the universal
  intro.
- **Model is persisted.** `E4Application` persists `workbench.xmi` by default. The host
  `RelationsLifeCycle` has a `@PreSave` hook, and `-clearPersistedState` is not set
  anywhere. Any part added at runtime is therefore restored on the next start unless it
  is removed first.
- **Startup activation already exists.** `RelationsLifeCycle.subscribeTest` reacts to
  `UIEvents.UILifeCycle.APP_STARTUP_COMPLETE` by activating the last browser in the
  `relations.views` stack. The welcome page has to win over that activation on a first
  start.
- **Help is opened externally.** `HelpContentHandler` calls
  `BaseHelpSystem.ensureWebappRunning()` and `Program.launch("http://host:port/help/index.jsp")`.
  Topics are addressable as `index.jsp?topic=/org.elbe.relations.help/html/<path>`. Both
  target topics exist: `html/gettingstarted/index.html` and `html/whatsNew/index.html`.
- **Existing Help > Welcome wiring in the host model:** menu item `_8T40IEozEeOlq5sfE9Wmlw`
  → command `org.eclipse.ui.help.quickStartAction` (`_4iC8EDDGEfGl_dCuvLkdnQ`, already
  localised as `%command.show.welcome` / `%command.welcome.desc` in en and de) → handler
  `org.elbe.relations.handler.intro` with **no** `contributionURI`.
- **Extension pattern.** Feature bundles plug in through their own `plugin.xml` →
  `org.eclipse.e4.workbench.model` fragment (see `org.elbe.relations.defaultBrowser`).
  The host model is left alone wherever possible.

## Goals / Non-Goals

**Goals:**
- Keep all welcome behaviour inside `org.elbe.relations.intro`. Without the bundle, the
  application runs exactly as before, apart from an inert Help > Welcome entry.
- Make the HTML content plain static files that can be edited without touching Java.

**Non-Goals:**
- A generic intro/extension mechanism for other bundles to add welcome content.
- Offline rendering of help topics inside the welcome page.

## Decisions

### D1. e4 part plus SWT `Browser` with static HTML, not the intro framework

The welcome page is an `MPartDescriptor` (`org.elbe.relations.intro.part.welcome`). Its
POJO part class creates an SWT `Browser` and loads a local `welcome.html` resolved from the
bundle through `FileLocator.toFileURL` (the bundle must be unpacked or the files
extracted, see D6).

- *Alternative: compatibility layer + Universal Intro.* Rejected by the maintainer. It
  would change the application type, lifecycle, model processing and key-binding
  behaviour.
- *Alternative: native SWT widgets (Labels, Links, FormText).* Possible, but it loses the
  prepared HTML/CSS styling, and every text tweak becomes a code change. It is kept only
  as the fallback (D7).

The three prepared `extensionContent` blocks (Overview, First Steps, What's New) are
converted by hand into **one** HTML page with three sections. A single page avoids
in-page navigation state and matches how short the content is. The universal intro's
"Workbench" / "close this welcome page" sentences are rewritten as the "Start working"
action.

### D2. Links via a custom URL scheme intercepted by a `LocationListener`

HTML links use `relations-welcome://help?topic=<path>` and `relations-welcome://close`. A
`LocationListener.changing` handler sets `event.doit = false` and dispatches:

- `help` → reuse the `HelpContentHandler` approach: `ensureWebappRunning()`, then
  `Program.launch(".../help/index.jsp?topic=" + topic)`. The webapp URL building is
  duplicated in the intro bundle (around five lines) instead of exporting host internals.
  The help bundle is referenced by its id only. There is no compile dependency on it.
- `close` → `EPartService.hidePart(part, true)`.
- any other `http(s)` URL → `Program.launch`, so the page itself never navigates away.

*Alternative: `BrowserFunction` (JS→Java callbacks).* Rejected. It needs JavaScript
enabled and more lifecycle care, while plain link interception needs neither.

### D3. Contribution through a model fragment in the intro bundle

`org.elbe.relations.intro/Welcome.e4xmi`, registered via `org.eclipse.e4.workbench.model`
in `plugin.xml`, contributes:

- the **part descriptor** (into `MApplication.descriptors`). It is closeable, has the
  label `%part.welcome.label` and the icon `icons/welcome.gif`, and carries the tag
  `removeOnHide`.
- a **handler** for the imported command `org.eclipse.ui.help.quickStartAction`
  (`MApplication.handlers`), with class `OpenWelcomeHandler`.
- an **addon** `WelcomeAddon` (`MApplication.addons`).

The host `Relations.e4xmi` changes in exactly one place: the dangling handler
`_O8VwIDDNEfGl_dCuvLkdnQ` is deleted, so it cannot compete with the fragment's handler.
The menu item and command stay in the host because they are already localised and
positioned there.

### D4. Showing the part: in front of `relations.views`

`OpenWelcomeHandler` and `WelcomeAddon` share a small helper. It looks for an existing
part with the descriptor id (`EPartService.findPart`) and activates it. If there is none,
it calls `EPartService.createPart(descriptorId)`, adds the part to the stack found by
`EModelService.find("relations.views", app)` and calls `showPart(..., ACTIVATE)`. This is
the main 60 % browser stack, so the page covers the browser area. Closing it reveals the
previously selected browser (spec: "Start working").

### D5. First-start logic and not restoring after exit

`WelcomeAddon` works in two steps:

1. In its `@PostConstruct`, it **removes any persisted welcome part** from the model.
   Addons are created after the model fragments are merged and before the model is
   rendered, so a page left open at exit is never restored and never flashes up. Doing
   this at startup is more robust than a shutdown hook, because it also covers crashes and
   the case where `@PreSave` does not run.
2. On `APP_STARTUP_COMPLETE`, it reads the instance-scope preference
   `org.elbe.relations.intro/welcome.shown`. If it is unset or `false`, the addon sets it
   to `true`, **flushes immediately**, and then shows the page through
   `UISynchronize.asyncExec`. The deferral makes the page show after the host's
   `subscribeTest` has activated the saved browser. Both listen to the same event and
   their order is undefined. Deferring by one UI cycle makes the welcome page reliably
   end up on top.

Setting and flushing the flag **before** showing the page satisfies "shown state
survives a crash". Instance scope is per workspace, which matches the spec ("first time
… in a given workspace"). Existing users upgrading have no key, so they see the page
once. That is intended, because it carries What's New.

### D6. Localisation and resource layout

```
org.elbe.relations.intro/
  plugin.xml                       (model fragment only)
  Welcome.e4xmi
  OSGI-INF/l10n/bundle.properties  (Bundle-Name, part.welcome.label …)
  OSGI-INF/l10n/bundle_de.properties
  welcome/welcome.html             (English)
  welcome/welcome_de.html          (German)
  welcome/css/welcome.css
  welcome/img/relations48.gif, RelationWare.png
  icons/welcome.gif
```

The page is resolved by language, not through `$nl$`: `FileLocator.find(bundle, "welcome")`
followed by `FileLocator.toFileURL` on the **directory**, which extracts the CSS and
images together with the pages. The part then picks `welcome_<language>.html` for the
injected e4 locale (`TranslationService.LOCALE`) and falls back to `welcome.html`.
Keeping all translations in the same folder lets them share the relative `css/` and `img/`
links. A `$nl$` lookup would need a separate `nl/de/welcome/` tree with `../../../`
links. The translated HTML files are full copies (two languages, three short sections).
They are not templated.

`MANIFEST.MF` gets `Bundle-Localization: OSGI-INF/l10n/bundle` and
`Bundle-ActivationPolicy: lazy`. The fallback text (D7) and the error log messages live
in the l10n properties files.

Images come from `org.elbe.relations/icons/` (`relations48.gif`, `RelationWare.png`)
and are copied into the intro bundle. They are not referenced across bundles.

### D7. Fallback when no browser engine is available

`new Browser(...)` is wrapped in a `try/catch (SWTError)`. On failure, the part logs the
error through the e4 `Logger` and renders a `Composite` with a wrapped `Label` (overview
text) and an SWT `Link` ("Open help") that uses the same help-launch code. Startup is
never blocked.

### D8. Manifest dependencies

The intro bundle drops `Require-Bundle: org.eclipse.ui.intro, org.eclipse.ui.intro.universal,
org.eclipse.ui` and uses `Import-Package` in line with the other feature bundles:
`jakarta.annotation`, `jakarta.inject`, `org.eclipse.core.runtime`,
`org.eclipse.core.runtime.preferences`, `org.osgi.service.prefs`, `org.eclipse.e4.core.di*`,
`org.eclipse.e4.core.services.log`, `org.eclipse.e4.ui.di`, `org.eclipse.e4.ui.model.application*`,
`org.eclipse.e4.ui.workbench`, `org.eclipse.e4.ui.workbench.modeling`, `org.eclipse.swt*`,
`org.eclipse.swt.browser`, `org.eclipse.swt.program`, `org.osgi.framework`, plus
`Require-Bundle: org.eclipse.help.base` for `BaseHelpSystem` / `WebappManager` (internal,
`@SuppressWarnings("restriction")`, the same as the host).

The host `org.elbe.relations` MANIFEST drops `org.eclipse.ui.intro` once `OpenIntroHandler`
is deleted. It keeps `org.eclipse.ui.workbench`, which other code still uses.

## Risks / Trade-offs

- [SWT `Browser` engine missing or misbehaving (WebKitGTK not installed on Linux, Edge
  WebView2 runtime missing on older Windows)] → D7 fallback. The manual check covers
  Windows, which is the maintainer's platform.
- [Fragment/persisted-model interplay: descriptor or addon duplicated after restarts] →
  fragment elements get stable `elementId`s, and e4 merges by id. The test plan includes
  three consecutive restarts.
- [Order race with `RelationsLifeCycle.subscribeTest`] → `asyncExec` deferral (D5). If it
  still loses on some platform, the fallback is to have the addon listen for the host's
  browser activation instead. That is an implementation detail and does not change the
  spec.
- [Using internal help API (`BaseHelpSystem`, `WebappManager`)] → already accepted in the
  host. Isolated in one helper method.
- [`relations.add.feature` still ships `org.eclipse.ui.intro*`] → harmless dead weight.
  Removal is a follow-up after a resolve check (proposal Non-goals).
- [No automated tests] → manual verification checklist in tasks.md. A unit test for the
  first-start decision logic can live in a new fragment under `tests/`, but it would not
  run in `mvn verify` (not a reactor module). It is optional.

## Migration Plan

- No data migration. The new preference key is created lazily.
- Upgraded installations have no key, so they see the page once on the first start after
  the upgrade.
- Rollback: remove `org.elbe.relations.intro` from `relations.rcp.feature`. Help > Welcome
  then becomes inert again (no handler). Nothing else depends on the bundle.

## Open Questions

- Exact German wording of the page. A draft translation will be written during
  implementation and needs a review by the maintainer.

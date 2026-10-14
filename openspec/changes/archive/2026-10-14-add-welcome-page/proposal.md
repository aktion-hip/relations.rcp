# Proposal

## Why

New users of Relations get no orientation. The application opens straight into an empty
browser, and nothing explains what terms, texts, persons and relations are, or where the
help lives. Content for a welcome page was already written in `org.elbe.relations.intro`
(overview, first steps, what's new), but it never reaches the user:

- The bundle is built, but no feature includes it, so it is not in the product.
- It targets the Eclipse **Universal Intro** framework (`org.eclipse.ui.intro[.universal]`).
  That framework runs on the 3.x `Workbench`/`IIntroManager`. Relations is a pure e4
  application (`E4Application`), so no 3.x workbench is created and the intro cannot be
  shown. `PlatformUI.getWorkbench()` throws.
- Help > Welcome already exists in `Relations.e4xmi`, but its handler has no
  `contributionURI`, so the menu entry does nothing. The two classes that were meant to
  open the intro (`OpenIntroHandler`, `IntroDisplayStartup`) are dead code.

The product decision is to keep Relations on e4 and **not** switch to the compatibility
layer. The prepared content is delivered through an e4-native welcome part instead.

## What Changes

- Add a **Welcome page** as an e4 part, contributed by `org.elbe.relations.intro` through
  its own model fragment. It shows the prepared content (Overview, First Steps, What's New)
  as local HTML in an SWT `Browser`, styled after the Relations branding.
- Links on the page open the matching Relations help topic, in the same way Help > Help
  Contents does (the help webapp in the system browser). A "Start working" action closes
  the page.
- The page **opens automatically on first start**. A preference in the intro bundle
  records that it has been shown. After that it opens only from **Help > Welcome**, which
  gets a working handler.
- The page is available in **English and German**. All page text, the part label and the
  menu text have both translations.
- Make the intro bundle a real member of the product: add it to `relations.rcp.feature`,
  and give it the icons it references (they currently exist only in
  `org.elbe.relations.help/intro`).
- **Remove the non-functional Universal Intro wiring**:
  - in `org.elbe.relations.intro`: the `org.eclipse.ui.intro*` extensions, `IntroHandler`
    (which uses 3.x internals), and the placeholder `introContent.xml`
  - in `org.elbe.relations`: the `org.eclipse.ui.intro.universal` / `INTRO_THEME` lines in
    `plugin_customization.ini`, `IntroDisplayStartup` and its `org.eclipse.ui.startup`
    extension, `OpenIntroHandler`, and the dangling handler element in `Relations.e4xmi`
  - in the product: the `<intro introId="org.eclipse.ui.intro.universal"/>` entry

  The prepared content files (`introOverview.xml`, `introFirststeps.xml`,
  `introWhatsNew.xml`) are the source for the new HTML and are then deleted.

## Non-goals

- Running the real Universal Intro (circles theme, root-page customisation, standby
  mode). That would need the 3.x compatibility layer.
- A "show on every startup" option or a toggle in the preference pages.
- Removing `org.eclipse.ui.intro*` from `relations.add.feature`. Other installed bundles
  may still require them, so this needs a separate resolve check. It is listed as a
  follow-up, not part of this change.
- Editing the help content itself (`gettingstarted`, `whatsNew`). The page links to it as
  it is.

## Capabilities

### New Capabilities
- `welcome-page`: A welcome page that introduces the application. Covers its content
  sections, its links into help, when it appears (first start, Help > Welcome), how it
  closes, and its availability in both languages.

### Modified Capabilities
<!-- none: openspec/specs/ is empty; no existing capability covers this -->

## Impact

- **Bundles**
  - `org.elbe.relations.intro`: rewritten. New part class, addon and handler, plus a
    `Welcome.e4xmi` fragment, HTML/CSS resources (en/de), `OSGI-INF/l10n` files and
    icons. The MANIFEST drops `org.eclipse.ui.intro*` and `org.eclipse.ui`, and adds e4
    UI/DI imports.
  - `org.elbe.relations`: deletions only (dead intro code, ini lines, the startup
    extension), plus removal of one handler element from `Relations.e4xmi`. The
    `org.eclipse.ui.intro` requirement in its MANIFEST becomes unused and is removed.
- **Feature/product**: `relations.rcp.feature/feature.xml` gains the intro bundle. The
  `.product` file loses the `<intro>` element.
- **Runtime dependency**: SWT `Browser` (Edge/WebView2 on Windows, WebKitGTK on Linux,
  WebKit on macOS). If no browser engine is available, the page degrades to a plain-text
  fallback instead of failing.
- **Data/DB**: none. One new instance-scope preference key in the intro bundle.
- **Verification**: there is no automated test path (`mvn verify` runs no tests). The
  change is checked manually in a launched product. The build only proves that it
  compiles.

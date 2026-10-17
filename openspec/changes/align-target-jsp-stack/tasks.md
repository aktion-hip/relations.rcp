# Tasks

## 1. Edit the target definition

- [x] 1.1 In the `releases/2026-03` location of `target-platform/target-platform.target`, add units `org.mortbay.jasper.mortbay-apache-jsp`, `org.mortbay.jasper.mortbay-apache-el`, `org.eclipse.jetty.servlet-api` and `org.apache.aries.spifly.dynamic.bundle` (version `0.0.0`). Verify with `git diff --ignore-cr-at-eol` that only these four lines are added there.
- [x] 1.2 In the `oomph/simrel-orbit/release/4.28.0` location, remove `org.eclipse.jetty.servlet-api` 4.0.6, `org.mortbay.jasper.apache-jsp` 9.0.52 and `org.mortbay.jasper.apache-el` 9.0.52, keeping all other units. Verify with `git diff --ignore-cr-at-eol` that exactly these three lines are removed.
- [x] 1.3 Remove the Maven `<dependency>` for `org.apache.tomcat:tomcat-jasper:11.0.26`. Verify that `grep -n tomcat target-platform/target-platform.target` returns nothing and the file is still well-formed XML (e.g. `python3 -c "import xml.dom.minidom,sys;xml.dom.minidom.parse(sys.argv[1])" target-platform/target-platform.target`).
- [x] 1.4 Keep the file's existing line endings. Verify that `git diff --ignore-cr-at-eol --stat` shows only the intended small change.

## 2. Resolve and build

- [x] 2.1 Run a cold Tycho build (`mvn -U clean verify`, with the p2 cache entries for `org.mortbay.jasper.*` removed or a fresh `-Dmaven.repo.local`). Verify that it finishes with BUILD SUCCESS and the target resolves without errors.
- [x] 2.2 In the built linux product's `configuration/org.eclipse.equinox.simpleconfigurator/bundles.info`, check that `org.mortbay.jasper.mortbay-apache-jsp` 9.0.111, `mortbay-apache-el` 9.0.111, `org.apache.aries.spifly.dynamic.bundle` and `org.eclipse.equinox.jsp.jasper` are listed, and `org.mortbay.jasper.apache-jsp` 9.0.52 is not.
- [x] 2.3 List every bundle in the built product that exports `org.apache.jasper.servlet` (inspect manifests under `plugins/`). Verify that all of them export a 9.x version; record any extra provider in design.md without removing it.

## 3. Runtime verification

- [x] 3.1 Start the built product's OSGi framework headless (copy of `configuration/`, `-Declipse.ignoreApp=true -Dosgi.noShutdown=true -console`) and run `diag org.eclipse.equinox.jsp.jasper` and `ss jasper`. Verify that the jasper bundles are RESOLVED/ACTIVE and that no unresolved-requirement error on `org.apache.jasper.servlet` is logged.
- [ ] 3.2 (User, in the IDE) Reload the target in the target editor, set it as the active target platform, and start the usual Relations launch configuration. Verify that the `Could not resolve module: org.eclipse.equinox.jsp.jasper` error no longer appears. If it does, apply the fallback in design.md, Decision 5 (enable "Include required Plug-ins automatically") and record the outcome.
- [ ] 3.3 (User) In the running application, open Help > Help Contents. Verify that the help pages are displayed without a server or JSP error.

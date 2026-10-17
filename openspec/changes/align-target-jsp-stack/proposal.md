# Proposal

## Why

Launching Relations fails to resolve the help system's JSP engine:

```
Could not resolve module: org.eclipse.equinox.jsp.jasper
  Unresolved requirement: Import-Package: org.apache.jasper.servlet; version="[9.0.0,10.0.0)"
```

`org.eclipse.help.webapp` (from the 2026-03 release train) needs
`equinox.jsp.jasper`, which in turn needs a Jasper 9.x provider. The target
platform takes that provider from an unrelated, older repository
(`oomph/simrel-orbit/release/4.28.0`, `org.mortbay.jasper.apache-jsp` 9.0.52).
It also carries a second, incompatible Jasper (Maven `tomcat-jasper` 11.0.26)
and servlet/EL APIs from three different release trains. The Tycho-built
product happens to resolve, but the IDE runtime does not. The JSP stack should
come from the same release train as the help bundles that consume it.

## What Changes

- Source the help JSP stack from the `releases/2026-03` location, which ships a
  matching set: `org.mortbay.jasper.mortbay-apache-jsp` 9.0.111,
  `org.mortbay.jasper.mortbay-apache-el` 9.0.111,
  `org.eclipse.jetty.servlet-api` 4.0.9 and
  `org.apache.aries.spifly.dynamic.bundle` 1.3.7.
- Remove the outdated Jasper/servlet units (`org.mortbay.jasper.apache-jsp`
  9.0.52, `org.mortbay.jasper.apache-el` 9.0.52,
  `org.eclipse.jetty.servlet-api` 4.0.6) from the `simrel-orbit/4.28.0`
  location. Keep its non-JSP units (batik, xmlgraphics, javax.annotation,
  jakarta.servlet).
- Remove the unused Maven dependency `org.apache.tomcat:tomcat-jasper:11.0.26`.
  No project bundle imports `org.apache.jasper.*`, `org.apache.tomcat.*` or
  `javax/jakarta.el`.
- Change only the target definition. Product, features and launch
  configurations are not changed (see design.md for the fallback if the IDE
  launch still fails).

## Capabilities

### New Capabilities
- `help-runtime`: the bundled Eclipse help system's JSP-based web application
  resolves and starts in the Relations runtime.

### Modified Capabilities
<!-- none: no existing specs in this project -->

## Impact

- `target-platform/target-platform.target`: the only file that changes.
- Built product: the Jasper bundle changes from `org.mortbay.jasper.apache-jsp`
  9.0.52 to `org.mortbay.jasper.mortbay-apache-jsp` 9.0.111, and
  `mortbay-apache-el` 9.0.111 is added. Product size changes slightly.
- Risk: target-platform edits are high risk (network-dependent multi-repo
  resolution). The change must be checked by a cold target resolve, a Tycho
  build and a runtime bundle diagnosis, not by the build alone.
- No Java code, database schema, UI strings or translations change.

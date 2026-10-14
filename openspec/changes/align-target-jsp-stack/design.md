# Design

## Context

Dependency chain behind the error (see proposal.md - Why):

```
org.eclipse.help.webapp          (releases/2026-03, via feature org.eclipse.help)
  -> org.eclipse.equinox.jsp.jasper.registry
     -> org.eclipse.equinox.jsp.jasper
          Import-Package org.apache.jasper.servlet [9,10)
          -> Jasper 9.x bundle   (not listed in any feature; pulled in only
             |                    by requirement resolution)
             | javax.el 3.0, javax.servlet 4.0, org.apache.el.util [9,10) (9.0.96+),
             | org.eclipse.jdt.internal.compiler.*
             | Require-Capability osgi.extender=osgi.serviceloader.processor
             v
             org.apache.aries.spifly.dynamic.bundle (+ org.objectweb.asm*)
```

Observed facts (checked 2026-10-13):

- **Target today.** The target lists `org.mortbay.jasper.apache-jsp` 9.0.52,
  `apache-el` 9.0.52 and `jetty.servlet-api` 4.0.6 from
  `oomph/simrel-orbit/release/4.28.0`. It also lists Maven
  `tomcat-jasper` 11.0.26, which exports `org.apache.jasper.*` 11.x and so can
  never satisfy `[9,10)`.
- **Built product.** The Tycho product built 2026-09-30 contains
  `apache-jsp` 9.0.52 and no `apache-el`. Started headless (framework only, no
  application), `apache-jsp` is RESOLVED and both `equinox.jsp.jasper` bundles
  start. The reported error (bundle id `[113]`) therefore comes from a different
  runtime, most likely a PDE launch from the IDE.
- **2026-03 repository.** `releases/2026-03/202603111000` contains
  `org.mortbay.jasper.mortbay-apache-jsp` 9.0.111 (exports
  `org.apache.jasper.servlet` 9.0.111),
  `org.mortbay.jasper.mortbay-apache-el` 9.0.111 (exports `javax.el` 3.0 and
  `org.apache.el.*`), `org.eclipse.jetty.servlet-api` 4.0.9 and
  `org.apache.aries.spifly.dynamic.bundle` 1.3.7. Note the new
  `mortbay-` IU/bundle IDs.
- **Project code.** No project `MANIFEST.MF` imports Jasper, Tomcat or EL
  packages.

## Goals / Non-Goals

**Goals:**
- One JSP engine (9.x) in the target, taken from the same release train as
  `help.webapp`, together with everything it needs.
- Remove target entries that offer conflicting or unused Jasper providers.

**Non-Goals:**
- Changing `relations.add.feature`, the `.product`, or IDE launch
  configurations. If the IDE launch still fails after the target fix, that is
  handled under the fallback in Decisions.
- Cleaning up the other mixed servlet/EL APIs (`jakarta.servlet-api` 6.1,
  `jakarta.servlet` 5.0, `javax.servlet.jsp-api` 2.3.3, `com.sun.el.javax.el`,
  `javax.el-api`). They are outside the error path. Removing them widens the
  risk without fixing anything here.
- Updating the pinned `org.eclipse.jdt.core.compiler.batch` 3.40 from 4.34.
  Jasper imports its packages without a version range, so 3.40 satisfies them.

## Decisions

1. **Take the JSP stack from `releases/2026-03`, not from a newer Orbit
   repository.** `help.webapp` and `equinox.jsp.jasper` come from 2026-03, and
   the release train was built and tested with its own Jasper 9.0.111.
   *Alternative:* keep `simrel-orbit/4.28.0` and add `apache-el` 9.0.52 there.
   This mixes trains again and leaves an old Jasper with known CVEs.

2. **List the units explicitly with version `0.0.0`.** The units are
   `org.mortbay.jasper.mortbay-apache-jsp`,
   `org.mortbay.jasper.mortbay-apache-el`, `org.eclipse.jetty.servlet-api` and
   `org.apache.aries.spifly.dynamic.bundle`, all in the 2026-03 location.
   Today they only arrive indirectly through planner resolution. Listing them
   makes the target self-documenting and guarantees PDE has them available.
   `0.0.0` matches the convention already used in that location.
   *Alternative:* pin exact versions. That is more reproducible, but the
   location already floats with `0.0.0` and the repository URL is
   release-pinned anyway.

3. **Delete the 9.0.52 Jasper, 9.0.52 EL and 4.0.6 servlet-api units from the
   4.28.0 location.** Keep the location for batik, xmlgraphics,
   javax.annotation and jakarta.servlet, which other bundles may need.

4. **Delete the Maven `tomcat-jasper` 11.0.26 dependency.** It is unused and
   exports a conflicting `org.apache.jasper.*` 11.x.

5. **Fallback if the IDE launch still fails.** The target fix guarantees the
   bundles are *available*. A PDE launch with plug-ins or features selected can
   still leave them out. In that case the fix is to enable "Include required
   Plug-ins automatically" in the launch configuration (a local, uncommitted
   setting). Adding the bundles to `relations.add.feature` is a separate
   follow-up change, not part of this one.

## Risks / Trade-offs

- [Another repository in the target still brings in a second Jasper, e.g.
  Orbit 2024-12 `org.eclipse.orbit.maven.osgi.all`, which could contain
  `apache-jsp` 9.0.96] -> After resolving, list every bundle that exports
  `org.apache.jasper.servlet`. If an extra 9.x provider remains it is harmless
  (it fits the range), but record it. Do not widen the scope to remove it.
- [Bundle ID rename `apache-jsp` -> `mortbay-apache-jsp`] -> Nothing in the
  repository refers to the old ID (checked by grep), so nothing breaks.
- [Network-dependent multi-repo resolution] -> Verify with a cold resolve
  (`mvn -U clean verify` with the affected p2 cache entries removed, or a fresh
  `-Dmaven.repo.local`) in addition to "Reload" in the IDE target editor.
- [CRLF churn] -> The working copy of the `.target` already has whole-file
  CRLF changes. Keep the existing line endings and judge the diff with
  `git diff --ignore-cr-at-eol`.

## Migration Plan

Edit the target, reload it in the IDE (Set as Active Target Platform), rebuild
with Tycho, and verify as described in tasks.md. To roll back, revert
`target-platform/target-platform.target`. No user data is affected.

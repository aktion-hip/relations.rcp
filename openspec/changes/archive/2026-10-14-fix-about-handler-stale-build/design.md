# Design

## Context

See proposal.md (Why) for the diagnosis. Relevant mechanics:

- Eclipse JDT compiles with "proceed on errors". A class whose types cannot be resolved
  is still written, as a stub. Its methods throw `java.lang.Error("Unresolved compilation
  problems: …")`, and unresolved types appear in signatures under their simple name
  (`LShell;`). When the e4 injector reflects over such a class (`getDeclaredMethods`), the
  JVM tries to load a class literally named `Shell`, which produces this
  `NoClassDefFoundError`.
- The marker string `Unresolved compilation problem` (singular or plural) is stored in the
  class file's constant pool. That makes stubs detectable by a plain byte search, without
  a Java toolchain.
- The PDE classpath container (`org.eclipse.pde.core.requiredPlugins` in `.classpath`)
  is computed from `MANIFEST.MF` against the active target platform. While either is
  unresolvable, for example in the middle of editing the manifest or reloading the
  target, every dependency vanishes for the files compiled at that moment.
- The IDE output folder is `bin/` (`.classpath`) and is git-ignored (`**/bin/`). Tycho
  compiles into `target/classes` independently, so the two builds never share class
  files.

## Goals / Non-Goals

**Goals:**
- A working Help > About when launching from the IDE.
- Fast detection of this class of problem before the next launch.

**Non-Goals:**
- Changing how the bundle declares SWT/JFace. They come in through the reexport of
  `Require-Bundle: org.eclipse.ui`. That is unrelated to this defect, because the Tycho
  build resolves it fine.
- Automating the check in Maven, since there is no CI and `tests/` is not a reactor module.

## Decisions

### D1. Fix by clean rebuild, not by touching the source

Editing `AboutHandler.java`, just to force recompilation, would only paper over the
cause. If the classpath is still broken, it would produce the same stub again. The tasks
first make sure the classpath resolves (target reload, no manifest errors in the Problems
view), then delete the two stubs and run Project > Clean. Deleting the stubs alone,
without a clean, would also work, because JDT recompiles missing class files on the next
build. Doing both is cheap and certain.

### D2. Guard script in Python under `tools/`

`tools/check-ide-output.py` walks `bundles/*/bin`, reads each `.class` and checks for
`b"Unresolved compilation problem"`. It prints `bundle: path` per hit and a summary, and
exits 1 on hits. It runs from the repository root and uses the standard library only.
Python is already used for the change-local `check-imports.py`.

*Alternatives:*
- A shell `grep -rla`. Fine on Linux, but awkward on Windows, the maintainer's platform.
- A Java/Maven check. It would need a reactor module and is overkill without CI.

## Risks / Trade-offs

- [The classpath breaks again during the next manifest edit] → the guard script catches
  it before launch. The tasks also tell the maintainer to save `MANIFEST.MF` before
  saving dependent sources when bumping versions.
- [A new top-level `tools/` directory] → small, does not take part in the build. It can
  later host the archived `check-imports.py` as well (not in scope).

## Migration Plan

None.

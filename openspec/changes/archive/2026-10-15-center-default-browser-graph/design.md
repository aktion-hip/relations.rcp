# Design

## Context

See proposal.md for motivation. All layout happens in
`bundles/org.elbe.relations.defaultBrowser/src/org/elbe/relations/defaultbrowser/internal/DefaultBrowserPart.java`:

- `setAroundCenter()` computes a translation `T` and places every figure at
  `T + position`, where the focus item gets position `(0,0)` and related items get the ring
  positions from `ItemPositionCalculator`. `T` is the focus item's top-left corner:
  `T = (w/2 - ITEM_WIDTH/2, h/2 - ITEM_HEIGHT)`.
- `(w, h)` comes from `getSize()`, which returns the persisted preference
  `dft.browser.initial.size` (or `Constants.DEFAULT_SIZE` 655×540) whenever the viewer
  control is not the focus control, and otherwise `Control.getSize()`, walking up to parent
  composites while the size is `0×0`.
- A `SWT.Resize` listener calls `recenter()`, which moves every figure by
  `(newSize - oldSize) / 2` using integer division.
- After the arrangement, `reveal()` calls `viewer.reveal(editPart)`, which may scroll the
  `ScrollingGraphicalViewer`.

Observed defects in this code, each of which moves the figure away from the visible center:

1. `getSize()` normally returns a size that is not the current visible area: the preference
   value (last session's size, or 655×540) when the browser is not focused – which is the
   usual case, since the user typically clicks elsewhere – or a parent composite's larger size.
   A size larger than the actual area moves the figure right and down.
2. `Control.getSize()` includes the control's trim (border and any visible scroll bars),
   so even when the control is used its center lies right/below the client-area center.
3. `recenter()` corrects only by the size delta. If `oldSize` was a wrong size (defect 1),
   that error is preserved; integer division of odd deltas also accumulates drift.
4. The vertical term uses `ITEM_HEIGHT` instead of `ITEM_HEIGHT / 2`, putting the focus item
   15 px above the center (this one shifts up; it is fixed together with the others so the
   result is exact rather than accidentally compensated).

5. **Monitor zoom (the decisive defect, found after fixing 1–4 did not help).** With GEF
   3.29 / draw2d 3.23 on Windows, draw2d's own autoscaling is on by default
   (`draw2d.enableAutoscale`). The `FigureCanvas` then has SWT autoscaling disabled, so its
   size and client area are reported in **device pixels**, while
   `FreeformGraphicalRootEditPart` installs an `AutoscaleFreeformViewport` whose contents are
   scaled by the monitor zoom. A center computed from pixels is scaled once more when drawn:
   at 150 % the focus item lands at 1.5 × the center, i.e. to the lower right.

Defects 1–4 are wrong independently of each other and are fixed too, but defect 5 is what
the user sees.

The ring geometry in `ItemPositionCalculator` is already correct: positions are offsets of
item top-left corners from the focus item's top-left corner, so item centers lie on circles
around the focus item's center, and full rings are point-symmetric (even counts are enforced).
`ItemPositionCalculatorTest` covers it.

## Goals / Non-Goals

**Goals:**
- One source of truth for the visible area: the viewer canvas' client area.
- Layout is a pure function of (visible area, model); resize re-runs it.

**Non-Goals:**
- Changing ring radius, spacing, item size, or the ordering of related items.
- Making a partially filled outer ring with an odd number of items symmetric (it keeps its
  current, slightly asymmetric distribution; the spec only requires symmetry of full rings
  and of even counts).
- Any change to the finder browser.
- Zoom or auto-fit of large graphs.

## Decisions

### D1: Use the canvas client area as the visible area

Take the `FigureCanvas` client area (which excludes border and visible scroll bars),
regardless of focus state, and convert its size into the coordinate system of the contents
layer with `contentsFigure.translateToRelative(PrecisionDimension)`. This divides by whatever
scale lies between the canvas and the layer (the monitor zoom under draw2d autoscaling, 1.0
otherwise), so `(w, h)` is in the same units as the figure locations. Re-arrange also on
`SWT.ZoomChanged` (window moved to a monitor with another zoom).

*Alternatives:* `FigureCanvas.getViewport().getClientArea()` – still in viewport (pixel)
units, because the scale sits below the viewport. Reading `AutoscaleFreeformViewport.getScale()`
directly – rejected, the class is marked `@NoReference` (internal API). Disabling draw2d
autoscaling via `-Ddraw2d.enableAutoscale=false` – rejected, it changes rendering of the whole
application. Keeping the focus-dependent branch – rejected, it is a source of wrong sizes.

### D2: Defer layout until the canvas has a real size

If the client area is empty (part created but not yet laid out), skip positioning and leave a
flag; the first `SWT.Resize` with a non-empty client area performs the arrangement. This
replaces the parent-walking loop and makes the `dft.browser.initial.size` preference
workaround (`getInitialSize`, `saveSize`, `INITIAL_SIZE`, `Constants.DEFAULT_SIZE`)
unnecessary; remove it.

*Alternative:* keep the preference as an initial guess and correct on resize – rejected,
because a correct value is always available by the time the part is shown, and the guess is
what produced the visible offset.

### D3: Resize re-runs the full arrangement

The resize listener calls `setAroundCenter()` (when a model is set) instead of `recenter()`
with a delta; `recenter()` and the `oldSize` field are removed. Layout cost is negligible
(a few dozen figures).

*Alternative:* fix `recenter()` to use exact deltas – rejected, it would still inherit any
error from the previous layout.

### D4: Exact center math and scroll handling

- `T = (round(w/2 - ITEM_WIDTH/2), round(h/2 - ITEM_HEIGHT/2))`, computed in a small
  static helper (e.g. `ItemPositionCalculator.getCenterTranslation(width, height, w, h)` or a
  package-private static method in `DefaultBrowserPart`) so it is unit-testable without SWT.
- After placing the figures, scroll the viewer to its origin
  (`((FigureCanvas) viewer.getControl()).scrollTo(0, 0)`) and drop the
  `viewer.reveal(editPart)` call when the focus item is already within the client area.
  When rings extend beyond the visible area, the focus item is by construction in the
  center of the client area, so it is visible at scroll origin; `reveal` is therefore not
  needed in either case. If testing shows the freeform extent shifts the origin when items
  lie at negative coordinates, keep `reveal` only for the focus item as a fallback.

## Risks / Trade-offs

- [Freeform extent with negative coordinates: outer rings on a small view can lie left of or
  above `(0,0)`, which may make the `FreeformViewport` extend its scroll range negatively]
  → Verify manually with an item that has more than one ring of relations in a small view;
  fall back to `reveal` of the focus item as described in D4.
- [Removing the preference workaround leaves a stale `dft.browser.initial.size` key in user
  preferences] → Harmless; no migration needed.
- [No automated UI test] → Manual check list in tasks; pure geometry is unit-tested in the
  test fragment, which must be run from the IDE because the Maven build runs no tests.

## Migration Plan

None. No data or API changes; rollback is reverting the bundle's source changes.

# Proposal

## Why

The default (graphical) relations browser places the focus item in the middle and its
related items on concentric rings around it, but the whole figure is drawn visibly
shifted towards the lower right of the view instead of in its center. The browser is
the main way users navigate the item graph, so the off-center drawing is noticeable on
every item change and wastes space on the upper and left sides.

## What Changes

- The default browser computes the view center from the area that is actually visible
  in the viewer (its client area), not from a remembered preference size, a default
  size, or the size of a parent composite.
- The focus item is placed so that its center, and therefore the center of all rings,
  coincides with the center of the visible area (the vertical offset currently uses the
  full item height instead of half of it).
- The arrangement is recomputed from scratch when the view is resized instead of being
  shifted incrementally by half the size delta, so rounding errors and a wrong initial
  size can no longer accumulate.
- When the arranged figure fits into the view, the view is not scrolled, so the center
  stays in the middle of what the user sees.
- No change to the ring geometry itself (radius, spacing, number of items per ring) and
  no change to the finder browser (`org.elbe.relations.browser.finder`).

## Capabilities

### New Capabilities
- `default-browser-layout`: how the default graphical browser positions the focus item
  and its related items within the view, including centering and behavior on resize.

### Modified Capabilities
<!-- none: no existing spec covers the browsers -->

## Impact

- Code: `bundles/org.elbe.relations.defaultBrowser`, mainly
  `internal/DefaultBrowserPart.java` (`setAroundCenter`, `getSize`, the resize listener,
  `recenter`, and the persisted `dft.browser.initial.size` workaround). Possibly
  `internal/controller/ItemPositionCalculator.java` if a helper for the center offset is
  extracted.
- Tests: `tests/org.elbe.relations.defaultBrowser.test` (pure-geometry unit test for the
  center computation). These tests are not run by the Maven build.
- No API, model-fragment, DS, preference-UI, translation or data-model changes. The
  `dft.browser.initial.size` preference may become unused; existing values stay harmless.

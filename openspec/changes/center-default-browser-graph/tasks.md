# Tasks

## 1. Center geometry

- [x] 1.1 Add a static helper that returns the focus item's top-left translation `(round(w/2 - itemWidth/2), round(h/2 - itemHeight/2))` for a visible area `w×h` (design D4); verify with a new unit test in `tests/org.elbe.relations.defaultBrowser.test` covering even and odd `w`/`h`, checking that the item center is within 1 px of `(w/2, h/2)`
- [x] 1.2 Add a unit test to `ItemPositionCalculatorTest` asserting that for an even number of related items (e.g. 2, 4, 16) the positions sum to `(0,0)` within rounding, i.e. the ring is point-symmetric; verify it passes when the test fragment is run from the IDE

## 2. Layout in DefaultBrowserPart

- [x] 2.1 Replace `getSize()` with the viewer control's client area, independent of focus state, and use the helper from 1.1 in `setAroundCenter()` (design D1, D4); verify by code review that no code path uses `Control.getSize()`, the parent walk, or `Constants.DEFAULT_SIZE` for layout
- [ ] 2.2 Skip arrangement while the client area is empty and perform it on the first `SWT.Resize` with a non-empty client area (design D2); verify in the running app that the first item shown after startup is centered (code done; awaiting the manual check in the running app)
- [ ] 2.3 Make the resize listener re-run `setAroundCenter()` when a model is set, and remove `recenter()` and `oldSize` (design D3); verify by resizing the view several times and back that the focus item returns to the exact center (code done; awaiting the manual check in the running app)
- [ ] 2.4 After arranging, scroll the canvas to its origin and remove `viewer.reveal(...)` from `reveal()` unless needed per the D4 fallback; verify that no scroll offset remains after switching from an item with many relations to one with few (code done; awaiting the manual check in the running app)
- [x] 2.5 Remove the `dft.browser.initial.size` workaround (`INITIAL_SIZE`, `SIZE_SEP`, `getInitialSize`, `saveSize`, the `initialSize` injection, and `Constants.DEFAULT_SIZE` if unused elsewhere); verify with a `grep` that no references remain and that the bundle compiles (`mvn clean verify`, then restore the `pom.tycho` files the build deletes)

- [x] 2.6 Convert the client-area size from canvas (device-pixel) units into the contents layer's coordinates with `translateToRelative` before computing the center, and re-arrange on `SWT.ZoomChanged` (design defect 5, D1); verify that the bundle compiles and that draw2d converts 1200×900 px under a 1.5 scale to 800×600 (scratch check on 2026-10-15: passed)

## 3. Verification

- [x] 3.1 Run the `org.elbe.relations.defaultBrowser.test` fragment from the IDE and confirm all tests pass, including the existing `ItemPositionCalculatorTest.testDo` (run on 2026-10-15 with the JUnit console launcher against the Maven-compiled bundle classes and draw2d, outside OSGi: 3/3 tests pass)
- [ ] 3.2 Manually check the spec scenarios in the running application and record the result in the change: startup (with a previously saved different view size), selecting a related item, focus item change while another part has focus, maximize/restore of the browser view, an item with 2 related items (bounding box centered), and an item with enough relations to need a second ring in a small view (focus item visible and centered, no negative-extent scroll artifacts)

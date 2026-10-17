# default-browser-layout Specification

## Purpose

Defines where the default graphical relations browser draws the focus item and its
related items, so that the figure is centered in the visible view and stays centered when
the view changes size.

## Requirements

### Requirement: Focus item is centered in the visible view

When the default browser displays an item, it SHALL place the focus item so that the
center of the focus item lies at the center of the area of the view that is visible to the
user, within 1 pixel horizontally and vertically. The visible area SHALL exclude window
trim such as borders and any visible scroll bars, and SHALL NOT be taken from a remembered
or default size or from an enclosing container.

#### Scenario: Item displayed in a freshly opened view

- **WHEN** the application starts and the default browser shows the focus item for the first time
- **THEN** the center of the focus item is at the center of the visible view area, regardless of the view size the browser had in the previous session

#### Scenario: Another item becomes the focus item

- **WHEN** the user selects a related item to become the new focus item
- **THEN** the new focus item is drawn with its center at the center of the visible view area

#### Scenario: Browser view is not the focused part

- **WHEN** the focus item changes while another part (for example the inspector or a search view) has keyboard focus
- **THEN** the focus item is still drawn with its center at the center of the visible view area

### Requirement: Related items are arranged on rings around the centered focus item

The related items SHALL be placed on concentric rings whose common center is the center
of the focus item. For every ring that is completely filled, the arrangement SHALL be
point-symmetric about that center, so the figure as a whole is not shifted in any
direction relative to the view center. Ring radius, item spacing and the number of items
per ring SHALL remain as before this change.

#### Scenario: Even number of related items

- **WHEN** the focus item has an even number of related items that all fit on the first ring
- **THEN** the bounding box of all displayed items is centered at the view center, within 1 pixel

#### Scenario: Several rings

- **WHEN** the focus item has more related items than fit on the first ring
- **THEN** the items of every ring lie on a circle centered at the center of the focus item, and the first ring holds the same number of items as before this change

### Requirement: Centering is kept when the view is resized

When the size of the default browser view changes, the browser SHALL redraw the figure so
that the focus item is again centered in the new visible area. Repeated resizes SHALL NOT
cause the figure to drift away from the center.

#### Scenario: View enlarged

- **WHEN** the user enlarges the browser view (for example by maximizing it or moving a sash)
- **THEN** the center of the focus item is at the center of the new visible view area

#### Scenario: Repeated resizing

- **WHEN** the user resizes the browser view several times and finally returns it to its original size
- **THEN** the focus item is at the same, centered position as before the first resize

### Requirement: No scrolling when the figure fits

When all displayed items fit into the visible view area, the browser SHALL show the
figure without a scroll offset, so that the centered position is what the user sees. When
the figure is larger than the view, the browser SHALL keep the focus item visible.

#### Scenario: Figure fits into the view

- **WHEN** a focus item is displayed whose items all fit into the visible view area
- **THEN** no scroll bars are scrolled away from their origin and the focus item is centered on screen

#### Scenario: Figure larger than the view

- **WHEN** the focus item has so many related items that the outer rings extend beyond the view
- **THEN** the focus item is visible to the user

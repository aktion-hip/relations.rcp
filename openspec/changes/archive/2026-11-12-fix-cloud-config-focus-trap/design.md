# Design

## Context

See proposal.md (Why) for the problem and specs/cloud-config/spec.md for the required
behaviour. Current mechanics that shape the approach:

- The preferences dialog (`com.opcoach.e4.preferences`) is a standard JFace
  `PreferenceDialog`. Switching pages calls `currentPage.okToLeave()`, which by default
  returns `isValid()`. The OK button is enabled from the **current** page's `isValid()`
  only. `okPressed()` calls `performOk()` on every page that was created.
- `CloudConfigPrefPage.createGroup` passes one shared callback, `this::setValid`, to
  every `ICloudProviderConfig.createConfigContents`. Providers call it from SWT modify
  listeners, and those listeners also fire when `initialize()` calls `setText(...)`.
- Each provider has its own "Active:" radio in its own `Group`. Because the radios
  have different parents, SWT does not make them mutually exclusive;
  `handleActivationChange` does that by hand.
- `savePreferences()` writes `cloud_<name>_pref` for **every** provider and writes
  `activeCloudConfig` for whichever radio is selected, whether its configuration is
  valid or not.
- `ExportToCloudAction` already treats an empty or missing `activeCloudConfig` as
  "no Cloud Provider configured" and shows a dialog.

## Goals / Non-Goals

**Goals:**
- Fix everything inside `CloudConfigPrefPage`, with no change to the
  `ICloudProviderConfig` SPI and no change to the three provider bundles.

**Non-Goals:**
- Changing the provider-specific UIs or their validation rules (e.g. the Azure
  connection-string regex, or Azure firing validity events during `initialize()`).
- The separate "Cloud Configuration Helper" dialogs (`ICloudProviderConfigurationHelper`).
- Cleaning up stale entries in existing user profiles, beyond what the next save does.

## Decisions

### D1: The page never marks itself invalid; problems go to the error message

The page stops calling `setValid(false)`, so it stays valid and `okToLeave()` keeps
returning `true`. Additionally, override `okToLeave()` to return `true`, so that the
"always leavable" rule does not depend on validity bookkeeping. Problems are shown only
through `setErrorMessage(...)`.

*Alternative considered:* keep `setValid` and only override `okToLeave()`. Rejected:
JFace enables OK from the current page's `isValid()`, so OK would still be greyed out
while the page shows an incomplete configuration. That conflicts with the decision that
OK saves the values and leaves the provider inactive.

### D2: One wrapped validity callback per provider

In `createGroup`, each provider gets its own callback instead of `this::setValid`:
`valid -> refreshMessage()` (ignores its argument). `refreshMessage()` takes validity
only from the provider whose radio is selected, using its `isValid()`, and sets or
clears the error message. Signals from inactive providers then have no visible effect,
because the message is always computed from the active provider. This includes the
signals fired by `initialize()`, which runs before the radio selection is set.
`refreshMessage()` is also called once after all groups are built, and from the radio
selection handler.

*Alternative considered:* add a provider-scoped callback to the SPI. Rejected because it
needs changes in three bundles for no behavioural gain.

### D3: "None" as a manually managed radio

Add a "None" radio in its own row above the provider groups. It joins the existing
manual exclusivity logic: selecting it deselects and disables every provider; selecting
a provider deselects "None". It is selected initially when `activeCloudConfig` is empty
or names a provider that is no longer registered.

*Alternative considered:* let the user deselect the active provider's radio. Rejected:
radio buttons cannot be deselected by clicking, and a checkbox would break the
"exactly one" model.

### D4: "Empty" is derived from `getConfig()`

A configuration counts as empty when every property in the provider's `getConfig()`
JSON is an empty string, or there are no properties. This works for all three
providers without changing the SPI.

### D5: Save rules

In `savePreferences()`, for every provider:
- if the configuration is empty, `remove` its `cloud_<name>_pref` key (so values the
  user deleted do not come back), otherwise `put` it;
- if its radio is selected **and** `isValid()`, `put` `activeCloudConfig` = its name.

If no provider was stored as active this way ("None" selected, or the selected one is
invalid), `remove` `activeCloudConfig`. Removing the key rather than writing `""` gives
the same result in `ExportToCloudAction` (default `""`) and in the page (D3).

### D6: Messages

Two new keys in `RelationsMessages.properties` and `RelationsMessages_de.properties`:
- `CloudConfigPrefPage.btn.none.lbl`: label for the "None" radio (e.g. "None" / "Keiner").
- `CloudConfigPrefPage.msg.incomplete`: format string with the provider name, e.g.
  "The %s configuration is incomplete and will not be activated." /
  "Die %s-Konfiguration ist unvollständig und wird nicht aktiviert."

## Risks / Trade-offs

- [Assumption: SWT `Text.setText` fires Modify even when the text does not change.
  The original trap depends on it, but it was not confirmed in a running app] →
  D2 removes the effect either way, and the manual test with stale empty entries
  (tasks) covers it.
- [Azure: toggling the protocol radio fills the connection string from a template,
  even with empty account fields, so `getConfig()` is not empty and will be saved] →
  The value is harmless, because it is invalid and so never activated. Accepted.
- [A user who edits a working configuration into a broken one and presses OK loses
  their working cloud export until they fix it] → Intended (spec: "Previously valid
  provider edited into an invalid state"). The error message on the page warns before
  OK is pressed.
- [No automated test covers preference pages] → Verification is manual on Windows; see
  tasks.

## Migration Plan

No migration. Existing empty `cloud_<name>_pref` entries no longer cause errors (D2)
and are removed at the next OK or Apply (D5). An `activeCloudConfig` that points to a
valid provider keeps working. One that points to an invalid provider shows the error
message when the page is opened, and is cleared at the next save. Rollback is reverting
the single class and the two properties files. Older code tolerates a missing
`activeCloudConfig` key.

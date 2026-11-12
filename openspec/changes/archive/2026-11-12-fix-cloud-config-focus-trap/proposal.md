# Proposal

## Why

The "Cloud Configuration" preference page traps the user: once it is shown, the
user often cannot switch to another page (e.g. "Peer Configuration") until they
have entered a valid configuration for some cloud provider, and the page does not
say why. Most users never configure a cloud backup, so they can get stuck on a
page they only wanted to look at. The cause is that the page marks itself
invalid, and the JFace preference dialog refuses to leave an invalid page. The
page becomes invalid when:

- validity signals from *inactive* providers reach the page (every provider gets
  the same `setValid` callback, and `initialize()` fires modify events);
- empty configurations of inactive providers are stored, so on the next open
  they are loaded again and the page starts out invalid;
- selecting a provider's "Active" radio cannot be undone, because there is no
  "no provider" option.

## What Changes

- The cloud configuration page can always be left, whatever its content.
- Only the provider marked active affects the page's error state; input in
  inactive providers is ignored.
- A "None" choice is added, so the user can say "no cloud provider active". It
  is selected when no provider is active.
- If the active provider's input is incomplete or malformed, the page shows an
  error message saying the configuration will not be activated, instead of
  blocking navigation.
- When OK or Apply is pressed and the selected provider's configuration is
  invalid, its field values are still saved, but the provider is **not**
  activated: the stored active-provider setting is cleared. This also applies
  when a provider that was valid before has been edited into an invalid state.
  Export to Cloud then shows its existing "no Cloud Provider configured"
  message.
- Providers whose configuration is empty are no longer written to the
  preference store.

## Capabilities

### New Capabilities
- `cloud-config`: the user-facing behaviour of the Cloud Configuration
  preference page: choosing the active cloud provider (or none), leaving the
  page, error display, and what is saved and activated on OK or Apply.

### Modified Capabilities
<!-- none -->

## Impact

- Code: `bundles/org.elbe.relations/src/org/elbe/relations/internal/preferences/CloudConfigPrefPage.java`
  (all of the logic).
- Messages: `bundles/org.elbe.relations/src/RelationsMessages.properties` and
  `RelationsMessages_de.properties` (label for "None", error message text).
- Not affected: the `ICloudProviderConfig` SPI and the Azure, Dropbox and
  Google Drive provider bundles; `ExportToCloudAction` (it already handles
  "no active provider").
- Stored data: existing `cloud_<name>_pref` entries with empty values remain
  in user profiles. They are harmless once inactive providers are ignored, and
  they are not rewritten after the next save. No migration is needed.
- Testing: no test fragment covers preference pages, so verification is manual
  in the running application (see tasks).

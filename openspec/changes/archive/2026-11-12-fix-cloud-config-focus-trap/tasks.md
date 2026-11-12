# Tasks

## 1. Messages

- [x] 1.1 Add `CloudConfigPrefPage.btn.none.lbl` and `CloudConfigPrefPage.msg.incomplete` (with a `%s` placeholder for the provider name) to `bundles/org.elbe.relations/src/RelationsMessages.properties` and `RelationsMessages_de.properties` (design D6); verify both files contain both keys, and that the German file uses `ü`-style escapes like its neighbouring entries

## 2. CloudConfigPrefPage

- [x] 2.1 Replace the shared `this::setValid` callback with a per-provider callback that calls a new `refreshMessage()`. `refreshMessage()` sets or clears the error message from the selected provider's `isValid()` only (design D2). Remove every `setValid(...)` call and override `okToLeave()` to return `true` (D1). Verify by reading the code: no path calls `setValid`
- [x] 2.2 Add the "None" radio above the provider groups and include it in the manual exclusivity logic in `handleActivationChange`. Select it when `activeCloudConfig` is empty or names an unregistered provider, and call `refreshMessage()` after all groups are built and after every selection change (D3). Verify by reading the code: exactly one of "None" and the provider radios can be selected
- [x] 2.3 Add an "is empty" check based on `getConfig()` (D4). Rewrite `savePreferences()`/`store()` so that an empty configuration removes its key, a non-empty one is stored, and `activeCloudConfig` is stored only for a selected **and** valid provider and removed otherwise (D5). Verify by reading the code against the scenarios in the spec requirement "Only a valid configuration is activated on save"
- [x] 2.4 Build with Maven (`mvn clean verify`) and confirm `org.elbe.relations` compiles. Then restore the `pom.tycho` files that the build deletes under bundles, features and products, and confirm `git status` shows only the intended changes (compared with `--ignore-cr-at-eol`). Note: the build runs no tests

## 3. Manual verification in the running application (Windows)

- [x] 3.1 Fresh profile, no cloud configured: open Preferences > Cloud Configuration. Expect "None" selected, all provider fields disabled, no error message. Switch to Peer Configuration and expect it to open
- [x] 3.2 Stale profile: put `cloud_msazure_pref={"AzureConnectionString":""}` (and similar empty entries for Dropbox and Google Drive) into the preference store, open the Cloud Configuration page, and expect no error message and that the page can be left. After OK, confirm the empty entries are gone from the store
- [x] 3.3 Select MS Azure, enter only an account name. Expect the "incomplete, will not be activated" error, and that switching to another page works. Select "None" and expect the error to disappear and fields to be disabled
- [x] 3.4 Select MS Azure with incomplete input, press OK. Expect the values to be kept when reopening, with "None" selected, and Export to Cloud to show "no Cloud Provider configured"
- [x] 3.5 Configure a provider validly, press OK. Expect it to be stored as active and Export to Cloud to use it. Then edit it into an invalid state and press OK. Expect it to be deactivated (Export to Cloud reports no provider)
- [x] 3.6 Repeat 3.1 and 3.3 with the German UI and expect the "None" label and the error message to be translated

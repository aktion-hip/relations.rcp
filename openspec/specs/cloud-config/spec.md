# cloud-config Specification

## Purpose

Lets the user choose which cloud provider (if any) receives cloud backups and enter
that provider's settings in the Cloud Configuration preference page, without the
page ever preventing them from moving on to other preferences.

## Requirements

### Requirement: Cloud configuration page can always be left

The Cloud Configuration preference page SHALL never prevent the user from switching
to another preference page, whatever the content of its fields and whichever
provider is selected.

#### Scenario: Leave the page without entering anything

- **WHEN** the user opens the preferences, selects "Cloud Configuration", enters nothing, and selects "Peer Configuration"
- **THEN** the "Peer Configuration" page is shown

#### Scenario: Leave the page with incomplete input for the selected provider

- **WHEN** the user selects a cloud provider as active, enters an incomplete configuration for it, and selects another preference page
- **THEN** the other preference page is shown

#### Scenario: Page opens cleanly with previously stored empty configurations

- **WHEN** the preference store already contains empty configurations for cloud providers that are not active, and the user opens the "Cloud Configuration" page
- **THEN** the page shows no error message and can be left

### Requirement: No cloud provider can be chosen

The page SHALL offer a "None" choice, alongside the cloud providers, meaning that no
cloud provider is active. Exactly one of "None" and the providers SHALL be selected at
any time. "None" SHALL be selected when no provider is stored as active. The "None"
label SHALL be available in every supported interface language.

#### Scenario: Default when nothing is configured

- **WHEN** no cloud provider is stored as active and the user opens the "Cloud Configuration" page
- **THEN** "None" is selected and the input fields of all providers are disabled

#### Scenario: Undo a provider selection

- **WHEN** the user selects a provider as active and then selects "None"
- **THEN** no provider is selected, the providers' input fields are disabled, and no error message is shown

#### Scenario: Save with None selected

- **WHEN** "None" is selected and the user presses OK or Apply
- **THEN** no cloud provider is stored as active, and "Export to Cloud" reports that no cloud provider is configured

### Requirement: Only the active provider's input is validated

The page SHALL judge validity only from the configuration of the provider selected as
active. Input in, or loading of, providers that are not selected SHALL NOT produce an
error message on the page.

#### Scenario: Inactive provider is empty

- **WHEN** a provider is selected as active with a complete configuration, and another provider's fields are empty
- **THEN** no error message is shown

#### Scenario: Switching the active provider re-evaluates validity

- **WHEN** the user switches the active selection from a provider with incomplete input to a provider with a complete configuration
- **THEN** the error message disappears

### Requirement: Incomplete active configuration is reported, not blocking

When the provider selected as active has an incomplete or malformed configuration, the
page SHALL show an error message naming that provider and stating that its
configuration will not be activated. The message SHALL disappear as soon as the
configuration becomes valid or another choice without errors is selected. The message
SHALL be available in every supported interface language.

#### Scenario: Error shown while input is incomplete

- **WHEN** the user selects "MS Azure" as active and enters only an account name
- **THEN** the page shows an error message saying the MS Azure configuration is incomplete and will not be activated

#### Scenario: Error cleared after completing the input

- **WHEN** the error message is shown and the user completes the active provider's configuration
- **THEN** the error message is no longer shown

### Requirement: Only a valid configuration is activated on save

When the user presses OK or Apply, the page SHALL save the entered values of every
provider whose configuration is not empty. A provider selected as active SHALL be stored
as the active provider only if its configuration is valid; otherwise no provider SHALL be
stored as active. This SHALL also apply when the selected provider was valid before and
has been edited into an invalid state. Providers whose configuration is empty SHALL NOT
be written to the preference store.

#### Scenario: Save a valid configuration

- **WHEN** the user selects a provider as active, enters a valid configuration, and presses OK
- **THEN** the provider's values are saved and the provider is stored as active, so "Export to Cloud" uses it

#### Scenario: Save an incomplete configuration

- **WHEN** the user selects a provider as active, enters an incomplete configuration, and presses OK
- **THEN** the entered values are saved, no provider is stored as active, and reopening the page shows the entered values with "None" selected

#### Scenario: Previously valid provider edited into an invalid state

- **WHEN** a provider is stored as active, the user edits its configuration so that it becomes invalid, and presses OK
- **THEN** no provider is stored as active anymore, and "Export to Cloud" reports that no cloud provider is configured

#### Scenario: Empty provider configurations are not stored

- **WHEN** the user presses OK while some providers have no values entered
- **THEN** no configuration entries are written for those providers

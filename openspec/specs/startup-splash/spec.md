# startup-splash Specification

## Purpose

Shows the user, while Relations starts, that the application is working and how far
startup has progressed. It does this with a progress bar and a status message on the
splash screen.

## Requirements

### Requirement: Splash screen shows startup progress

While the splash screen is visible, the application SHALL display a progress bar and a
status message on it. The progress bar SHALL be placed in the product's
`startupProgressRect`, and the message in `startupMessageRect` using
`startupForegroundColor`, so both fit the layout of the splash image.

#### Scenario: Progress is visible during startup

- **WHEN** Relations is started with its splash screen
- **THEN** a progress bar and a status message appear in the lower area of the splash image, without covering the rest of the image

### Requirement: Progress advances through the startup phases

The progress bar SHALL be determinate and SHALL advance when startup enters each of
these phases, in this order: preparing the application, opening the database, loading
the data, checking the search index, restoring the browsers, building the user interface.
The status message SHALL name the current phase. The bar SHALL never move backwards.

#### Scenario: Phases in order

- **WHEN** Relations starts with an existing database
- **THEN** the status message changes through the phases in the stated order, and the bar fills further with each phase

#### Scenario: Long phase stays visible

- **WHEN** a phase takes a long time (for example, rebuilding the search index)
- **THEN** that phase's message stays on the splash screen until the next phase starts

### Requirement: Status messages are bilingual

The status messages SHALL be available in English and German, and SHALL follow the
application's interface language, with English as the fallback.

#### Scenario: German interface

- **WHEN** Relations starts with a German locale
- **THEN** all splash status messages are in German

### Requirement: Progress display ends with the splash screen

The progress display SHALL disappear together with the splash screen once the main window
has been built. No progress element SHALL remain visible after startup.

#### Scenario: Main window opens

- **WHEN** startup completes and the main window is shown
- **THEN** the splash screen, including the progress bar and message, is gone

### Requirement: Startup without a splash screen is unaffected

If no splash screen is available (for example, started with `-nosplash`, or when the
splash cannot be accessed), the application SHALL start normally and SHALL NOT show any
progress window instead.

#### Scenario: Started with -nosplash

- **WHEN** Relations is started with `-nosplash`
- **THEN** it starts as before, with no error and no progress display

### Requirement: Startup dialogs remain usable

If startup opens a dialog while the splash screen is visible (for example, the database
error dialog), the progress display SHALL NOT prevent the user from seeing and answering
it.

#### Scenario: Database error during startup

- **WHEN** the embedded database configuration is missing and the startup error dialog is shown
- **THEN** the dialog is visible and can be confirmed, and startup then continues

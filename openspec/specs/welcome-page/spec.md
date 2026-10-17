# welcome-page Specification

## Purpose

Introduces Relations to the user on first start and on request. It explains what the
application is for, how to take the first steps, and what is new in this release, and it
links into the Relations help.

## Requirements

### Requirement: Welcome page content

The welcome page SHALL present three sections: **Overview** (what Relations is: a
knowledge-management tool where terms, texts and persons are created and related to each
other), **First Steps** (pointing to the "Getting started with Relations" help topic), and
**What's New** (pointing to the "What's new" help topic of the current release). The page
SHALL show the Relations branding and SHALL NOT contain placeholder or third-party
example text.

#### Scenario: All sections are shown

- **WHEN** the welcome page is displayed
- **THEN** the Overview, First Steps and What's New sections are visible, each with its title and text, together with the Relations logo

### Requirement: Links open Relations help topics

Each help link on the welcome page SHALL open the referenced topic of the Relations help
in the same viewer that Help > Help Contents uses. Following a link SHALL NOT replace the
welcome page with the help content.

#### Scenario: Open Getting Started

- **WHEN** the user clicks the First Steps link
- **THEN** the help opens on the "Getting started with Relations" topic and the welcome page stays as it was

#### Scenario: Open What's New

- **WHEN** the user clicks the What's New link
- **THEN** the help opens on the "What's new" topic

### Requirement: Welcome page is shown automatically on first start

The application SHALL display the welcome page automatically the first time it starts in
a given workspace. This includes the first start after upgrading from a version that had
no welcome page. On every later start, the welcome page SHALL NOT open automatically. The
normal start behaviour (database connection, restoring the active browser) SHALL be
unaffected.

#### Scenario: First start

- **WHEN** Relations starts and the welcome page has never been shown in this workspace
- **THEN** the welcome page opens in front of the browser area once the main window is up

#### Scenario: Subsequent start

- **WHEN** Relations starts and the welcome page has already been shown in this workspace
- **THEN** the welcome page does not open, and the previously active browser is shown as before

#### Scenario: Shown state survives a crash

- **WHEN** the welcome page was shown automatically and the application then terminated abnormally
- **THEN** the welcome page does not open automatically on the next start

### Requirement: Welcome page can be opened from the Help menu

The Help menu SHALL contain a **Welcome** entry that opens the welcome page at any time.
If the page is already open, the entry SHALL bring it to the front instead of opening a
second instance.

#### Scenario: Open from menu

- **WHEN** the user chooses Help > Welcome
- **THEN** the welcome page opens and is activated

#### Scenario: Already open

- **WHEN** the welcome page is open and the user chooses Help > Welcome again
- **THEN** the existing welcome page is activated and no second page appears

### Requirement: Welcome page can be closed

The user SHALL be able to close the welcome page, either with the part's close control or
with a "Start working" action on the page itself. Closing it SHALL reveal the browser that
was active before. A welcome page that was open at exit SHALL NOT be restored on the next
start.

#### Scenario: Start working

- **WHEN** the user clicks "Start working" on the welcome page
- **THEN** the welcome page closes and the browser area shows the previously active browser

#### Scenario: Not restored after exit

- **WHEN** the user exits Relations while the welcome page is open and later restarts it (not a first start)
- **THEN** the welcome page is not shown

### Requirement: Welcome page is bilingual

The welcome page content, its tab label and the Help > Welcome menu entry SHALL be
available in English and German. The language SHALL follow the application's interface
language, with English as the fallback.

#### Scenario: German interface

- **WHEN** Relations runs with a German locale and the welcome page is shown
- **THEN** all section titles, texts, link labels and the tab label are in German

#### Scenario: Other interface language

- **WHEN** Relations runs with a locale for which no welcome translation exists
- **THEN** the welcome page is shown in English

### Requirement: Welcome page degrades without an embedded browser

If the platform cannot provide an embedded browser, the welcome page SHALL still open and
show the overview text and a way to open the help, instead of failing or blocking
startup.

#### Scenario: No browser engine

- **WHEN** the welcome page is opened on a system where the embedded browser cannot be created
- **THEN** a plain fallback with the overview text and a help link is shown, the error is logged, and the application continues normally

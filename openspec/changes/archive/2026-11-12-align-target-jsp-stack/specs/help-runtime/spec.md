# Spec Delta

## Purpose

Makes sure the Eclipse help system bundled with Relations, including its JSP-based web application, resolves and starts in every supported way of running the application.

## ADDED Requirements

### Requirement: Help web application dependencies resolve
The Relations runtime SHALL resolve every bundle of the help system's JSP-based web application, including the JSP engine and the expression-language implementation it needs, without unresolved-requirement errors.

#### Scenario: Built product starts without JSP resolution errors
- **WHEN** the Tycho-built product is started
- **THEN** no `BundleException` reporting an unresolved requirement on `org.apache.jasper.servlet` or another JSP/EL package is logged
- **AND** the JSP integration bundle and the help web application bundle reach the RESOLVED or ACTIVE state

#### Scenario: Application launched from the IDE against the target platform
- **WHEN** the application is launched from the Eclipse IDE with the project's target platform active
- **THEN** no `BundleException` reporting an unresolved requirement on `org.apache.jasper.servlet` is logged

### Requirement: Single consistent JSP engine
The target platform SHALL offer exactly one JSP engine major version, and it SHALL satisfy the version range required by the help system's JSP integration bundle.

#### Scenario: Resolved target contains one compatible Jasper provider
- **WHEN** the target platform is resolved
- **THEN** every bundle exporting `org.apache.jasper.servlet` exports a version within the range imported by the JSP integration bundle

### Requirement: Help content is viewable
Users SHALL be able to open the Relations help contents from the application.

#### Scenario: Open help contents
- **WHEN** the user opens the help contents from the Help menu
- **THEN** the help pages are displayed without a server or JSP error

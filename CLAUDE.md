# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.


## Project Overview

ApproveJ is an approval testing library for the JVM.
It provides a fluent API to compare actual values against previously approved "golden master" files.


## Build Commands

```bash
# Run all checks (tests, linting, etc.)
./gradlew check

# Format code (must pass before commit)
./gradlew spotlessApply

# Run a single test class
./gradlew :modules:core:test --tests "org.approvej.ApprovalBuilderTest"

# Run a single test method
./gradlew :modules:core:test --tests "org.approvej.ApprovalBuilderTest.byValue"

# Generate Javadoc
./gradlew javadoc

# Build website (manual + Javadoc) for approvej.org
./gradlew updatePages -Pversion=<version>   # e.g., ./update-pages 1.3.2
```


## Module Structure

- **modules/core** - Core framework with no external dependencies. Contains `ApprovalBuilder`, `Approver`, `Scrubber`, `PrintFormat`, and `FileReviewer` interfaces.
- **modules/json-jackson** - JSON support using Jackson 2.x
- **modules/json-jackson3** - JSON support using Jackson 3.x
- **modules/yaml-jackson** - YAML support using Jackson 2.x
- **modules/yaml-jackson3** - YAML support using Jackson 3.x
- **modules/http** - HTTP stub server for approving HTTP requests
- **modules/http-wiremock** - WireMock adapter for HTTP testing
- **plugins/approvej-gradle-plugin** - Gradle plugin for managing approved files
- **plugins/approvej-maven-plugin** - Maven plugin for managing approved files
- **bom** - Maven Bill of Materials
- **manual** - AsciiDoc documentation (code samples included from tests in `manual/src/test`)


## End-User Documentation

The manual is used as ApproveJ's homepage.
Any new feature needs to be documented.
The manual's structure should be

1. Introduction:
   What is approval testing and why would I want to use it?
   What is ApproveJ and why would I want to use it?
   Show a before/after comparison (traditional assertions vs. approval test).
2. Getting Started:
   What do I need to have/do to use ApproveJ?
3. Basics:
   How do I make the simplest approvals without any additional concept?
   How can I have multiple approvals per test?
4. Printing:
   What is printing, why should I care?
   How do I use any of the built-in print formats?
   How can I implement my own Printer/PrintFormat?
   How do I configure a global default print format?
5. Scrubbing:
   What is scrubbing and when do I want to do it, what alternatives exist?
   Which built-in scrubbers are there and what does each one match?
   What are replacements and how do I change what scrubbed values become?
   How can I build my own scrubbers?
6. Approving:
   How can I change the way approvals are done?
   How can I put approved/received files in different places?
   How can I do approvals inplace (without files), and what are the trade-offs?
7. Reviewing:
   What is reviewing and why do I want to do it?
   How do I configure a diff tool (with practical examples for common IDEs/tools)?
   How can I automatically review any diff between received and approved files?
8. Extensions:
   One sub-chapter for each of the extension [modules](modules) that are not [core](modules/core).
   Why would I use this extension and how do I use it?
   Needs to include dependency coordinates for Gradle and Maven.
   Sub-chapters: JSON with Jackson (print POJOs as JSON, pretty print JSON strings, JSON field scrubbing),
   YAML with Jackson (print POJOs as YAML),
   HTTP (catch integration risks by approving outgoing HTTP requests).
9. Build Plugins:
   How do I set up the Gradle/Maven plugin?
   How can I clean up leftover approved files?
   How can I batch-review all unapproved files in the project?
   How can I approve all unapproved files in the project?
   How does the inventory work? How can I disable it?
10. Configuration:
    How are configuration sources resolved (priority order)?
    How can I register custom print formats and reviewers via SPI?
    (Individual properties are documented in their respective chapters and the cheat sheet.)
11. Cheat Sheet:
    Very quick reference of things people are likely to want to look up later.
12. API Reference:
    Links to the Javadoc for each module.

### Manual Content Principles

The chapters' content should be written in a how-to guide style.

- **Configuration first**: When a feature can be configured globally (print formats, reviewers), lead with configuration and show `printedAs()`/`reviewedBy()` as per-test overrides.
- **Common before expert**: Present built-in/common content before custom/advanced content (e.g., built-in scrubbers before custom scrubbers).
- **Print format sections should mention global configuration**: Each section describing a print format (including in extension chapters) should show how to set it as the default via `approvej.properties`.

If code is shown in the manual it should generally be included from the tests located in the manual module, to make sure it always works and gets automatically refactored along with the other code in the project.
Each chapter should have a dedicated Code file in the Kotlin and the Java source sets.
If there's a variant for Java and Kotlin, or Gradle and Maven, the code blocks should be put in tabs on top of each other to make the manual more compact.

### AsciiDoc Anchor Conventions

- All headings down to `===` (h3) must have explicit `[id=...]` anchors.
- Anchors are named by concept, not the explaining subtitle (e.g., `scrubbing` not `scrubbing_make_random_parts_static`).
- h3 anchors are prefixed with their parent h2 anchor (e.g., `approve_by_file_next_to_test`, `built_in_scrubbers_date_time`).


## Architecture

The approval flow is:

1. **Print** - Convert value to String via `PrintFormat` (e.g., `JsonPrintFormat`, `YamlPrintFormat`)
2. **Scrub** - Remove dynamic data (timestamps, UUIDs) via `Scrubber` implementations
3. **Approve** - Compare against approved value via `Approver` (file-based or inline)
4. **Review** - On mismatch, optionally open diff tool via `FileReviewer`

Key entry point: `ApprovalBuilder.approve(value)` with fluent methods `.printedAs()`, `.scrubbedOf()`, `.byFile()`.

Package structure: `org.approvej` for core, `org.approvej.<format>.<library>` for modules (e.g., `org.approvej.json.jackson`).


## Code Conventions

- Java 21, formatted with Google Java Style via Spotless
- Two-space indentation
- All classes should be immutable where possible (final fields, initialized in constructor)
- Only API elements required by users are `public`; internal classes are package-private
- Built-in `Scrubber` implementations have package-private constructors (use `Scrubbers` factory)
- Do not abbreviate variable names (e.g., use `statement` not `stmt`, `connection` not `conn`, `recordingDataSource` not `recordingDs`)


## Testing

- JUnit 5 with AssertJ for assertions
- Tests must contain at least one assertion
- Avoid mocks; use real objects or Nullable Infrastructure components
- Local variables in tests should NOT be declared final (unlike production code)

### Test Class Naming

Test classes are named `<ClassUnderTest>Test` (e.g., `ImageScrubbersTest`).
This keeps the subject under test clearly visible in the filesystem.

### Test Method Naming

Test method names use underscores as separators.
Each name starts with a clear reference to the thing being tested:

- For a **method**: use the method's name (e.g., `apply`, `divide`, `region`).
- For a **constructor**: use `constructor`, optionally followed by distinguishing parameters (e.g., `constructor_person_address`).
- For an **HTTP endpoint**: use the HTTP method and path (e.g., `GET_article`).

If there is only **one test case** for a thing, the name is just the reference itself (e.g., `divide`).
If there are **multiple cases**, append a description of how the case differs from the base case (e.g., `divide_0_divisor`).

Examples:

- `divide` — single/base case
- `divide_0_divisor` — additional case with zero divisor
- `GET_article` — base case calling `GET /api/article/{id}` with a valid ID
- `GET_article_unknown` — case with an unknown ID

### Test Method Ordering

Test methods follow the order of the things under test as defined in the production code.
Cases about the same thing are grouped together and roughly sorted by difference from the base case.
The more complex the setup, the later the case should appear.


## Dependency Management

Entries in `gradle/libs.versions.toml` must be sorted alphabetically within each section (`[versions]`, `[libraries]`, `[plugins]`).


## Commit Messages

- Present tense, imperative mood ("Add feature" not "Added feature")
- Subject line max 50 chars, no period at end
- Explain _what_ and _why_ in body, not _how_


## Markdown Formatting

Spotless enforces trailing whitespace removal and final newlines.
The following spacing conventions are not auto-enforced:

- End each sentence with a line break so changes affect only one line in diffs.
- One empty line after a heading.
- Two empty lines between a paragraph and the next heading.
- Exception: only one empty line between consecutive headings with no content between them.
- One empty line before and after lists, code blocks, and similar block elements.

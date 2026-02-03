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
./update-pages <version>   # e.g., ./update-pages 0.12
```


## Module Structure

- **modules/core** - Core framework with no external dependencies. Contains `ApprovalBuilder`, `Approver`, `Scrubber`, `PrintFormat`, and `FileReviewer` interfaces.
- **modules/json-jackson** - JSON support using Jackson 2.x
- **modules/json-jackson3** - JSON support using Jackson 3.x
- **modules/yaml-jackson** - YAML support using Jackson 2.x
- **modules/yaml-jackson3** - YAML support using Jackson 3.x
- **modules/http** - HTTP stub server for approving HTTP requests
- **modules/http-wiremock** - WireMock adapter for HTTP testing
- **bom** - Maven Bill of Materials
- **manual** - AsciiDoc documentation (code samples included from tests in `manual/src/test`)


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


## Testing

- JUnit 5 with AssertJ for assertions
- Tests must contain at least one assertion
- Test method naming: `<methodName>[_<case>]` (e.g., `apply_empty_string`)
- Avoid mocks; use real objects or Nullable Infrastructure components
- Local variables in tests should NOT be declared final (unlike production code)


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

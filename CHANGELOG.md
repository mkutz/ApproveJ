# Changelog


## v1.4.7

### IntelliJ plugin

* ✨ **Diff view navigation and actions**
  The diff viewer now shows a notification panel with "Navigate to Test", "Approve", and "Reject"
  actions. Content titles clearly label which side is "Received" and which is "Approved".
  ([#250](https://github.com/mkutz/ApproveJ/issues/250))

* ✨ **One-click reject**
  A new "Reject" action deletes the received file without copying its content to the approved file.
  Available in the diff viewer and in the received file editor banner.

* 🎨 **Simplified gutter icon for pending approvals**
  When a received file exists, clicking the gutter icon now directly opens the diff view instead
  of showing a popup menu.

**Full Changelog**: https://github.com/mkutz/ApproveJ/compare/v1.4.6...v1.4.7


## v1.4.6

### core

* 🐛 **Preserve inventory entries for unexecuted approvals**
  When a test with multiple `approve().byFile()` calls failed before reaching all approvals,
  the inventory entries for unexecuted approvals were dropped.
  This caused IntelliJ plugin gutter icons to disappear for those approvals.
  ([#230](https://github.com/mkutz/ApproveJ/issues/230))
  Thanks to [@DeepsanBhandari](https://github.com/DeepsanBhandari) for the contribution!

**Full Changelog**: https://github.com/mkutz/ApproveJ/compare/v1.4.5...v1.4.6


## v1.4.5

### core

* 🐛 **Capture ISO instants without fractions of a second**
  `Scrubbers.isoInstants()` now also matches instants without fractional seconds (e.g., `2019-02-25T12:34:56Z`).
  Thanks to [@helgewessels](https://github.com/helgewessels) for the contribution!

**Full Changelog**: https://github.com/mkutz/ApproveJ/compare/v1.4.4...v1.4.5


## v1.4.4

### core

* 🐛 **Fix CRLF/LF mismatch in file comparison**
  Normalize `\r\n` to `\n` when reading approved files and when receiving printed values, so approvals work correctly on Windows.
  ([#238](https://github.com/mkutz/ApproveJ/issues/238))

* 🐛 **Fix inventory using OS-specific path separators**
  Normalize backslashes to forward slashes when writing inventory entries, so the inventory file is portable across platforms.
  ([#239](https://github.com/mkutz/ApproveJ/issues/239))

* 🐛 **Use `\n` instead of `%n` in printers and `ApprovalError`**
  Java's `%n` produces `\r\n` on Windows, making printer output platform-dependent.
  All built-in printers now produce deterministic `\n` line endings.

### json-jackson, json-jackson3, yaml-jackson & yaml-jackson3

* 🐛 **Normalize CRLF in Jackson printer output**
  Jackson's `DefaultPrettyPrinter` uses `System.lineSeparator()`, producing `\r\n` on Windows.
  All Jackson-based print formats now normalize output to `\n`.

### CI

* ✅ **Add Windows CI job**
  The PR build workflow now includes a `windows-latest` job to catch platform-specific regressions.
  ([#236](https://github.com/mkutz/ApproveJ/issues/236))

**Full Changelog**: https://github.com/mkutz/ApproveJ/compare/v1.4.3...v1.4.4


## v1.4.3

### core

* 🐛 **Fix Windows file path handling in `StackTraceTestFinderUtil`**
  Use `URL.toURI()` instead of `URL.getPath()` to avoid `InvalidPathException` on Windows.
  Normalize backslashes before regex matching so file lookup works on both platforms.
  ([#234](https://github.com/mkutz/ApproveJ/issues/234))


## v1.4.2

IntelliJ plugin only, see [plugin changelog](plugins/approvej-intellij-plugin/CHANGELOG.md).


## v1.4.1

IntelliJ plugin only, see [plugin changelog](plugins/approvej-intellij-plugin/CHANGELOG.md).


## v1.4

### core

* 🛡️ **Dangling approval detection**
  ApproveJ now detects `approve()` calls that are missing a terminal method (`byFile`, `byValue`, `by`).
  Annotate your test class with `@ApprovalTest` to enable a JUnit `AfterEachCallback` that fails
  the test when a dangling approval is found. A shutdown hook provides a fallback warning when
  the extension is not active.


### manual

* 📖 **Restructured and polished documentation**
  The manual has been reorganized for usability: chapters follow a clearer progression,
  custom extensions moved to the Extensions chapter, and typos and grammar have been fixed throughout.

* 📖 **New IntelliJ Plugin chapter**
  A dedicated chapter documents installation, diff viewer, one-click approval, navigation,
  and dangling approval inspection with screenshots.

**Full Changelog**: https://github.com/mkutz/ApproveJ/compare/v1.3.2...v1.4


## v1.3.2

### plugins

Note that the Gradle plugin is still pending release on the Gradle Plugin portal.

* 👀 review all unapproved files
  The new task/goal allows to trigger a review on all remaining unapproved received files via `gradle approvejReviewUnapproved` or `mvn approvej:review-unapproved` without needing to re-run all other tests.
  This uses the currently configured `defaultFileReviewerScript` or `defaultFileReviewer`.
  See [manual](https://approvej.org/#cleanup_review_unapproved)

* 🙈 auto-approve all received files
  The new task/goal in the build tool plugins allows to automatically approve all remaining received files in a project via `gradle approvejApproveAll` or `mvn approvej:approve-all`.
  Just like the automatic file reviewer, you should only use this if you still review the changes before committing them to version control!
  See [manual](https://approvej.org/#cleanup_approve_all)

**Full Changelog**: https://github.com/mkutz/ApproveJ/compare/v1.3.1...v1.3.2


## v1.3.1

### core

* 🐞 this release fixes a nasty bug, which caused ApproveJ to misplace approved files in a wrong folders like `bin` instead of next to test source as intended (see #200)
  This probably happened for quite some time and was only now discovered via the new inventory mechanism released in v1.3.


## v1.3

### core

* 📋 new inventory mechanism
  ApproveJ now keeps all created approved files in an inventory file (`.approvej/inventory.properties` in your project).
  This file allows to implement a cleanup mechanism (see below)


### approvej-gradle-plugin & approvej-maven-plugin

* ⚙️ [cleanup mechanism](https://approvej.org/#cleanup)
  You can now get a list of all approved files that no longer have a corresponding test by executing `./gradlew approvejFindLeftovers` /`mvn approvej:find-leftovers`
  Running `./gradlew approvejCleanup`/`mvn approvej:cleanup` automatically deletes those files for you.

**Full Changelog**: https://github.com/mkutz/ApproveJ/compare/v1.2.2...v1.3


## v1.2.2

ab5140c Defer staging repository discovery to projectsEvaluated
da4fdcc Discover staging repositories dynamically for JReleaser
fc12451 Document dependency sorting convention
5a22ed4 Bump org.junit.platform:junit-platform-launcher from 6.0.2 to 6.0.3
e750fed Bump junit from 6.0.2 to 6.0.3


## v1.2.1

### core

* 🔍 getter-only property discovery in `MultiLineStringPrintFormat`
  `get*` and `is*` methods without backing fields are now discovered and included as properties, consistent with the Jackson-based formats.
  Field-backed properties appear in declaration order, followed by getter-only properties alphabetically; sorted mode interleaves all by name.
  ⚠️ Note that this is a potential breaking change as your approved files will now contain additional lines


### json-jackson & json-jackson3

* 🪲 the order of method-only properties is now deterministic
  Before, the properties defined by getters (`get*` or `is*`) were printed in arbitrary order.
  Now these properties are printed in alphabetic order after the field-backed properties, which are still in order of declaration.
  Resolves #173
  ⚠️ Note that this is a potential breaking change the order of properties in you existing approved files might be different


### yaml-jackson & yaml-jackson3

* 🎯 deterministic property ordering is now applied to YAML output, matching the behavior of the JSON modules.
  ⚠️ Note that this is a potential breaking change the order of properties in you existing approved files might be different


### manual

* 📖 added cheat sheet (HTML and printable PDF), cross-references between chapters, and CI/CD guidance
* 📖 documented getter-only property discovery

**Full Changelog**: https://github.com/mkutz/ApproveJ/compare/v1.2...v1.2.1


## v1.2

### json-jackson & yaml-jackson

* ⚠️ potentially breaking change: both modules now require you to define the Jackson dependency yourself.
  If you're using ApproveJ in a Spring Boot service or a similar setting, you probably already have that.


### 🆕 json-jackson3 & yaml-jackson3

Technically new modules, that allow to use Jackson 3 (instead of Jackson 2 as used by json-jackson and yaml-jackson).
The APIs are identical, though.


### 🆕 http

The mew module can be used to test the integration with external service.

* 🥸 the [`HttpStubServer`](https://approvej.org/javadoc/http/org/approvej/http/HttpStubServer.html) can be used to simulate the service and store the received requests for approval.
  If you already use WireMock, you can simply use the [`http-wiremock`](https://approvej.org/#_wiremock_adapter) dependency to get some adapter logic.

* 🖨️ received HTTP requests can be printed as http request files (as [defined by JetBrains](https://www.jetbrains.com/help/idea/exploring-http-syntax.html)) using the new [ReceivedHttpRequestPrintFormat](https://approvej.org/javadoc/http/org/approvej/http/ReceivedHttpRequestPrintFormat.html)

* 🧽 [scrubbers](https://approvej.org/#_http_scrubbers) for headers are included

Check the [manual](https://approvej.org/#_http) for more details.

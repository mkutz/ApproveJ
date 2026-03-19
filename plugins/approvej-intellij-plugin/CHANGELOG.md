# Changelog


## v1.5.0

* ✨ **Image approval support**
  The plugin now recognizes `approveImage()` calls from the `image` module.
  Gutter icons, dangling approval inspection, and duplicate unnamed approval inspection
  all work with `ImageApprovalBuilder` chains.


## v1.4.7

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


## v1.4.6

* 🧹 **Simplify approved file filtering in line marker provider**
  Inline the stream chain and consolidate the filter condition into a single return statement.
  Thanks to [@DeepsanBhandari](https://github.com/DeepsanBhandari) for the contribution!


## v1.4.4

* ✨ **Duplicate unnamed approval inspection**
  A new UAST-based inspection warns when a test method contains multiple no-arg `byFile()` calls
  that resolve to the same approved file. Detects both unnamed duplicates and duplicate `.named()`
  values. Offers a quick fix to insert `.named("TODO")` with the placeholder selected for
  immediate editing.


## v1.4.3

* ✨ **Rename refactoring support for approved files**
  When renaming a test class or method via IntelliJ's refactoring (Shift+F6),
  approved and received files are now automatically renamed along with it.
  Supports both Java and Kotlin, next-to-test and subdirectory naming patterns,
  affixed files, and keeps the inventory in sync.

* 🐞 **Fix gutter icon filtering for nextToTest naming pattern**
  Gutter icons now correctly match approved files using the default `nextToTest`
  naming pattern where filenames include the class name prefix.


## v1.4.2.1

* 🐞 **Fix missing gutter icons**
  The SVG icon files were never committed, causing all gutter icons to fail silently.


## v1.4.2

* ✨ **Plugin recommendations via dependency support**
  IntelliJ now suggests the ApproveJ plugin when a project uses any ApproveJ dependency.

* 🎨 **Custom gutter icons**
  Gutter markers use dedicated ApproveJ icons: green for approved, orange when a received file exists.

* 📝 **Improved Marketplace listing**
  Richer plugin description with feature list and automated change notes from the changelog.


## v1.4.1

* 🐞 **Fix navigation in multi-module Gradle projects**
  The IntelliJ plugin only looked for a single inventory file at the project root.
  In multi-module projects each module writes its own `.approvej/inventory.properties`,
  so navigation features (gutter icons, editor banners) found nothing.
  The plugin now discovers and merges all inventory files across modules.


## v1.4

A new IntelliJ IDEA plugin reduces friction when working with `.received` files directly in your IDE:

* 👀 **Diff viewer**
  Open a side-by-side diff between the received and approved file from the editor notification
  banner or via context menu actions in the Project View and Editor.

* ✅ **One-click approve**
  Approve a received file with a single click — copies received to approved and deletes the
  received file. The action is undoable via IntelliJ's undo mechanism.

* 🧭 **Bidirectional navigation**
  Gutter icons on `approve()…byFile()` chains navigate to the approved file.
  When a received file exists, a popup offers to compare or navigate to either file.
  Editor banners on approved and received files link back to the test method via inventory lookup.

* 🔍 **Dangling approval inspection**
  A UAST-based code inspection highlights `approve()` calls not concluded with a terminal method
  and offers quick fixes to append `.byFile()` or `.byValue("")`.

See [manual](https://approvej.org/#intellij_plugin)

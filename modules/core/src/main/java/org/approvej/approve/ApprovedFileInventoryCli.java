package org.approvej.approve;

import static java.util.stream.Collectors.joining;

import java.nio.file.Path;
import java.util.List;
import org.approvej.approve.ApprovedFileInventory.ApproveResult;
import org.approvej.approve.ApprovedFileInventory.CleanupResult;
import org.approvej.configuration.Configuration;
import org.jspecify.annotations.NullMarked;

/**
 * Thin CLI wrapper for the approved file inventory.
 *
 * <p>All business logic lives in {@link ApprovedFileInventory}. This class is responsible only for
 * formatting domain results into CLI output with appropriate exit codes.
 */
@NullMarked
final class ApprovedFileInventoryCli {

  record CliResult(String output, int exitCode) {}

  private ApprovedFileInventoryCli() {}

  static CliResult findLeftovers(ApprovedFileInventory inventory) {
    List<InventoryEntry> leftovers = inventory.findLeftovers();
    if (leftovers.isEmpty()) {
      return new CliResult("No leftover approved files found.", 0);
    }
    return new CliResult(
        leftovers.stream()
            .map(
                leftover ->
                    "  %s\n    from %s"
                        .formatted(leftover.relativePath().toUri(), leftover.testReference()))
            .collect(joining("\n", "Leftover approved files:\n", "")),
        0);
  }

  static CliResult cleanup(ApprovedFileInventory inventory, Path inventoryPath) {
    CleanupResult result = inventory.removeLeftovers(inventoryPath);
    if (result.removed().isEmpty() && result.failed().isEmpty()) {
      return new CliResult("No leftover approved files found.", 0);
    }
    String output =
        result.removed().stream()
            .map(leftover -> "  %s".formatted(leftover.relativePath().toUri()))
            .collect(joining("\n", "Removed leftover approved files:\n", ""));
    if (!result.failed().isEmpty()) {
      if (!output.isEmpty()) {
        output += "\n";
      }
      output +=
          result.failed().stream()
              .map(entry -> "  %s".formatted(entry.relativePath()))
              .collect(
                  joining(
                      "\n",
                      "Failed to delete %d leftover file(s):\n".formatted(result.failed().size()),
                      ""));
      return new CliResult(output, 1);
    }
    return new CliResult(output, 0);
  }

  static CliResult approveAll(ApprovedFileInventory inventory) {
    ApproveResult result = inventory.approveAll();
    if (result.approved().isEmpty() && result.failed().isEmpty()) {
      return new CliResult("No unapproved files found.", 0);
    }
    String output =
        result.approved().stream()
            .map(path -> "  %s".formatted(path.toUri()))
            .collect(joining("\n", "Approved files:\n", ""));
    if (!result.failed().isEmpty()) {
      if (!output.isEmpty()) {
        output += "\n";
      }
      output +=
          result.failed().stream()
              .map("  %s"::formatted)
              .collect(
                  joining(
                      "\n",
                      "Failed to approve %d file(s):\n".formatted(result.failed().size()),
                      ""));
      return new CliResult(output, 1);
    }
    return new CliResult(output, 0);
  }

  static CliResult reviewUnapproved(ApprovedFileInventory inventory) {
    var reviewed = inventory.reviewUnapproved(Configuration.configuration.defaultFileReviewer());
    if (reviewed.isEmpty()) {
      return new CliResult("No unapproved files found.", 0);
    }
    String output =
        reviewed.stream()
            .map(pathProvider -> "  %s".formatted(pathProvider.receivedPath().toUri()))
            .collect(joining("\n", "Unapproved files:\n", ""));
    return new CliResult(output, 0);
  }

  /**
   * CLI entry point for build tool plugins.
   *
   * @param args {@code --find-leftovers} to list leftovers, {@code --cleanup} to delete them,
   *     {@code --approve-all} to approve all unapproved files, {@code --review-unapproved} to
   *     review all unapproved files
   */
  public static void main(String[] args) {
    String usage =
        "Usage: ApprovedFileInventoryCli --find-leftovers | --cleanup | --approve-all |"
            + " --review-unapproved";
    if (args.length == 0) {
      System.err.println(usage);
      System.exit(1);
    }

    Path inventoryPath = ApprovedFileInventoryUpdater.DEFAULT_INVENTORY_FILE;
    ApprovedFileInventory inventory = ApprovedFileInventory.loadInventory(inventoryPath);

    String command = args[0];
    CliResult result =
        switch (command) {
          case "--find-leftovers" -> findLeftovers(inventory);
          case "--cleanup" -> cleanup(inventory, inventoryPath);
          case "--approve-all" -> approveAll(inventory);
          case "--review-unapproved" -> reviewUnapproved(inventory);
          default -> {
            System.err.printf("Unknown command: %s%n", command);
            System.err.println(usage);
            yield new CliResult("", 1);
          }
        };

    if (!result.output().isEmpty()) {
      System.out.println(result.output());
    }
    System.exit(result.exitCode());
  }
}

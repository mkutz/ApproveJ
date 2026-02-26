package org.approvej.approve;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static java.util.Arrays.stream;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import org.approvej.configuration.Configuration;
import org.approvej.review.FileReviewer;
import org.jspecify.annotations.NullMarked;

/**
 * CLI actions for the approved file inventory.
 *
 * <p>Operates on a given inventory map, separating business logic from the static recording side in
 * {@link ApprovedFileInventory}. The {@link #main(String[])} method loads the inventory from disk
 * and delegates to instance methods.
 */
@NullMarked
final class ApprovedFileInventoryCli {

  private final TreeMap<Path, InventoryEntry> inventory;

  ApprovedFileInventoryCli(TreeMap<Path, InventoryEntry> inventory) {
    this.inventory = inventory;
  }

  /**
   * Finds leftover inventory entries whose test methods no longer exist.
   *
   * @return a list of leftover inventory entries
   */
  List<InventoryEntry> findLeftovers() {
    return inventory.values().stream().filter(ApprovedFileInventoryCli::isLeftover).toList();
  }

  private static boolean isLeftover(InventoryEntry entry) {
    try {
      return stream(Class.forName(entry.className()).getDeclaredMethods())
          .noneMatch(method -> method.getName().equals(entry.methodName()));
    } catch (ClassNotFoundException e) {
      return true;
    }
  }

  /** Result of a {@link #removeLeftovers()} operation. */
  record CleanupResult(List<InventoryEntry> removed, int failures) {}

  /**
   * Removes leftover approved files and updates the inventory.
   *
   * @return the result containing the list of removed leftover entries and the number of failures
   */
  CleanupResult removeLeftovers() {
    List<InventoryEntry> leftovers = findLeftovers();
    if (leftovers.isEmpty()) {
      return new CleanupResult(leftovers, 0);
    }

    List<InventoryEntry> removed = new ArrayList<>();
    int failures = 0;
    for (InventoryEntry leftover : leftovers) {
      try {
        Files.deleteIfExists(leftover.relativePath());
        inventory.remove(leftover.relativePath());
        removed.add(leftover);
      } catch (IOException e) {
        System.err.printf(
            "Failed to delete leftover file: %s (%s: %s)%n",
            leftover.relativePath(), e.getClass().getSimpleName(), e.getMessage());
        failures++;
      }
    }

    ApprovedFileInventory.saveInventory(inventory);

    return new CleanupResult(removed, failures);
  }

  /** Result of an {@link #approveAll()} operation. */
  record ApproveResult(List<Path> approved, int failures) {}

  /**
   * Approves all unapproved files by moving each received file to its corresponding approved file.
   *
   * @return the result containing the list of approved file paths that were updated and the number
   *     of failures
   */
  ApproveResult approveAll() {
    List<Path> approved = new ArrayList<>();
    int failures = 0;
    for (Path approvedPath : inventory.keySet()) {
      Path received = PathProviders.approvedPath(approvedPath).receivedPath();
      if (!Files.exists(received)) {
        continue;
      }
      try {
        Files.move(received, approvedPath, REPLACE_EXISTING);
        approved.add(approvedPath);
      } catch (IOException e) {
        System.err.printf("Failed to approve %s: %s%n", received, e.getMessage());
        failures++;
      }
    }
    return new ApproveResult(approved, failures);
  }

  /**
   * Reviews all unapproved files using the given {@link FileReviewer}.
   *
   * @param reviewer the {@link FileReviewer} to use for reviewing each unapproved file
   */
  void reviewUnapproved(FileReviewer reviewer) {
    var unapprovedEntries =
        inventory.keySet().stream()
            .map(PathProviders::approvedPath)
            .filter(pathProvider -> Files.exists(pathProvider.receivedPath()))
            .sorted(java.util.Comparator.comparing(PathProvider::approvedPath))
            .toList();
    if (unapprovedEntries.isEmpty()) {
      System.out.println("No unapproved files found.");
      return;
    }
    System.out.println("Unapproved files:");
    unapprovedEntries.forEach(
        pathProvider -> {
          System.out.printf("  %s%n", pathProvider.receivedPath().toUri());
          reviewer.apply(pathProvider);
        });
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

    ApprovedFileInventoryCli cli =
        new ApprovedFileInventoryCli(ApprovedFileInventory.loadInventory());

    String command = args[0];
    switch (command) {
      case "--find-leftovers" -> {
        List<InventoryEntry> leftovers = cli.findLeftovers();
        if (leftovers.isEmpty()) {
          System.out.println("No leftover approved files found.");
        } else {
          System.out.println("Leftover approved files:");
          leftovers.forEach(
              leftover ->
                  System.out.printf(
                      "  %s%n    from %s%n",
                      leftover.relativePath().toUri(), leftover.testReference()));
        }
      }
      case "--cleanup" -> {
        CleanupResult cleanupResult = cli.removeLeftovers();
        if (cleanupResult.removed().isEmpty() && cleanupResult.failures() == 0) {
          System.out.println("No leftover approved files found.");
        } else {
          if (!cleanupResult.removed().isEmpty()) {
            System.out.println("Removed leftover approved files:");
            cleanupResult
                .removed()
                .forEach(leftover -> System.out.printf("  %s%n", leftover.relativePath().toUri()));
          }
          if (cleanupResult.failures() > 0) {
            System.err.printf("Failed to delete %d leftover file(s).%n", cleanupResult.failures());
            System.exit(1);
          }
        }
      }
      case "--approve-all" -> {
        ApproveResult result = cli.approveAll();
        if (result.approved().isEmpty() && result.failures() == 0) {
          System.out.println("No unapproved files found.");
        } else {
          if (!result.approved().isEmpty()) {
            System.out.println("Approved files:");
            result.approved().forEach(path -> System.out.printf("  %s%n", path.toUri()));
          }
          if (result.failures() > 0) {
            System.err.printf("Failed to approve %d file(s).%n", result.failures());
            System.exit(1);
          }
        }
      }
      case "--review-unapproved" ->
          cli.reviewUnapproved(Configuration.configuration.defaultFileReviewer());
      default -> {
        System.err.printf("Unknown command: %s%n", command);
        System.err.println(usage);
        System.exit(1);
      }
    }
  }
}

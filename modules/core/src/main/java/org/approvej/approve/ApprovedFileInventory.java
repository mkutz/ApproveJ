package org.approvej.approve;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.joining;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;
import org.approvej.review.FileReviewer;
import org.jspecify.annotations.NullMarked;

/**
 * Wraps a loaded approved file inventory and provides domain operations like {@link
 * #findLeftovers()}, {@link #removeLeftovers()}, {@link #approveAll()}, and {@link
 * #reviewUnapproved(FileReviewer)}.
 *
 * <p>Recording approved files during test execution is handled separately by {@link
 * ApprovedFileInventoryUpdater}.
 */
@NullMarked
public class ApprovedFileInventory {

  private static final Logger LOGGER = Logger.getLogger(ApprovedFileInventory.class.getName());

  private static final String HEADER =
      "# ApproveJ Approved File Inventory (auto-generated, do not edit)";

  private final List<InventoryEntry> inventory;
  private final Path inventoryPath;

  ApprovedFileInventory(List<InventoryEntry> inventory, Path inventoryPath) {
    this.inventory = inventory;
    this.inventoryPath = inventoryPath.toAbsolutePath();
  }

  /** Returns the inventory entries. */
  List<InventoryEntry> entries() {
    return List.copyOf(inventory);
  }

  /**
   * Loads the inventory from the given properties file.
   *
   * @param inventoryPath the path to the inventory properties file
   * @return a new {@link ApprovedFileInventory} wrapping the loaded entries
   */
  static ApprovedFileInventory loadInventory(Path inventoryPath) {
    if (!Files.exists(inventoryPath)) {
      return new ApprovedFileInventory(List.of(), inventoryPath);
    }
    try (BufferedReader reader = Files.newBufferedReader(inventoryPath)) {
      Properties properties = new Properties();
      properties.load(reader);
      return new ApprovedFileInventory(
          properties.stringPropertyNames().stream()
              .map(key -> new InventoryEntry(Path.of(key), properties.getProperty(key)))
              .sorted(Comparator.comparing(InventoryEntry::relativePath))
              .toList(),
          inventoryPath);
    } catch (IOException e) {
      LOGGER.warning("Failed to read inventory file: %s".formatted(e.getMessage()));
      return new ApprovedFileInventory(List.of(), inventoryPath);
    }
  }

  /**
   * Finds leftover inventory entries whose test methods no longer exist.
   *
   * @return a list of leftover inventory entries
   */
  List<InventoryEntry> findLeftovers() {
    return inventory.stream()
        .filter(
            entry -> {
              try {
                return stream(Class.forName(entry.className()).getDeclaredMethods())
                    .noneMatch(method -> method.getName().equals(entry.methodName()));
              } catch (ClassNotFoundException e) {
                return true;
              }
            })
        .toList();
  }

  /** Result of a {@link #removeLeftovers()} operation. */
  record CleanupResult(List<InventoryEntry> removed, List<InventoryEntry> failed) {}

  /**
   * Removes leftover approved files and updates the inventory.
   *
   * @return the result containing the list of removed and failed leftover entries
   */
  CleanupResult removeLeftovers() {
    List<InventoryEntry> leftovers = findLeftovers();
    if (leftovers.isEmpty()) {
      return new CleanupResult(leftovers, List.of());
    }

    List<InventoryEntry> removed = new ArrayList<>();
    List<InventoryEntry> failed = new ArrayList<>();
    leftovers.forEach(
        entry -> {
          try {
            Files.deleteIfExists(entry.relativePath());
            removed.add(entry);
          } catch (IOException e) {
            failed.add(entry);
          }
        });

    new ApprovedFileInventory(
            inventory.stream().filter(entry -> !removed.contains(entry)).toList(), inventoryPath)
        .saveInventory();

    return new CleanupResult(removed, failed);
  }

  /** Result of an {@link #approveAll()} operation. */
  record ApproveResult(List<Path> approved, List<Path> failed) {}

  /**
   * Approves all unapproved files by moving each received file to its corresponding approved file.
   *
   * @return the result containing the list of approved and failed file paths
   */
  ApproveResult approveAll() {
    List<Path> approved = new ArrayList<>();
    List<Path> failed = new ArrayList<>();

    inventory.stream()
        .map(entry -> PathProviders.approvedPath(entry.relativePath()))
        .filter(pathProvider -> Files.exists(pathProvider.receivedPath()))
        .forEach(
            pathProvider -> {
              try {
                Files.move(
                    pathProvider.receivedPath(), pathProvider.approvedPath(), REPLACE_EXISTING);
                approved.add(pathProvider.approvedPath());
              } catch (IOException e) {
                failed.add(pathProvider.receivedPath());
              }
            });

    return new ApproveResult(approved, failed);
  }

  /**
   * Finds all unapproved files (those with a received file present) and reviews them using the
   * given {@link FileReviewer}.
   *
   * @param reviewer the {@link FileReviewer} to use for reviewing each unapproved file
   * @return the list of reviewed {@link PathProvider}s
   */
  List<PathProvider> reviewUnapproved(FileReviewer reviewer) {
    List<PathProvider> unapproved =
        inventory.stream()
            .map(entry -> PathProviders.approvedPath(entry.relativePath()))
            .filter(pathProvider -> Files.exists(pathProvider.receivedPath()))
            .sorted(Comparator.comparing(PathProvider::approvedPath))
            .toList();
    unapproved.forEach(reviewer::apply);
    return unapproved;
  }

  /** Writes the inventory to the inventory file it was loaded from. */
  void saveInventory() {
    try {
      if (inventory.isEmpty()) {
        Files.deleteIfExists(inventoryPath);
      } else {
        Files.createDirectories(inventoryPath.getParent());
        String content =
            inventory.stream()
                .map(
                    entry ->
                        "%s = %s"
                            .formatted(
                                escapeKey(entry.relativePath().toString().replace('\\', '/')),
                                entry.testReference()))
                .collect(joining("\n", HEADER + "\n", "\n"));
        Files.writeString(inventoryPath, content);
      }
    } catch (IOException e) {
      LOGGER.warning("Failed to write inventory file: %s".formatted(e.getMessage()));
    }
  }

  private static String escapeKey(String key) {
    return key.replace("\\", "\\\\").replace(" ", "\\ ").replace("=", "\\=").replace(":", "\\:");
  }
}

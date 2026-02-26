package org.approvej.approve;

import static java.util.stream.Collectors.joining;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * Tracks approved files in an inventory so that leftover files (from renamed or deleted tests) can
 * be detected and cleaned up.
 *
 * <p>During a test run, each {@code byFile()} call records the approved file path and its
 * originating test method. At JVM shutdown, the inventory is merged with any existing inventory
 * file and written to {@code .approvej/inventory.properties}.
 */
@NullMarked
public class ApprovedFileInventory {

  private static final Path DEFAULT_INVENTORY_FILE = Path.of(".approvej/inventory.properties");
  private static final String HEADER =
      "# ApproveJ Approved File Inventory (auto-generated, do not edit)";

  private static final ConcurrentHashMap<Path, InventoryEntry> entries = new ConcurrentHashMap<>();
  private static final ConcurrentHashMap<String, Boolean> executedMethods =
      new ConcurrentHashMap<>();
  private static final AtomicReference<@Nullable Thread> shutdownHook = new AtomicReference<>();

  private static final AtomicReference<Path> inventoryFile =
      new AtomicReference<>(DEFAULT_INVENTORY_FILE);

  private ApprovedFileInventory() {}

  /**
   * Records an approved file path in the inventory.
   *
   * @param pathProvider the path provider for the approved file
   */
  public static void registerApprovedFile(PathProvider pathProvider) {
    TestMethod testMethod;
    try {
      testMethod = StackTraceTestFinderUtil.currentTestMethod();
    } catch (TestMethodNotFoundInStackTraceError e) {
      return;
    }

    String testReference =
        "%s#%s".formatted(testMethod.testClass().getName(), testMethod.testCaseName());

    addEntry(pathProvider.approvedPath(), testReference);

    Thread hook = new Thread(ApprovedFileInventory::writeInventory, "ApproveJ-Inventory-Writer");
    if (shutdownHook.compareAndSet(null, hook)) {
      Runtime.getRuntime().addShutdownHook(hook);
    }
  }

  static void writeInventory() {
    TreeMap<Path, InventoryEntry> merged = loadInventory();

    merged
        .entrySet()
        .removeIf(entry -> executedMethods.containsKey(entry.getValue().testReference()));
    merged.putAll(entries);

    saveInventory(merged);
  }

  /**
   * Loads the inventory from the properties file.
   *
   * @return a mutable map of approved file paths to their inventory entries
   */
  static TreeMap<Path, InventoryEntry> loadInventory() {
    TreeMap<Path, InventoryEntry> result = new TreeMap<>();
    Path inventoryPath = inventoryFile.get();
    if (!Files.exists(inventoryPath)) {
      return result;
    }
    Properties properties = new Properties();
    try (BufferedReader reader = Files.newBufferedReader(inventoryPath)) {
      properties.load(reader);
    } catch (IOException e) {
      System.err.printf("Failed to read inventory file: %s%n", e.getMessage());
      return result;
    }
    properties
        .stringPropertyNames()
        .forEach(
            key -> {
              Path path = Path.of(key);
              result.put(path, new InventoryEntry(path, properties.getProperty(key)));
            });
    return result;
  }

  static void saveInventory(TreeMap<Path, InventoryEntry> inventory) {
    Path inventoryPath = inventoryFile.get();
    try {
      if (inventory.isEmpty()) {
        Files.deleteIfExists(inventoryPath);
      } else {
        Files.createDirectories(inventoryPath.getParent());
        String content =
            "%s%n%s"
                .formatted(
                    HEADER,
                    inventory.values().stream()
                        .map(
                            e ->
                                "%s = %s"
                                    .formatted(
                                        escapeKey(e.relativePath().toString()), e.testReference()))
                        .collect(joining("\n", "", "\n")));
        Files.writeString(inventoryPath, content);
      }
    } catch (IOException e) {
      System.err.printf("Failed to write inventory file: %s%n", e.getMessage());
    }
  }

  private static String escapeKey(String key) {
    return key.replace("\\", "\\\\").replace(" ", "\\ ").replace("=", "\\=").replace(":", "\\:");
  }

  /** Adds an entry directly. For testing only. */
  static void addEntry(Path relativePath, String testReference) {
    entries.put(relativePath, new InventoryEntry(relativePath, testReference));
    executedMethods.put(testReference, Boolean.TRUE);
  }

  /** Resets static state and sets the inventory file path. For testing only. */
  static void reset(Path testInventoryFile) {
    entries.clear();
    executedMethods.clear();
    Thread hook = shutdownHook.getAndSet(null);
    if (hook != null) {
      try {
        Runtime.getRuntime().removeShutdownHook(hook);
      } catch (IllegalStateException e) {
        // JVM is already shutting down
      }
    }
    inventoryFile.set(testInventoryFile);
  }

  /** Resets static state to defaults. For testing only. */
  static void reset() {
    reset(DEFAULT_INVENTORY_FILE);
  }
}

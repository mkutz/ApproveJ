package org.approvej.approve;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.joining;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Properties;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import org.jspecify.annotations.NullMarked;

/**
 * Tracks approved files in an inventory so that orphaned files (from renamed or deleted tests) can
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

  private static final ConcurrentHashMap<String, String> entries = new ConcurrentHashMap<>();
  private static final ConcurrentHashMap<String, Boolean> executedMethods =
      new ConcurrentHashMap<>();
  private static final AtomicBoolean shutdownHookRegistered = new AtomicBoolean(false);

  private static Path inventoryFile = DEFAULT_INVENTORY_FILE;

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
    String relativePath =
        Path.of("").toAbsolutePath().relativize(pathProvider.approvedPath()).toString();

    addEntry(relativePath, testReference);

    if (shutdownHookRegistered.compareAndSet(false, true)) {
      Runtime.getRuntime().addShutdownHook(new Thread(ApprovedFileInventory::writeInventory));
    }
  }

  static void writeInventory() {
    TreeMap<String, String> merged = loadInventory();

    merged.values().removeAll(executedMethods.keySet());
    merged.putAll(entries);

    saveInventory(merged);
  }

  /**
   * Finds orphaned inventory entries whose test methods no longer exist.
   *
   * @return a list of orphaned inventory entries
   */
  static List<InventoryEntry> findOrphans() {
    return loadInventory().entrySet().stream()
        .map(entry -> new InventoryEntry(entry.getKey(), entry.getValue()))
        .filter(ApprovedFileInventory::isOrphan)
        .toList();
  }

  private static boolean isOrphan(InventoryEntry entry) {
    try {
      return stream(Class.forName(entry.className()).getDeclaredMethods())
          .noneMatch(method -> method.getName().equals(entry.methodName()));
    } catch (ClassNotFoundException e) {
      return true;
    }
  }

  /**
   * Removes orphaned approved files and updates the inventory.
   *
   * @return the list of removed orphan entries
   */
  static List<InventoryEntry> removeOrphans() {
    List<InventoryEntry> orphans = findOrphans();
    if (orphans.isEmpty()) {
      return orphans;
    }

    TreeMap<String, String> inventory = loadInventory();
    orphans.forEach(
        orphan -> {
          try {
            Files.deleteIfExists(Path.of(orphan.relativePath()));
          } catch (IOException e) {
            System.err.printf("Failed to delete orphaned file: %s%n", orphan.relativePath());
          }
          inventory.remove(orphan.relativePath());
        });

    saveInventory(inventory);

    return orphans;
  }

  static TreeMap<String, String> loadInventory() {
    TreeMap<String, String> result = new TreeMap<>();
    if (!Files.exists(inventoryFile)) {
      return result;
    }
    Properties properties = new Properties();
    try (BufferedReader reader = Files.newBufferedReader(inventoryFile)) {
      properties.load(reader);
    } catch (IOException e) {
      System.err.printf("Failed to read inventory file: %s%n", e.getMessage());
      return result;
    }
    properties.stringPropertyNames().forEach(key -> result.put(key, properties.getProperty(key)));
    return result;
  }

  private static void saveInventory(TreeMap<String, String> inventory) {
    try {
      if (inventory.isEmpty()) {
        Files.deleteIfExists(inventoryFile);
      } else {
        Files.createDirectories(inventoryFile.getParent());
        String content =
            "%s%n%s"
                .formatted(
                    HEADER,
                    inventory.entrySet().stream()
                        .map(e -> "%s = %s".formatted(escapeKey(e.getKey()), e.getValue()))
                        .collect(joining("\n", "", "\n")));
        Files.writeString(inventoryFile, content);
      }
    } catch (IOException e) {
      System.err.printf("Failed to write inventory file: %s%n", e.getMessage());
    }
  }

  private static String escapeKey(String key) {
    return key.replace("\\", "\\\\").replace(" ", "\\ ").replace("=", "\\=").replace(":", "\\:");
  }

  /** Adds an entry directly. For testing only. */
  static void addEntry(String relativePath, String testReference) {
    entries.put(relativePath, testReference);
    executedMethods.put(testReference, Boolean.TRUE);
  }

  /** Resets static state and sets the inventory file path. For testing only. */
  static void reset(Path testInventoryFile) {
    entries.clear();
    executedMethods.clear();
    shutdownHookRegistered.set(false);
    inventoryFile = testInventoryFile;
  }

  /** Resets static state to defaults. For testing only. */
  static void reset() {
    reset(DEFAULT_INVENTORY_FILE);
  }

  /**
   * CLI entry point for build tool plugins.
   *
   * @param args {@code --find} to list orphans, {@code --remove} to delete them
   */
  public static void main(String[] args) {
    String usage = "Usage: ApprovedFileInventory --find | --remove";
    if (args.length == 0) {
      System.err.println(usage);
      System.exit(1);
    }

    String command = args[0];

    switch (command) {
      case "--find" -> {
        List<InventoryEntry> orphans = findOrphans();
        if (orphans.isEmpty()) {
          System.out.println("No orphaned approved files found.");
        } else {
          System.out.println("Orphaned approved files:");
          orphans.forEach(
              orphan ->
                  System.out.printf(
                      "  %s (from %s)%n", orphan.relativePath(), orphan.testReference()));
        }
      }
      case "--remove" -> {
        List<InventoryEntry> removed = removeOrphans();
        if (removed.isEmpty()) {
          System.out.println("No orphaned approved files found.");
        } else {
          System.out.println("Removed orphaned approved files:");
          removed.forEach(orphan -> System.out.printf("  %s%n", orphan.relativePath()));
        }
      }
      default -> {
        System.err.printf("Unknown command: %s%n", command);
        System.err.println(usage);
        System.exit(1);
      }
    }
  }
}

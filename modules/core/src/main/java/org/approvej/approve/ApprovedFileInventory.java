package org.approvej.approve;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.joining;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import org.jspecify.annotations.NullMarked;

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

  private static final ConcurrentHashMap<String, String> entries = new ConcurrentHashMap<>();
  private static final ConcurrentHashMap<String, Boolean> executedMethods =
      new ConcurrentHashMap<>();
  private static final AtomicReference<Thread> shutdownHook = new AtomicReference<>();

  private static volatile Path inventoryFile = DEFAULT_INVENTORY_FILE;

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
    Path cwd = Path.of("").toAbsolutePath();
    Path approvedPath = pathProvider.approvedPath().toAbsolutePath();
    String relativePath;
    try {
      relativePath = cwd.relativize(approvedPath).toString();
    } catch (IllegalArgumentException e) {
      relativePath = approvedPath.toString();
    }

    addEntry(relativePath, testReference);

    Thread hook = new Thread(ApprovedFileInventory::writeInventory, "ApproveJ-Inventory-Writer");
    if (shutdownHook.compareAndSet(null, hook)) {
      Runtime.getRuntime().addShutdownHook(hook);
    }
  }

  static void writeInventory() {
    TreeMap<String, String> merged = loadInventory();

    merged.entrySet().removeIf(entry -> executedMethods.containsKey(entry.getValue()));
    merged.putAll(entries);

    saveInventory(merged);
  }

  /**
   * Finds leftover inventory entries whose test methods no longer exist.
   *
   * @return a list of leftover inventory entries
   */
  static List<InventoryEntry> findLeftovers() {
    return loadInventory().entrySet().stream()
        .map(entry -> new InventoryEntry(entry.getKey(), entry.getValue()))
        .filter(ApprovedFileInventory::isLeftover)
        .toList();
  }

  private static boolean isLeftover(InventoryEntry entry) {
    try {
      return stream(Class.forName(entry.className()).getDeclaredMethods())
          .noneMatch(method -> method.getName().equals(entry.methodName()));
    } catch (ClassNotFoundException e) {
      return true;
    }
  }

  /**
   * Removes leftover approved files and updates the inventory.
   *
   * @return the list of removed leftover entries
   */
  static List<InventoryEntry> removeLeftovers() {
    List<InventoryEntry> leftovers = findLeftovers();
    if (leftovers.isEmpty()) {
      return leftovers;
    }

    TreeMap<String, String> inventory = loadInventory();
    List<InventoryEntry> removed = new ArrayList<>();
    for (InventoryEntry leftover : leftovers) {
      try {
        Files.deleteIfExists(Path.of(leftover.relativePath()));
        inventory.remove(leftover.relativePath());
        removed.add(leftover);
      } catch (IOException e) {
        System.err.printf("Failed to delete leftover file: %s%n", leftover.relativePath());
      }
    }

    saveInventory(inventory);

    return removed;
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
    Thread hook = shutdownHook.getAndSet(null);
    if (hook != null) {
      try {
        Runtime.getRuntime().removeShutdownHook(hook);
      } catch (IllegalStateException e) {
        // JVM is already shutting down
      }
    }
    inventoryFile = testInventoryFile;
  }

  /** Resets static state to defaults. For testing only. */
  static void reset() {
    reset(DEFAULT_INVENTORY_FILE);
  }

  /**
   * CLI entry point for build tool plugins.
   *
   * @param args {@code --find} to list leftovers, {@code --remove} to delete them
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
        List<InventoryEntry> leftovers = findLeftovers();
        if (leftovers.isEmpty()) {
          System.out.println("No leftover approved files found.");
        } else {
          System.out.println("Leftover approved files:");
          leftovers.forEach(
              leftover ->
                  System.out.printf(
                      "  %s%n    from %s%n",
                      Path.of(leftover.relativePath()).toUri(), leftover.testReference()));
        }
      }
      case "--remove" -> {
        List<InventoryEntry> removed = removeLeftovers();
        if (removed.isEmpty()) {
          System.out.println("No leftover approved files found.");
        } else {
          System.out.println("Removed leftover approved files:");
          removed.forEach(
              leftover -> System.out.printf("  %s%n", Path.of(leftover.relativePath()).toUri()));
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

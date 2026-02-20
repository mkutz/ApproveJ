package org.approvej.approve;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
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
import org.approvej.configuration.Configuration;
import org.approvej.review.FileReviewer;
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
  private static final Path DEFAULT_WORKING_DIRECTORY = Path.of(".");
  private static final String HEADER =
      "# ApproveJ Approved File Inventory (auto-generated, do not edit)";

  private static final ConcurrentHashMap<Path, InventoryEntry> entries = new ConcurrentHashMap<>();
  private static final ConcurrentHashMap<String, Boolean> executedMethods =
      new ConcurrentHashMap<>();
  private static final AtomicReference<@Nullable Thread> shutdownHook = new AtomicReference<>();

  private static final AtomicReference<Path> inventoryFile =
      new AtomicReference<>(DEFAULT_INVENTORY_FILE);

  private static final AtomicReference<Path> workingDirectory =
      new AtomicReference<>(DEFAULT_WORKING_DIRECTORY);

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
   * Finds leftover inventory entries whose test methods no longer exist.
   *
   * @return a list of leftover inventory entries
   */
  static List<InventoryEntry> findLeftovers() {
    return loadInventory().values().stream().filter(ApprovedFileInventory::isLeftover).toList();
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

    TreeMap<Path, InventoryEntry> inventory = loadInventory();
    List<InventoryEntry> removed = new ArrayList<>();
    for (InventoryEntry leftover : leftovers) {
      try {
        Files.deleteIfExists(leftover.relativePath());
        inventory.remove(leftover.relativePath());
        removed.add(leftover);
      } catch (IOException e) {
        System.err.printf("Failed to delete leftover file: %s%n", leftover.relativePath());
      }
    }

    saveInventory(inventory);

    return removed;
  }

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

  private static void saveInventory(TreeMap<Path, InventoryEntry> inventory) {
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
    workingDirectory.set(DEFAULT_WORKING_DIRECTORY);
  }

  /**
   * Resets static state and sets the inventory file path and working directory. For testing only.
   */
  static void reset(Path testInventoryFile, Path testWorkingDirectory) {
    reset(testInventoryFile);
    workingDirectory.set(testWorkingDirectory);
  }

  /** Resets static state to defaults. For testing only. */
  static void reset() {
    reset(DEFAULT_INVENTORY_FILE);
  }

  /**
   * Finds all received files in the working directory recursively.
   *
   * @return a sorted list of paths to received files
   */
  static List<Path> findReceivedFiles() {
    try (var stream = Files.walk(workingDirectory.get())) {
      return stream
          .filter(
              path -> {
                String filename = path.getFileName().toString();
                return filename.contains("-received.") || filename.endsWith("-received");
              })
          .map(Path::normalize)
          .sorted()
          .toList();
    } catch (IOException e) {
      System.err.printf("Failed to search for received files: %s%n", e.getMessage());
      return List.of();
    }
  }

  private static Path approvedPathFor(Path receivedPath) {
    String filename = receivedPath.getFileName().toString();
    String approvedFilename;
    if (filename.contains("-received.")) {
      approvedFilename = filename.replace("-received.", "-approved.");
    } else {
      approvedFilename =
          filename.substring(0, filename.length() - "-received".length()) + "-approved";
    }
    return receivedPath.getParent().resolve(approvedFilename);
  }

  /**
   * Approves all unapproved files by moving each received file to its corresponding approved file.
   *
   * @return the list of approved file paths (the received files that were moved)
   */
  static List<Path> approveAll() {
    List<Path> receivedFiles = findReceivedFiles();
    List<Path> approved = new ArrayList<>();
    for (Path received : receivedFiles) {
      Path approvedPath = approvedPathFor(received);
      try {
        Files.move(received, approvedPath, REPLACE_EXISTING);
        approved.add(received);
      } catch (IOException e) {
        System.err.printf("Failed to approve %s: %s%n", received, e.getMessage());
      }
    }
    return approved;
  }

  /**
   * Reviews all unapproved files using the configured {@link FileReviewer}.
   *
   * @param reviewer the {@link FileReviewer} to use for reviewing each unapproved file
   */
  static void reviewUnapproved(FileReviewer reviewer) {
    List<Path> receivedFiles = findReceivedFiles();
    if (receivedFiles.isEmpty()) {
      System.out.println("No unapproved files found.");
      return;
    }
    System.out.println("Unapproved files:");
    receivedFiles.forEach(
        received -> {
          Path approvedPath = approvedPathFor(received);
          System.out.printf("  %s%n", received.toUri());
          reviewer.apply(PathProviders.approvedPath(approvedPath));
        });
  }

  /**
   * CLI entry point for build tool plugins.
   *
   * @param args {@code --find} to list leftovers, {@code --remove} to delete them, {@code
   *     --approve-all} to approve all unapproved files, {@code --review-unapproved} to review all
   *     unapproved files
   */
  public static void main(String[] args) {
    String usage =
        "Usage: ApprovedFileInventory --find | --remove | --approve-all | --review-unapproved";
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
                      leftover.relativePath().toUri(), leftover.testReference()));
        }
      }
      case "--remove" -> {
        List<InventoryEntry> removed = removeLeftovers();
        if (removed.isEmpty()) {
          System.out.println("No leftover approved files found.");
        } else {
          System.out.println("Removed leftover approved files:");
          removed.forEach(leftover -> System.out.printf("  %s%n", leftover.relativePath().toUri()));
        }
      }
      case "--approve-all" -> {
        List<Path> approved = approveAll();
        if (approved.isEmpty()) {
          System.out.println("No unapproved files found.");
        } else {
          System.out.println("Approved files:");
          approved.forEach(path -> System.out.printf("  %s%n", path.toUri()));
        }
      }
      case "--review-unapproved" -> {
        reviewUnapproved(Configuration.configuration.defaultFileReviewer());
      }
      default -> {
        System.err.printf("Unknown command: %s%n", command);
        System.err.println(usage);
        System.exit(1);
      }
    }
  }
}

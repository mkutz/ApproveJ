package org.approvej.approve;

import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;
import java.util.stream.Stream;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * Records approved file paths during test execution and merges them into the inventory file at JVM
 * shutdown.
 *
 * <p>This class holds the write buffer for a running test suite. Each {@code byFile()} call goes
 * through {@link #registerApprovedFile(PathProvider)}, which accumulates entries in memory. A
 * shutdown hook then loads the existing inventory, merges the new entries, and writes the result
 * back.
 *
 * <p>This is intentionally separated from {@link ApprovedFileInventory}, which operates on a loaded
 * inventory without any static state.
 */
@NullMarked
public final class ApprovedFileInventoryUpdater {

  static final Path DEFAULT_INVENTORY_FILE = Path.of(".approvej/inventory.properties");

  private static final ConcurrentHashMap<Path, InventoryEntry> collected =
      new ConcurrentHashMap<>();
  private static final AtomicReference<@Nullable Thread> shutdownHook = new AtomicReference<>();

  private static final Logger LOGGER =
      Logger.getLogger(ApprovedFileInventoryUpdater.class.getName());

  private static final AtomicReference<Path> inventoryFile =
      new AtomicReference<>(DEFAULT_INVENTORY_FILE);

  private ApprovedFileInventoryUpdater() {}

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

    addEntry(new InventoryEntry(pathProvider.approvedPath(), testReference));

    shutdownHook.updateAndGet(
        existing -> {
          if (existing != null) {
            return existing;
          }
          Thread hook =
              new Thread(ApprovedFileInventoryUpdater::writeInventory, "ApproveJ-Inventory-Writer");
          Runtime.getRuntime().addShutdownHook(hook);
          return hook;
        });
  }

  static void writeInventory() {
    Path path = inventoryFile.get();
    ApprovedFileInventory loaded = ApprovedFileInventory.loadInventory(path);

    List<InventoryEntry> merged =
        Stream.concat(
                collected.values().stream(),
                loaded.entries().stream()
                    .filter(entry -> !collected.containsKey(entry.relativePath())))
            .sorted(Comparator.comparing(InventoryEntry::relativePath))
            .toList();

    new ApprovedFileInventory(merged, path).saveInventory();
  }

  /** Adds an entry directly. For testing only. */
  static void addEntry(InventoryEntry entry) {
    collected.put(entry.relativePath(), entry);
  }

  /** Resets static state and sets the inventory file path. For testing only. */
  static void reset(Path testInventoryFile) {
    collected.clear();
    Thread hook = shutdownHook.getAndSet(null);
    if (hook != null) {
      try {
        Runtime.getRuntime().removeShutdownHook(hook);
      } catch (IllegalStateException e) {
        LOGGER.fine("Could not remove shutdown hook: " + e.getMessage());
      }
    }
    inventoryFile.set(testInventoryFile);
  }

  /** Resets static state to defaults. For testing only. */
  static void reset() {
    reset(DEFAULT_INVENTORY_FILE);
  }
}

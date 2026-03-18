package org.approvej.approve;

import static java.nio.file.Files.writeString;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ApprovedFileInventoryUpdaterTest {

  @TempDir private Path tempDir;

  private Path inventoryFile;

  @BeforeEach
  void setUp() {
    inventoryFile = tempDir.resolve("inventory.properties");
    ApprovedFileInventoryUpdater.reset(inventoryFile);
  }

  @AfterEach
  void tearDown() {
    ApprovedFileInventoryUpdater.reset();
  }

  @Test
  void writeInventory() {
    var entry =
        new InventoryEntry(
            Path.of("src/test/MyTest-myTest-approved.txt"), "com.example.MyTest#myTest");
    ApprovedFileInventoryUpdater.addEntry(entry);

    ApprovedFileInventoryUpdater.writeInventory();

    var inventory = ApprovedFileInventory.loadInventory(inventoryFile);
    assertThat(inventory.entries()).containsExactly(entry);
  }

  @Test
  void writeInventory_multiple_entries() {
    var entryB =
        new InventoryEntry(Path.of("src/test/BTest-b-approved.txt"), "com.example.BTest#b");
    var entryA =
        new InventoryEntry(Path.of("src/test/ATest-a-approved.txt"), "com.example.ATest#a");
    ApprovedFileInventoryUpdater.addEntry(entryB);
    ApprovedFileInventoryUpdater.addEntry(entryA);

    ApprovedFileInventoryUpdater.writeInventory();

    var inventory = ApprovedFileInventory.loadInventory(inventoryFile);
    assertThat(inventory.entries()).containsExactly(entryA, entryB);
  }

  @Test
  void writeInventory_named_approvals() {
    var alpha =
        new InventoryEntry(
            Path.of("src/test/MyTest-myTest-alpha-approved.txt"), "com.example.MyTest#myTest");
    var beta =
        new InventoryEntry(
            Path.of("src/test/MyTest-myTest-beta-approved.txt"), "com.example.MyTest#myTest");
    ApprovedFileInventoryUpdater.addEntry(alpha);
    ApprovedFileInventoryUpdater.addEntry(beta);

    ApprovedFileInventoryUpdater.writeInventory();

    var inventory = ApprovedFileInventory.loadInventory(inventoryFile);
    assertThat(inventory.entries()).containsExactly(alpha, beta);
  }

  @Test
  void writeInventory_preserves_old_entries_for_same_method() throws IOException {
    writeString(
        inventoryFile,
        """
        # ApproveJ Approved File Inventory (auto-generated, do not edit)
        src/test/MyTest-myTest-alpha-approved.txt = com.example.MyTest#myTest
        src/test/MyTest-myTest-beta-approved.txt = com.example.MyTest#myTest
        """,
        StandardOpenOption.CREATE);

    var gamma =
        new InventoryEntry(
            Path.of("src/test/MyTest-myTest-gamma-approved.txt"), "com.example.MyTest#myTest");
    var beta =
        new InventoryEntry(
            Path.of("src/test/MyTest-myTest-beta-approved.txt"), "com.example.MyTest#myTest");
    ApprovedFileInventoryUpdater.addEntry(gamma);
    ApprovedFileInventoryUpdater.addEntry(beta);

    ApprovedFileInventoryUpdater.writeInventory();

    var alpha =
        new InventoryEntry(
            Path.of("src/test/MyTest-myTest-alpha-approved.txt"), "com.example.MyTest#myTest");
    var inventory = ApprovedFileInventory.loadInventory(inventoryFile);
    assertThat(inventory.entries()).containsExactly(alpha, beta, gamma);
  }

  @Test
  void writeInventory_preserves_entries_for_unexecuted_approvals_in_same_method()
      throws IOException {
    writeString(
        inventoryFile,
        """
        # ApproveJ Approved File Inventory (auto-generated, do not edit)
        src/test/MyTest-myTest-alpha-approved.txt = com.example.MyTest#myTest
        src/test/MyTest-myTest-beta-approved.txt = com.example.MyTest#myTest
        """,
        StandardOpenOption.CREATE);

    // Only alpha was reached during this run (e.g. the test failed before beta)
    var alpha =
        new InventoryEntry(
            Path.of("src/test/MyTest-myTest-alpha-approved.txt"), "com.example.MyTest#myTest");
    ApprovedFileInventoryUpdater.addEntry(alpha);

    ApprovedFileInventoryUpdater.writeInventory();

    var beta =
        new InventoryEntry(
            Path.of("src/test/MyTest-myTest-beta-approved.txt"), "com.example.MyTest#myTest");
    var inventory = ApprovedFileInventory.loadInventory(inventoryFile);
    assertThat(inventory.entries()).containsExactly(alpha, beta);
  }

  @Test
  void writeInventory_preserves_entries_for_unexecuted_methods() throws IOException {
    writeString(
        inventoryFile,
        """
        # ApproveJ Approved File Inventory (auto-generated, do not edit)
        src/test/OtherTest-other-approved.txt = com.example.OtherTest#other
        """,
        StandardOpenOption.CREATE);

    var entry =
        new InventoryEntry(
            Path.of("src/test/MyTest-myTest-approved.txt"), "com.example.MyTest#myTest");
    ApprovedFileInventoryUpdater.addEntry(entry);

    ApprovedFileInventoryUpdater.writeInventory();

    var other =
        new InventoryEntry(
            Path.of("src/test/OtherTest-other-approved.txt"), "com.example.OtherTest#other");
    var inventory = ApprovedFileInventory.loadInventory(inventoryFile);
    assertThat(inventory.entries()).containsExactly(entry, other);
  }
}

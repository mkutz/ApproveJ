package org.approvej.approve;

import static java.nio.file.Files.writeString;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.TreeMap;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ApprovedFileInventoryTest {

  @TempDir private Path tempDir;

  private Path inventoryFile;

  @BeforeEach
  void setUp() {
    inventoryFile = tempDir.resolve("inventory.properties");
    ApprovedFileInventory.reset(inventoryFile);
  }

  @AfterEach
  void tearDown() {
    ApprovedFileInventory.reset();
  }

  @Test
  void writeInventory() {
    ApprovedFileInventory.addEntry(
        Path.of("src/test/MyTest-myTest-approved.txt"), "com.example.MyTest#myTest");

    ApprovedFileInventory.writeInventory();

    TreeMap<Path, InventoryEntry> inventory = ApprovedFileInventory.loadInventory();
    assertThat(inventory.values())
        .containsExactly(
            new InventoryEntry(
                Path.of("src/test/MyTest-myTest-approved.txt"), "com.example.MyTest#myTest"));
  }

  @Test
  void writeInventory_multiple_entries() {
    ApprovedFileInventory.addEntry(Path.of("src/test/BTest-b-approved.txt"), "com.example.BTest#b");
    ApprovedFileInventory.addEntry(Path.of("src/test/ATest-a-approved.txt"), "com.example.ATest#a");

    ApprovedFileInventory.writeInventory();

    TreeMap<Path, InventoryEntry> inventory = ApprovedFileInventory.loadInventory();
    assertThat(inventory).hasSize(2);
    assertThat(inventory.firstKey()).isEqualTo(Path.of("src/test/ATest-a-approved.txt"));
  }

  @Test
  void writeInventory_named_approvals() {
    ApprovedFileInventory.addEntry(
        Path.of("src/test/MyTest-myTest-alpha-approved.txt"), "com.example.MyTest#myTest");
    ApprovedFileInventory.addEntry(
        Path.of("src/test/MyTest-myTest-beta-approved.txt"), "com.example.MyTest#myTest");

    ApprovedFileInventory.writeInventory();

    TreeMap<Path, InventoryEntry> inventory = ApprovedFileInventory.loadInventory();
    assertThat(inventory.values())
        .hasSize(2)
        .contains(
            new InventoryEntry(
                Path.of("src/test/MyTest-myTest-alpha-approved.txt"), "com.example.MyTest#myTest"),
            new InventoryEntry(
                Path.of("src/test/MyTest-myTest-beta-approved.txt"), "com.example.MyTest#myTest"));
  }

  @Test
  void writeInventory_replaces_entries_for_executed_methods() throws IOException {
    writeString(
        inventoryFile,
        """
        # ApproveJ Approved File Inventory (auto-generated, do not edit)
        src/test/MyTest-myTest-alpha-approved.txt = com.example.MyTest#myTest
        src/test/MyTest-myTest-beta-approved.txt = com.example.MyTest#myTest
        """,
        StandardOpenOption.CREATE);

    ApprovedFileInventory.addEntry(
        Path.of("src/test/MyTest-myTest-gamma-approved.txt"), "com.example.MyTest#myTest");
    ApprovedFileInventory.addEntry(
        Path.of("src/test/MyTest-myTest-beta-approved.txt"), "com.example.MyTest#myTest");

    ApprovedFileInventory.writeInventory();

    TreeMap<Path, InventoryEntry> inventory = ApprovedFileInventory.loadInventory();
    assertThat(inventory.values())
        .hasSize(2)
        .contains(
            new InventoryEntry(
                Path.of("src/test/MyTest-myTest-gamma-approved.txt"), "com.example.MyTest#myTest"),
            new InventoryEntry(
                Path.of("src/test/MyTest-myTest-beta-approved.txt"), "com.example.MyTest#myTest"));
    assertThat(inventory).doesNotContainKey(Path.of("src/test/MyTest-myTest-alpha-approved.txt"));
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

    ApprovedFileInventory.addEntry(
        Path.of("src/test/MyTest-myTest-approved.txt"), "com.example.MyTest#myTest");

    ApprovedFileInventory.writeInventory();

    TreeMap<Path, InventoryEntry> inventory = ApprovedFileInventory.loadInventory();
    assertThat(inventory.values())
        .hasSize(2)
        .contains(
            new InventoryEntry(
                Path.of("src/test/OtherTest-other-approved.txt"), "com.example.OtherTest#other"),
            new InventoryEntry(
                Path.of("src/test/MyTest-myTest-approved.txt"), "com.example.MyTest#myTest"));
  }
}

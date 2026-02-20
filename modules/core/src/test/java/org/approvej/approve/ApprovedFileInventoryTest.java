package org.approvej.approve;

import static java.nio.file.Files.writeString;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
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
        "src/test/MyTest-myTest-approved.txt", "com.example.MyTest#myTest");

    ApprovedFileInventory.writeInventory();

    TreeMap<String, String> inventory = ApprovedFileInventory.loadInventory();
    assertThat(inventory)
        .containsEntry("src/test/MyTest-myTest-approved.txt", "com.example.MyTest#myTest");
  }

  @Test
  void writeInventory_multiple_entries() {
    ApprovedFileInventory.addEntry("src/test/BTest-b-approved.txt", "com.example.BTest#b");
    ApprovedFileInventory.addEntry("src/test/ATest-a-approved.txt", "com.example.ATest#a");

    ApprovedFileInventory.writeInventory();

    TreeMap<String, String> inventory = ApprovedFileInventory.loadInventory();
    assertThat(inventory).hasSize(2);
    assertThat(inventory.firstKey()).isEqualTo("src/test/ATest-a-approved.txt");
  }

  @Test
  void writeInventory_named_approvals() {
    ApprovedFileInventory.addEntry(
        "src/test/MyTest-myTest-alpha-approved.txt", "com.example.MyTest#myTest");
    ApprovedFileInventory.addEntry(
        "src/test/MyTest-myTest-beta-approved.txt", "com.example.MyTest#myTest");

    ApprovedFileInventory.writeInventory();

    TreeMap<String, String> inventory = ApprovedFileInventory.loadInventory();
    assertThat(inventory)
        .hasSize(2)
        .containsEntry("src/test/MyTest-myTest-alpha-approved.txt", "com.example.MyTest#myTest")
        .containsEntry("src/test/MyTest-myTest-beta-approved.txt", "com.example.MyTest#myTest");
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
        "src/test/MyTest-myTest-gamma-approved.txt", "com.example.MyTest#myTest");
    ApprovedFileInventory.addEntry(
        "src/test/MyTest-myTest-beta-approved.txt", "com.example.MyTest#myTest");

    ApprovedFileInventory.writeInventory();

    TreeMap<String, String> inventory = ApprovedFileInventory.loadInventory();
    assertThat(inventory)
        .hasSize(2)
        .containsEntry("src/test/MyTest-myTest-gamma-approved.txt", "com.example.MyTest#myTest")
        .containsEntry("src/test/MyTest-myTest-beta-approved.txt", "com.example.MyTest#myTest")
        .doesNotContainKey("src/test/MyTest-myTest-alpha-approved.txt");
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
        "src/test/MyTest-myTest-approved.txt", "com.example.MyTest#myTest");

    ApprovedFileInventory.writeInventory();

    TreeMap<String, String> inventory = ApprovedFileInventory.loadInventory();
    assertThat(inventory)
        .hasSize(2)
        .containsEntry("src/test/OtherTest-other-approved.txt", "com.example.OtherTest#other")
        .containsEntry("src/test/MyTest-myTest-approved.txt", "com.example.MyTest#myTest");
  }

  @Test
  void findLeftovers() throws IOException {
    writeString(
        inventoryFile,
        """
        # ApproveJ Approved File Inventory (auto-generated, do not edit)
        src/test/NonExistent-test-approved.txt = com.nonexistent.NonExistentTest#test
        """,
        StandardOpenOption.CREATE);

    List<InventoryEntry> leftovers = ApprovedFileInventory.findLeftovers();

    assertThat(leftovers).hasSize(1);
    assertThat(leftovers.getFirst().relativePath())
        .isEqualTo("src/test/NonExistent-test-approved.txt");
    assertThat(leftovers.getFirst().testReference())
        .isEqualTo("com.nonexistent.NonExistentTest#test");
  }

  @Test
  void findLeftovers_missing_method() throws IOException {
    writeString(
        inventoryFile,
        """
        # ApproveJ Approved File Inventory (auto-generated, do not edit)
        src/test/ApprovedFileInventoryTest-nonExistentMethod-approved.txt = org.approvej.approve.ApprovedFileInventoryTest#nonExistentMethod
        """,
        StandardOpenOption.CREATE);

    List<InventoryEntry> leftovers = ApprovedFileInventory.findLeftovers();

    assertThat(leftovers).hasSize(1);
    assertThat(leftovers.getFirst().testReference())
        .isEqualTo("org.approvej.approve.ApprovedFileInventoryTest#nonExistentMethod");
  }

  @Test
  void removeLeftovers() throws IOException {
    Path leftoverFile = tempDir.resolve("leftover-approved.txt");
    writeString(leftoverFile, "old content", StandardOpenOption.CREATE);

    Path validFile = tempDir.resolve("valid-approved.txt");

    writeString(
        inventoryFile,
        "# ApproveJ Approved File Inventory (auto-generated, do not edit)\n"
            + leftoverFile
            + " = com.nonexistent.NonExistentTest#test\n"
            + validFile
            + " = org.approvej.approve.ApprovedFileInventoryTest#removeLeftovers\n",
        StandardOpenOption.CREATE);

    List<InventoryEntry> removed = ApprovedFileInventory.removeLeftovers();

    assertThat(removed).hasSize(1);
    assertThat(leftoverFile).doesNotExist();
    TreeMap<String, String> inventory = ApprovedFileInventory.loadInventory();
    assertThat(inventory)
        .doesNotContainKey(leftoverFile.toString())
        .containsKey(validFile.toString());
  }
}

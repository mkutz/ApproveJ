package org.approvej.approve;

import static java.nio.file.Files.writeString;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import org.approvej.review.FileReviewResult;
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
        .isEqualTo(Path.of("src/test/NonExistent-test-approved.txt"));
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

    ApprovedFileInventory.CleanupResult result = ApprovedFileInventory.removeLeftovers();

    assertThat(result.removed()).hasSize(1);
    assertThat(result.failures()).isZero();
    assertThat(leftoverFile).doesNotExist();
    TreeMap<Path, InventoryEntry> inventory = ApprovedFileInventory.loadInventory();
    assertThat(inventory).doesNotContainKey(leftoverFile).containsKey(validFile);
  }

  @Test
  void approveAll() throws IOException {
    Path receivedFile = tempDir.resolve("MyTest-myMethod-received.txt");
    writeString(receivedFile, "received content", StandardOpenOption.CREATE);
    Path approvedFile = tempDir.resolve("MyTest-myMethod-approved.txt");
    writeString(approvedFile, "old approved content", StandardOpenOption.CREATE);
    writeString(
        inventoryFile,
        "# ApproveJ Approved File Inventory (auto-generated, do not edit)\n"
            + approvedFile
            + " = com.example.MyTest#myMethod\n",
        StandardOpenOption.CREATE);

    ApprovedFileInventory.ApproveResult result = ApprovedFileInventory.approveAll();

    assertThat(result.approved()).containsExactly(approvedFile);
    assertThat(result.failures()).isZero();
    assertThat(receivedFile).doesNotExist();
    assertThat(approvedFile).exists().hasContent("received content");
  }

  @Test
  void approveAll_no_received_files() {
    ApprovedFileInventory.ApproveResult result = ApprovedFileInventory.approveAll();

    assertThat(result.approved()).isEmpty();
    assertThat(result.failures()).isZero();
  }

  @Test
  void reviewUnapproved() throws IOException {
    Path receivedFile = tempDir.resolve("MyTest-myMethod-received.txt");
    writeString(receivedFile, "received content", StandardOpenOption.CREATE);
    Path approvedFile = tempDir.resolve("MyTest-myMethod-approved.txt");
    writeString(approvedFile, "approved content", StandardOpenOption.CREATE);
    writeString(
        inventoryFile,
        "# ApproveJ Approved File Inventory (auto-generated, do not edit)\n"
            + approvedFile
            + " = com.example.MyTest#myMethod\n",
        StandardOpenOption.CREATE);

    var reviewedProviders = new ArrayList<PathProvider>();
    ApprovedFileInventory.reviewUnapproved(
        pathProvider -> {
          reviewedProviders.add(pathProvider);
          return new FileReviewResult(false);
        });

    assertThat(reviewedProviders).hasSize(1);
    assertThat(reviewedProviders.getFirst().receivedPath()).isEqualTo(receivedFile.normalize());
    assertThat(reviewedProviders.getFirst().approvedPath()).isEqualTo(approvedFile.normalize());
  }

  @Test
  void reviewUnapproved_no_received_files() {
    var reviewedProviders = new ArrayList<PathProvider>();
    ApprovedFileInventory.reviewUnapproved(
        pathProvider -> {
          reviewedProviders.add(pathProvider);
          return new FileReviewResult(false);
        });

    assertThat(reviewedProviders).isEmpty();
  }

  @Test
  void main_approve_all() throws IOException {
    Path receivedFile = tempDir.resolve("MyTest-myMethod-received.txt");
    writeString(receivedFile, "received content", StandardOpenOption.CREATE);
    Path approvedFile = tempDir.resolve("MyTest-myMethod-approved.txt");
    writeString(approvedFile, "old approved content", StandardOpenOption.CREATE);
    writeString(
        inventoryFile,
        "# ApproveJ Approved File Inventory (auto-generated, do not edit)\n"
            + approvedFile
            + " = com.example.MyTest#myMethod\n",
        StandardOpenOption.CREATE);

    ApprovedFileInventory.main(new String[] {"--approve-all"});

    assertThat(receivedFile).doesNotExist();
    assertThat(approvedFile).exists().hasContent("received content");
  }

  @Test
  void main_review_unapproved() throws IOException {
    Path receivedFile = tempDir.resolve("MyTest-myMethod-received.txt");
    writeString(receivedFile, "received content", StandardOpenOption.CREATE);
    Path approvedFile = tempDir.resolve("MyTest-myMethod-approved.txt");
    writeString(approvedFile, "approved content", StandardOpenOption.CREATE);
    writeString(
        inventoryFile,
        "# ApproveJ Approved File Inventory (auto-generated, do not edit)\n"
            + approvedFile
            + " = com.example.MyTest#myMethod\n",
        StandardOpenOption.CREATE);

    ApprovedFileInventory.main(new String[] {"--review-unapproved"});

    assertThat(receivedFile).exists();
    assertThat(approvedFile).exists();
  }
}

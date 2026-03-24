package org.approvej.approve;

import static java.nio.file.Files.readString;
import static java.nio.file.Files.writeString;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import org.approvej.review.FileReviewResult;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ApprovedFileInventoryTest {

  @TempDir private Path tempDir;

  private Path inventoryPath() {
    return tempDir.resolve("inventory.properties");
  }

  @Test
  void loadInventory_non_existent_file() {
    var inventory = ApprovedFileInventory.loadInventory(inventoryPath());

    assertThat(inventory.entries()).isEmpty();
  }

  @Test
  void loadInventory_valid_file() throws IOException {
    Path file = inventoryPath();
    writeString(
        file,
        """
        # ApproveJ Approved File Inventory (auto-generated, do not edit)
        src/test/MyTest-myTest-approved.txt = com.example.MyTest#myTest
        """,
        StandardOpenOption.CREATE);

    var inventory = ApprovedFileInventory.loadInventory(file);

    assertThat(inventory.entries())
        .containsExactly(
            new InventoryEntry(
                Path.of("src/test/MyTest-myTest-approved.txt"), "com.example.MyTest#myTest"));
  }

  @Test
  void findLeftovers() {
    var inventory =
        new ApprovedFileInventory(
            List.of(
                new InventoryEntry(
                    Path.of("src/test/NonExistent-test-approved.txt"),
                    "com.nonexistent.NonExistentTest#test")),
            inventoryPath());

    var leftovers = inventory.findLeftovers();

    assertThat(leftovers).hasSize(1);
    assertThat(leftovers.getFirst().relativePath())
        .isEqualTo(Path.of("src/test/NonExistent-test-approved.txt"));
    assertThat(leftovers.getFirst().testReference())
        .isEqualTo("com.nonexistent.NonExistentTest#test");
  }

  @Test
  void findLeftovers_missing_method() {
    var inventory =
        new ApprovedFileInventory(
            List.of(
                new InventoryEntry(
                    Path.of("src/test/ApprovedFileInventoryTest-nonExistent-approved.txt"),
                    "org.approvej.approve.ApprovedFileInventoryTest#nonExistentMethod")),
            inventoryPath());

    var leftovers = inventory.findLeftovers();

    assertThat(leftovers).hasSize(1);
    assertThat(leftovers.getFirst().testReference())
        .isEqualTo("org.approvej.approve.ApprovedFileInventoryTest#nonExistentMethod");
  }

  @Test
  void findLeftovers_existing_method() {
    var inventory =
        new ApprovedFileInventory(
            List.of(
                new InventoryEntry(
                    Path.of("src/test/ApprovedFileInventoryTest-findLeftovers-approved.txt"),
                    "org.approvej.approve.ApprovedFileInventoryTest#findLeftovers_existing_method")),
            inventoryPath());

    var leftovers = inventory.findLeftovers();

    assertThat(leftovers).isEmpty();
  }

  @Test
  void findLeftovers_existing_method_nested_class_canonical_name() {
    var inventory =
        new ApprovedFileInventory(
            List.of(
                new InventoryEntry(
                    Path.of("src/test/PathProvidersTest.NestedTest-nextToTest-approved.txt"),
                    "org.approvej.approve.PathProvidersTest.NestedTest#nextToTest")),
            inventoryPath());

    var leftovers = inventory.findLeftovers();

    assertThat(leftovers).isEmpty();
  }

  @Test
  void findLeftovers_existing_method_nested_class_binary_name() {
    var inventory =
        new ApprovedFileInventory(
            List.of(
                new InventoryEntry(
                    Path.of("src/test/PathProvidersTest.NestedTest-nextToTest-approved.txt"),
                    "org.approvej.approve.PathProvidersTest$NestedTest#nextToTest")),
            inventoryPath());

    var leftovers = inventory.findLeftovers();

    assertThat(leftovers).isEmpty();
  }

  @Test
  void findLeftovers_existing_method_doubly_nested_class_canonical_name() {
    var inventory =
        new ApprovedFileInventory(
            List.of(
                new InventoryEntry(
                    Path.of(
                        "src/test/PathProvidersTest.NestedTest.DoublyNestedTest"
                            + "-nextToTest-approved.txt"),
                    "org.approvej.approve.PathProvidersTest.NestedTest"
                        + ".DoublyNestedTest#nextToTest")),
            inventoryPath());

    var leftovers = inventory.findLeftovers();

    assertThat(leftovers).isEmpty();
  }

  @Nested
  class ResolveClass {

    @Test
    void top_level_class() throws ClassNotFoundException {
      assertThat(ApprovedFileInventory.resolveClass("org.approvej.approve.ApprovedFileInventory"))
          .isEqualTo(ApprovedFileInventory.class);
    }

    @Test
    void nested_class_canonical_name() throws ClassNotFoundException {
      assertThat(
              ApprovedFileInventory.resolveClass(
                  "org.approvej.approve.PathProvidersTest.NestedTest"))
          .isEqualTo(PathProvidersTest.NestedTest.class);
    }

    @Test
    void nested_class_binary_name() throws ClassNotFoundException {
      assertThat(
              ApprovedFileInventory.resolveClass(
                  "org.approvej.approve.PathProvidersTest$NestedTest"))
          .isEqualTo(PathProvidersTest.NestedTest.class);
    }

    @Test
    void doubly_nested_class_canonical_name() throws ClassNotFoundException {
      assertThat(
              ApprovedFileInventory.resolveClass(
                  "org.approvej.approve.PathProvidersTest.NestedTest.DoublyNestedTest"))
          .isEqualTo(PathProvidersTest.NestedTest.DoublyNestedTest.class);
    }

    @Test
    void non_existent_class() {
      assertThatThrownBy(() -> ApprovedFileInventory.resolveClass("com.nonexistent.NoSuchClass"))
          .isInstanceOf(ClassNotFoundException.class);
    }
  }

  @Test
  void removeLeftovers() throws IOException {
    Path leftoverFile = tempDir.resolve("leftover-approved.txt");
    writeString(leftoverFile, "old content", StandardOpenOption.CREATE);

    Path validFile = tempDir.resolve("valid-approved.txt");

    var inventory =
        new ApprovedFileInventory(
            List.of(
                new InventoryEntry(leftoverFile, "com.nonexistent.NonExistentTest#test"),
                new InventoryEntry(
                    validFile, "org.approvej.approve.ApprovedFileInventoryTest#removeLeftovers")),
            inventoryPath());

    var result = inventory.removeLeftovers();

    assertThat(result.removed()).hasSize(1);
    assertThat(result.failed()).isEmpty();
    assertThat(leftoverFile).doesNotExist();

    var savedInventory = ApprovedFileInventory.loadInventory(inventoryPath());
    assertThat(savedInventory.entries())
        .containsExactly(
            new InventoryEntry(
                validFile, "org.approvej.approve.ApprovedFileInventoryTest#removeLeftovers"))
        .doesNotContain(new InventoryEntry(leftoverFile, "com.nonexistent.NonExistentTest#test"));
  }

  @Test
  void approveAll() throws IOException {
    Path receivedFile = tempDir.resolve("MyTest-myMethod-received.txt");
    writeString(receivedFile, "received content", StandardOpenOption.CREATE);
    Path approvedFile = tempDir.resolve("MyTest-myMethod-approved.txt");
    writeString(approvedFile, "old approved content", StandardOpenOption.CREATE);

    var inventory =
        new ApprovedFileInventory(
            List.of(new InventoryEntry(approvedFile, "com.example.MyTest#myMethod")),
            inventoryPath());

    var result = inventory.approveAll();

    assertThat(result.approved()).containsExactly(approvedFile);
    assertThat(result.failed()).isEmpty();
    assertThat(receivedFile).doesNotExist();
    assertThat(approvedFile).exists().hasContent("received content");
  }

  @Test
  void approveAll_no_received_files() {
    var inventory = new ApprovedFileInventory(List.of(), inventoryPath());

    var result = inventory.approveAll();

    assertThat(result.approved()).isEmpty();
    assertThat(result.failed()).isEmpty();
  }

  @Test
  void approveAll_only_received_file() throws IOException {
    Path receivedFile = tempDir.resolve("MyTest-myMethod-received.txt");
    writeString(receivedFile, "received content", StandardOpenOption.CREATE);
    Path approvedFile = tempDir.resolve("MyTest-myMethod-approved.txt");

    var inventory =
        new ApprovedFileInventory(
            List.of(new InventoryEntry(approvedFile, "com.example.MyTest#myMethod")),
            inventoryPath());

    var result = inventory.approveAll();

    assertThat(result.approved()).containsExactly(approvedFile);
    assertThat(result.failed()).isEmpty();
    assertThat(receivedFile).doesNotExist();
    assertThat(approvedFile).exists().hasContent("received content");
  }

  @Test
  void reviewUnapproved() throws IOException {
    Path receivedFile = tempDir.resolve("MyTest-myMethod-received.txt");
    writeString(receivedFile, "received content", StandardOpenOption.CREATE);
    Path approvedFile = tempDir.resolve("MyTest-myMethod-approved.txt");
    writeString(approvedFile, "approved content", StandardOpenOption.CREATE);

    var inventory =
        new ApprovedFileInventory(
            List.of(new InventoryEntry(approvedFile, "com.example.MyTest#myMethod")),
            inventoryPath());

    var reviewedProviders = new ArrayList<PathProvider>();
    var reviewed =
        inventory.reviewUnapproved(
            pathProvider -> {
              reviewedProviders.add(pathProvider);
              return new FileReviewResult(false);
            });

    assertThat(reviewed).hasSize(1);
    assertThat(reviewedProviders).hasSize(1);
    assertThat(reviewedProviders.getFirst().receivedPath()).isEqualTo(receivedFile.normalize());
    assertThat(reviewedProviders.getFirst().approvedPath()).isEqualTo(approvedFile.normalize());
  }

  @Test
  void reviewUnapproved_no_received_files() {
    var inventory = new ApprovedFileInventory(List.of(), inventoryPath());

    var reviewed = inventory.reviewUnapproved(pathProvider -> new FileReviewResult(false));

    assertThat(reviewed).isEmpty();
  }

  @Test
  void saveInventory_creates_parent_directories() {
    Path nestedPath = tempDir.resolve("nested/dir/inventory.properties");
    var inventory =
        new ApprovedFileInventory(
            List.of(
                new InventoryEntry(
                    Path.of("src/test/MyTest-myTest-approved.txt"), "com.example.MyTest#myTest")),
            nestedPath);

    inventory.saveInventory();

    assertThat(nestedPath).exists();
    var loaded = ApprovedFileInventory.loadInventory(nestedPath);
    assertThat(loaded.entries()).hasSize(1);
  }

  @Test
  void saveInventory_uses_forward_slashes() throws IOException {
    var inventory =
        new ApprovedFileInventory(
            List.of(
                new InventoryEntry(
                    Path.of("src\\test\\MyTest-myTest-approved.txt"), "com.example.MyTest#myTest")),
            inventoryPath());

    inventory.saveInventory();

    var content = readString(inventoryPath());
    assertThat(content).contains("src/test/MyTest-myTest-approved.txt").doesNotContain("\\");
  }

  @Test
  void saveInventory_empty_inventory_deletes_file() throws IOException {
    Path path = inventoryPath();
    writeString(path, "some = content", StandardOpenOption.CREATE);
    assertThat(path).exists();

    var inventory = new ApprovedFileInventory(List.of(), path);

    inventory.saveInventory();

    assertThat(path).doesNotExist();
  }
}

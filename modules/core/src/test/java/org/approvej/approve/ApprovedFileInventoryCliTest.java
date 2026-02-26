package org.approvej.approve;

import static java.nio.file.Files.writeString;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.TreeMap;
import org.approvej.review.FileReviewResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ApprovedFileInventoryCliTest {

  @TempDir private Path tempDir;

  private TreeMap<Path, InventoryEntry> inventory(InventoryEntry... entries) {
    var map = new TreeMap<Path, InventoryEntry>();
    for (InventoryEntry entry : entries) {
      map.put(entry.relativePath(), entry);
    }
    return map;
  }

  @Test
  void findLeftovers() {
    var cli =
        new ApprovedFileInventoryCli(
            inventory(
                new InventoryEntry(
                    Path.of("src/test/NonExistent-test-approved.txt"),
                    "com.nonexistent.NonExistentTest#test")));

    var leftovers = cli.findLeftovers();

    assertThat(leftovers).hasSize(1);
    assertThat(leftovers.getFirst().relativePath())
        .isEqualTo(Path.of("src/test/NonExistent-test-approved.txt"));
    assertThat(leftovers.getFirst().testReference())
        .isEqualTo("com.nonexistent.NonExistentTest#test");
  }

  @Test
  void findLeftovers_missing_method() {
    var cli =
        new ApprovedFileInventoryCli(
            inventory(
                new InventoryEntry(
                    Path.of("src/test/ApprovedFileInventoryCliTest-nonExistent-approved.txt"),
                    "org.approvej.approve.ApprovedFileInventoryCliTest#nonExistentMethod")));

    var leftovers = cli.findLeftovers();

    assertThat(leftovers).hasSize(1);
    assertThat(leftovers.getFirst().testReference())
        .isEqualTo("org.approvej.approve.ApprovedFileInventoryCliTest#nonExistentMethod");
  }

  @Test
  void removeLeftovers() throws IOException {
    Path leftoverFile = tempDir.resolve("leftover-approved.txt");
    writeString(leftoverFile, "old content", StandardOpenOption.CREATE);

    Path validFile = tempDir.resolve("valid-approved.txt");

    var inventoryMap =
        inventory(
            new InventoryEntry(leftoverFile, "com.nonexistent.NonExistentTest#test"),
            new InventoryEntry(
                validFile, "org.approvej.approve.ApprovedFileInventoryCliTest#removeLeftovers"));

    var cli = new ApprovedFileInventoryCli(inventoryMap);

    var result = cli.removeLeftovers();

    assertThat(result.removed()).hasSize(1);
    assertThat(result.failures()).isZero();
    assertThat(leftoverFile).doesNotExist();
    assertThat(inventoryMap).doesNotContainKey(leftoverFile).containsKey(validFile);
  }

  @Test
  void approveAll() throws IOException {
    Path receivedFile = tempDir.resolve("MyTest-myMethod-received.txt");
    writeString(receivedFile, "received content", StandardOpenOption.CREATE);
    Path approvedFile = tempDir.resolve("MyTest-myMethod-approved.txt");
    writeString(approvedFile, "old approved content", StandardOpenOption.CREATE);

    var cli =
        new ApprovedFileInventoryCli(
            inventory(new InventoryEntry(approvedFile, "com.example.MyTest#myMethod")));

    var result = cli.approveAll();

    assertThat(result.approved()).containsExactly(approvedFile);
    assertThat(result.failures()).isZero();
    assertThat(receivedFile).doesNotExist();
    assertThat(approvedFile).exists().hasContent("received content");
  }

  @Test
  void approveAll_no_received_files() {
    var cli = new ApprovedFileInventoryCli(inventory());

    var result = cli.approveAll();

    assertThat(result.approved()).isEmpty();
    assertThat(result.failures()).isZero();
  }

  @Test
  void reviewUnapproved() throws IOException {
    Path receivedFile = tempDir.resolve("MyTest-myMethod-received.txt");
    writeString(receivedFile, "received content", StandardOpenOption.CREATE);
    Path approvedFile = tempDir.resolve("MyTest-myMethod-approved.txt");
    writeString(approvedFile, "approved content", StandardOpenOption.CREATE);

    var cli =
        new ApprovedFileInventoryCli(
            inventory(new InventoryEntry(approvedFile, "com.example.MyTest#myMethod")));

    var reviewedProviders = new ArrayList<PathProvider>();
    cli.reviewUnapproved(
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
    var cli = new ApprovedFileInventoryCli(inventory());

    var reviewedProviders = new ArrayList<PathProvider>();
    cli.reviewUnapproved(
        pathProvider -> {
          reviewedProviders.add(pathProvider);
          return new FileReviewResult(false);
        });

    assertThat(reviewedProviders).isEmpty();
  }
}

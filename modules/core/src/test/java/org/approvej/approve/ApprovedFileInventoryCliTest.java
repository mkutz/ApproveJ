package org.approvej.approve;

import static java.nio.file.Files.writeString;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import org.approvej.approve.ApprovedFileInventoryCli.CliResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ApprovedFileInventoryCliTest {

  @TempDir private Path tempDir;

  private ApprovedFileInventory inventory(InventoryEntry... entries) {
    return new ApprovedFileInventory(List.of(entries), tempDir.resolve("inventory.properties"));
  }

  @Test
  void findLeftovers_no_leftovers() {
    CliResult result = ApprovedFileInventoryCli.findLeftovers(inventory());

    assertThat(result.output()).isEqualTo("No leftover approved files found.");
    assertThat(result.exitCode()).isZero();
  }

  @Test
  void findLeftovers_with_leftovers() {
    CliResult result =
        ApprovedFileInventoryCli.findLeftovers(
            inventory(
                new InventoryEntry(
                    Path.of("src/test/NonExistent-test-approved.txt"),
                    "com.nonexistent.NonExistentTest#test")));

    assertThat(result.output()).startsWith("Leftover approved files:");
    assertThat(result.output()).contains("NonExistent-test-approved.txt");
    assertThat(result.output()).contains("com.nonexistent.NonExistentTest#test");
    assertThat(result.exitCode()).isZero();
  }

  @Test
  void cleanup_no_leftovers() {
    CliResult result = ApprovedFileInventoryCli.cleanup(inventory());

    assertThat(result.output()).isEqualTo("No leftover approved files found.");
    assertThat(result.exitCode()).isZero();
  }

  @Test
  void cleanup_with_leftovers() throws IOException {
    Path leftoverFile = tempDir.resolve("leftover-approved.txt");
    writeString(leftoverFile, "old content", StandardOpenOption.CREATE);

    CliResult result =
        ApprovedFileInventoryCli.cleanup(
            inventory(new InventoryEntry(leftoverFile, "com.nonexistent.NonExistentTest#test")));

    assertThat(result.output()).startsWith("Removed leftover approved files:");
    assertThat(result.output()).contains("leftover-approved.txt");
    assertThat(result.exitCode()).isZero();
  }

  @Test
  void cleanup_with_failed_deletion() throws IOException {
    Path readOnlyDir = tempDir.resolve("readonly");
    Files.createDirectory(readOnlyDir);
    Path leftoverFile = readOnlyDir.resolve("leftover-approved.txt");
    writeString(leftoverFile, "old content", StandardOpenOption.CREATE);
    readOnlyDir.toFile().setWritable(false);

    try {
      CliResult result =
          ApprovedFileInventoryCli.cleanup(
              new ApprovedFileInventory(
                  List.of(new InventoryEntry(leftoverFile, "com.nonexistent.NonExistentTest#test")),
                  tempDir.resolve("inventory.properties")));

      assertThat(result.output())
          .contains("Failed to delete 1 leftover file(s):")
          .contains("leftover-approved.txt");
      assertThat(result.exitCode()).isEqualTo(1);
    } finally {
      readOnlyDir.toFile().setWritable(true);
    }
  }

  @Test
  void approveAll_no_unapproved() {
    CliResult result = ApprovedFileInventoryCli.approveAll(inventory());

    assertThat(result.output()).isEqualTo("No unapproved files found.");
    assertThat(result.exitCode()).isZero();
  }

  @Test
  void approveAll_with_unapproved() throws IOException {
    Path receivedFile = tempDir.resolve("MyTest-myMethod-received.txt");
    writeString(receivedFile, "received content", StandardOpenOption.CREATE);
    Path approvedFile = tempDir.resolve("MyTest-myMethod-approved.txt");
    writeString(approvedFile, "old approved content", StandardOpenOption.CREATE);

    CliResult result =
        ApprovedFileInventoryCli.approveAll(
            inventory(new InventoryEntry(approvedFile, "com.example.MyTest#myMethod")));

    assertThat(result.output()).startsWith("Approved files:");
    assertThat(result.output()).contains("MyTest-myMethod-approved.txt");
    assertThat(result.exitCode()).isZero();
  }

  @Test
  void approveAll_with_failed_approval() throws IOException {
    Path readOnlyDir = tempDir.resolve("readonly");
    Files.createDirectory(readOnlyDir);
    Path receivedFile = readOnlyDir.resolve("MyTest-myMethod-received.txt");
    writeString(receivedFile, "received content", StandardOpenOption.CREATE);
    Path approvedFile = readOnlyDir.resolve("MyTest-myMethod-approved.txt");
    readOnlyDir.toFile().setWritable(false);

    try {
      CliResult result =
          ApprovedFileInventoryCli.approveAll(
              new ApprovedFileInventory(
                  List.of(new InventoryEntry(approvedFile, "com.example.MyTest#myMethod")),
                  tempDir.resolve("inventory.properties")));

      assertThat(result.output())
          .contains("Failed to approve 1 file(s):")
          .contains("MyTest-myMethod-received.txt");
      assertThat(result.exitCode()).isEqualTo(1);
    } finally {
      readOnlyDir.toFile().setWritable(true);
    }
  }

  @Test
  void reviewUnapproved_no_unapproved() {
    CliResult result = ApprovedFileInventoryCli.reviewUnapproved(inventory());

    assertThat(result.output()).isEqualTo("No unapproved files found.");
    assertThat(result.exitCode()).isZero();
  }

  @Test
  void reviewUnapproved_with_unapproved() throws IOException {
    Path receivedFile = tempDir.resolve("MyTest-myMethod-received.txt");
    writeString(receivedFile, "received content", StandardOpenOption.CREATE);
    Path approvedFile = tempDir.resolve("MyTest-myMethod-approved.txt");
    writeString(approvedFile, "approved content", StandardOpenOption.CREATE);

    CliResult result =
        ApprovedFileInventoryCli.reviewUnapproved(
            inventory(new InventoryEntry(approvedFile, "com.example.MyTest#myMethod")));

    assertThat(result.output()).startsWith("Unapproved files:");
    assertThat(result.output()).contains("MyTest-myMethod-received.txt");
    assertThat(result.exitCode()).isZero();
  }
}

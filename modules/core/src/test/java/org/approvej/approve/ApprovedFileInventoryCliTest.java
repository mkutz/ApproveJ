package org.approvej.approve;

import static java.nio.file.Files.writeString;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import org.approvej.approve.ApprovedFileInventoryCli.CliResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ApprovedFileInventoryCliTest {

  @TempDir private Path tempDir;

  private ApprovedFileInventory inventory(InventoryEntry... entries) {
    return new ApprovedFileInventory(List.of(entries));
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
    Path inventoryPath = tempDir.resolve("inventory.properties");
    CliResult result = ApprovedFileInventoryCli.cleanup(inventory(), inventoryPath);

    assertThat(result.output()).isEqualTo("No leftover approved files found.");
    assertThat(result.exitCode()).isZero();
  }

  @Test
  void cleanup_with_leftovers() throws IOException {
    Path leftoverFile = tempDir.resolve("leftover-approved.txt");
    writeString(leftoverFile, "old content", StandardOpenOption.CREATE);

    Path inventoryPath = tempDir.resolve("inventory.properties");
    CliResult result =
        ApprovedFileInventoryCli.cleanup(
            inventory(new InventoryEntry(leftoverFile, "com.nonexistent.NonExistentTest#test")),
            inventoryPath);

    assertThat(result.output()).startsWith("Removed leftover approved files:");
    assertThat(result.output()).contains("leftover-approved.txt");
    assertThat(result.exitCode()).isZero();
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

package org.approvej.review;

import static java.nio.file.Files.readString;

import java.io.IOException;
import org.approvej.ApprovalResult;
import org.approvej.approve.FileApprovalResult;
import org.approvej.approve.PathProvider;

/**
 * A {@link FileReviewer} implementation that executes the given script.
 *
 * @param script the script to be executed with placeholders <code>{@value RECEIVED_PLACEHOLDER}
 *     </code> and <code>{@value APPROVED_PLACEHOLDER}</code>
 */
public record FileReviewerScript(String script) implements FileReviewer {

  private static final String RECEIVED_PLACEHOLDER = "{receivedFile}";
  private static final String APPROVED_PLACEHOLDER = "{approvedFile}";

  @Override
  public ApprovalResult apply(PathProvider pathProvider) {
    try {
      String command =
          script
              .replace(RECEIVED_PLACEHOLDER, pathProvider.receivedPath().toString())
              .replace(APPROVED_PLACEHOLDER, pathProvider.approvedPath().toString());
      new ProcessBuilder(command.split("\\s+")).inheritIO().start().waitFor();

      return new FileApprovalResult(
          readString(pathProvider.receivedPath()),
          readString(pathProvider.approvedPath()),
          pathProvider);
    } catch (IOException | InterruptedException e) {
      throw new ReviewerError("Failed to open %s".formatted(getClass().getSimpleName()), e);
    }
  }
}

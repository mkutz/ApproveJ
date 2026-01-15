package org.approvej.review;

import static java.nio.file.Files.move;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

import java.io.IOException;
import org.jspecify.annotations.NullMarked;

/** Collection of static methods to create {@link FileReviewer} instances. */
@NullMarked
public class Reviewers {

  private Reviewers() {}

  /**
   * A {@link FileReviewer} that executes the given script.
   *
   * @param script the script to be executed with placeholders <code>
   *     {@value FileReviewerScript#RECEIVED_PLACEHOLDER}
   *     </code> and <code>{@value FileReviewerScript#APPROVED_PLACEHOLDER}</code>
   * @return the new {@link FileReviewerScript}
   */
  public static FileReviewer script(String script) {
    return new FileReviewerScript(script);
  }

  /**
   * A {@link FileReviewer} that accepts any given received value, ignoring the previously approved
   * value.
   *
   * <p>This may be a good idea when you have a lot of tests with changed results, and you simply
   * want to update them all at once. You probably want to review the changed approved files before
   * committing them to version control!
   *
   * @return a {@link FileReviewer} that accepts any received value automatically
   */
  public static FileReviewer automatic() {
    return pathProvider -> {
      try {
        move(pathProvider.receivedPath(), pathProvider.approvedPath(), REPLACE_EXISTING);
        return new FileReviewResult(true);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    };
  }
}

package org.approvej.review;

import static java.nio.file.Files.deleteIfExists;
import static java.nio.file.Files.move;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

import java.io.IOException;
import java.nio.file.Path;
import java.util.logging.Logger;
import org.approvej.approve.PathProvider;
import org.jspecify.annotations.NullMarked;

/**
 * A {@link Reviewer} that accepts any given received value, ignoring the previously approved value.
 *
 * <p>This may be a good idea when you have a lot of tests with changed results, and you simply want
 * to update them all at once. You probably want to review the changed approved files before
 * committing them to version control!
 */
@NullMarked
public record AutomaticReviewer() implements Reviewer, ReviewerProvider {

  private static final Logger LOGGER = Logger.getLogger(AutomaticReviewer.class.getName());

  @Override
  public ReviewResult apply(PathProvider pathProvider) {
    Path receivedPath = pathProvider.receivedPath();
    Path approvedPath = pathProvider.approvedPath();
    try {
      move(receivedPath, approvedPath, REPLACE_EXISTING);
      deleteIfExists(pathProvider.diffPath());
      return new ReviewResultRecord(true);
    } catch (IOException e) {
      LOGGER.info(
          "Automatic overwriting approved file at %s with received file at %s failed with exception %s."
              .formatted(approvedPath, receivedPath, e));
    }
    return new ReviewResultRecord(false);
  }

  @Override
  public String alias() {
    return "automatic";
  }

  @Override
  public Reviewer create() {
    return new AutomaticReviewer();
  }
}

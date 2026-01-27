package org.approvej.review;

import static java.nio.file.Files.move;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

import java.io.IOException;
import java.nio.file.Path;
import java.util.logging.Logger;
import org.approvej.approve.PathProvider;
import org.jspecify.annotations.NullMarked;

/**
 * A {@link FileReviewer} that accepts any given received value, ignoring the previously approved
 * value.
 *
 * <p>This may be a good idea when you have a lot of tests with changed results, and you simply want
 * to update them all at once. You probably want to review the changed approved files before
 * committing them to version control!
 */
@NullMarked
public record AutomaticFileReviewer() implements FileReviewer, FileReviewerProvider {

  /** The alias for this reviewer used in configuration. */
  public static final String ALIAS = "automatic";

  private static final Logger LOGGER = Logger.getLogger(AutomaticFileReviewer.class.getName());

  @Override
  public ReviewResult apply(PathProvider pathProvider) {
    Path receivedPath = pathProvider.receivedPath();
    Path approvedPath = pathProvider.approvedPath();
    try {
      move(receivedPath, approvedPath, REPLACE_EXISTING);
      return new FileReviewResult(true);
    } catch (IOException e) {
      LOGGER.info(
          "Automatic overwriting approved file at %s with received file at %s failed with exception %s."
              .formatted(approvedPath, receivedPath, e));
    }
    return new FileReviewResult(false);
  }

  @Override
  public String alias() {
    return ALIAS;
  }

  @Override
  public FileReviewer create() {
    return new AutomaticFileReviewer();
  }
}

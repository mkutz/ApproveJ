package org.approvej.approve;

import static org.approvej.print.PrintFormat.DEFAULT_FILENAME_EXTENSION;

import java.nio.file.Path;
import org.approvej.print.PrintFormat;
import org.jspecify.annotations.NullMarked;

/**
 * A record to provide the paths of the approved and received files.
 *
 * <p>Generally, the constructor should not be used, but one of the {@link PathProviders}
 * initializers.
 *
 * @param directory the {@link Path} containing the {@link #approvedPath()} and the {@link
 *     #receivedPath()}
 * @param baseFilename the first part of the filename used to identify the test class and case the
 *     files belong to
 * @param filenameAffix an addition to the baseFilename to identify multiple files belonging to the
 *     same test case
 * @param approvedLabel a label to identify the approved file – usually {@value APPROVED}, but can
 *     be left empty
 * @param filenameExtension the filename extension for the approved and received file
 */
@NullMarked
public record PathProvider(
    Path directory,
    String baseFilename,
    String filenameAffix,
    String approvedLabel,
    String filenameExtension) {

  /**
   * The infix of the file containing the latest received value that didn't match the previously
   * approved.
   */
  public static final String RECEIVED = "received";

  /** The infix of the file containing a previously approved value. */
  public static final String APPROVED = "approved";

  /**
   * Set the {@link #directory} where the approved and received files are stored.
   *
   * @param directory the {@link Path} where the approved and received files are stored
   * @return a copy of this with the given {@link #directory}
   */
  public PathProvider directory(Path directory) {
    return new PathProvider(
        directory, baseFilename, filenameAffix, approvedLabel, filenameExtension);
  }

  /**
   * Extends the {@link #baseFilename} with the given {@link String}.
   *
   * @param filenameAffix affix to add to the base filename
   * @return a copy of this using the {@link #baseFilename} affixed by the given {@link String}
   */
  public PathProvider filenameAffix(String filenameAffix) {
    return new PathProvider(
        directory, baseFilename, filenameAffix, approvedLabel, filenameExtension);
  }

  /**
   * Creates a new {@link PathProvider} with the current values and the given filenameExtension,
   * <em>unless</em> the currently set {@link #filenameExtension} is not empty and given is the
   * default {@value PrintFormat#DEFAULT_FILENAME_EXTENSION}.
   *
   * @param filenameExtension the filename extension to use
   * @return a copy of this with using the given filenameExtension
   */
  public PathProvider filenameExtension(String filenameExtension) {
    if (!this.filenameExtension.isBlank() && filenameExtension.equals(DEFAULT_FILENAME_EXTENSION)) {
      return this;
    }
    return new PathProvider(
        directory, baseFilename, filenameAffix, approvedLabel, filenameExtension);
  }

  /**
   * Resolves and returns the {@link Path} to the received file in the {@link #directory}, using the
   * {@link #baseFilename}, followed by {@link #approvedLabel} (if any), followed by the {@link
   * #filenameExtension} (if any).
   *
   * @return the absolute and normalized {@link Path} to the approved file
   */
  public Path approvedPath() {
    return directory
        .resolve(
            "%s%s%s%s"
                .formatted(
                    baseFilename,
                    filenameAffix.isBlank() ? "" : "-%s".formatted(filenameAffix),
                    approvedLabel.isBlank() ? "" : "-%s".formatted(approvedLabel),
                    filenameExtension.isBlank() ? "" : ".%s".formatted(filenameExtension)))
        .toAbsolutePath()
        .normalize();
  }

  /**
   * Resolves and returns the {@link Path} to the received file in the {@link #directory}, using the
   * {@link #baseFilename}, followed by {@value RECEIVED} (if any), followed by the {@link
   * #filenameExtension} (if any).
   *
   * @return the absolute and normalized {@link Path} to the received file
   */
  public Path receivedPath() {
    return directory
        .resolve(
            "%s%s%s%s"
                .formatted(
                    baseFilename,
                    filenameAffix.isBlank() ? "" : "-%s".formatted(filenameAffix),
                    "-%s".formatted(RECEIVED),
                    filenameExtension.isBlank() ? "" : ".%s".formatted(filenameExtension)))
        .toAbsolutePath()
        .normalize();
  }

  /**
   * Creates a new {@link PathProvider} that uses the given approved {@link Path}.
   *
   * @param approvedPath the {@link Path} to the approved file
   * @return a new {@link PathProvider}
   * @deprecated use {@link PathProviders#approvedPath(Path)}
   */
  @Deprecated(since = "0.12", forRemoval = true)
  public static PathProvider approvedPath(Path approvedPath) {
    return PathProviders.approvedPath(approvedPath);
  }

  /**
   * Creates a new {@link PathProvider} that uses the given approved path}.
   *
   * @param approvedPathString the path to the approved file
   * @return a new {@link PathProvider}
   * @deprecated use {@link PathProviders#approvedPath(String)}
   */
  @Deprecated(since = "0.12", forRemoval = true)
  public static PathProvider approvedPath(String approvedPathString) {
    return PathProviders.approvedPath(Path.of(approvedPathString));
  }

  /**
   * Creates a {@link PathProvider} that uses a stack trace to determine the paths of the approved
   * and received files.
   *
   * @return a new {@link PathProvider}
   * @deprecated use {@link PathProviders#nextToTest()}
   */
  @Deprecated(since = "0.12", forRemoval = true)
  public static PathProvider nextToTest() {
    return PathProviders.nextToTest();
  }

  /**
   * Creates a {@link PathProvider} that uses a stack trace to determine the paths of the approved
   * and received files in a subdirectory named after the currect test class simple name.
   *
   * @return a new {@link PathProvider}
   * @deprecated use {@link PathProviders#nextToTestInSubdirectory()}
   */
  @Deprecated(since = "0.12", forRemoval = true)
  public static PathProvider nextToTestInSubdirectory() {
    return PathProviders.nextToTestInSubdirectory();
  }
}

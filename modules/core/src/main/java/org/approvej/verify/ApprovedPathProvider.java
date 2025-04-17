package org.approvej.verify;

import java.nio.file.Path;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jspecify.annotations.NullMarked;

/**
 * A {@link PathProvider} that uses the path of a previously approved file to determine the path the
 * received file and the filename extension.
 *
 * <p>For example,
 *
 * <ol>
 *   <li>if the given approved path is {@code /path/to/file-approved.json}, the {@link
 *       #receivedPath} will be {@code /path/to/file-received.json}
 *   <li>if the given approved path is {@code /path/to/file.txt}, the {@link #receivedPath} will be
 *       {@code /path/to/file-received.txt}
 * </ol>
 *
 * <p>Note that the {@code approved} infix is not enforced on the given approved file path. It is
 * also not necessary for the approved file to exist.
 */
@NullMarked
public class ApprovedPathProvider implements PathProvider {

  private static final Pattern FILE_NAME_PATTERN =
      Pattern.compile(
          "(?<baseName>.+?)(?<approved>[-_. ]" + APPROVED + ")?(?:\\.(?<extension>[^.]*))?$");
  private final Path directory;
  private final Path receivedPath;
  private final Path approvedPath;

  /**
   * Creates a new {@link ApprovedPathProvider} that uses the given approved path.
   *
   * @param approvedPath the {@link Path} of the approved file
   */
  ApprovedPathProvider(Path approvedPath) {
    this.approvedPath = approvedPath;
    this.directory = approvedPath.getParent();
    Path parentPath = approvedPath.getParent();
    Matcher matcher = FILE_NAME_PATTERN.matcher(approvedPath.getFileName().toString());
    String baseName =
        matcher.matches() ? matcher.group("baseName") : approvedPath.getFileName().toString();
    String extension =
        matcher.matches() ? Objects.requireNonNullElse(matcher.group("extension"), "txt") : "txt";
    this.receivedPath = parentPath.resolve("%s-%s.%s".formatted(baseName, RECEIVED, extension));
  }

  @Override
  public Path directory() {
    return directory;
  }

  @Override
  public Path receivedPath() {
    return receivedPath;
  }

  @Override
  public Path approvedPath() {
    return approvedPath;
  }
}

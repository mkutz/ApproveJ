package org.approvej.verify;

import java.nio.file.Path;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
public class BasePathProvider implements PathProvider {

  private static final Pattern FILE_NAME_PATTERN =
      Pattern.compile(
          "(?<baseName>.+?)(?<approved>-" + APPROVED + ")?(?:\\.(?<extension>[^.]*))?$");
  private final Path receivedPath;
  private final Path approvedPath;

  /**
   * Creates a new {@link BasePathProvider} that uses the given approved path.
   *
   * @param approvedPath the {@link Path} to the approved file
   * @return a new {@link BasePathProvider}
   */
  public static BasePathProvider approvedPath(Path approvedPath) {
    return new BasePathProvider(approvedPath);
  }

  /**
   * Creates a new {@link BasePathProvider} that uses the given approved path.
   *
   * @param approvedPath the path to the approved file
   * @return a new {@link BasePathProvider}
   */
  public static BasePathProvider approvedPath(String approvedPath) {
    return approvedPath(Path.of(approvedPath));
  }

  private BasePathProvider(Path approvedPath) {
    this.approvedPath = approvedPath;
    Path parentPath = approvedPath.getParent();
    Matcher matcher = FILE_NAME_PATTERN.matcher(approvedPath.getFileName().toString());
    String baseName =
        matcher.matches() ? matcher.group("baseName") : approvedPath.getFileName().toString();
    String extension =
        matcher.matches() ? Objects.requireNonNullElse(matcher.group("extension"), "txt") : "txt";
    this.receivedPath = parentPath.resolve("%s-%s.%s".formatted(baseName, RECEIVED, extension));
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

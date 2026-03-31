package org.approvej.review;

import java.nio.file.Path;
import java.util.function.Function;
import org.approvej.approve.PathProvider;

/**
 * Interface for triggering a review by the user.
 *
 * <p>This usually means that a diff/merge tool is opened, which presents the difference between the
 * received and the previously approved value to users in case they differ.
 */
public interface Reviewer extends Function<PathProvider, ReviewResult> {

  /** Placeholder in commands that is replaced with the received file path. */
  String RECEIVED_PLACEHOLDER = "{receivedFile}";

  /** Placeholder in commands that is replaced with the approved file path. */
  String APPROVED_PLACEHOLDER = "{approvedFile}";

  /**
   * Resolves placeholders in the given command template with the actual file paths.
   *
   * @param commandTemplate the command containing {@value RECEIVED_PLACEHOLDER} and {@value
   *     APPROVED_PLACEHOLDER} placeholders
   * @param pathProvider the path provider to resolve paths from
   * @return the command with placeholders replaced by actual paths
   */
  static String resolveCommand(String commandTemplate, PathProvider pathProvider) {
    return resolveCommand(
        commandTemplate, pathProvider.approvedPath(), pathProvider.receivedPath());
  }

  /**
   * Resolves placeholders in the given command template with the actual file paths.
   *
   * @param commandTemplate the command containing {@value RECEIVED_PLACEHOLDER} and {@value
   *     APPROVED_PLACEHOLDER} placeholders
   * @param approvedPath the approved file path
   * @param receivedPath the received file path
   * @return the command with placeholders replaced by actual paths
   */
  static String resolveCommand(String commandTemplate, Path approvedPath, Path receivedPath) {
    return commandTemplate
        .replace(RECEIVED_PLACEHOLDER, "%s".formatted(receivedPath))
        .replace(APPROVED_PLACEHOLDER, "%s".formatted(approvedPath));
  }
}

package org.approvej;

import static java.util.stream.Collectors.joining;

import java.nio.file.Path;
import java.util.stream.Collectors;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/** An {@link AssertionError} thrown when an approval fails. */
@NullMarked
public class ApprovalError extends AssertionError {

  private static final String DELIMITER = "\n  ";

  private final @Nullable Path approvedPath;
  private final @Nullable Path receivedPath;

  /**
   * Creates an {@link ApprovalError} for when the given received value does not match the
   * previouslyApproved.
   *
   * @param previouslyApproved the previously approved value
   * @param received the received value
   */
  public ApprovalError(String previouslyApproved, String received) {
    this(previouslyApproved, received, null, null);
  }

  /**
   * Creates an {@link ApprovalError} for when the given received value does not match the
   * previouslyApproved, including the paths to the approved and received files.
   *
   * <p>The file paths are included in the error message so that IDEs can make them clickable,
   * enabling seamless navigation to the relevant files.
   *
   * @param previouslyApproved the previously approved value
   * @param received the received value
   * @param approvedPath the {@link Path} to the approved file, or null if not file-based
   * @param receivedPath the {@link Path} to the received file, or null if not file-based
   */
  public ApprovalError(
      String previouslyApproved,
      String received,
      @Nullable Path approvedPath,
      @Nullable Path receivedPath) {
    super(buildMessage(previouslyApproved, received, approvedPath, receivedPath));
    this.approvedPath = approvedPath;
    this.receivedPath = receivedPath;
  }

  private static String buildMessage(
      String previouslyApproved,
      String received,
      @Nullable Path approvedPath,
      @Nullable Path receivedPath) {
    String base =
        previouslyApproved.isEmpty()
            ? "Missing approval for received%n  \"%s\"%n"
                .formatted(received.lines().collect(Collectors.joining(DELIMITER)))
            : "Approval mismatch:%nexpected:%n  \"%s\"%n but was:%n  \"%s\"%n"
                .formatted(
                    previouslyApproved.lines().collect(joining(DELIMITER)),
                    received.lines().collect(joining(DELIMITER)));
    if (approvedPath != null && receivedPath != null) {
      base += "approved: %s%nreceived: %s%n".formatted(approvedPath, receivedPath);
    }
    return base;
  }

  /**
   * Returns the {@link Path} to the approved file, if this error originated from a file-based
   * approval.
   *
   * @return the {@link Path} to the approved file, or null
   */
  public @Nullable Path approvedPath() {
    return approvedPath;
  }

  /**
   * Returns the {@link Path} to the received file, if this error originated from a file-based
   * approval.
   *
   * @return the {@link Path} to the received file, or null
   */
  public @Nullable Path receivedPath() {
    return receivedPath;
  }
}

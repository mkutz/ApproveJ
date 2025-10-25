package org.approvej;

import static java.util.stream.Collectors.joining;

import java.util.stream.Collectors;
import org.jspecify.annotations.NullMarked;

/** An {@link AssertionError} thrown when an approval fails. */
@NullMarked
public class ApprovalError extends AssertionError {

  private static final String DELIMITER = "\n  ";

  /**
   * Creates an {@link ApprovalError} for when the given received value does not match the
   * previouslyApproved.
   *
   * @param previouslyApproved the previously approved value
   * @param received the received value
   */
  public ApprovalError(String previouslyApproved, String received) {
    super(
        previouslyApproved.isEmpty()
            ? "Missing approval for received%n  \"%s\"%n"
                .formatted(received.lines().collect(Collectors.joining(DELIMITER)))
            : "Approval mismatch:%nexpected:%n  \"%s\"%n but was:%n  \"%s\"%n"
                .formatted(
                    previouslyApproved.lines().collect(joining(DELIMITER)),
                    received.lines().collect(joining(DELIMITER))));
  }
}

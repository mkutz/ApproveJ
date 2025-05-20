package org.approvej;

import static java.util.stream.Collectors.joining;

import org.jspecify.annotations.NullMarked;

/** An {@link AssertionError} thrown when an approval fails. */
@NullMarked
public class ApprovalError extends AssertionError {

  /**
   * Creates an {@link ApprovalError} for when the given received value does not match the
   * previouslyApproved.
   *
   * @param received the received value
   * @param previouslyApproved the previously approved value
   */
  public ApprovalError(String received, String previouslyApproved) {
    super(
        "Approval mismatch:%nexpected:%n  \"%s\"%n but was:%n  \"%s\"%n"
            .formatted(
                previouslyApproved.lines().collect(joining("\n  ")),
                received.lines().collect(joining("\n  "))));
  }
}

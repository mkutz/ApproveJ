package org.approvej;

import com.github.difflib.text.DiffRow;
import com.github.difflib.text.DiffRowGenerator;
import java.util.stream.Collectors;
import org.jspecify.annotations.NullMarked;

/** An {@link AssertionError} thrown when an approval fails. */
@NullMarked
public class ApprovalError extends AssertionError {

  private static final DiffRowGenerator diffRowGenerator =
      DiffRowGenerator.create()
          .showInlineDiffs(true)
          .mergeOriginalRevised(true)
          .inlineDiffByWord(true)
          .oldTag(f -> f ? "\u001B[32m" : "\u001B[0m")
          .newTag(f -> f ? "\u001B[34m" : "\u001B[0m")
          .build();

  private static final String shortMessageFormat =
      """
      Approval mismatch: \
      previously approved: <%s>, \
      received: <%s>""";
  private static final String coloredDiffMessageFormat =
      """
      Approval mismatch: \
      previously approved: <\u001B[32m%s\u001B[0m>, \
      received: <\u001B[34m%s\u001B[0m>
      %s""";

  /** The received value */
  private final String received;

  /** The previously approved value */
  private final String previouslyApproved;

  /**
   * Creates an {@link ApprovalError} for when the given received value does not match the
   * previouslyApproved.
   *
   * @param received the received value
   * @param previouslyApproved the previously approved value
   */
  public ApprovalError(String received, String previouslyApproved) {
    super(shortMessageFormat.formatted(previouslyApproved, received));
    this.received = received;
    this.previouslyApproved = previouslyApproved;
  }

  /**
   * Creates and retunes a colored message including a diff.
   *
   * @return a colored message with diff
   */
  public String getColoredDiffMessage() {
    return coloredDiffMessageFormat.formatted(
        previouslyApproved,
        received,
        diffRowGenerator
            .generateDiffRows(previouslyApproved.lines().toList(), received.lines().toList())
            .stream()
            .map(DiffRow::getOldLine)
            .collect(Collectors.joining("\n")));
  }
}

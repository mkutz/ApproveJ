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
          .lineNormalizer(line -> line)
          .showInlineDiffs(true)
          .mergeOriginalRevised(true)
          .inlineDiffByWord(false)
          .oldTag(f -> f ? "\u001B[9;2m" : "\u001B[0m")
          .newTag(f -> f ? "\u001B[1m" : "\u001B[0m")
          .build();

  private static final String shortMessageFormat =
      """
      Approval mismatch: \
      previously approved: <%s>, \
      received: <%s>""";
  private static final String coloredDiffMessageFormat =
      """
      Approval mismatch: \
      previously approved: <%s>, \
      received: <%s>

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
    super(
        coloredDiffMessageFormat.formatted(
            previouslyApproved,
            received,
            diffRowGenerator
                .generateDiffRows(previouslyApproved.lines().toList(), received.lines().toList())
                .stream()
                .map(DiffRow::getOldLine)
                .collect(Collectors.joining("\n"))));
    this.received = received;
    this.previouslyApproved = previouslyApproved;
  }
}

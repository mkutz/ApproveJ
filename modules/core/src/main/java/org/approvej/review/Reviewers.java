package org.approvej.review;

import org.jspecify.annotations.NullMarked;

/** Collection of static methods to create {@link FileReviewer} instances. */
@NullMarked
public class Reviewers {

  private static final FileReviewer NONE = pathProvider -> new FileReviewResult(false);
  private static final AutomaticFileReviewer AUTOMATIC = new AutomaticFileReviewer();

  private Reviewers() {}

  public static FileReviewer none() {
    return NONE;
  }

  /**
   * A {@link FileReviewer} that executes the given script.
   *
   * @param script the script to be executed with placeholders <code>
   *     {@value ScriptFileReviewer#RECEIVED_PLACEHOLDER}
   *     </code> and <code>{@value ScriptFileReviewer#APPROVED_PLACEHOLDER}</code>
   * @return the new {@link ScriptFileReviewer}
   */
  public static FileReviewer script(String script) {
    return new ScriptFileReviewer(script);
  }

  /**
   * A {@link FileReviewer} that accepts any given received value, ignoring the previously approved
   * value.
   *
   * <p>This may be a good idea when you have a lot of tests with changed results, and you simply
   * want to update them all at once. You probably want to review the changed approved files before
   * committing them to version control!
   *
   * @return a {@link FileReviewer} that accepts any received value automatically
   */
  public static FileReviewer automatic() {
    return AUTOMATIC;
  }
}

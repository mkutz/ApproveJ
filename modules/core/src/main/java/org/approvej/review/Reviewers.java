package org.approvej.review;

import org.jspecify.annotations.NullMarked;

/** Collection of static methods to create {@link FileReviewer} instances. */
@NullMarked
public class Reviewers {

  private static final NoneFileReviewer NONE = new NoneFileReviewer();
  private static final AutomaticFileReviewer AUTOMATIC = new AutomaticFileReviewer();

  private Reviewers() {}

  /**
   * A {@link FileReviewer} that does nothing and never triggers a reapproval.
   *
   * @return a {@link FileReviewer} that does nothing
   */
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
   * A {@link FileReviewer} that calls an AI CLI tool to review the difference between the received
   * and approved files.
   *
   * <p>For text files, a unified diff is included in the prompt. For image files, the file paths
   * are passed so the AI CLI can read them directly. If the AI responds with "YES", the received
   * file is automatically approved.
   *
   * @param command the AI CLI command to execute (e.g., "claude", "gemini")
   * @return the new {@link AiFileReviewer}
   */
  public static FileReviewer ai(String command) {
    return new AiFileReviewer(command);
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

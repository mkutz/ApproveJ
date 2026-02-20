package org.approvej.review.console;

import org.jspecify.annotations.NullMarked;

/** Abstraction for writing output to a terminal and querying its capabilities. */
@NullMarked
interface Terminal {

  /**
   * Prints the given text to the terminal.
   *
   * @param text the text to print
   */
  void print(String text);

  /**
   * Returns whether this terminal supports ANSI color codes.
   *
   * @return {@code true} if ANSI colors can be used
   */
  boolean supportsColor();

  /** Creates a terminal that writes to {@code System.out} and respects the {@code NO_COLOR} env. */
  static Terminal system() {
    return new Terminal() {
      @Override
      public void print(String text) {
        System.out.print(text);
      }

      @Override
      public boolean supportsColor() {
        return System.getenv("NO_COLOR") == null;
      }
    };
  }
}

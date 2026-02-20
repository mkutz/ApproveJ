package org.approvej.review.console;

import org.jspecify.annotations.NullMarked;

/**
 * An in-memory {@link Terminal} for testing.
 *
 * <p>Captures all output in a {@link StringBuilder}.
 */
@NullMarked
final class NullableTerminal implements Terminal {

  private final StringBuilder output = new StringBuilder();
  private final boolean supportsColor;

  NullableTerminal() {
    this(false);
  }

  NullableTerminal(boolean supportsColor) {
    this.supportsColor = supportsColor;
  }

  @Override
  public void print(String text) {
    output.append(text);
  }

  @Override
  public boolean supportsColor() {
    return supportsColor;
  }

  /**
   * Returns all output that was printed to this terminal.
   *
   * @return the captured output
   */
  String output() {
    return output.toString();
  }
}

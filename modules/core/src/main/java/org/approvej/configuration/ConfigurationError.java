package org.approvej.configuration;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * An error that occurs when there is an issue with loading the configuration.
 *
 * @see Configuration
 */
@NullMarked
public class ConfigurationError extends RuntimeException {

  /**
   * Creates a new {@link ConfigurationError} with the given message and cause.
   *
   * @param message a message describing the error
   * @param cause the cause of the error
   */
  public ConfigurationError(String message, @Nullable Throwable cause) {
    super(message, cause);
  }
}

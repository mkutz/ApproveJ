package org.approvej.configuration;

/**
 * An error that occurs when there is an issue with loading the configuration.
 *
 * @see Configuration
 */
public class ConfigurationError extends RuntimeException {

  /**
   * Creates a new {@link ConfigurationError} with the given message and cause.
   *
   * @param message a message describing the error
   * @param cause the cause of the error
   */
  public ConfigurationError(String message, Throwable cause) {
    super(message, cause);
  }
}

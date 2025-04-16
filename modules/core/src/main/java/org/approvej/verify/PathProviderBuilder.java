package org.approvej.verify;

/** A builder for {@link PathProvider} instances. */
public interface PathProviderBuilder {

  /**
   * Builds the {@link PathProvider} with the current configuration.
   *
   * @return a new {@link PathProvider} with the current configuration
   */
  PathProvider build();
}

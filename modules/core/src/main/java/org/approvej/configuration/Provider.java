package org.approvej.configuration;

import org.jspecify.annotations.NullMarked;

/**
 * Service Provider Interface (SPI) for registering configurable components.
 *
 * <p>Implementations of this interface can be registered via {@code
 * META-INF/services/org.approvej.configuration.Provider} and will be automatically discovered by
 * the {@link Registry}.
 *
 * <p>This interface is typically implemented directly by the component class itself, allowing the
 * component to serve as its own provider. Use specialized sub-interfaces like {@code
 * PrintFormatProvider} or {@code FileReviewerProvider} which provide a default implementation for
 * {@link #type()}.
 *
 * @param <T> the type of component this provider creates
 */
@NullMarked
public interface Provider<T> {

  /**
   * Returns the alias that can be used to reference this provider in configuration.
   *
   * <p>For example, "json" or "yaml" for print formats, or "none" or "automatic" for reviewers.
   *
   * @return the alias for this provider
   */
  String alias();

  /**
   * Creates a new instance of the component.
   *
   * <p>When implemented by the component class itself, this typically returns a new instance of the
   * same class via its no-argument constructor.
   *
   * @return a new instance of the component
   */
  T create();

  /**
   * Returns the type of component this provider creates.
   *
   * <p>Specialized sub-interfaces like {@code PrintFormatProvider} and {@code FileReviewerProvider}
   * provide default implementations for this method.
   *
   * @return the component type class
   */
  Class<? super T> type();
}

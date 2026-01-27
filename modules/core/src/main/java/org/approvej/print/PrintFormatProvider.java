package org.approvej.print;

import org.approvej.configuration.Provider;
import org.jspecify.annotations.NullMarked;

/**
 * Specialized {@link Provider} for {@link PrintFormat} implementations.
 *
 * <p>This interface pre-defines the {@link #type()} method, so implementations only need to provide
 * {@link #alias()}.
 *
 * @param <T> the type of value the print format handles
 */
@NullMarked
public interface PrintFormatProvider<T> extends Provider<PrintFormat<T>> {

  @Override
  default Class<PrintFormat<T>> type() {
    @SuppressWarnings("unchecked")
    Class<PrintFormat<T>> type = (Class<PrintFormat<T>>) (Class<?>) PrintFormat.class;
    return type;
  }
}

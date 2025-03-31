package org.approvej.scrub;

import java.util.function.UnaryOperator;

/**
 * An {@link UnaryOperator} that scrubs certain information from a value. This might be useful
 * especially for dynamic data like timestamps, dates or generally random values.
 *
 * @param <T> the type of value to scrub
 */
public interface Scrubber<T> extends UnaryOperator<T> {}

package org.approvej.print;

import java.util.function.Function;

/**
 * A {@link Function} that converts an object to a {@link String}.
 *
 * @param <T> the type of the object to print
 */
public interface Printer<T> extends Function<T, String> {}

package org.approvej;

import java.util.function.Function;

/**
 * A function that converts an object to a string.
 *
 * @param <T> the type of the object to print
 */
public interface Printer<T> extends Function<T, String> {}

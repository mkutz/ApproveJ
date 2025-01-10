package org.approvej;

import java.util.function.Function;

public interface Printer<T> extends Function<T, String> {}

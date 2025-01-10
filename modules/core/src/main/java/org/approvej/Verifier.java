package org.approvej;

import java.util.function.Consumer;

/**
 * A {@link Consumer} that verifies a printed value and (optionally) stores the value in some
 * fashion if approved for a later execution.
 */
public interface Verifier extends Consumer<String> {}

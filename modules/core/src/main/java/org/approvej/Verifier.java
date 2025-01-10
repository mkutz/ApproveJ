package org.approvej;

import java.util.function.Consumer;

/** A {@link Consumer} that verifies a printed value. */
public interface Verifier extends Consumer<String> {}

package org.approvej.approve;

import java.util.function.Consumer;

/**
 * A {@link Consumer} that approved a printed value and (optionally) stores the value in some
 * fashion if approved for a later execution.
 */
public interface Approver extends Consumer<String> {}

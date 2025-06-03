package org.approvej.approve;

import java.util.function.Consumer;
import java.util.function.Function;
import org.approvej.ApprovalResult;

/**
 * A {@link Consumer} that approved a printed value and (optionally) stores the value in some
 * fashion if approved for a later execution.
 */
public interface Approver extends Function<String, ApprovalResult> {}

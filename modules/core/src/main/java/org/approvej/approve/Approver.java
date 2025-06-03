package org.approvej.approve;

import java.util.function.Consumer;
import java.util.function.Function;
import org.approvej.ApprovalResult;

/**
 * A {@link Consumer} that approved a printed value and (optionally) stores the value in some
 * fashion if approved for a later execution.
 */
public interface Approver extends Consumer<String>, Function<String, ApprovalResult> {

  /**
   * Approves a received value against a previously approved value.
   *
   * @param received the received value
   * @throws org.approvej.ApprovalError if the received value was not approved
   * @deprecated use {@link #apply(Object)} to get an {@link ApprovalResult} and (optionally) call
   *     {@link ApprovalResult#throwIfNotApproved()} to achieve the same outcome.
   */
  @Override
  @Deprecated(since = "0.8.6", forRemoval = true)
  default void accept(String received) {
    apply(received).throwIfNotApproved();
  }
}

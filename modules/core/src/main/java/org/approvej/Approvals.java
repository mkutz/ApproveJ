package org.approvej;

public class Approvals {

  private Approvals() {}

  public static <T> ApprovalBuilder<T> approve(T value) {
    return new ApprovalBuilder<>(value);
  }
}

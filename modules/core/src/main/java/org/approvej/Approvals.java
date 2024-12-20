package org.approvej;

public class Approvals {

  private Approvals() {}

  public static ApprovalBuilder approve(String value) {
    return new ApprovalBuilder(value);
  }
}

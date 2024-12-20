package org.approvej;

public class ApprovalBuilder {

  private final String originalValue;
  private String scrubbedValue;

  public ApprovalBuilder(String originalValue) {
    this.originalValue = originalValue.trim();
    this.scrubbedValue = this.originalValue;
  }

  public ApprovalBuilder scrubbedWith(Scrubber scrubber) {
    this.scrubbedValue = scrubber.apply(scrubbedValue).trim();
    return this;
  }

  public void verify(String previouslyApprovedValue) {
    if (!scrubbedValue.trim().equals(previouslyApprovedValue.trim())) {
      throw new AssertionError(
          "Approval mismatch: expected: <%s> but was: <%s>"
              .formatted(previouslyApprovedValue.trim(), scrubbedValue.trim()));
    }
  }
}

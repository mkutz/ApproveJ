package org.approvej.verify;

import static org.approvej.verify.Verifiers.value;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatNoException;

import org.approvej.ApprovalError;
import org.junit.jupiter.api.Test;

class InplaceVerifierTest {

  @Test
  void accept() {
    String previouslyApproved = "Some text";
    InplaceVerifier inplaceVerifier = value(previouslyApproved);

    assertThatNoException().isThrownBy(() -> inplaceVerifier.accept(previouslyApproved));
  }

  @Test
  void accept_previously_approved_differs() {
    String previouslyApproved = "Some other text";
    InplaceVerifier inplaceVerifier = value(previouslyApproved);

    assertThatExceptionOfType(ApprovalError.class)
        .isThrownBy(() -> inplaceVerifier.accept("Some text"));
  }
}

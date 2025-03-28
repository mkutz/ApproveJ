package org.approvej.verify;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatNoException;

import org.approvej.ApprovalError;
import org.junit.jupiter.api.Test;

class InplaceVerifierTest {

  @Test
  void accept() {
    String previouslyApproved = "Some text";
    InplaceVerifier inplaceVerifier = new InplaceVerifier(previouslyApproved);

    assertThatNoException().isThrownBy(() -> inplaceVerifier.accept(previouslyApproved));
  }

  @Test
  void accept_previously_approved_differs() {
    String previouslyApproved = "Some other text";
    InplaceVerifier inplaceVerifier = new InplaceVerifier(previouslyApproved);

    assertThatExceptionOfType(ApprovalError.class)
        .isThrownBy(() -> inplaceVerifier.accept("Some text"));
  }
}

package org.approvej;

import static org.approvej.ApprovalBuilder.approve;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatNoException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DanglingApprovalTrackerTest {

  @BeforeEach
  void setUp() {
    DanglingApprovalTracker.reset();
  }

  @AfterEach
  void tearDown() {
    DanglingApprovalTracker.reset();
  }

  @Test
  void checkAndReset() {
    approve("Some text");

    assertThatExceptionOfType(DanglingApprovalError.class)
        .isThrownBy(DanglingApprovalTracker::checkAndReset)
        .withMessageContaining("Dangling approval detected");
  }

  @Test
  void checkAndReset_multiple_named() {
    approve("First").named("first");
    approve("Second").named("second");

    assertThatExceptionOfType(DanglingApprovalError.class)
        .isThrownBy(DanglingApprovalTracker::checkAndReset)
        .withMessageContaining("Dangling approval detected");
  }

  @Test
  void checkAndReset_concluded() {
    approve("Some text").byValue("Some text");

    assertThatNoException().isThrownBy(DanglingApprovalTracker::checkAndReset);
  }

  @Test
  void reset() {
    approve("Some text");
    DanglingApprovalTracker.reset();

    assertThatNoException().isThrownBy(DanglingApprovalTracker::checkAndReset);
  }
}

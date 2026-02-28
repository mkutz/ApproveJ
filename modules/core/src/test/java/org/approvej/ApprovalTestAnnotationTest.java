package org.approvej;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

class ApprovalTestAnnotationTest {

  @Test
  void annotation_includes_ExtendWith() {
    ExtendWith extendWith = ApprovalTest.class.getAnnotation(ExtendWith.class);

    assertThat(extendWith).isNotNull();
    assertThat(extendWith.value()).containsExactly(DanglingApprovalExtension.class);
  }
}

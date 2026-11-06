package org.approvej.spring;

import static org.approvej.ApprovalBuilder.approve;
import static org.approvej.spring.SpringTestContextsPrintFormat.contextConfigurations;

import org.junit.jupiter.api.Test;

class SpringTestContextsPrintFormatTest {

  private final SpringTestContexts springTestContexts =
      SpringTestContexts.springTestContexts("org.approvej.spring.fixtures");

  @Test
  void printer() {
    approve(springTestContexts).printedAs(contextConfigurations()).byFile();
  }

  @Test
  void printer_with_test_classes() {
    approve(springTestContexts).printedAs(contextConfigurations().withTestClasses()).byFile();
  }
}

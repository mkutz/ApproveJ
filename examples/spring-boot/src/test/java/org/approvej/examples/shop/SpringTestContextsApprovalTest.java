package org.approvej.examples.shop;

import static org.approvej.ApprovalBuilder.approve;
import static org.approvej.spring.SpringTestContexts.springTestContexts;
import static org.approvej.spring.SpringTestContextsPrintFormat.contextConfigurations;

import org.junit.jupiter.api.Test;

class SpringTestContextsApprovalTest {

  @Test
  void spring_test_contexts() {
    // tag::spring_test_contexts[]
    approve(springTestContexts("org.approvej.examples")) // <1>
        .printedAs(contextConfigurations()) // <2>
        .byFile();
    // end::spring_test_contexts[]
  }

  @Test
  void spring_test_contexts_with_test_classes() {
    // tag::spring_test_contexts_with_test_classes[]
    approve(springTestContexts("org.approvej.examples"))
        .printedAs(contextConfigurations().withTestClasses())
        .byFile();
    // end::spring_test_contexts_with_test_classes[]
  }
}

package org.approvej.spring.fixtures.inherited.sub;

import org.approvej.spring.fixtures.inherited.PackagePrivateTestTemplate;
import org.springframework.context.ApplicationContext;

class ShadowingBootTest extends PackagePrivateTestTemplate {

  void context(ApplicationContext context) {
    // Intentionally empty: shadows the signature of the superclass's package-private test method
    // from another package without overriding it, so JUnit still runs the inherited test.
  }
}

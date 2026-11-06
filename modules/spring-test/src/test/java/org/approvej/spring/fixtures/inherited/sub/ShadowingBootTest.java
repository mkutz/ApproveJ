package org.approvej.spring.fixtures.inherited.sub;

import org.approvej.spring.fixtures.inherited.PackagePrivateTestTemplate;
import org.springframework.context.ApplicationContext;

class ShadowingBootTest extends PackagePrivateTestTemplate {

  /** Does not override the package-private test method of the other package's superclass. */
  void context(ApplicationContext context) {}
}

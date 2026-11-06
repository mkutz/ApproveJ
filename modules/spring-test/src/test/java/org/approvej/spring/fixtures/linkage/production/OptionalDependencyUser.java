package org.approvej.spring.fixtures.linkage.production;

import org.approvej.spring.fixtures.linkage.OptionalDependency;

public class OptionalDependencyUser {

  public String use(OptionalDependency dependency) {
    return dependency.toString();
  }
}

package org.approvej.approve;

import org.jspecify.annotations.NullMarked;

/** Collection of static methods to create {@link Approver} instances. */
@NullMarked
public class Approvers {

  private Approvers() {}

  /**
   * Creates a {@link Approver} using the given previouslyApproved value.
   *
   * @param previouslyApproved the approved value
   * @return a new {@link InplaceApprover} for the given previouslyApproved value.
   */
  public static InplaceApprover value(String previouslyApproved) {
    return new InplaceApprover(previouslyApproved);
  }

  /**
   * Creates a new {@link Approver} that uses the given {@link PathProvider} to determine the paths
   * of approved and received files.
   *
   * @param pathProvider the provider for the paths of the approved and received files
   * @return a new {@link FileApprover} that uses the given {@link PathProvider}
   */
  public static Approver file(PathProvider pathProvider) {
    return new FileApprover(pathProvider);
  }
}

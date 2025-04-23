package org.approvej.verify;

import static org.approvej.verify.PathProviders.approvedPath;
import static org.approvej.verify.PathProviders.nextToTest;

import java.nio.file.Path;
import org.jspecify.annotations.NullMarked;

/** Collection of static methods to create {@link Verifier} instances. */
@NullMarked
public class Verifiers {

  private Verifiers() {}

  /**
   * Creates a {@link Verifier} using the given previouslyApproved value.
   *
   * @param previouslyApproved the approved value
   * @return a new {@link InplaceVerifier} for the given previouslyApproved value.
   */
  public static InplaceVerifier value(String previouslyApproved) {
    return new InplaceVerifier(previouslyApproved);
  }

  /**
   * Creates a new {@link Verifier} that uses the given {@link PathProvider} to determine the paths
   * of approved and received files.
   *
   * @param pathProvider the provider for the paths of the approved and received files
   * @return a new {@link FileVerifier} that uses the given {@link PathProvider}
   */
  public static FileVerifier file(PathProvider pathProvider) {
    return new FileVerifier(pathProvider);
  }

  /**
   * Builds the given {@link PathProviderBuilder} and uses it to create a new {@link FileVerifier}.
   *
   * @param pathProviderBuilder the {@link PathProviderBuilder}
   * @return a new {@link FileVerifier} that uses the given {@link PathProvider}
   * @see #file(PathProviderBuilder)
   */
  public static FileVerifier file(PathProviderBuilder pathProviderBuilder) {
    return file(pathProviderBuilder.build());
  }

  /**
   * Creates a new {@link Verifier} that uses the stack trace to determine the paths of approved and
   * received files.
   *
   * @return a new {@link FileVerifier} that uses a {@link NextToTestPathProvider} to determine the
   *     paths
   */
  public static FileVerifier file() {
    return file(nextToTest());
  }

  /**
   * Creates a new {@link Verifier} that uses the given {@link Path} to the approved file to
   * determine the {@link Path} to the received file.
   *
   * @param approvedPath the {@link Path} to the approved file
   * @return a new {@link FileVerifier} that uses an {@link ApprovedPathProvider}
   * @see ApprovedPathProvider
   */
  public static FileVerifier file(Path approvedPath) {
    return file(approvedPath(approvedPath));
  }

  /**
   * Creates a new {@link Verifier} that uses the given approvedPath to the approved file to
   * determine the {@link Path} to the received file.
   *
   * @param approvedPath the path to the approved file
   * @return a new {@link FileVerifier} that uses an {@link ApprovedPathProvider}
   * @see ApprovedPathProvider
   */
  public static FileVerifier file(String approvedPath) {
    return file(Path.of(approvedPath));
  }
}

package org.approvej.approve;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.joining;

import org.jspecify.annotations.NullMarked;

@NullMarked
class TestMethodNotFoundInStackTraceError extends RuntimeException {

  public TestMethodNotFoundInStackTraceError(StackTraceElement[] stackTrace) {
    super(
        "Could not locate test method in stack trace:%n%s"
            .formatted(stream(stackTrace).map(StackTraceElement::toString).collect(joining("\n"))));
  }
}

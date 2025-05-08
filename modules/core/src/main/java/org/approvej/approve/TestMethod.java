package org.approvej.approve;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import org.jspecify.annotations.NullMarked;

@NullMarked
public interface TestMethod {

  /**
   * Tries to create a {@link TestMethod} from the given {@link Method}.
   *
   * @param method the potential test {@link Method}
   * @return an {@link Optional} containing the {@link TestMethod} if the {@link Method} is a test
   *     case.
   */
  static Optional<TestMethod> create(Method method) {
    return Stream.<TestMethod>of(
            new JUnitTestMethod(method), new TestNGTestMethod(method), new SpockTestMethod(method))
        .filter(testMethod -> !testMethod.testAnnotations().isEmpty())
        .findFirst();
  }

  /**
   * Returns the {@link Method} of the test case.
   *
   * @return the {@link Method} of the test case
   */
  Method method();

  /**
   * Returns the name of the test case.
   *
   * @return the name of test case
   */
  String testCaseName();

  /**
   * Returns the {@link Class} of the {@link TestMethod}.
   *
   * @return the {@link Class} of the {@link TestMethod}
   */
  Class<?> testClass();

  /**
   * Returns all test related {@link Annotation}s of the {@link TestMethod}.
   *
   * @return all test related {@link Annotation}s of the {@link TestMethod}
   */
  List<Annotation> testAnnotations();

  /**
   * {@link TestMethod} implementation for JUnit/JUnit5.
   *
   * @param method the potential test {@link Method}
   */
  record JUnitTestMethod(Method method) implements TestMethod {

    @Override
    public String testCaseName() {
      return method.getName();
    }

    @Override
    public Class<?> testClass() {
      return method.getDeclaringClass();
    }

    @Override
    public List<Annotation> testAnnotations() {
      return Arrays.stream(method.getDeclaredAnnotations())
          .filter(annotation -> annotation.annotationType().getName().startsWith("org.junit."))
          .toList();
    }
  }

  /**
   * {@link TestMethod} implementation for TestNG.
   *
   * @param method the potential test {@link Method}
   */
  record TestNGTestMethod(Method method) implements TestMethod {

    @Override
    public String testCaseName() {
      return method.getName();
    }

    @Override
    public Class<?> testClass() {
      return method.getDeclaringClass();
    }

    @Override
    public List<Annotation> testAnnotations() {
      return Arrays.stream(method.getDeclaredAnnotations())
          .filter(
              annotation ->
                  annotation.annotationType().getName().startsWith("org.testng.annotations."))
          .toList();
    }
  }

  /**
   * {@link TestMethod} implementation for Spock.
   *
   * @param method the potential test {@link Method}
   */
  record SpockTestMethod(Method method) implements TestMethod {

    @Override
    public String testCaseName() {
      return testAnnotations().stream()
          .filter(
              annotation ->
                  annotation
                      .annotationType()
                      .getName()
                      .equals("org.spockframework.runtime.model.FeatureMetadata"))
          .findFirst()
          .map(
              annotation -> {
                try {
                  return annotation
                      .annotationType()
                      .getMethod("name")
                      .invoke(annotation)
                      .toString();
                } catch (IllegalAccessException
                    | InvocationTargetException
                    | NoSuchMethodException e) {
                  return null;
                }
              })
          .orElse(method.getName());
    }

    @Override
    public Class<?> testClass() {
      return method.getDeclaringClass();
    }

    @Override
    public List<Annotation> testAnnotations() {
      return Arrays.stream(method.getDeclaredAnnotations())
          .filter(
              annotation -> annotation.annotationType().getName().startsWith("org.spockframework."))
          .toList();
    }
  }
}

package org.approvej.spring;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.joining;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import org.approvej.print.PrintFormat;
import org.approvej.print.Printer;
import org.jspecify.annotations.NullMarked;

/**
 * {@link PrintFormat} for {@link SpringTestContexts} that prints each distinct context with all
 * details relevant to Spring's context caching.
 *
 * <p>The contexts are sorted by their printed form, so adding or removing test classes does not
 * reorder the output. For example:
 *
 * <pre>
 * - loader: SpringBootContextLoader
 *   classes:
 *     - com.example.Application
 *   profiles:
 *     - test
 *   customizers:
 *     - &#64;MockitoBean com.example.PaymentClient field=paymentClient
 *     - &#64;SpringBootTest(webEnvironment=MOCK)
 * </pre>
 *
 * <p>Some details of unknown context customizers cannot be printed. If two distinct contexts are
 * printed identically, both are marked with a note.
 *
 * @param includeTestClasses whether to list the test classes using each context
 */
@NullMarked
public record SpringTestContextsPrintFormat(boolean includeTestClasses)
    implements PrintFormat<SpringTestContexts> {

  /**
   * Creates a new {@link SpringTestContextsPrintFormat} that does not list the test classes, so
   * adding a test class that uses an existing context does not change the output.
   *
   * @return a new {@link SpringTestContextsPrintFormat}
   */
  public static SpringTestContextsPrintFormat contextConfigurations() {
    return new SpringTestContextsPrintFormat(false);
  }

  /**
   * Returns a copy of this that also lists the test classes using each context.
   *
   * <p>Note that this changes the output whenever a test class is added, removed, or renamed.
   *
   * @return a copy of this including the test classes
   */
  public SpringTestContextsPrintFormat withTestClasses() {
    return new SpringTestContextsPrintFormat(true);
  }

  @Override
  public Printer<SpringTestContexts> printer() {
    return springTestContexts -> {
      List<PrintedContext> printedContexts =
          springTestContexts.contexts().stream()
              .map(
                  context ->
                      new PrintedContext(
                          ContextConfigurationPrinter.print(context.configuration()),
                          context.testClasses()))
              .sorted(Comparator.comparing(PrintedContext::configuration))
              .toList();
      Map<String, Long> occurrences =
          printedContexts.stream()
              .map(PrintedContext::configuration)
              .collect(groupingBy(identity(), counting()));
      return printedContexts.stream()
          .map(
              printedContext ->
                  print(printedContext, occurrences.get(printedContext.configuration()) > 1))
          .collect(joining("\n\n"));
    };
  }

  private String print(PrintedContext printedContext, boolean ambiguous) {
    List<String> lines = new ArrayList<>(printedContext.lines());
    if (ambiguous) {
      lines.add("note: differs from an identically printed context in details not printed");
    }
    if (includeTestClasses) {
      lines.add("test classes:");
      printedContext.testClasses().forEach(testClass -> lines.add("  - " + testClass.getName()));
    }
    return "- " + String.join("\n  ", lines);
  }

  private record PrintedContext(List<String> lines, List<Class<?>> testClasses) {

    String configuration() {
      return String.join("\n", lines);
    }
  }
}

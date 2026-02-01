package org.approvej.http;

import static java.lang.String.join;

import org.approvej.print.PrintFormat;
import org.approvej.print.Printer;
import org.jspecify.annotations.NullMarked;

/**
 * {@link Printer} implementation for {@link ReceivedHttpRequest} that prints the request data as an
 * <a href="https://www.jetbrains.com/help/idea/exploring-http-syntax.html">HTTP request file</a>.
 *
 * <p>For example, a POST request to http://localhost:54321/something with the body {@code
 * {"some":"value"}} would be printed like this
 *
 * <pre>
 * POST /something
 * Connection: Upgrade, HTTP2-Settings
 * Content-length: 16
 * Content-type: application/json
 * Host: http://localhost:54321
 * Http2-settings: AAEAAEAAAAIAAAAAAAMAAAAAAAQBAAAAAAUAAEAAAAYABgAA
 * Upgrade: h2c
 * User-agent: Java-http-client/21.0.7
 *
 * {"some":"value"}
 * </pre>
 */
@NullMarked
public class ReceivedHttpRequestPrintFormat implements PrintFormat<ReceivedHttpRequest> {

  /** Default constructor. */
  public ReceivedHttpRequestPrintFormat() {
    // No initialization needed
  }

  @Override
  public Printer<ReceivedHttpRequest> printer() {
    return (ReceivedHttpRequest request) -> {
      StringBuilder sb = new StringBuilder();
      sb.append("%s %s".formatted(request.method(), request.uri()));
      request
          .headers()
          .forEach((key, value) -> sb.append("%n%s: %s".formatted(key, join(", ", value))));

      if (!request.body().isBlank()) {
        sb.append("%n%n%s".formatted(request.body()));
      }
      return sb.toString();
    };
  }

  @Override
  public String filenameExtension() {
    return "http";
  }

  /**
   * Creates and returns a new {@link ReceivedHttpRequestPrintFormat} instance.
   *
   * @return the new instance
   */
  public static ReceivedHttpRequestPrintFormat httpRequest() {
    return new ReceivedHttpRequestPrintFormat();
  }
}

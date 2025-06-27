package org.approvej.http;

import static java.lang.String.join;

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
public class HttpRequestPrinter implements Printer<ReceivedHttpRequest> {

  /**
   * Creates a and returns new {@link HttpRequestPrinter} instance.
   *
   * @return the new instance
   */
  public static HttpRequestPrinter requestPrinter() {
    return new HttpRequestPrinter();
  }

  /**
   * Creates a new {@link HttpRequestPrinter} instance.
   *
   * <p>This constructor is public to allow instantiation via reflection, e.g. in the {@link
   * org.approvej.Configuration} class.
   */
  public HttpRequestPrinter() {}

  @Override
  public String apply(ReceivedHttpRequest request) {
    StringBuilder sb = new StringBuilder();
    sb.append("%s %s".formatted(request.method(), request.uri()));
    request
        .headers()
        .forEach((key, value) -> sb.append("%n%s: %s".formatted(key, join(", ", value))));

    if (!request.body().isBlank()) {
      sb.append("%n%n%s".formatted(request.body()));
    }
    return sb.toString();
  }

  @Override
  public String filenameExtension() {
    return "http";
  }
}

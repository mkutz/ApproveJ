package org.approvej.http.wiremock;

import com.github.tomakehurst.wiremock.http.HttpHeaders;
import com.github.tomakehurst.wiremock.http.Request;
import java.net.URI;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import org.approvej.http.ReceivedHttpRequest;
import org.jspecify.annotations.NullMarked;

/**
 * Utility class for converting WireMock's {@link Request} to ApproveJ's {@link
 * ReceivedHttpRequest}.
 *
 * <p>The {@link Request} interface is implemented by {@link
 * com.github.tomakehurst.wiremock.verification.LoggedRequest}, which is returned by {@code
 * ServeEvent.getRequest()}.
 *
 * <p>Example usage:
 *
 * <pre>{@code
 * import static org.approvej.http.wiremock.WireMockRequests.toReceivedHttpRequest;
 * import static org.approvej.ApprovalBuilder.approve;
 *
 * // Convert a single request
 * Request request = wireMockServer.getAllServeEvents().getFirst().getRequest();
 * approve(toReceivedHttpRequest(request)).byFile();
 *
 * // Convert multiple requests
 * wireMockServer.getAllServeEvents().stream()
 *     .map(event -> toReceivedHttpRequest(event.getRequest()))
 *     .forEach(request -> approve(request).byFile());
 * }</pre>
 *
 * <p><strong>Note on ordering:</strong> WireMock's {@code getAllServeEvents()} returns events in
 * reverse chronological order (most recent first). If you need chronological order, reverse the
 * list.
 */
@NullMarked
public final class WireMockRequests {

  private WireMockRequests() {
    // Utility class - prevent instantiation
  }

  /**
   * Converts a WireMock {@link Request} to an ApproveJ {@link ReceivedHttpRequest}.
   *
   * <p>The conversion maps:
   *
   * <ul>
   *   <li>{@code Request.getMethod()} to {@code ReceivedHttpRequest.method()}
   *   <li>{@code Request.getUrl()} to {@code ReceivedHttpRequest.uri()}
   *   <li>{@code Request.getHeaders()} to {@code ReceivedHttpRequest.headers()}
   *   <li>{@code Request.getBodyAsString()} to {@code ReceivedHttpRequest.body()}
   * </ul>
   *
   * @param request the WireMock {@link Request} to convert (e.g., {@link
   *     com.github.tomakehurst.wiremock.verification.LoggedRequest})
   * @return a new {@link ReceivedHttpRequest} with the converted data
   */
  public static ReceivedHttpRequest toReceivedHttpRequest(Request request) {
    return new ReceivedHttpRequest(
        request.getMethod().getName(),
        URI.create(request.getUrl()),
        convertHeaders(request.getHeaders()),
        request.getBodyAsString());
  }

  private static SortedMap<String, List<String>> convertHeaders(HttpHeaders wireMockHeaders) {
    SortedMap<String, List<String>> headers = new TreeMap<>();
    wireMockHeaders.all().forEach(header -> headers.put(header.key(), header.values()));
    return headers;
  }
}

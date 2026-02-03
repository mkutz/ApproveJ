package org.approvej.http;

import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import org.approvej.scrub.Replacement;
import org.approvej.scrub.Scrubber;
import org.jspecify.annotations.NullMarked;

/**
 * A Scrubber for {@link ReceivedHttpRequest}s that replaces a specific header's value.
 *
 * <p>By default, a header "MyHeaderName" will be replaced with "{{MyHeaderName}}". This can be
 * changed via {@link #replacement(Replacement)}.
 *
 * @param headerName the name of the header to be scrubbed
 * @param replacement the replacement function for the header value
 */
@NullMarked
record HttpHeaderValueScrubber(String headerName, Replacement<String> replacement)
    implements Scrubber<HttpHeaderValueScrubber, ReceivedHttpRequest, String> {

  /**
   * Creates a {@link Scrubber} for the given headerName with the default replacement.
   *
   * @param headerName the name of the header to be scrubbed
   */
  public HttpHeaderValueScrubber(String headerName) {
    this(headerName, (match, count) -> "{{%s}}".formatted(match));
  }

  @Override
  public ReceivedHttpRequest apply(ReceivedHttpRequest request) {
    SortedMap<String, List<String>> newHeaders = new TreeMap<>(request.headers());
    newHeaders.put(headerName, List.of(replacement.apply(headerName, 1)));
    return new ReceivedHttpRequest(request.method(), request.uri(), newHeaders, request.body());
  }

  @Override
  public HttpHeaderValueScrubber replacement(Replacement<String> replacement) {
    return new HttpHeaderValueScrubber(headerName, replacement);
  }
}

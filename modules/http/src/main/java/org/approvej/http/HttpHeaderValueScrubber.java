package org.approvej.http;

import java.util.List;
import org.approvej.scrub.Replacement;
import org.approvej.scrub.Scrubber;
import org.jspecify.annotations.NullMarked;

/** A Scrubber for {@link ReceivedHttpRequest}s that allows to scrub a specific header's value. */
@NullMarked
public class HttpHeaderValueScrubber
    implements Scrubber<HttpHeaderValueScrubber, ReceivedHttpRequest, String> {

  private final String headerName;
  private Replacement<String> replacement;

  /**
   * Creates a {@link Scrubber} for the given headerName.
   *
   * <p>By default, a header "MyHeaderName" will be replaced with "{{MyHeaderName}}". This can be
   * changed via {@link #replacement(Replacement)}
   *
   * @param headerName the name of the Header to be scrubbed
   */
  HttpHeaderValueScrubber(String headerName) {
    this.headerName = headerName;
    this.replacement = (match, count) -> "{{%s}}".formatted(match);
  }

  @Override
  public ReceivedHttpRequest apply(ReceivedHttpRequest request) {
    request.headers().put(headerName, List.of(replacement.apply(headerName, 1)));
    return request;
  }

  @Override
  public HttpHeaderValueScrubber replacement(Replacement<String> replacement) {
    this.replacement = replacement;
    return this;
  }
}

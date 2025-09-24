package org.approvej.http;

import java.util.List;
import java.util.function.UnaryOperator;
import org.approvej.scrub.Scrubber;
import org.jspecify.annotations.NullMarked;

/** A Scrubber for {@link ReceivedHttpRequest}s that allows to scrub a specific header's value. */
@NullMarked
public class HttpHeaderValueScrubber implements Scrubber<ReceivedHttpRequest> {

  private final String headerName;
  private UnaryOperator<String> replacement;

  /**
   * Creates a {@link Scrubber} for the given headerName.
   *
   * <p>By default, a header "MyHeaderName" will be replaced with "{{MyHeaderName}}". This can be
   * changed via {@link #replacement(String)} or {@link #replacement(UnaryOperator)}
   *
   * @param headerName the name of the Header to be scrubbed
   */
  HttpHeaderValueScrubber(String headerName) {
    this.headerName = headerName;
    this.replacement = "{{%s}}"::formatted;
  }

  @Override
  public ReceivedHttpRequest apply(ReceivedHttpRequest request) {
    request.headers().get(headerName);
    request.headers().put(headerName, List.of(replacement.apply(headerName)));
    return request;
  }

  /**
   * Changes the replacement of the header's value with the given static {@link String}.
   *
   * @param staticReplacement a {@link String} to replace the header's value
   * @return this
   */
  public HttpHeaderValueScrubber replacement(String staticReplacement) {
    this.replacement = headerName -> staticReplacement;
    return this;
  }

  /**
   * Changes the replacement of the header's value with the given {@link UnaryOperator} which
   * receives the {@link #headerName} as parameter.
   *
   * @param replacement a {@link UnaryOperator} to replace the header's value
   * @return this
   */
  public HttpHeaderValueScrubber replacement(UnaryOperator<String> replacement) {
    this.replacement = replacement;
    return this;
  }
}

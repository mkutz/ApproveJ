package org.approvej.http;

import org.jspecify.annotations.NullMarked;

/** Collection of predefined HTTP-related {@link org.approvej.scrub.Scrubber}s. */
@NullMarked
public class HttpScrubbers {

  private HttpScrubbers() {}

  /**
   * Creates a {@link HttpHeaderValueScrubber} for the given headerName.
   *
   * @param headerName the name of the header to be scrubbed.
   * @return the new {@link HttpHeaderValueScrubber}
   */
  public static HttpHeaderValueScrubber header(String headerName) {
    return new HttpHeaderValueScrubber(headerName);
  }

  /**
   * Creates a {@link HttpHeaderValueScrubber} for the {@code Host} header.
   *
   * @return the new {@link HttpHeaderValueScrubber}
   */
  public static HttpHeaderValueScrubber hostHeader() {
    return header("Host");
  }
}

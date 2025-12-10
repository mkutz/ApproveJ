package org.approvej.http;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import org.jspecify.annotations.NullMarked;

/**
 * A {@link StubbedHttpResponse} defines the response that will be returned by the {@link
 * HttpStubServer}.
 *
 * <p>Please use the {@link #response()} initializer to create this with a fluent builder API.
 *
 * @param statusCode status code that will be used in the response
 * @param body body that will be used for the response
 * @param headers headers that will be set in the response
 */
@NullMarked
public record StubbedHttpResponse(int statusCode, String body, Map<String, List<String>> headers) {

  /**
   * Creates a new {@link Builder}
   *
   * @return a new {@link Builder}
   */
  public static Builder response() {
    return new Builder();
  }

  /** A fluent API builder for {@link StubbedHttpResponse}. */
  public static class Builder {

    private String body = "";

    private final SortedMap<String, List<String>> headers = new TreeMap<>();

    private Builder() {}

    /**
     * Sets the body for the response.
     *
     * @param body that will be used for the response
     * @return this
     */
    public Builder body(String body) {
      this.body = body;
      return this;
    }

    /**
     * Adds a header that will be set in the response.
     *
     * @param name header name
     * @param value header value or values
     * @return this
     */
    public Builder header(String name, String... value) {
      headers.computeIfAbsent(name, key -> new ArrayList<>()).addAll(List.of(value));
      return this;
    }

    /**
     * Sets the status code for the response and builds the {@link StubbedHttpResponse}.
     *
     * @param statusCode status code that will be used in the response
     * @return the built {@link StubbedHttpResponse}
     */
    public StubbedHttpResponse statusCode(int statusCode) {
      return new StubbedHttpResponse(statusCode, body, headers);
    }
  }
}

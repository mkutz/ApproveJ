package org.approvej.http;

import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class ReceivedHttpRequestBuilder {

  private String method = "GET";
  private URI uri = URI.create("http://localhost");
  private SortedMap<String, List<String>> headers = new TreeMap<>();
  private String body = "";

  public static ReceivedHttpRequestBuilder aReceivedHttpRequest() {
    return new ReceivedHttpRequestBuilder();
  }

  private ReceivedHttpRequestBuilder() {}

  public ReceivedHttpRequestBuilder method(String method) {
    this.method = method;
    return this;
  }

  public ReceivedHttpRequestBuilder uri(URI uri) {
    this.uri = uri;
    return this;
  }

  public ReceivedHttpRequestBuilder headers(SortedMap<String, List<String>> headers) {
    this.headers = headers;
    return this;
  }

  public ReceivedHttpRequestBuilder body(String body) {
    this.body = body;
    return this;
  }

  public ReceivedHttpRequest build() {
    return new ReceivedHttpRequest(method, uri, headers, body);
  }

  public ReceivedHttpRequestBuilder header(String name, String... values) {
    headers.put(name, Arrays.asList(values));
    return this;
  }
}

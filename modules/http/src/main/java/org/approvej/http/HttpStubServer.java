package org.approvej.http;

import static org.approvej.http.StubbedHttpResponse.response;

import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import org.jspecify.annotations.NullMarked;

/**
 * A simple stub for an HTTP server. It wraps a {@link HttpServer} bound to a random port on
 * localhost and simply handles each received request by storing it as a {@link ReceivedHttpRequest}
 * object that can then be approved.
 *
 * <p>If your use case requires the stubbed service to return a specific response, you can specify
 * {@link #nextResponse}, giving the desired {@link StubbedHttpResponse}. By default, the server
 * will respond with status 200 and the body "OK".
 */
@NullMarked
public class HttpStubServer implements AutoCloseable {

  private final HttpServer server;
  private final String address;
  private final List<ReceivedHttpRequest> receivedRequests = new ArrayList<>();
  private StubbedHttpResponse nextResponse = response().body("OK").statusCode(200);

  /**
   * Creates and starts the server.
   *
   * <p>As this is an {@link AutoCloseable}, you might want to make sure it gets closed after your
   * tests are done. For example, by using JUnit's <a
   * href="https://docs.junit.org/current/user-guide/#writing-tests-built-in-extensions-AutoClose">{@code @AutoClose}
   * extension</a>.
   */
  public HttpStubServer() {
    try {
      server = HttpServer.create(new InetSocketAddress(0), 0);
      server.start();
      address = "http://localhost:%d".formatted(server.getAddress().getPort());
      server.createContext(
          "/",
          exchange -> {
            receivedRequests.add(
                new ReceivedHttpRequest(
                    exchange.getRequestMethod(),
                    exchange.getRequestURI(),
                    new TreeMap<>(exchange.getRequestHeaders()),
                    new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8)));

            exchange.getResponseHeaders().putAll(nextResponse.headers());
            exchange.sendResponseHeaders(nextResponse.statusCode(), nextResponse.body().length());
            try (OutputStream responseBodyOutputStream = exchange.getResponseBody()) {
              responseBodyOutputStream.write(nextResponse.body().getBytes());
            }
            exchange.close();
          });
    } catch (IOException e) {
      throw new HttpStubServerException(e);
    }
  }

  /**
   * Returns the server's current base address, e.g. {@code http://localhost:54321}.
   *
   * @return the server's current base address
   */
  public String address() {
    return address;
  }

  /**
   * Returns the {@link List} of {@link ReceivedHttpRequest} in the order they were received since
   * the server was started, or until the last call of {@link #resetReceivedRequests()}.
   *
   * @return the {@link ReceivedHttpRequest}s
   */
  public List<ReceivedHttpRequest> receivedRequests() {
    return receivedRequests;
  }

  /**
   * Returns the last (latest) {@link ReceivedHttpRequest}.
   *
   * @return the last (latest) {@link ReceivedHttpRequest}.
   */
  public ReceivedHttpRequest lastReceivedRequest() {
    return receivedRequests.getLast();
  }

  /**
   * Sets the given {@link StubbedHttpResponse} as the next response of this.
   *
   * @param nextResponse a {@link StubbedHttpResponse}
   * @return this
   */
  public HttpStubServer nextResponse(StubbedHttpResponse nextResponse) {
    this.nextResponse = nextResponse;
    return this;
  }

  /**
   * Resets the {@link #receivedRequests} to an empty list. You probably want to do this before each
   * test case.
   *
   * @return this
   */
  public HttpStubServer resetReceivedRequests() {
    receivedRequests.clear();
    return this;
  }

  /** Stops the wrapped {@link HttpServer} immediately. */
  @Override
  public void close() {
    server.stop(0);
  }
}

package org.approvej.http;

import static java.net.http.HttpRequest.BodyPublishers.ofString;
import static java.net.http.HttpResponse.BodyHandlers.ofString;
import static org.approvej.http.StubbedHttpResponse.response;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import org.assertj.core.api.Condition;
import org.junit.jupiter.api.AutoClose;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class HttpStubServerTest {

  @AutoClose private static final HttpStubServer server = new HttpStubServer();

  @AutoClose private static final HttpClient client = HttpClient.newHttpClient();

  Condition<URI> ephemeralPort =
      new Condition<>(uri -> uri.getPort() > 32768, "has an ephemeral port");

  @BeforeEach
  void resetServer() {
    server.resetReceivedRequests();
  }

  @Test
  void address() {
    String address = server.address();
    assertThat(URI.create(address)).hasHost("localhost").has(ephemeralPort);
  }

  @Test
  void receivedRequests() throws IOException, InterruptedException {
    assertThat(server.receivedRequests()).isEmpty();

    String path = "/api/hello";
    client.send(
        HttpRequest.newBuilder(URI.create(server.address()).resolve(path))
            .GET()
            .header("Accept", "application/json")
            .build(),
        ofString());
    client.send(
        HttpRequest.newBuilder(URI.create(server.address()).resolve(path))
            .POST(ofString("World"))
            .header("Accept", "application/json")
            .build(),
        ofString());

    assertThat(server.receivedRequests()).hasSize(2);
    ReceivedHttpRequest getRequest = server.receivedRequests().getFirst();
    assertThat(getRequest.method()).isEqualTo("GET");
    assertThat(getRequest.uri()).isEqualTo(URI.create(path));
    assertThat(getRequest.headers()).containsEntry("Accept", List.of("application/json"));
    assertThat(getRequest.body()).isEmpty();
    ReceivedHttpRequest postRequest = server.receivedRequests().getLast();
    assertThat(postRequest.method()).isEqualTo("POST");
    assertThat(postRequest.uri()).isEqualTo(URI.create(path));
    assertThat(postRequest.headers()).containsEntry("Accept", List.of("application/json"));
    assertThat(postRequest.body()).isEqualTo("World");
  }

  @Test
  void resetReceivedRequests() throws IOException, InterruptedException {
    client.send(
        HttpRequest.newBuilder(URI.create(server.address()).resolve("/api/hello"))
            .GET()
            .header("Accept", "application/json")
            .build(),
        ofString());
    assertThat(server.receivedRequests()).isNotEmpty();

    server.resetReceivedRequests();

    assertThat(server.receivedRequests()).isEmpty();
  }

  @Test
  void nextResponse() throws IOException, InterruptedException {
    server.nextResponse(
        response().body("I'm a testpot").header("Content-Type", "text/plain").statusCode(418));

    HttpResponse<String> response =
        client.send(
            HttpRequest.newBuilder(URI.create(server.address()).resolve("/api/hello"))
                .GET()
                .header("Accept", "application/json")
                .build(),
            ofString());

    assertThat(response.statusCode()).isEqualTo(418);
    assertThat(response.body()).isEqualTo("I'm a testpot");
    assertThat(response.headers().firstValue("Content-Type"))
        .isPresent()
        .get()
        .isEqualTo("text/plain");
  }
}

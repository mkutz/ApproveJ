package org.approvej.http.wiremock;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.verification.LoggedRequest;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import org.approvej.http.ReceivedHttpRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class WireMockRequestsTest {

  private WireMockServer wireMockServer;
  private HttpClient httpClient;

  @BeforeEach
  void setUp() {
    wireMockServer = new WireMockServer(0);
    wireMockServer.start();
    wireMockServer.stubFor(any(anyUrl()).willReturn(ok("OK")));
    httpClient = HttpClient.newHttpClient();
  }

  @AfterEach
  void tearDown() {
    wireMockServer.stop();
  }

  @Test
  void toReceivedHttpRequest_get() throws IOException, InterruptedException {
    httpClient.send(
        HttpRequest.newBuilder(URI.create(wireMockServer.baseUrl() + "/api/hello"))
            .GET()
            .header("Accept", "application/json")
            .build(),
        HttpResponse.BodyHandlers.ofString());

    LoggedRequest loggedRequest = wireMockServer.getAllServeEvents().getFirst().getRequest();
    ReceivedHttpRequest request = WireMockRequests.toReceivedHttpRequest(loggedRequest);

    assertThat(request.method()).isEqualTo("GET");
    assertThat(request.uri()).isEqualTo(URI.create("/api/hello"));
    assertThat(request.headers()).containsKey("Accept");
    assertThat(request.headers().get("Accept")).contains("application/json");
    assertThat(request.body()).isEmpty();
  }

  @Test
  void toReceivedHttpRequest_post() throws IOException, InterruptedException {
    httpClient.send(
        HttpRequest.newBuilder(URI.create(wireMockServer.baseUrl() + "/api/users"))
            .POST(HttpRequest.BodyPublishers.ofString("{\"name\":\"John\"}"))
            .header("Content-Type", "application/json")
            .build(),
        HttpResponse.BodyHandlers.ofString());

    LoggedRequest loggedRequest = wireMockServer.getAllServeEvents().getFirst().getRequest();
    ReceivedHttpRequest request = WireMockRequests.toReceivedHttpRequest(loggedRequest);

    assertThat(request.method()).isEqualTo("POST");
    assertThat(request.uri()).isEqualTo(URI.create("/api/users"));
    assertThat(request.body()).isEqualTo("{\"name\":\"John\"}");
  }

  @Test
  void toReceivedHttpRequest_multiple_requests() throws IOException, InterruptedException {
    httpClient.send(
        HttpRequest.newBuilder(URI.create(wireMockServer.baseUrl() + "/first")).GET().build(),
        HttpResponse.BodyHandlers.ofString());
    httpClient.send(
        HttpRequest.newBuilder(URI.create(wireMockServer.baseUrl() + "/second")).GET().build(),
        HttpResponse.BodyHandlers.ofString());

    List<ReceivedHttpRequest> requests =
        wireMockServer.getAllServeEvents().stream()
            .map(event -> WireMockRequests.toReceivedHttpRequest(event.getRequest()))
            .toList();

    assertThat(requests).hasSize(2);
    // WireMock returns events in reverse order (most recent first)
    assertThat(requests.get(0).uri()).isEqualTo(URI.create("/second"));
    assertThat(requests.get(1).uri()).isEqualTo(URI.create("/first"));
  }

  @Test
  void toReceivedHttpRequests_headers_are_sorted() throws IOException, InterruptedException {
    httpClient.send(
        HttpRequest.newBuilder(URI.create(wireMockServer.baseUrl() + "/api/hello"))
            .GET()
            .header("Z-Header", "last")
            .header("A-Header", "first")
            .build(),
        HttpResponse.BodyHandlers.ofString());

    LoggedRequest loggedRequest = wireMockServer.getAllServeEvents().getFirst().getRequest();
    ReceivedHttpRequest request = WireMockRequests.toReceivedHttpRequest(loggedRequest);

    // Headers should be in a SortedMap (alphabetically sorted)
    assertThat(request.headers().firstKey()).isEqualTo("A-Header");
  }

  @Test
  void toReceivedHttpRequests_query_is_preserved() throws IOException, InterruptedException {
    httpClient.send(
        HttpRequest.newBuilder(URI.create(wireMockServer.baseUrl() + "/api/search?q=test&limit=10"))
            .GET()
            .build(),
        HttpResponse.BodyHandlers.ofString());

    LoggedRequest loggedRequest = wireMockServer.getAllServeEvents().getFirst().getRequest();
    ReceivedHttpRequest request = WireMockRequests.toReceivedHttpRequest(loggedRequest);

    assertThat(request.uri()).isEqualTo(URI.create("/api/search?q=test&limit=10"));
  }
}

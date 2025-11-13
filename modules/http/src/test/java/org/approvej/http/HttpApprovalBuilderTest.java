package org.approvej.http;

import static java.net.http.HttpRequest.BodyPublishers.ofString;
import static java.net.http.HttpRequest.newBuilder;
import static java.net.http.HttpResponse.BodyHandlers.discarding;
import static java.net.http.HttpResponse.BodyHandlers.ofString;
import static org.approvej.ApprovalBuilder.approve;
import static org.approvej.http.HttpScrubbers.hostHeaderValue;
import static org.approvej.http.ReceivedHttpRequestPrintFormat.httpRequest;
import static org.approvej.http.StubbedHttpResponse.response;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import org.junit.jupiter.api.AutoClose;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class HttpApprovalBuilderTest {

  @AutoClose private static final HttpStubServer server = new HttpStubServer();

  @AutoClose private static final HttpClient client = HttpClient.newHttpClient();

  @BeforeEach
  void cleanUpRequests() {
    server.receivedRequests().clear();
  }

  @Test
  void approve_get() throws IOException, InterruptedException {
    client.send(
        newBuilder(URI.create(server.address()).resolve("/something"))
            .GET()
            .header("X-my-header", "some header value")
            .build(),
        discarding());

    approve(server.lastReceivedRequest())
        .scrubbedOf(hostHeaderValue())
        .printedAs(httpRequest())
        .byFile();
  }

  @Test
  void approve_post() throws IOException, InterruptedException {
    client.send(
        newBuilder(URI.create(server.address()).resolve("/something"))
            .POST(ofString("{\"some\":\"value\"}"))
            .header("Content-Type", "application/json")
            .build(),
        discarding());

    approve(server.lastReceivedRequest())
        .scrubbedOf(hostHeaderValue())
        .printedAs(httpRequest())
        .byFile();
  }

  @Test
  void approve_custom_response() throws IOException, InterruptedException {
    server.nextResponse(
        response()
            .header("My-Response-Header", "some header value")
            .body("My response body")
            .statusCode(201));

    HttpResponse<String> response =
        client.send(
            newBuilder(URI.create(server.address()).resolve("/something"))
                .POST(ofString("{\"some\":\"value\"}"))
                .header("Content-Type", "application/json")
                .build(),
            ofString());

    assertThat(response.statusCode()).isEqualTo(201);
    assertThat(response.body()).isEqualTo("My response body");
    assertThat(response.headers().firstValue("My-Response-Header"))
        .isPresent()
        .get()
        .isEqualTo("some header value");
  }
}

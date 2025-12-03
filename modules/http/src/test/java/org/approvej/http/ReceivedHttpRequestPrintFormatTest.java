package org.approvej.http;

import static java.net.http.HttpRequest.newBuilder;
import static java.net.http.HttpResponse.BodyHandlers.ofString;
import static org.approvej.http.ReceivedHttpRequestPrintFormat.httpRequest;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import org.junit.jupiter.api.Test;

class ReceivedHttpRequestPrintFormatTest {

  @Test
  void printer_get() throws IOException, InterruptedException {
    try (HttpStubServer server = new HttpStubServer()) {
      try (HttpClient client = HttpClient.newHttpClient()) {
        client.send(
            newBuilder()
                .GET()
                .uri(URI.create(server.address()).resolve("/something"))
                .header("My-header", "some value")
                .build(),
            ofString());

        assertThat(httpRequest().printer().apply(server.receivedRequests().getFirst()))
            .startsWith("GET /something")
            .contains("My-header", "some value");
      }
    }
  }
}

package org.approvej.http;

import static java.net.http.HttpRequest.newBuilder;
import static java.net.http.HttpResponse.BodyHandlers.ofString;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import org.junit.jupiter.api.Test;

class HttpRequestPrinterTest {

  HttpRequestPrinter printer = new HttpRequestPrinter();

  @Test
  public void apply_get() throws IOException, InterruptedException {
    try (HttpStubServer server = new HttpStubServer()) {
      try (HttpClient client = HttpClient.newHttpClient()) {
        client.send(
            newBuilder()
                .GET()
                .uri(URI.create(server.address()).resolve("/something"))
                .header("My-header", "some value")
                .build(),
            ofString());

        assertThat(printer.apply(server.receivedRequests().getFirst()))
            .startsWith("GET /something")
            .contains("My-header", "some value");
      }
    }
  }
}

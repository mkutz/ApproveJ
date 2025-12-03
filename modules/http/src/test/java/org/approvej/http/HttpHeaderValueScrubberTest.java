package org.approvej.http;

import static org.approvej.http.ReceivedHttpRequestBuilder.aReceivedHttpRequest;
import static org.approvej.scrub.Replacements.numbered;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class HttpHeaderValueScrubberTest {

  @Test
  void apply() {
    ReceivedHttpRequest unscrubbedRequest =
        aReceivedHttpRequest()
            .header("My-header", UUID.randomUUID().toString(), UUID.randomUUID().toString())
            .header("Another-header", "some value", "another value")
            .build();

    ReceivedHttpRequest scrubbedRequest =
        HttpScrubbers.headerValue("My-header").apply(unscrubbedRequest);

    assertThat(scrubbedRequest.headers()).containsEntry("My-header", List.of("{{My-header}}"));
    assertThat(scrubbedRequest.headers())
        .containsEntry("Another-header", List.of("some value", "another value"));
  }

  @Test
  void apply_host() {
    ReceivedHttpRequest scrubbedRequest =
        HttpScrubbers.hostHeaderValue()
            .apply(aReceivedHttpRequest().header("Host", UUID.randomUUID().toString()).build());

    assertThat(scrubbedRequest.headers()).containsEntry("Host", List.of("{{Host}}"));
  }

  @Test
  void apply_user_agent() {
    ReceivedHttpRequest scrubbedRequest =
        HttpScrubbers.userAgentHeaderValue()
            .apply(
                aReceivedHttpRequest().header("User-agent", UUID.randomUUID().toString()).build());

    assertThat(scrubbedRequest.headers()).containsEntry("User-agent", List.of("{{User-agent}}"));
  }

  @Test
  void replacement() {
    ReceivedHttpRequest unscrubbedRequest =
        aReceivedHttpRequest().header("My-header", UUID.randomUUID().toString()).build();

    ReceivedHttpRequest scrubbedRequest =
        HttpScrubbers.headerValue("My-header")
            .replacement(numbered("my header"))
            .apply(unscrubbedRequest);

    assertThat(scrubbedRequest.headers()).containsEntry("My-header", List.of("[my header 1]"));
  }

  @Test
  void replacement_static() {
    ReceivedHttpRequest unscrubbedRequest =
        aReceivedHttpRequest().header("My-header", UUID.randomUUID().toString()).build();

    ReceivedHttpRequest scrubbedRequest =
        HttpScrubbers.headerValue("My-header").replacement("[my header]").apply(unscrubbedRequest);

    assertThat(scrubbedRequest.headers()).containsEntry("My-header", List.of("[my header]"));
  }
}

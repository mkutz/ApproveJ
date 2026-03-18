package org.approvej.examples.shop.payment;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class PaymentGateway {

  private final RestClient restClient;

  public PaymentGateway(
      @Value("${payment.gateway.url}") String baseUrl,
      @Value("${payment.gateway.token}") String token) {
    this.restClient =
        RestClient.builder()
            .baseUrl(baseUrl)
            .defaultHeader("Authorization", "Bearer " + token)
            .build();
  }

  public PaymentResponse charge(PaymentRequest request) {
    return restClient
        .post()
        .uri("/v1/charges")
        .contentType(MediaType.APPLICATION_JSON)
        .body(request)
        .retrieve()
        .body(PaymentResponse.class);
  }
}

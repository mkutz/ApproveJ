package org.approvej.examples.shop.payment;

import static org.approvej.ApprovalBuilder.approve;
import static org.approvej.http.HttpScrubbers.headerValue;
import static org.approvej.http.HttpScrubbers.hostHeaderValue;
import static org.approvej.http.ReceivedHttpRequestPrintFormat.httpRequest;
import static org.approvej.http.StubbedHttpResponse.response;
import static org.approvej.scrub.Scrubbers.uuids;

import java.math.BigDecimal;
import java.util.List;
import org.approvej.examples.shop.order.OrderService;
import org.approvej.examples.shop.product.Product;
import org.approvej.examples.shop.product.ProductRepository;
import org.approvej.http.HttpStubServer;
import org.junit.jupiter.api.AutoClose;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

@SpringBootTest
@Testcontainers
class PaymentApprovalTest {

  @Container @ServiceConnection
  static final PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:17");

  @AutoClose static final HttpStubServer paymentServer = new HttpStubServer();

  @DynamicPropertySource
  static void configurePayment(DynamicPropertyRegistry registry) {
    registry.add("payment.gateway.url", paymentServer::address);
    registry.add("payment.gateway.token", () -> "test-token-123");
  }

  @Autowired private OrderService orderService;
  @Autowired private ProductRepository productRepository;

  @BeforeEach
  void setUp() {
    paymentServer.resetReceivedRequests();
    paymentServer.nextResponse(
        response()
            .header("Content-Type", "application/json")
            .body(
                "{\"paymentId\":\"pay-abc-123\",\"status\":\"succeeded\","
                    + "\"processedAt\":\"2026-03-11T10:00:00Z\"}")
            .statusCode(200));
  }

  @Test
  void place_order_payment_request() {
    Product product =
        productRepository.save(
            new Product(
                "Espresso Machine",
                "Professional espresso machine",
                new BigDecimal("299.99"),
                "ESP-200"));

    orderService.placeOrder("Eve", "eve@example.com", List.of(product.getId()));

    approve(paymentServer.lastReceivedRequest())
        .scrubbedOf(hostHeaderValue())
        .printedAs(httpRequest())
        .scrubbedOf(uuids())
        .byFile();
  }

  @Test
  void place_order_payment_request_scrubbed() {
    Product product =
        productRepository.save(
            new Product(
                "Coffee Grinder", "Burr coffee grinder", new BigDecimal("89.99"), "GRD-200"));

    orderService.placeOrder("Frank", "frank@example.com", List.of(product.getId()));

    approve(paymentServer.lastReceivedRequest())
        .scrubbedOf(hostHeaderValue())
        .scrubbedOf(headerValue("Authorization"))
        .printedAs(httpRequest())
        .scrubbedOf(uuids())
        .byFile();
  }
}

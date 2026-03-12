package org.approvej.examples.shop.order;

import static org.approvej.ApprovalBuilder.approve;
import static org.approvej.database.DatabaseScrubbers.columnValue;
import static org.approvej.database.DatabaseSnapshot.query;
import static org.approvej.database.QueryResultPrintFormat.queryResult;
import static org.approvej.database.SqlPrintFormat.sql;
import static org.approvej.http.StubbedHttpResponse.response;
import static org.approvej.json.jackson3.JsonPrintFormat.json;
import static org.approvej.scrub.Scrubbers.isoInstants;
import static org.approvej.scrub.Scrubbers.uuids;

import java.math.BigDecimal;
import java.util.List;
import javax.sql.DataSource;
import org.approvej.database.RecordingDataSource;
import org.approvej.examples.shop.product.Product;
import org.approvej.examples.shop.product.ProductRepository;
import org.approvej.http.HttpStubServer;
import org.junit.jupiter.api.AutoClose;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

@SpringBootTest
@Testcontainers
@Import(OrderApprovalTest.RecordingDataSourceConfiguration.class)
class OrderApprovalTest {

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
  @Autowired private DataSource dataSource;

  @TestConfiguration
  static class RecordingDataSourceConfiguration {

    @Bean
    static BeanPostProcessor recordingDataSourceWrapper() {
      return new BeanPostProcessor() {
        @Override
        public Object postProcessAfterInitialization(Object bean, String beanName) {
          if (bean instanceof DataSource ds && !(bean instanceof RecordingDataSource)) {
            return new RecordingDataSource(ds);
          }
          return bean;
        }
      };
    }
  }

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
  void place_order() {
    Product product =
        productRepository.save(
            new Product(
                "Espresso Machine",
                "Professional espresso machine",
                new BigDecimal("299.99"),
                "ESP-100"));

    Order order = orderService.placeOrder("Alice", "alice@example.com", List.of(product.getId()));

    approve(order).printedAs(json()).scrubbedOf(uuids()).scrubbedOf(isoInstants()).byFile();
  }

  @Test
  void place_order_database_state() {
    Product product =
        productRepository.save(
            new Product(
                "Coffee Grinder", "Burr coffee grinder", new BigDecimal("89.99"), "GRD-100"));

    orderService.placeOrder("Bob", "bob@example.com", List.of(product.getId()));

    approve(
            query(
                dataSource,
                "SELECT customer_name, customer_email, status FROM orders"
                    + " WHERE customer_name = 'Bob'"))
        .printedAs(queryResult())
        .byFile();
  }

  @Test
  void place_order_order_items_state() {
    Product product =
        productRepository.save(
            new Product(
                "Milk Frother", "Automatic milk frother", new BigDecimal("49.99"), "FRT-100"));

    orderService.placeOrder("Carol", "carol@example.com", List.of(product.getId()));

    approve(
            query(
                dataSource,
                "SELECT oi.id, oi.quantity, oi.unit_price FROM order_items oi"
                    + " JOIN orders o ON oi.order_id = o.id"
                    + " WHERE o.customer_name = 'Carol'"))
        .scrubbedOf(columnValue("id"))
        .printedAs(queryResult())
        .byFile();
  }

  @Test
  void place_order_sql() {
    Product product =
        productRepository.save(
            new Product("Tamper", "Precision tamper", new BigDecimal("34.99"), "TMP-100"));

    RecordingDataSource recordingDataSource = (RecordingDataSource) dataSource;
    recordingDataSource.resetRecordedQueries();

    orderService.placeOrder("Dave", "dave@example.com", List.of(product.getId()));

    approve(recordingDataSource.lastRecordedQuery()).printedAs(sql()).scrubbedOf(uuids()).byFile();
  }
}

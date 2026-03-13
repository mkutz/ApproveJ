package org.approvej.examples.shop.order;

import static org.approvej.ApprovalBuilder.approve;
import static org.approvej.database.SqlPrintFormat.sql;
import static org.approvej.http.StubbedHttpResponse.response;
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

// tag::spring_boot_database[]
@SpringBootTest
@Testcontainers
@Import(OrderSqlApprovalTest.RecordingDataSourceConfiguration.class) // <1>
class OrderSqlApprovalTest {

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
  @Autowired private DataSource dataSource; // <2>

  @TestConfiguration
  static class RecordingDataSourceConfiguration {

    @Bean
    static BeanPostProcessor recordingDataSourceWrapper() { // <3>
      return new BeanPostProcessor() {
        @Override
        public Object postProcessAfterInitialization(Object bean, String beanName) {
          if (bean instanceof DataSource dataSource && !(bean instanceof RecordingDataSource)) {
            return new RecordingDataSource(dataSource);
          }
          return bean;
        }
      };
    }
  }

  @BeforeEach
  void resetPaymentStub() {
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
  void place_order_sql() {
    Product product =
        productRepository.save(
            new Product("Tamper", "Precision tamper", new BigDecimal("34.99"), "TMP-100"));

    RecordingDataSource recordingDataSource = (RecordingDataSource) dataSource;
    recordingDataSource.resetRecordedQueries();

    orderService.placeOrder("Dave", "dave@example.com", List.of(product.getId()));

    approve(recordingDataSource.lastRecordedQuery()) // <4>
        .printedAs(sql())
        .scrubbedOf(uuids())
        .byFile();
  }
}
// end::spring_boot_database[]

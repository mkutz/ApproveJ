package org.approvej.examples.shop.product;

import static org.approvej.image.ImageApprovalBuilder.approveImage;
import static org.testcontainers.containers.wait.strategy.Wait.forListeningPort;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.postgresql.PostgreSQLContainer;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ProductPageApprovalTest {

  @ServiceConnection
  static final PostgreSQLContainer postgres =
      new PostgreSQLContainer("postgres:17").waitingFor(forListeningPort());

  @LocalServerPort int port;

  @Autowired ProductRepository productRepository;

  @Test
  void product_detail_page() {
    Product product =
        productRepository.save(
            new Product(
                "Espresso Machine",
                "Professional 15-bar espresso machine with built-in grinder",
                new BigDecimal("299.99"),
                "ESP-001"));

    try (Playwright playwright = Playwright.create()) {
      Browser browser =
          playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(true));
      Page page = browser.newPage();
      page.setViewportSize(1280, 720);

      page.navigate("http://localhost:" + port + "/products/" + product.getSku());
      page.waitForLoadState();

      approveImage(page.screenshot()).byFile();
    }
  }
}

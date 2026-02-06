package examples.java;

import static org.approvej.image.ImageApprovalBuilder.approveImage;
import static org.approvej.image.compare.ImageComparators.perceptualHash;
import static org.approvej.image.compare.ImageComparators.pixel;
import static org.approvej.image.scrub.ImageScrubbers.region;
import static org.approvej.image.scrub.ImageScrubbers.regions;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import java.awt.Rectangle;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

class ImageDocTest {

  @Test
  void approve_screenshot() {
    // tag::approve_screenshot[]
    try (Playwright playwright = Playwright.create()) {
      Browser browser =
          playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(true));
      Page page = browser.newPage();
      page.setViewportSize(1280, 720);

      page.navigate("https://approvej.org");

      approveImage(page.screenshot()) // <1>
          .byFile(); // <2>
    }
    // end::approve_screenshot[]
  }

  @Test
  void approve_screenshot_with_perceptual_hash() {
    // tag::approve_screenshot_phash[]
    try (Playwright playwright = Playwright.create()) {
      Browser browser =
          playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(true));
      Page page = browser.newPage();
      page.setViewportSize(1280, 720);

      page.navigate("https://approvej.org");

      approveImage(page.screenshot())
          .comparedBy(perceptualHash()) // <1>
          .byFile();
    }
    // end::approve_screenshot_phash[]
  }

  @Test
  void approve_screenshot_with_custom_threshold() {
    // tag::approve_screenshot_threshold[]
    try (Playwright playwright = Playwright.create()) {
      Browser browser =
          playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(true));
      Page page = browser.newPage();
      page.setViewportSize(1280, 720);

      page.navigate("https://approvej.org");

      approveImage(page.screenshot())
          .comparedBy(perceptualHash().withThreshold(0.95)) // <1>
          .byFile();
    }
    // end::approve_screenshot_threshold[]
  }

  @Test
  void approve_screenshot_with_pixel_comparison() {
    // tag::approve_screenshot_pixel[]
    try (Playwright playwright = Playwright.create()) {
      Browser browser =
          playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(true));
      Page page = browser.newPage();
      page.setViewportSize(1280, 720);

      page.navigate("https://approvej.org");

      approveImage(page.screenshot())
          .comparedBy(pixel().withThreshold(0.01)) // <1>
          .byFile();
    }
    // end::approve_screenshot_pixel[]
  }

  @Test
  void approve_screenshot_wait_for_animations() {
    // tag::approve_screenshot_animations[]
    try (Playwright playwright = Playwright.create()) {
      Browser browser =
          playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(true));
      Page page = browser.newPage();
      page.setViewportSize(1280, 720);

      page.navigate("https://approvej.org");

      // Wait for network to be idle (all resources loaded)
      page.waitForLoadState(); // <1>

      // Disable CSS animations and transitions
      page.addStyleTag( // <2>
          new Page.AddStyleTagOptions()
              .setContent(
                  """
                  *, *::before, *::after {
                    animation-duration: 0s !important;
                    animation-delay: 0s !important;
                    transition-duration: 0s !important;
                    transition-delay: 0s !important;
                  }
                  """));

      approveImage(page.screenshot()).byFile();
    }
    // end::approve_screenshot_animations[]
  }

  @Test
  void approve_screenshot_element() {
    // tag::approve_screenshot_element[]
    try (Playwright playwright = Playwright.create()) {
      Browser browser =
          playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(true));
      Page page = browser.newPage();
      page.setViewportSize(1280, 720);

      page.navigate("https://approvej.org");
      page.waitForLoadState();

      // Screenshot only a specific element instead of full page
      approveImage(page.locator("h1").screenshot()) // <1>
          .byFile();
    }
    // end::approve_screenshot_element[]
  }

  @Test
  void approve_screenshot_scrubbed() {
    // tag::approve_screenshot_scrubbed[]
    try (Playwright playwright = Playwright.create()) {
      Browser browser =
          playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(true));
      Page page = browser.newPage();
      page.setViewportSize(1280, 720);

      page.navigate("https://approvej.org");
      page.waitForLoadState();

      // Scrub dynamic content like version numbers by masking a region
      approveImage(page.screenshot())
          .scrubbedOf(region(10, 50, 100, 20)) // <1>
          .byFile();
    }
    // end::approve_screenshot_scrubbed[]
  }

  @Test
  void approve_screenshot_scrubbed_element() {
    // tag::approve_screenshot_scrubbed_element[]
    try (Playwright playwright = Playwright.create()) {
      Browser browser =
          playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(true));
      Page page = browser.newPage();
      page.setViewportSize(1280, 720);

      page.navigate("https://approvej.org");
      page.waitForLoadState();

      // Use element bounding box to scrub dynamic content
      var versionElement = page.locator("#revnumber");
      var bounds = versionElement.boundingBox();

      approveImage(page.screenshot())
          .scrubbedOf(
              region( // <1>
                  (int) bounds.x, (int) bounds.y, (int) bounds.width, (int) bounds.height))
          .byFile();
    }
    // end::approve_screenshot_scrubbed_element[]
  }

  @Test
  void approve_screenshot_scrubbed_multiple() {
    // tag::approve_screenshot_scrubbed_multiple[]
    try (Playwright playwright = Playwright.create()) {
      Browser browser =
          playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(true));
      Page page = browser.newPage();
      page.setViewportSize(1280, 720);

      page.navigate("https://approvej.org");
      page.waitForLoadState();

      // Scrub multiple regions at once
      approveImage(page.screenshot())
          .scrubbedOf(
              regions( // <1>
                  new Rectangle(10, 50, 100, 20), // version number
                  new Rectangle(200, 100, 80, 15))) // timestamp
          .byFile();
    }
    // end::approve_screenshot_scrubbed_multiple[]
  }

  @Test
  void approve_screenshot_selenium() {
    // tag::approve_screenshot_selenium[]
    ChromeOptions options = new ChromeOptions();
    options.addArguments("--headless");
    WebDriver driver = new ChromeDriver(options);
    try {
      driver.manage().window().setSize(new Dimension(1280, 720));
      driver.get("https://approvej.org");

      byte[] screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES); // <1>

      approveImage(screenshot) // <2>
          .byFile();
    } finally {
      driver.quit();
    }
    // end::approve_screenshot_selenium[]
  }
}

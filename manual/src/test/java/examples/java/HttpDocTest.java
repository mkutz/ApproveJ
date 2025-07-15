package examples.java;

import static org.approvej.ApprovalBuilder.approve;
import static org.approvej.http.HttpScrubbers.headerValue;
import static org.approvej.http.HttpScrubbers.hostHeaderValue;
import static org.approvej.http.ReceivedHttpRequestPrinter.httpRequestPrinter;
import static org.approvej.http.StubbedHttpResponse.response;
import static org.assertj.core.api.Assertions.assertThat;

import examples.PriceComparator;
import examples.PriceComparator.CheeeperVendor;
import examples.PriceComparator.LookupResult;
import examples.PriceComparator.PrycyVendor;
import java.util.List;
import org.approvej.http.HttpStubServer;
import org.junit.jupiter.api.AutoClose;
import org.junit.jupiter.api.Test;

class HttpDocTest {

  // tag::initialize[]
  @AutoClose
  private static final HttpStubServer cheeeperStub =
      new HttpStubServer().nextResponse(response().body("2999").statusCode(200));

  @AutoClose
  private static final HttpStubServer prycyStub =
      new HttpStubServer().nextResponse(response().body("3199").statusCode(200));

  // end::initialize[]

  @Test
  void approve_http_request() {
    // tag::approve_http_request[]
    PriceComparator priceComparator =
        new PriceComparator(
            new CheeeperVendor(cheeeperStub.address()),
            new PrycyVendor(prycyStub.address(), "secret token"));

    List<LookupResult> lookupResults = priceComparator.lookupPrice("1234567890123");

    assertThat(lookupResults).hasSize(2);

    approve(cheeeperStub.lastReceivedRequest())
        .named("cheeper")
        .scrubbedOf(hostHeaderValue())
        .scrubbedOf(headerValue("User-agent"))
        .printWith(httpRequestPrinter())
        .byFile();
    approve(prycyStub.lastReceivedRequest())
        .named("prycy")
        .scrubbedOf(hostHeaderValue())
        .scrubbedOf(headerValue("User-agent"))
        .printWith(httpRequestPrinter())
        .byFile();
    // end::approve_http_request[]
  }
}

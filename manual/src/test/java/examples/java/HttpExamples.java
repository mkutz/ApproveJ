package examples.java;

import static org.approvej.ApprovalBuilder.approve;
import static org.approvej.http.HttpRequestPrinter.requestPrinter;
import static org.approvej.http.HttpScrubbers.hostHeader;
import static org.approvej.http.StubbedHttpResponse.response;
import static org.assertj.core.api.Assertions.assertThat;

import examples.PriceComparator;
import examples.PriceComparator.LookupResult;
import examples.PriceComparator.Vendor;
import java.util.List;
import org.approvej.http.HttpStubServer;
import org.junit.jupiter.api.AutoClose;
import org.junit.jupiter.api.Test;

class HttpExamples {

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
    PriceComparator priceComparator =
        new PriceComparator(
            new Vendor("Cheeeper", cheeeperStub.address()),
            new Vendor("Prycy", prycyStub.address()));

    List<LookupResult> lookupResults = priceComparator.lookupPrice("1234567890123");

    assertThat(lookupResults).hasSize(2);

    approve(cheeeperStub.lastReceivedRequest())
        .scrubbedOf(hostHeader())
        .printWith(requestPrinter())
        .byFile();
    approve(prycyStub.lastReceivedRequest())
        .scrubbedOf(hostHeader())
        .printWith(requestPrinter())
        .byFile();
  }
}

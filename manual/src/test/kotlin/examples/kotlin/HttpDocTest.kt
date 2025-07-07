package examples.kotlin

import examples.PriceComparator
import examples.PriceComparator.CheeeperVendor
import examples.PriceComparator.PrycyVendor
import org.approvej.ApprovalBuilder.approve
import org.approvej.http.HttpScrubbers.hostHeader
import org.approvej.http.HttpStubServer
import org.approvej.http.ReceivedHttpRequestPrinter.httpRequestPrinter
import org.approvej.http.StubbedHttpResponse.response
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AutoClose
import org.junit.jupiter.api.Test

class HttpDocTest {
  @Test
  fun approve_http_request() {
    // tag::approve_http_request[]
    val priceComparator =
      PriceComparator(
        CheeeperVendor(cheeeperStub.address()),
        PrycyVendor(prycyStub.address(), "secret token"),
      )

    val lookupResults = priceComparator.lookupPrice("1234567890123")

    assertThat(lookupResults).hasSize(2)

    approve(cheeeperStub.lastReceivedRequest())
      .named("cheeeper")
      .scrubbedOf(hostHeader())
      .printWith(httpRequestPrinter())
      .byFile()
    approve(prycyStub.lastReceivedRequest())
      .named("prycy")
      .scrubbedOf(hostHeader())
      .printWith(httpRequestPrinter())
      .byFile()
    // end::approve_http_request[]
  }

  companion object {
    // tag::initialize[]
    @AutoClose
    private val cheeeperStub =
      HttpStubServer().nextResponse(response().body("2999").statusCode(200))

    @AutoClose
    private val prycyStub = HttpStubServer().nextResponse(response().body("3199").statusCode(200))
    // end::initialize[]
  }
}

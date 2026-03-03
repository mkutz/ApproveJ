package examples.kotlin

import examples.ExampleClass.createOrderSummary
import org.approvej.ApprovalBuilder.approve
import org.approvej.print.MultiLineStringPrintFormat.multiLineString
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

@org.approvej.ApprovalTest
class IntroductionDocTest {

  @Test
  fun `traditional assertions`() {
    // tag::traditional_assertions[]
    val order = createOrderSummary()

    assertThat(order.orderId()).isEqualTo("ORD-12345")
    assertThat(order.customerName()).isEqualTo("Jane Smith")
    assertThat(order.shippingAddress()).isEqualTo("123 Main St, Springfield")
    assertThat(order.items()).containsExactly("Widget A", "Gadget B", "Doohickey C")
    assertThat(order.itemCount()).isEqualTo(3)
    assertThat(order.subtotal()).isEqualTo(59.97)
    assertThat(order.tax()).isEqualTo(4.80)
    // missing: total // <1>
    assertThat(order.status()).isEqualTo("confirmed")
    // end::traditional_assertions[]
  }

  @Test
  fun `approval test`() {
    // tag::approval_test[]
    val order = createOrderSummary()

    approve(order).printedAs(multiLineString()).byFile()
    // end::approval_test[]
  }
}

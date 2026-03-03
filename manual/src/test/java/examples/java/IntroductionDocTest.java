package examples.java;

import static examples.ExampleClass.createOrderSummary;
import static org.approvej.ApprovalBuilder.approve;
import static org.approvej.print.MultiLineStringPrintFormat.multiLineString;
import static org.assertj.core.api.Assertions.assertThat;

import examples.ExampleClass.OrderSummary;
import org.junit.jupiter.api.Test;

@org.approvej.ApprovalTest
class IntroductionDocTest {

  @Test
  void traditional_assertions() {
    // tag::traditional_assertions[]
    OrderSummary order = createOrderSummary();

    assertThat(order.orderId()).isEqualTo("ORD-12345");
    assertThat(order.customerName()).isEqualTo("Jane Smith");
    assertThat(order.shippingAddress()).isEqualTo("123 Main St, Springfield");
    assertThat(order.items()).containsExactly("Widget A", "Gadget B", "Doohickey C");
    assertThat(order.itemCount()).isEqualTo(3);
    assertThat(order.subtotal()).isEqualTo(59.97);
    assertThat(order.tax()).isEqualTo(4.80);
    // missing: total // <1>
    assertThat(order.status()).isEqualTo("confirmed");
    // end::traditional_assertions[]
  }

  @Test
  void approval_test() {
    // tag::approval_test[]
    OrderSummary order = createOrderSummary();

    approve(order).printedAs(multiLineString()).byFile();
    // end::approval_test[]
  }
}

package examples.java;

import static examples.ExampleClass.Order.Status.OPEN;
import static examples.ExampleClass.getOrderById;
import static org.approvej.ApprovalBuilder.approve;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import examples.ExampleClass.Order;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class IntroductionDocTest {

  @Test
  void assertions() {
    // tag::assertions[]
    String id = UUID.randomUUID().toString();

    Order order = getOrderById(id);

    assertEquals(id, order.id());
    assertTrue(order.orderDateTime().isBefore(LocalDateTime.now()));
    assertEquals(OPEN, order.status());

    assertNotNull(order.customer());
    assertNotNull(order.customer().id());
    assertEquals("John", order.customer().firstname());
    assertEquals("Doe", order.customer().surname());

    assertNotNull(order.deliveryAddress());
    assertEquals("Liberty St", order.deliveryAddress().street());
    assertEquals("1A", order.deliveryAddress().houseNumber());
    assertEquals("10007", order.deliveryAddress().postalCode());
    assertEquals("New York", order.deliveryAddress().city());
    assertEquals("US", order.deliveryAddress().country());

    assertNotNull(order.billingAddress());
    assertEquals("Independence Ave", order.billingAddress().street());
    assertEquals("1776", order.billingAddress().houseNumber());
    assertEquals("20500", order.billingAddress().postalCode());
    assertEquals("Washington", order.billingAddress().city());
    assertEquals("US", order.billingAddress().country());

    assertEquals(2, order.items().size());
    Order.Item firstItem = order.items().getFirst();
    assertEquals("323e4567-e89b-12d3-a456-426614174002", firstItem.id());
    assertEquals("Baseball Cap", firstItem.title());
    assertEquals(2, firstItem.quantity());
    assertEquals(new BigDecimal("24.99"), firstItem.pricePerUnit());
    assertEquals(new BigDecimal("49.98"), firstItem.totalPrice());
    Order.Item secondItem = order.items().get(1);
    assertEquals("423e4567-e89b-12d3-a456-426614174003", secondItem.id());
    assertEquals("Hot Dog", secondItem.title());
    assertEquals(5, secondItem.quantity());
    assertEquals(new BigDecimal("3.50"), secondItem.pricePerUnit());
    assertEquals(new BigDecimal("17.50"), secondItem.totalPrice());
    // end::assertions[]
  }

  @Test
  void approval() {
    // tag::approval[]
    String id = UUID.randomUUID().toString();

    Order order = getOrderById(id);

    approve(order).byFile();
    // end::approval[]
  }
}

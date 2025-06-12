package examples.kotlin

import examples.ExampleClass.Order.Status.OPEN
import examples.ExampleClass.getOrderById
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*
import org.approvej.ApprovalBuilder.approve
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class IntroductionDocTest {

  @Test
  fun assertions() {
    // tag::assertions[]
    val id = UUID.randomUUID().toString()

    val order = getOrderById(id)

    assertEquals(id, order.id())
    assertTrue(order.orderDateTime().isBefore(LocalDateTime.now()))
    assertEquals(OPEN, order.status())

    assertNotNull(order.customer())
    assertNotNull(order.customer().id())
    assertEquals("John", order.customer().firstname())
    assertEquals("Doe", order.customer().surname())

    assertNotNull(order.deliveryAddress())
    assertEquals("Liberty St", order.deliveryAddress().street())
    assertEquals("1A", order.deliveryAddress().houseNumber())
    assertEquals("10007", order.deliveryAddress().postalCode())
    assertEquals("New York", order.deliveryAddress().city())
    assertEquals("US", order.deliveryAddress().country())

    assertNotNull(order.billingAddress())
    assertEquals("Independence Ave", order.billingAddress().street())
    assertEquals("1776", order.billingAddress().houseNumber())
    assertEquals("20500", order.billingAddress().postalCode())
    assertEquals("Washington", order.billingAddress().city())
    assertEquals("US", order.billingAddress().country())

    assertEquals(2, order.items().size)
    val firstItem = order.items().first()
    assertEquals("323e4567-e89b-12d3-a456-426614174002", firstItem.id())
    assertEquals("Baseball Cap", firstItem.title())
    assertEquals(2, firstItem.quantity())
    assertEquals(BigDecimal("24.99"), firstItem.pricePerUnit())
    assertEquals(BigDecimal("49.98"), firstItem.totalPrice())
    val secondItem = order.items()[1]
    assertEquals("423e4567-e89b-12d3-a456-426614174003", secondItem.id())
    assertEquals("Hot Dog", secondItem.title())
    assertEquals(5, secondItem.quantity())
    assertEquals(BigDecimal("3.50"), secondItem.pricePerUnit())
    assertEquals(BigDecimal("17.50"), secondItem.totalPrice())
    // end::assertions[]
  }

  @Test
  fun approval() {
    // tag::approval[]
    val id = UUID.randomUUID().toString()

    val order = getOrderById(id)

    approve(order).byFile()
    // end::approval[]
  }
}

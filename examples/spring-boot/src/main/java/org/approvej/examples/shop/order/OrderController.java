package org.approvej.examples.shop.order;

import java.util.List;
import java.util.UUID;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

  private final OrderService orderService;

  public OrderController(OrderService orderService) {
    this.orderService = orderService;
  }

  @PostMapping
  public Order placeOrder(@RequestBody PlaceOrderRequest request) {
    return orderService.placeOrder(
        request.customerName(), request.customerEmail(), request.productIds());
  }

  record PlaceOrderRequest(String customerName, String customerEmail, List<UUID> productIds) {}
}

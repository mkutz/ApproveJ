package org.approvej.examples.shop.order;

import java.util.List;
import java.util.UUID;
import org.approvej.examples.shop.payment.PaymentGateway;
import org.approvej.examples.shop.payment.PaymentRequest;
import org.approvej.examples.shop.payment.PaymentResponse;
import org.approvej.examples.shop.product.Product;
import org.approvej.examples.shop.product.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderService {

  private final OrderRepository orderRepository;
  private final ProductRepository productRepository;
  private final PaymentGateway paymentGateway;

  public OrderService(
      OrderRepository orderRepository,
      ProductRepository productRepository,
      PaymentGateway paymentGateway) {
    this.orderRepository = orderRepository;
    this.productRepository = productRepository;
    this.paymentGateway = paymentGateway;
  }

  @Transactional
  public Order placeOrder(String customerName, String customerEmail, List<UUID> productIds) {
    Order order = new Order(customerName, customerEmail);

    for (UUID productId : productIds) {
      Product product =
          productRepository
              .findById(productId)
              .orElseThrow(() -> new IllegalArgumentException("Product not found: " + productId));
      order.addItem(new OrderItem(product, 1, product.getPrice()));
    }

    orderRepository.save(order);

    PaymentRequest paymentRequest =
        new PaymentRequest(order.getId(), order.total(), "EUR", customerEmail);
    PaymentResponse response = paymentGateway.charge(paymentRequest);

    order.setPaymentId(response.paymentId());
    order.setStatus("succeeded".equals(response.status()) ? OrderStatus.PAID : OrderStatus.FAILED);

    return orderRepository.save(order);
  }
}

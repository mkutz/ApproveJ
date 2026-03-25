package org.approvej.examples.shop.product;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class ProductNotFoundException extends RuntimeException {

  public ProductNotFoundException(String sku) {
    super("Product not found: " + sku);
  }
}

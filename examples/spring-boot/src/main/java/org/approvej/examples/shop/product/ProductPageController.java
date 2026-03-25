package org.approvej.examples.shop.product;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class ProductPageController {

  private final ProductRepository productRepository;

  public ProductPageController(ProductRepository productRepository) {
    this.productRepository = productRepository;
  }

  @GetMapping("/products/{sku}")
  public String productDetail(@PathVariable String sku, Model model) {
    Product product =
        productRepository.findBySku(sku).orElseThrow(() -> new ProductNotFoundException(sku));
    model.addAttribute("product", product);
    return "product-detail";
  }
}

package org.approvej.examples.shop.product;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "products")
public class Product {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  private String name;
  private String description;
  private BigDecimal price;

  @Column(unique = true)
  private String sku;

  private Instant createdAt;

  protected Product() {}

  public Product(String name, String description, BigDecimal price, String sku) {
    this.name = name;
    this.description = description;
    this.price = price;
    this.sku = sku;
    this.createdAt = Instant.now();
  }

  public UUID getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public String getDescription() {
    return description;
  }

  public BigDecimal getPrice() {
    return price;
  }

  public String getSku() {
    return sku;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  @Override
  public String toString() {
    return "Product{id=%s, name='%s', description='%s', price=%s, sku='%s', createdAt=%s}"
        .formatted(id, name, description, price, sku, createdAt);
  }
}

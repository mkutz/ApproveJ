package org.approvej.examples.shop.product;

import static org.approvej.ApprovalBuilder.approve;
import static org.approvej.json.jackson3.JsonPointerScrubber.jsonPointer;
import static org.approvej.json.jackson3.JsonPrintFormat.json;
import static org.approvej.scrub.Scrubbers.isoInstants;
import static org.approvej.scrub.Scrubbers.uuids;
import static org.approvej.yaml.jackson3.YamlPrintFormat.yaml;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

class ProductApprovalTest {

  @Test
  void product_toString() {
    var product =
        new Product(
            "Espresso Machine",
            "Professional espresso machine",
            new BigDecimal("299.99"),
            "ESP-001");

    approve(product.toString()).scrubbedOf(uuids()).scrubbedOf(isoInstants()).byFile();
  }

  @Test
  void product_json() {
    var product =
        new Product(
            "Espresso Machine",
            "Professional espresso machine",
            new BigDecimal("299.99"),
            "ESP-001");

    approve(product).printedAs(json()).scrubbedOf(uuids()).scrubbedOf(isoInstants()).byFile();
  }

  @Test
  void product_yaml() {
    var product =
        new Product(
            "Espresso Machine",
            "Professional espresso machine",
            new BigDecimal("299.99"),
            "ESP-001");

    approve(product).printedAs(yaml()).scrubbedOf(uuids()).scrubbedOf(isoInstants()).byFile();
  }

  @Test
  void product_json_scrubbed() throws Exception {
    var product =
        new Product(
            "Espresso Machine",
            "Professional espresso machine",
            new BigDecimal("299.99"),
            "ESP-001");

    ObjectMapper mapper = JsonMapper.builder().build();
    var jsonNode = mapper.valueToTree(product);

    approve(jsonNode)
        .scrubbedOf(jsonPointer("/id").replacement("[scrubbed id]"))
        .scrubbedOf(jsonPointer("/createdAt").replacement("[scrubbed createdAt]"))
        .printedAs(json())
        .byFile();
  }
}

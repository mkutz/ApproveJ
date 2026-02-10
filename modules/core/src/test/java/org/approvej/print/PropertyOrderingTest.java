package org.approvej.print;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.function.Function;
import org.junit.jupiter.api.Test;

class PropertyOrderingTest {

  @Test
  void reorder_record() {
    record Sample(String alpha, int beta, boolean gamma) {}

    List<String> properties = List.of("beta", "gamma", "alpha");

    assertThat(PropertyOrdering.reorder(Sample.class, properties, Function.identity()))
        .containsExactly("alpha", "beta", "gamma");
  }

  @Test
  void reorder_class_hierarchy() {
    List<String> properties = List.of("name", "species", "legs");

    assertThat(PropertyOrdering.reorder(Pet.class, properties, Function.identity()))
        .containsExactly("species", "legs", "name");
  }

  @Test
  void reorder_getter_only_properties_sorted_alphabetically_after_fields() {
    List<String> properties = List.of("description", "name", "species", "legs");

    assertThat(PropertyOrdering.reorder(Pet.class, properties, Function.identity()))
        .containsExactly("species", "legs", "name", "description");
  }

  @Test
  void reorder_all_getter_only() {
    record Empty() {}

    List<String> properties = List.of("charlie", "alpha", "bravo");

    assertThat(PropertyOrdering.reorder(Empty.class, properties, Function.identity()))
        .containsExactly("alpha", "bravo", "charlie");
  }

  @Test
  void reorder_empty() {
    List<String> properties = List.of();

    assertThat(PropertyOrdering.reorder(Object.class, properties, Function.identity())).isEmpty();
  }

  @Test
  void reorder_with_name_extractor() {
    record Pair(String key, String value) {}

    List<Pair> properties =
        List.of(new Pair("value", "b"), new Pair("key", "a"), new Pair("extra", "c"));

    assertThat(PropertyOrdering.reorder(Pair.class, properties, Pair::key))
        .extracting(Pair::key)
        .containsExactly("key", "value", "extra");
  }
}

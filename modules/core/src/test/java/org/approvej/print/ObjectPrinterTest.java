package org.approvej.print;

import static org.approvej.print.ObjectPrinter.objectPrinter;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ObjectPrinterTest {

  ObjectPrinter printer = objectPrinter();

  @Test
  void apply() {
    var exampleObject = new SimpleExampleClass("value1", 42, true);

    assertThat(printer.apply(exampleObject))
        .isEqualTo(
            """
            SimpleExampleClass [
              field1=value1,
              field2=42,
              field3=true
            ]""");
  }

  private record SimpleExampleClass(String field1, int field2, boolean field3) {}

  @Test
  void apply_complex_property() {
    var exampleObject =
        new ComplexExampleClass("value1", 42, true, new SimpleExampleClass("value2", 24, false));

    assertThat(printer.apply(exampleObject))
        .isEqualTo(
            """
            ComplexExampleClass [
              field1=value1,
              field2=42,
              field3=true,
              field4=SimpleExampleClass [
                field1=value2,
                field2=24,
                field3=false
              ]
            ]""");
  }

  private record ComplexExampleClass(
      String field1, int field2, boolean field3, SimpleExampleClass field4) {}

  @Test
  void apply_list() {
    assertThat(printer.apply(List.of("a", "b", "c")))
        .isEqualTo(
            """
            [
              a,
              b,
              c
            ]""");
  }

  @Test
  void apply_nested_lists() {
    assertThat(printer.apply(List.of("a", List.of(1, 2, 3), "c")))
        .isEqualTo(
            """
            [
              a,
              [
                1,
                2,
                3
              ],
              c
            ]""");
  }

  @Test
  void apply_map() {
    assertThat(printer.apply(Map.of("a", 1, "b", 2, "c", 3)))
        .isEqualTo(
            """
            [
              a=1,
              b=2,
              c=3
            ]""");
  }

  @Test
  void apply_map_of_complex() {
    assertThat(
            printer.apply(Map.of("a", new SimpleExampleClass("b", 2, true), "b", "hello", "c", 3)))
        .isEqualTo(
            """
            [
              a=SimpleExampleClass [
                field1=b,
                field2=2,
                field3=true
              ],
              b=hello,
              c=3
            ]""");
  }

  @Test
  void apply_collection_property() {
    var value = new ComplexExampleCollectionClass("value1", List.of("a", "b", "c"));

    assertThat(printer.apply(value))
        .isEqualTo(
            """
            ComplexExampleCollectionClass [
              field1=value1,
              field2=[
                a,
                b,
                c
              ]
            ]""");
  }

  private record ComplexExampleCollectionClass(String field1, List<String> field2) {}

  @Test
  void apply_collection_of_complex() {
    var value =
        List.of(
            new ComplexExampleClass(
                "value1", 42, true, new SimpleExampleClass("value2", 24, false)));

    assertThat(printer.apply(value))
        .isEqualTo(
            """
            [
              ComplexExampleClass [
                field1=value1,
                field2=42,
                field3=true,
                field4=SimpleExampleClass [
                  field1=value2,
                  field2=24,
                  field3=false
                ]
              ]
            ]""");
  }

  @Test
  void apply_temporal() {
    var value = List.of(LocalDateTime.of(2345, 6, 7, 8, 9, 10), Duration.ofHours(1));

    assertThat(printer.apply(value))
        .isEqualTo(
            """
            [
              2345-06-07T08:09:10,
              PT1H
            ]""");
  }
}

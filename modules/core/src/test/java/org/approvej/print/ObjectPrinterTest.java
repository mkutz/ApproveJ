package org.approvej.print;

import static org.approvej.print.ObjectPrinter.objectPrinter;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class ObjectPrinterTest {

  ObjectPrinter<?> printer = objectPrinter();

  @ParameterizedTest(name = "{displayName}({arguments})")
  @MethodSource("applySimpleArguments")
  void apply_simple(Object value) {
    assertThat(printer.apply(value)).isEqualTo("%s".formatted(value));
  }

  static Stream<Object> applySimpleArguments() {
    return Stream.of(
        true,
        "string",
        'a',
        1,
        1L,
        1.23f,
        1.23d,
        (byte) 1,
        (short) 1,
        UUID.randomUUID(),
        Instant.now(),
        LocalDateTime.now(),
        ZonedDateTime.now(),
        Duration.ofDays(1).plusHours(2).plusMinutes(3).plusSeconds(4),
        DayOfWeek.MONDAY,
        String.class,
        BigDecimal.ONE,
        BigInteger.ONE);
  }

  @Test
  void apply() {
    SimpleExampleClass exampleObject = new SimpleExampleClass("value1", 42, true);

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
    ComplexExampleClass exampleObject =
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
    ComplexExampleCollectionClass value =
        new ComplexExampleCollectionClass("value1", List.of("a", "b", "c"));

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
    List<ComplexExampleClass> value =
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
}

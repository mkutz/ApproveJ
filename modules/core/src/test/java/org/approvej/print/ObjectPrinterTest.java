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
        BigInteger.ONE,
        null);
  }

  @Test
  void apply() {
    SimpleExampleClass exampleObject =
        new SimpleExampleClass(
            UUID.fromString("00000000-0000-0000-0000-000000000001"), 42, true, null);

    assertThat(printer.apply(exampleObject))
        .isEqualTo(
            """
            SimpleExampleClass [
              uuid=00000000-0000-0000-0000-000000000001,
              number=42,
              bool=true,
              object=null
            ]\
            """);
  }

  @Test
  void apply_sorted() {
    SimpleExampleClass exampleObject =
        new SimpleExampleClass(
            UUID.fromString("00000000-0000-0000-0000-000000000001"), 42, true, null);

    assertThat(printer.sorted().apply(exampleObject))
        .isEqualTo(
            """
            SimpleExampleClass [
              bool=true,
              number=42,
              object=null,
              uuid=00000000-0000-0000-0000-000000000001
            ]\
            """);
  }

  private record SimpleExampleClass(UUID uuid, int number, boolean bool, Object object) {}

  @Test
  void apply_complex_property() {
    ComplexExampleClass exampleObject =
        new ComplexExampleClass(
            UUID.fromString("00000000-0000-0000-0000-000000000001"),
            42,
            true,
            new SimpleExampleClass(
                UUID.fromString("00000000-0000-0000-0000-000000000002"), 24, false, null));

    assertThat(printer.apply(exampleObject))
        .isEqualTo(
            """
            ComplexExampleClass [
              uuid=00000000-0000-0000-0000-000000000001,
              number=42,
              bool=true,
              object=SimpleExampleClass [
                uuid=00000000-0000-0000-0000-000000000002,
                number=24,
                bool=false,
                object=null
              ]
            ]\
            """);
  }

  private record ComplexExampleClass(
      UUID uuid, int number, boolean bool, SimpleExampleClass object) {}

  @Test
  void apply_list() {
    assertThat(printer.apply(List.of("a", "b", "c")))
        .isEqualTo(
            """
            [
              a,
              b,
              c
            ]\
            """);
  }

  @Test
  void apply_list_empty() {
    assertThat(printer.apply(List.of())).isEqualTo("[]");
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
            ]\
            """);
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
            ]\
            """);
  }

  @Test
  void apply_map_empty() {
    assertThat(printer.apply(Map.of())).isEqualTo("[]");
  }

  @Test
  void apply_map_of_complex() {
    assertThat(
            printer.apply(
                Map.of(
                    "a",
                    new SimpleExampleClass(
                        UUID.fromString("00000000-0000-0000-0000-000000000001"), 2, true, null),
                    "b",
                    "hello",
                    "c",
                    3)))
        .isEqualTo(
            """
            [
              a=SimpleExampleClass [
                uuid=00000000-0000-0000-0000-000000000001,
                number=2,
                bool=true,
                object=null
              ],
              b=hello,
              c=3
            ]\
            """);
  }

  @Test
  void apply_collection_property() {
    ComplexExampleCollectionClass value =
        new ComplexExampleCollectionClass(
            UUID.fromString("00000000-0000-0000-0000-000000000001"), List.of("a", "b", "c"));

    assertThat(printer.apply(value))
        .isEqualTo(
            """
            ComplexExampleCollectionClass [
              uuid=00000000-0000-0000-0000-000000000001,
              list=[
                a,
                b,
                c
              ]
            ]\
            """);
  }

  private record ComplexExampleCollectionClass(UUID uuid, List<String> list) {}

  @Test
  void apply_collection_of_complex() {
    List<ComplexExampleClass> value =
        List.of(
            new ComplexExampleClass(
                UUID.fromString("00000000-0000-0000-0000-000000000001"),
                42,
                true,
                new SimpleExampleClass(
                    UUID.fromString("00000000-0000-0000-0000-000000000002"), 24, false, null)));

    assertThat(printer.apply(value))
        .isEqualTo(
            """
            [
              ComplexExampleClass [
                uuid=00000000-0000-0000-0000-000000000001,
                number=42,
                bool=true,
                object=SimpleExampleClass [
                  uuid=00000000-0000-0000-0000-000000000002,
                  number=24,
                  bool=false,
                  object=null
                ]
              ]
            ]\
            """);
  }
}

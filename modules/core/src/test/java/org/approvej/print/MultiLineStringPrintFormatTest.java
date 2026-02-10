package org.approvej.print;

import static org.approvej.print.MultiLineStringPrintFormat.multiLineString;
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

class MultiLineStringPrintFormatTest {

  @Test
  void constructor() {
    assertThat(new MultiLineStringPrintFormat()).isNotNull();
  }

  @ParameterizedTest(name = "{displayName}({arguments})")
  @MethodSource("printerSimpleArguments")
  void printer_simple(Object value) {
    assertThat(multiLineString().printer().apply(value)).isEqualTo("%s".formatted(value));
  }

  static Stream<Object> printerSimpleArguments() {
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
  void printer() {
    SimpleExampleClass exampleObject =
        new SimpleExampleClass(
            UUID.fromString("00000000-0000-0000-0000-000000000001"), 42, true, null);

    assertThat(multiLineString().printer().apply(exampleObject))
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
  void printer_sorted() {
    SimpleExampleClass exampleObject =
        new SimpleExampleClass(
            UUID.fromString("00000000-0000-0000-0000-000000000001"), 42, true, null);

    assertThat(multiLineString().sorted().printer().apply(exampleObject))
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
  void printer_getter() {
    //noinspection unused
    record Person(String name, int age) {
      public String getDisplayString() {
        return "%s (%d)".formatted(name, age);
      }

      public boolean isAdult() {
        return age >= 18;
      }
    }

    assertThat(multiLineString().printer().apply(new Person("Micha", 42)))
        .isEqualTo(
            """
            Person [
              name=Micha,
              age=42,
              adult=true,
              displayString=Micha (42)
            ]\
            """);
  }

  @Test
  void printer_getter_non_boolean_is_method() {
    //noinspection unused
    record Widget(String name, boolean active) {
      public String isType() {
        return "special";
      }
    }

    assertThat(multiLineString().printer().apply(new Widget("Gear", true)))
        .isEqualTo(
            """
            Widget [
              name=Gear,
              active=true
            ]\
            """);
  }

  @Test
  void printer_pojo_getters() {
    assertThat(multiLineString().printer().apply(new PersonPojo("Micha", "Kutz")))
        .isEqualTo(
            """
            PersonPojo [
              firstName=Micha,
              lastName=Kutz,
              fullName=Micha Kutz,
              initials=MK
            ]\
            """);
  }

  @Test
  void printer_inheritance() {
    assertThat(multiLineString().printer().apply(new Pet("Luna", "cat", 4)))
        .isEqualTo(
            """
            Pet [
              species=cat,
              legs=4,
              name=Luna,
              description=Luna the cat
            ]\
            """);
  }

  @Test
  void printer_sorted_with_getters() {
    assertThat(multiLineString().sorted().printer().apply(new PersonPojo("Micha", "Kutz")))
        .isEqualTo(
            """
            PersonPojo [
              firstName=Micha,
              fullName=Micha Kutz,
              initials=MK,
              lastName=Kutz
            ]\
            """);
  }

  @Test
  void printer_complex_property() {
    ComplexExampleClass exampleObject =
        new ComplexExampleClass(
            UUID.fromString("00000000-0000-0000-0000-000000000001"),
            42,
            true,
            new SimpleExampleClass(
                UUID.fromString("00000000-0000-0000-0000-000000000002"), 24, false, null));

    assertThat(multiLineString().printer().apply(exampleObject))
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
  void printer_list() {
    assertThat(multiLineString().printer().apply(List.of("a", "b", "c")))
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
  void printer_list_empty() {
    assertThat(multiLineString().printer().apply(List.of())).isEqualTo("[]");
  }

  @Test
  void printer_lists_nested() {
    assertThat(multiLineString().printer().apply(List.of("a", List.of(1, 2, 3), "c")))
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
  void printer_map() {
    assertThat(multiLineString().printer().apply(Map.of("a", 1, "b", 2, "c", 3)))
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
  void printer_map_empty() {
    assertThat(multiLineString().printer().apply(Map.of())).isEqualTo("[]");
  }

  @Test
  void printer_map_of_complex() {
    assertThat(
            multiLineString()
                .printer()
                .apply(
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
  void printer_collection_property() {
    ComplexExampleCollectionClass value =
        new ComplexExampleCollectionClass(
            UUID.fromString("00000000-0000-0000-0000-000000000001"), List.of("a", "b", "c"));

    assertThat(multiLineString().printer().apply(value))
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
  void printer_collection_of_complex() {
    List<ComplexExampleClass> value =
        List.of(
            new ComplexExampleClass(
                UUID.fromString("00000000-0000-0000-0000-000000000001"),
                42,
                true,
                new SimpleExampleClass(
                    UUID.fromString("00000000-0000-0000-0000-000000000002"), 24, false, null)));

    assertThat(multiLineString().printer().apply(value))
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

  @Test
  void printer_subclass() {
    ExampleSubClass value =
        new ExampleSubClass(
            UUID.fromString("00000000-0000-0000-0000-000000000001"), "Some Name", 123);

    assertThat(multiLineString().printer().apply(value))
        .isEqualTo(
            """
            ExampleSubClass [
              uuid=00000000-0000-0000-0000-000000000001,
              string=Some Name,
              number=123
            ]\
            """);
  }

  @Test
  void filenameExtension() {
    assertThat(multiLineString().filenameExtension()).isEqualTo("txt");
  }

  @SuppressWarnings("unused")
  private static class ExampleSuperClass {
    private static final int STATIC_FIELD = 42;
    private final UUID uuid;
    private final String string;
    private final String fieldWithoutGetter = "should not show";

    public ExampleSuperClass(UUID uuid, String string) {
      this.uuid = uuid;
      this.string = string;
    }

    public UUID getUuid() {
      return uuid;
    }

    public String getString() {
      return string;
    }
  }

  @SuppressWarnings("unused")
  private static class ExampleSubClass extends ExampleSuperClass {
    private final int number;

    public ExampleSubClass(UUID uuid, String string, int number) {
      super(uuid, string);
      this.number = number;
    }

    public int number() {
      return number;
    }
  }
}

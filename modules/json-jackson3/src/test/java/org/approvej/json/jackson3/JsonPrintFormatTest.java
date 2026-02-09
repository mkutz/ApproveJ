package org.approvej.json.jackson3;

import static org.approvej.json.jackson3.JsonPrintFormat.json;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.time.LocalDate;
import java.time.Period;
import org.approvej.print.Printer;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

class JsonPrintFormatTest {

  @Test
  void constructors() {
    assertThat(new JsonPrintFormat<>()).isNotNull();
  }

  @Test
  void initializers() {
    assertThat(json()).isNotNull();
    assertThat(json(JsonMapper.builder().build())).isNotNull();
  }

  @Test
  void printer() {
    assertThat(json().printer().apply(new Person("Micha", LocalDate.of(1982, 2, 19))))
        .isEqualTo(
            """
            {
              "name" : "Micha",
              "birthday" : "1982-02-19"
            }\
            """);
  }

  @Test
  void printer_json_string() {
    assertThat(json().printer().apply("{\"name\":\"Micha\",\"birthday\":\"1982-02-19\"}"))
        .isEqualTo(
            """
            {
              "name" : "Micha",
              "birthday" : "1982-02-19"
            }\
            """);
  }

  @Test
  void printer_getter() {
    //noinspection unused
    record PersonWithGetters(String name, LocalDate birthday) {
      public String getDisplayString() {
        return "%s (%d)".formatted(name, getAge());
      }

      public boolean isBirthdayToday() {
        LocalDate today = LocalDate.now();
        return birthday.getDayOfMonth() == today.getDayOfMonth()
            && birthday.getMonth() == today.getMonth();
      }

      public int getAge() {
        return Period.between(birthday, LocalDate.now()).getYears();
      }
    }

    assertThat(json().printer().apply(new PersonWithGetters("Micha", LocalDate.of(1982, 2, 19))))
        .isEqualTo(
            """
            {
              "name" : "Micha",
              "birthday" : "1982-02-19",
              "age" : 43,
              "birthdayToday" : false,
              "displayString" : "Micha (43)"
            }\
            """);
  }

  @Test
  void printer_pojo_getters() {
    assertThat(json().printer().apply(new PersonPojo("Micha", "Kutz")))
        .isEqualTo(
            """
            {
              "firstName" : "Micha",
              "lastName" : "Kutz",
              "fullName" : "Micha Kutz",
              "initials" : "MK"
            }\
            """);
  }

  @Test
  void printer_inheritance() {
    assertThat(json().printer().apply(new Pet("Luna", "cat", 4)))
        .isEqualTo(
            """
            {
              "species" : "cat",
              "legs" : 4,
              "name" : "Luna",
              "description" : "Luna the cat"
            }\
            """);
  }

  @Test
  void printer_invalid() {
    Printer<Object> printer = json().printer();
    assertThatExceptionOfType(JsonPrinterException.class).isThrownBy(() -> printer.apply("{"));
  }

  @Test
  void filenameExtension() {
    assertThat(json().filenameExtension()).isEqualTo("json");
  }

  record Person(String name, LocalDate birthday) {}

  @SuppressWarnings("unused")
  static class PersonPojo {

    public final String firstName;
    public final String lastName;

    PersonPojo(String firstName, String lastName) {
      this.firstName = firstName;
      this.lastName = lastName;
    }

    public String getFullName() {
      return firstName + " " + lastName;
    }

    public String getInitials() {
      return "" + firstName.charAt(0) + lastName.charAt(0);
    }
  }

  @SuppressWarnings("unused")
  static class Animal {

    public final String species;
    public final int legs;

    Animal(String species, int legs) {
      this.species = species;
      this.legs = legs;
    }
  }

  @SuppressWarnings("unused")
  static class Pet extends Animal {

    public final String name;

    Pet(String name, String species, int legs) {
      super(species, legs);
      this.name = name;
    }

    public String getDescription() {
      return name + " the " + species;
    }
  }
}

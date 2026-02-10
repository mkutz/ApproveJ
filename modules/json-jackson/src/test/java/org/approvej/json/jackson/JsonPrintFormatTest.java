package org.approvej.json.jackson;

import static org.approvej.json.jackson.JsonPrintFormat.json;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import com.fasterxml.jackson.databind.json.JsonMapper;
import java.time.LocalDate;
import java.time.Period;
import org.approvej.print.PersonPojo;
import org.approvej.print.Pet;
import org.approvej.print.Printer;
import org.junit.jupiter.api.Test;

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
    record Person(String name, LocalDate birthday) {}

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
    record Person(String name, LocalDate birthday) {
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

    LocalDate birthday = LocalDate.of(1982, 2, 19);
    int age = Period.between(birthday, LocalDate.now()).getYears();
    LocalDate today = LocalDate.now();
    boolean birthdayToday =
        birthday.getDayOfMonth() == today.getDayOfMonth()
            && birthday.getMonth() == today.getMonth();

    assertThat(json().printer().apply(new Person("Micha", birthday)))
        .isEqualTo(
            """
            {
              "name" : "Micha",
              "birthday" : "1982-02-19",
              "age" : %d,
              "birthdayToday" : %s,
              "displayString" : "Micha (%d)"
            }\
            """
                .formatted(age, birthdayToday, age));
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
}

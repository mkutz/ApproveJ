package org.approvej.yaml.jackson;

import static org.approvej.yaml.jackson.YamlPrintFormat.yaml;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import java.time.LocalDate;
import java.time.Period;
import org.approvej.print.PersonPojo;
import org.approvej.print.Pet;
import org.approvej.print.Printer;
import org.junit.jupiter.api.Test;

class YamlPrintFormatTest {

  @Test
  void constructors() {
    assertThat(new YamlPrintFormat<>()).isNotNull();
  }

  @Test
  void initializers() {
    assertThat(yaml()).isNotNull();
    assertThat(yaml(YAMLMapper.builder().build().writer())).isNotNull();
    assertThat(yaml(YAMLMapper.builder().build())).isNotNull();
  }

  @Test
  void printer() {
    assertThat(yaml().printer().apply(new Person("Micha", LocalDate.of(1982, 2, 19))))
        .isEqualTo(
            """
            ---
            name: "Micha"
            birthday: "1982-02-19"
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

    assertThat(yaml().printer().apply(new Person("Micha", birthday)))
        .isEqualTo(
            """
            ---
            name: "Micha"
            birthday: "1982-02-19"
            age: %d
            birthdayToday: %s
            displayString: "Micha (%d)"
            """
                .formatted(age, birthdayToday, age));
  }

  @Test
  void printer_pojo_getters() {
    assertThat(yaml().printer().apply(new PersonPojo("Micha", "Kutz")))
        .isEqualTo(
            """
            ---
            firstName: "Micha"
            lastName: "Kutz"
            fullName: "Micha Kutz"
            initials: "MK"
            """);
  }

  @Test
  void printer_inheritance() {
    assertThat(yaml().printer().apply(new Pet("Luna", "cat", 4)))
        .isEqualTo(
            """
            ---
            species: "cat"
            legs: 4
            name: "Luna"
            description: "Luna the cat"
            """);
  }

  @Test
  void printer_failure() {
    Printer<Object> yamlPrinterNoJavaTimeModule = yaml(new ObjectMapper()).printer();
    LocalDate someLocalDate = LocalDate.of(1982, 2, 19);
    assertThatExceptionOfType(YamlPrinterException.class)
        .isThrownBy(() -> yamlPrinterNoJavaTimeModule.apply(someLocalDate))
        .withMessage("Failed to print %s".formatted(someLocalDate));
  }

  @Test
  void filenameExtension() {
    assertThat(yaml().filenameExtension()).isEqualTo("yaml");
  }

  record Person(String name, LocalDate birthday) {}
}

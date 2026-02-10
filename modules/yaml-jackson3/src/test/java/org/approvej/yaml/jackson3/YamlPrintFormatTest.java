package org.approvej.yaml.jackson3;

import static org.approvej.yaml.jackson3.YamlPrintFormat.yaml;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.Period;
import org.approvej.print.PersonPojo;
import org.approvej.print.Pet;
import org.junit.jupiter.api.Test;
import tools.jackson.dataformat.yaml.YAMLMapper;

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

    assertThat(yaml().printer().apply(new PersonWithGetters("Micha", LocalDate.of(1982, 2, 19))))
        .isEqualTo(
            """
            ---
            name: "Micha"
            birthday: "1982-02-19"
            age: 43
            birthdayToday: false
            displayString: "Micha (43)"
            """);
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
  void yamlPrinterException() {
    // Verify YamlPrinterException exists and extends RuntimeException
    assertThat(YamlPrinterException.class).isAssignableTo(RuntimeException.class);
  }

  @Test
  void filenameExtension() {
    assertThat(yaml().filenameExtension()).isEqualTo("yaml");
  }

  record Person(String name, LocalDate birthday) {}
}

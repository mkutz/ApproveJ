package org.approvej.yaml.jackson;

import static org.approvej.yaml.jackson.YamlPrintFormat.yaml;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import java.time.LocalDate;
import java.time.Period;
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

    assertThat(yaml().printer().apply(new Person("Micha", LocalDate.of(1982, 2, 19))))
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

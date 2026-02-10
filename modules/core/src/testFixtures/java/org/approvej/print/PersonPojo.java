package org.approvej.print;

@SuppressWarnings("unused")
public class PersonPojo {

  public final String firstName;
  public final String lastName;

  public PersonPojo(String firstName, String lastName) {
    this.firstName = firstName;
    this.lastName = lastName;
  }

  public String getFirstName() {
    return firstName;
  }

  public String getLastName() {
    return lastName;
  }

  public String getFullName() {
    return firstName + " " + lastName;
  }

  public String getInitials() {
    return "" + firstName.charAt(0) + lastName.charAt(0);
  }
}

package org.approvej.print;

@SuppressWarnings("unused")
public class Pet extends Animal {

  public final String name;

  public Pet(String name, String species, int legs) {
    super(species, legs);
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public String getDescription() {
    return name + " the " + species;
  }
}

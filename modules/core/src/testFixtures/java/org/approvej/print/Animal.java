package org.approvej.print;

@SuppressWarnings("unused")
public class Animal {

  public final String species;
  public final int legs;

  public Animal(String species, int legs) {
    this.species = species;
    this.legs = legs;
  }

  public String getSpecies() {
    return species;
  }

  public int getLegs() {
    return legs;
  }
}

package org.approvej;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ApprovalBuilder<T> {

  private final T originalValue;
  private final List<Scrubber<T>> scrubbers = new ArrayList<>();

  public ApprovalBuilder(T originalValue) {
    this.originalValue = originalValue;
  }

  public ApprovalBuilder<T> withScrubber(Scrubber<T> scrubber) {
    scrubbers.add(scrubber);
    return this;
  }

  public void verify() {
    T scrubbedValue = originalValue;
    for (Scrubber<T> scrubber : scrubbers) {
      scrubbedValue = scrubber.apply(scrubbedValue);
    }

    try (var writer = new FileWriter("output.txt")) {
      writer.write(scrubbedValue.toString());
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}

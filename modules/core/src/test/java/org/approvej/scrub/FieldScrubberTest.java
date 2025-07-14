package org.approvej.scrub;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.util.UUID;
import org.junit.jupiter.api.Test;

class FieldScrubberTest {

  @Test
  void apply_mutable_class() throws NoSuchFieldException {
    Thing thing = new ThingMutableClass("some thing");
    UUID replacement = new UUID(0, 0);
    FieldScrubber<Thing> scrubber =
        new FieldScrubber<>(ThingMutableClass.class.getDeclaredField("id"), replacement);

    assertThat(scrubber.apply(thing).id()).isEqualTo(replacement);
  }

  @Test
  void apply_record() throws NoSuchFieldException {
    Thing thing = new ThingRecord("some thing");
    UUID replacement = new UUID(0, 0);
    FieldScrubber<Thing> scrubber =
        new FieldScrubber<>(ThingRecord.class.getDeclaredField("id"), replacement);

    assertThatExceptionOfType(ScrubbingError.class).isThrownBy(() -> scrubber.apply(thing));
  }

  private interface Thing {
    UUID id();

    String name();
  }

  private static class ThingMutableClass implements Thing {
    private UUID id;
    private String name;

    public ThingMutableClass(String name) {
      this.id = UUID.randomUUID();
      this.name = name;
    }

    public UUID id() {
      return id;
    }

    public void setId(UUID id) {
      this.id = id;
    }

    public String name() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }
  }

  private record ThingRecord(UUID id, String name) implements Thing {
    private ThingRecord(String name) {
      this(UUID.randomUUID(), name);
    }
  }
}

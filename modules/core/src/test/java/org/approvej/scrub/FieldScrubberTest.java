package org.approvej.scrub;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.util.UUID;
import org.jspecify.annotations.NullMarked;
import org.junit.jupiter.api.Test;

class FieldScrubberTest {

  @Test
  void constructor_mutable_class_unknown_field() {
    assertThatExceptionOfType(ScrubbingError.class)
        .isThrownBy(() -> Scrubbers.field(ThingMutableClass.class, "lastName"))
        .withMessageStartingWith(
            "Cannot create FieldScrubber for field lastName on class"
                + " org.approvej.scrub.FieldScrubberTest$ThingMutableClass")
        .withCauseInstanceOf(NoSuchFieldException.class);
  }

  @Test
  void apply_mutable_class() {
    ThingMutableClass thing = new ThingMutableClass("some thing");
    UUID replacement = new UUID(0, 0);
    FieldScrubber<ThingMutableClass> scrubber =
        Scrubbers.field(ThingMutableClass.class, "id").replacement(replacement);

    assertThat(scrubber.apply(thing).id()).isEqualTo(replacement);
  }

  @Test
  void apply_immutable_class() {
    ThingImmutableClass thing = new ThingImmutableClass("some thing");
    UUID replacement = new UUID(0, 0);
    FieldScrubber<ThingImmutableClass> scrubber =
        Scrubbers.field(ThingImmutableClass.class, "id").replacement(replacement);

    assertThat(scrubber.apply(thing).id()).isEqualTo(replacement);
  }

  @Test
  void apply_record() {
    ThingRecord thing = new ThingRecord("some thing");
    UUID replacement = new UUID(0, 0);
    FieldScrubber<ThingRecord> scrubber =
        Scrubbers.field(ThingRecord.class, "id").replacement(replacement);

    assertThatExceptionOfType(ScrubbingError.class)
        .isThrownBy(() -> scrubber.apply(thing))
        .withMessageStartingWith("Failed to scrub field id on value ThingRecord")
        .withCauseInstanceOf(IllegalAccessException.class);
  }

  private interface Thing {
    UUID id();

    String name();
  }

  @NullMarked
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

  @NullMarked
  private static class ThingImmutableClass implements Thing {
    private final UUID id;
    private final String name;

    public ThingImmutableClass(String name) {
      this.id = UUID.randomUUID();
      this.name = name;
    }

    public UUID id() {
      return id;
    }

    public String name() {
      return name;
    }
  }

  @NullMarked
  private record ThingRecord(UUID id, String name) implements Thing {
    private ThingRecord(String name) {
      this(UUID.randomUUID(), name);
    }
  }
}

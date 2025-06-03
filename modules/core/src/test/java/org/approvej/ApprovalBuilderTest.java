package org.approvej;

import static java.nio.file.Files.copy;
import static java.nio.file.Files.readString;
import static java.nio.file.Files.writeString;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static java.time.format.DateTimeFormatter.ofPattern;
import static org.approvej.ApprovalBuilder.approve;
import static org.approvej.approve.Approvers.value;
import static org.approvej.approve.PathProviderBuilder.approvedPath;
import static org.approvej.approve.PathProviderBuilder.nextToTest;
import static org.approvej.print.ObjectPrinter.objectPrinter;
import static org.approvej.scrub.Scrubbers.dateTimeFormat;
import static org.approvej.scrub.Scrubbers.relativeDates;
import static org.approvej.scrub.Scrubbers.uuids;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.UUID;
import java.util.function.Function;
import org.approvej.approve.FileApprovalResult;
import org.approvej.approve.PathProvider;
import org.approvej.approve.PathProviderBuilder;
import org.approvej.review.FileReviewer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ApprovalBuilderTest {

  @TempDir private Path tempDir;

  @Test
  void approve_string_inplace() {
    approve("Some text").byValue("Some text");
  }

  @Test
  void approve_string_by_approver() {
    approve("Some text").by(value("Some text"));
  }

  @Test
  void approve_string_byFile() {
    approve("Some text").byFile();
  }

  @Test
  void approve_string_byFile_custom_pathProvider() throws IOException {
    PathProvider pathProvider =
        approvedPath(tempDir.resolve("approve_string_byFile_custom_pathProvider-approved.txt"));
    String received = "Some text";
    writeString(pathProvider.approvedPath(), received);

    approve(received).byFile(pathProvider);
  }

  @Test
  void approve_string_byFile_custom_pathProviderBuilder() throws IOException {
    PathProviderBuilder pathProviderBuilder = nextToTest();
    String received = "Some text";
    writeString(pathProviderBuilder.filenameExtension("txt").approvedPath(), received);

    approve(received).byFile(pathProviderBuilder);
  }

  @Test
  void approve_string_byFile_custom_pathProviderBuilder_path() throws IOException {
    Path path =
        tempDir.resolve("approve_string_byFile_custom_pathProviderBuilder_path-approved.txt");
    String received = "Some text";
    writeString(path, received);

    approve(received).byFile(path);
  }

  @Test
  void approve_string_byFile_custom_pathProviderBuilder_path_string() throws IOException {
    Path path =
        tempDir.resolve(
            "approve_string_byFile_custom_pathProviderBuilder_path_string-approved.txt");
    String received = "Some text";
    writeString(path, received);

    approve(received).byFile(path.toString());
  }

  @Test
  void approve_string_byValue() {
    approve("Some text").byValue("Some text");
  }

  @Test
  void approve_string_scrubbedOf() {
    String received =
        """
        This is my example text with a date: %s.
        There is also a UUID here: %s.
        Also here's a timestamp: %s, which will be partially scrubbed.
        """
            .formatted(LocalDate.now(), UUID.randomUUID(), LocalDate.now().atTime(11, 22, 33));

    String previouslyApprovedScrubbed =
        """
        This is my example text with a date: [today].
        There is also a UUID here: [uuid 1].
        Also here's a timestamp: [today]T11:22:33, which will be partially scrubbed.
        """;

    approve(received)
        .scrubbedOf(relativeDates(ofPattern("yyyy-MM-dd")))
        .scrubbedOf(uuids())
        .byValue(previouslyApprovedScrubbed);
  }

  @Test
  void approve_string_mismatch() {
    String received = "Some text";
    String previouslyApproved = "This is not the same text.";
    assertThatExceptionOfType(AssertionError.class)
        .isThrownBy(() -> approve(received).byValue(previouslyApproved))
        .withMessage(
            """
            Approval mismatch:
            expected:
              "%s"
             but was:
              "%s"
            """
                .formatted(previouslyApproved.indent(2).trim(), received.indent(2).trim()));
  }

  @Test
  void approve_pojo_default_printer() {
    approve(new Person("000000-0000-0000-00000001", "Micha", LocalDate.of(1982, 2, 19)))
        .byValue("Person[id=000000-0000-0000-00000001, name=Micha, birthday=1982-02-19]");
  }

  @Test
  void approve_pojo_printWith_function() {
    Function<Person, String> personPrinter =
        person -> "id=%s%nname=%s%nbirthday=%s".formatted(person.id, person.name, person.birthday);
    approve(new Person("000000-0000-0000-00000001", "Micha", LocalDate.of(1982, 2, 19)))
        .printWith(personPrinter)
        .byValue("id=000000-0000-0000-00000001\nname=Micha\nbirthday=1982-02-19");
  }

  @Test
  void approve_pojo_printWith_printer() {
    approve(new Person("000000-0000-0000-00000001", "Micha", LocalDate.of(1982, 2, 19)))
        .printWith(objectPrinter())
        .byValue(
            """
            Person [
              birthday=<inaccessible>,
              id=<inaccessible>,
              name=<inaccessible>
            ]""");
  }

  @Test
  void approve_reviewWith_fileReviewer() {
    PathProvider pathProvider =
        approvedPath(tempDir.resolve("approve_reviewWith_fileReviewer-approved.txt"));
    approve("Some text").reviewWith(new AutoAcceptFileReviewer()).byFile(pathProvider);
  }

  @Test
  void approve_pojo_byFile_pre_and_post_print_scrubbing() {
    approve(new Person("Micha", LocalDate.of(1982, 2, 19)))
        .scrubbedOf(person -> new Person("[scrubbed id]", person.name, person.birthday))
        .printWith(Object::toString)
        .scrubbedOf(dateTimeFormat("yyyy-MM-dd"))
        .byFile();
  }

  record Person(String id, String name, LocalDate birthday) {
    Person(String name, LocalDate birthday) {
      this(UUID.randomUUID().toString(), name, birthday);
    }
  }

  static class AutoAcceptFileReviewer implements FileReviewer {

    @Override
    public ApprovalResult apply(PathProvider pathProvider) {
      try {
        copy(pathProvider.receivedPath(), pathProvider.approvedPath(), REPLACE_EXISTING);
        return new FileApprovalResult(
            readString(pathProvider.approvedPath()).trim(),
            readString(pathProvider.receivedPath()).trim(),
            pathProvider);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
  }
}

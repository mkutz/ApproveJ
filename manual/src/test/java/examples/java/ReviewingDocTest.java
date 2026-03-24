package examples.java;

import static examples.ExampleClass.createPerson;
import static org.approvej.ApprovalBuilder.approve;
import static org.assertj.core.api.Assumptions.assumeThat;

import examples.ExampleClass.Person;
import examples.PersonYamlPrintFormat;
import java.io.IOException;
import java.time.LocalDate;
import org.approvej.review.Reviewers;
import org.junit.jupiter.api.Test;

@org.approvej.ApprovalTest
class ReviewingDocTest {

  @Test
  void approve_reviewedBy_fileReviewer() throws IOException, InterruptedException {
    assumeThat(new ProcessBuilder("which", "idea").start().waitFor()).isEqualTo(0);
    // tag::approve_reviewedBy_fileReviewer[]
    Person person = createPerson("John Doe", LocalDate.of(1990, 1, 1));

    approve(person)
        .printedAs(new PersonYamlPrintFormat())
        .reviewedBy("idea diff {receivedFile} {approvedFile}") // <1>
        .byFile(); // <2>
    // end::approve_reviewedBy_fileReviewer[]
  }

  @Test
  void approve_reviewedBy_ai() throws IOException, InterruptedException {
    assumeThat(new ProcessBuilder("which", "claude").start().waitFor()).isEqualTo(0);
    // tag::approve_reviewedBy_ai[]
    Person person = createPerson("John Doe", LocalDate.of(1990, 1, 1));

    approve(person)
        .printedAs(new PersonYamlPrintFormat())
        .reviewedBy(Reviewers.ai("claude -p --allowedTools Read")) // <1>
        .byFile(); // <2>
    // end::approve_reviewedBy_ai[]
  }

  @Test
  void approve_reviewedBy_automatic() {
    // tag::approve_reviewedBy_automatic[]
    Person person = createPerson("John Doe", LocalDate.of(1990, 1, 1));

    approve(person)
        .printedAs(new PersonYamlPrintFormat())
        .reviewedBy(Reviewers.automatic())
        .byFile();
    // end::approve_reviewedBy_automatic[]
  }
}

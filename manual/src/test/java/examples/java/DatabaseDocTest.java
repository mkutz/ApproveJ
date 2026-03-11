package examples.java;

import static org.approvej.ApprovalBuilder.approve;
import static org.approvej.database.DatabaseScrubbers.columnValue;
import static org.approvej.database.DatabaseSnapshot.query;
import static org.approvej.database.QueryResultPrintFormat.queryResult;
import static org.approvej.database.SqlPrintFormat.sql;
import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Connection;
import java.sql.Statement;
import javax.sql.DataSource;
import org.approvej.database.QueryResult;
import org.approvej.database.RecordingDataSource;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DatabaseDocTest {

  private DataSource dataSource;

  @BeforeEach
  void setUp() throws Exception {
    JdbcDataSource ds = new JdbcDataSource();
    ds.setURL("jdbc:h2:mem:doc_%s;DB_CLOSE_DELAY=-1".formatted(System.nanoTime()));
    dataSource = ds;
    try (Connection conn = dataSource.getConnection();
        Statement stmt = conn.createStatement()) {
      stmt.execute(
          "CREATE TABLE users (id INT PRIMARY KEY, name VARCHAR(100), email VARCHAR(200))");
      stmt.execute("INSERT INTO users VALUES (1, 'Alice', 'alice@test.com')");
      stmt.execute("INSERT INTO users VALUES (2, 'Bob', 'bob@test.com')");
    }
  }

  @Test
  void recording() throws Exception {
    // tag::recording[]
    RecordingDataSource recordingDs = new RecordingDataSource(dataSource);

    // ... pass recordingDs to your code instead of the real DataSource ...
    try (Connection conn = recordingDs.getConnection();
        Statement stmt = conn.createStatement()) {
      stmt.executeQuery("SELECT id, name, email FROM users WHERE id = 1");
    }

    approve(recordingDs.lastRecordedQuery()).printedAs(sql()).byFile();
    // end::recording[]
  }

  @Test
  void snapshot() {
    // tag::snapshot[]
    QueryResult result = query(dataSource, "SELECT * FROM users");
    // end::snapshot[]

    assertThat(result.rows()).hasSize(2);
  }

  @Test
  void approve_query() {
    // tag::approve[]
    approve(query(dataSource, "SELECT * FROM users"))
        .scrubbedOf(columnValue("id"))
        .printedAs(queryResult())
        .byFile();
    // end::approve[]
  }

  @Test
  void scrub() {
    // tag::scrub[]
    approve(query(dataSource, "SELECT * FROM users"))
        .scrubbedOf(columnValue("id"))
        .printedAs(queryResult())
        .byFile();
    // end::scrub[]
  }

  @Test
  void scrub_custom() {
    // tag::scrub_custom[]
    approve(query(dataSource, "SELECT * FROM users"))
        .scrubbedOf(columnValue("id").replacement("***"))
        .printedAs(queryResult())
        .byFile();
    // end::scrub_custom[]
  }
}

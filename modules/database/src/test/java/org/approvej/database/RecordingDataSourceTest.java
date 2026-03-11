package org.approvej.database;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import javax.sql.DataSource;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RecordingDataSourceTest {

  private RecordingDataSource recordingDataSource;

  @BeforeEach
  void setUp() throws Exception {
    JdbcDataSource ds = new JdbcDataSource();
    ds.setURL("jdbc:h2:mem:recording_%s;DB_CLOSE_DELAY=-1".formatted(System.nanoTime()));
    recordingDataSource = new RecordingDataSource(ds);
    try (Connection conn = recordingDataSource.getConnection();
        Statement stmt = conn.createStatement()) {
      stmt.execute("CREATE TABLE users (id INT PRIMARY KEY, name VARCHAR(100))");
    }
  }

  @Test
  void recordedQueries_statement() throws Exception {
    try (Connection conn = recordingDataSource.getConnection();
        Statement stmt = conn.createStatement()) {
      recordingDataSource.resetRecordedQueries();
      stmt.execute("INSERT INTO users VALUES (1, 'Alice')");
      stmt.executeUpdate("INSERT INTO users VALUES (2, 'Bob')");
      stmt.executeQuery("SELECT * FROM users");
    }

    assertThat(recordingDataSource.recordedQueries())
        .containsExactly(
            "INSERT INTO users VALUES (1, 'Alice')",
            "INSERT INTO users VALUES (2, 'Bob')",
            "SELECT * FROM users");
  }

  @Test
  void recordedQueries_prepared_statement() throws Exception {
    try (Connection conn = recordingDataSource.getConnection();
        PreparedStatement pstmt = conn.prepareStatement("INSERT INTO users VALUES (?, ?)")) {
      recordingDataSource.resetRecordedQueries();
      pstmt.setInt(1, 1);
      pstmt.setString(2, "Alice");
      pstmt.execute();
      pstmt.setInt(1, 2);
      pstmt.setString(2, "Bob");
      pstmt.execute();
    }

    assertThat(recordingDataSource.recordedQueries())
        .containsExactly("INSERT INTO users VALUES (?, ?)", "INSERT INTO users VALUES (?, ?)");
  }

  @Test
  void lastRecordedQuery() throws Exception {
    try (Connection conn = recordingDataSource.getConnection();
        Statement stmt = conn.createStatement()) {
      recordingDataSource.resetRecordedQueries();
      stmt.execute("INSERT INTO users VALUES (1, 'Alice')");
      stmt.execute("SELECT * FROM users");
    }

    assertThat(recordingDataSource.lastRecordedQuery()).isEqualTo("SELECT * FROM users");
  }

  @Test
  void resetRecordedQueries() throws Exception {
    try (Connection conn = recordingDataSource.getConnection();
        Statement stmt = conn.createStatement()) {
      stmt.execute("INSERT INTO users VALUES (1, 'Alice')");
    }

    assertThat(recordingDataSource.recordedQueries()).isNotEmpty();

    recordingDataSource.resetRecordedQueries();

    assertThat(recordingDataSource.recordedQueries()).isEmpty();
  }

  @Test
  void getConnection_username_password() throws Exception {
    JdbcDataSource ds = new JdbcDataSource();
    ds.setURL("jdbc:h2:mem:auth_%s;DB_CLOSE_DELAY=-1".formatted(System.nanoTime()));
    DataSource recording = new RecordingDataSource(ds);

    try (Connection conn = recording.getConnection("sa", "")) {
      assertThat(conn).isNotNull();
    }
  }
}

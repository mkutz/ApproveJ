package org.approvej.database.jdbc;

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
    JdbcDataSource jdbcDataSource = new JdbcDataSource();
    jdbcDataSource.setURL(
        "jdbc:h2:mem:recording_%s;DB_CLOSE_DELAY=-1".formatted(System.nanoTime()));
    recordingDataSource = new RecordingDataSource(jdbcDataSource);
    try (Connection connection = recordingDataSource.getConnection();
        Statement statement = connection.createStatement()) {
      statement.execute("CREATE TABLE users (id INT PRIMARY KEY, name VARCHAR(100))");
    }
  }

  @Test
  void recordedQueries_statement() throws Exception {
    try (Connection connection = recordingDataSource.getConnection();
        Statement statement = connection.createStatement()) {
      recordingDataSource.resetRecordedQueries();
      statement.execute("INSERT INTO users VALUES (1, 'Alice')");
      statement.executeUpdate("INSERT INTO users VALUES (2, 'Bob')");
      statement.executeQuery("SELECT * FROM users");
    }

    assertThat(recordingDataSource.recordedQueries())
        .containsExactly(
            "INSERT INTO users VALUES (1, 'Alice')",
            "INSERT INTO users VALUES (2, 'Bob')",
            "SELECT * FROM users");
  }

  @Test
  void recordedQueries_prepared_statement() throws Exception {
    try (Connection connection = recordingDataSource.getConnection();
        PreparedStatement statement =
            connection.prepareStatement("INSERT INTO users VALUES (?, ?)")) {
      recordingDataSource.resetRecordedQueries();
      statement.setInt(1, 1);
      statement.setString(2, "Alice");
      statement.execute();
      statement.setInt(1, 2);
      statement.setString(2, "Bob");
      statement.execute();
    }

    assertThat(recordingDataSource.recordedQueries())
        .containsExactly("INSERT INTO users VALUES (?, ?)", "INSERT INTO users VALUES (?, ?)");
  }

  @Test
  void lastRecordedQuery() throws Exception {
    try (Connection connection = recordingDataSource.getConnection();
        Statement statement = connection.createStatement()) {
      recordingDataSource.resetRecordedQueries();
      statement.execute("INSERT INTO users VALUES (1, 'Alice')");
      statement.execute("SELECT * FROM users");
    }

    assertThat(recordingDataSource.lastRecordedQuery()).isEqualTo("SELECT * FROM users");
  }

  @Test
  void resetRecordedQueries() throws Exception {
    try (Connection connection = recordingDataSource.getConnection();
        Statement statement = connection.createStatement()) {
      statement.execute("INSERT INTO users VALUES (1, 'Alice')");
    }

    assertThat(recordingDataSource.recordedQueries()).isNotEmpty();

    recordingDataSource.resetRecordedQueries();

    assertThat(recordingDataSource.recordedQueries()).isEmpty();
  }

  @Test
  void getConnection_username_password() throws Exception {
    JdbcDataSource dataSource = new JdbcDataSource();
    dataSource.setURL("jdbc:h2:mem:auth_%s;DB_CLOSE_DELAY=-1".formatted(System.nanoTime()));
    DataSource recording = new RecordingDataSource(dataSource);

    try (Connection connection = recording.getConnection("sa", "")) {
      assertThat(connection).isNotNull();
    }
  }
}

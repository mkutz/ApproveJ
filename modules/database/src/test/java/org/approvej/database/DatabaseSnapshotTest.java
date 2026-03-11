package org.approvej.database;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.sql.Connection;
import java.sql.Statement;
import javax.sql.DataSource;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DatabaseSnapshotTest {

  private DataSource dataSource;

  @BeforeEach
  void setUp() throws Exception {
    JdbcDataSource ds = new JdbcDataSource();
    ds.setURL("jdbc:h2:mem:test_%s;DB_CLOSE_DELAY=-1".formatted(System.nanoTime()));
    dataSource = ds;
    try (Connection conn = dataSource.getConnection();
        Statement stmt = conn.createStatement()) {
      stmt.execute(
          "CREATE TABLE users (id INT PRIMARY KEY, name VARCHAR(100), email VARCHAR(200))");
      stmt.execute("INSERT INTO users VALUES (1, 'Alice', 'alice@test.com')");
      stmt.execute("INSERT INTO users VALUES (2, 'Bob', 'bob@test.com')");
      stmt.execute("INSERT INTO users VALUES (3, NULL, 'charlie@test.com')");
    }
  }

  @Test
  void query() {
    QueryResult result = DatabaseSnapshot.query(dataSource, "SELECT * FROM users WHERE id <= 2");

    assertThat(result.query()).isEqualTo("SELECT * FROM users WHERE id <= 2");
    assertThat(result.columnNames()).containsExactly("ID", "NAME", "EMAIL");
    assertThat(result.rows()).hasSize(2);
    assertThat(result.rows().get(0)).containsExactly("1", "Alice", "alice@test.com");
    assertThat(result.rows().get(1)).containsExactly("2", "Bob", "bob@test.com");
  }

  @Test
  void query_null_values() {
    QueryResult result = DatabaseSnapshot.query(dataSource, "SELECT * FROM users WHERE id = 3");

    assertThat(result.rows()).hasSize(1);
    assertThat(result.rows().get(0)).containsExactly("3", "NULL", "charlie@test.com");
  }

  @Test
  void query_empty_result() {
    QueryResult result = DatabaseSnapshot.query(dataSource, "SELECT * FROM users WHERE id = 999");

    assertThat(result.columnNames()).containsExactly("ID", "NAME", "EMAIL");
    assertThat(result.rows()).isEmpty();
  }

  @Test
  void query_bad_sql() {
    assertThatThrownBy(() -> DatabaseSnapshot.query(dataSource, "SELECT * FROM nonexistent"))
        .isInstanceOf(DatabaseSnapshotException.class);
  }
}

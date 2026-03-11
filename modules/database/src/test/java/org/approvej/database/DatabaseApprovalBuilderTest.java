package org.approvej.database;

import static org.approvej.ApprovalBuilder.approve;
import static org.approvej.database.DatabaseScrubbers.columnValue;
import static org.approvej.database.DatabaseSnapshot.query;
import static org.approvej.database.QueryResultPrintFormat.queryResult;

import java.sql.Connection;
import java.sql.Statement;
import javax.sql.DataSource;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DatabaseApprovalBuilderTest {

  private DataSource dataSource;

  @BeforeEach
  void setUp() throws Exception {
    JdbcDataSource ds = new JdbcDataSource();
    ds.setURL("jdbc:h2:mem:approval_%s;DB_CLOSE_DELAY=-1".formatted(System.nanoTime()));
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
  void approve_query() {
    approve(query(dataSource, "SELECT * FROM users"))
        .scrubbedOf(columnValue("id"))
        .printedAs(queryResult())
        .byFile();
  }
}

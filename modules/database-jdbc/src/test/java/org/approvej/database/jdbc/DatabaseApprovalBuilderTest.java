package org.approvej.database.jdbc;

import static org.approvej.ApprovalBuilder.approve;
import static org.approvej.database.jdbc.DatabaseScrubbers.columnValue;
import static org.approvej.database.jdbc.DatabaseSnapshot.query;
import static org.approvej.database.jdbc.MarkdownTablePrintFormat.markdownTable;

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
    JdbcDataSource datasource = new JdbcDataSource();
    datasource.setURL("jdbc:h2:mem:approval_%s;DB_CLOSE_DELAY=-1".formatted(System.nanoTime()));
    dataSource = datasource;
    try (Connection connection = dataSource.getConnection();
        Statement statement = connection.createStatement()) {
      statement.execute(
          "CREATE TABLE users (id INT PRIMARY KEY, name VARCHAR(100), email VARCHAR(200))");
      statement.execute("INSERT INTO users VALUES (1, 'Alice', 'alice@test.com')");
      statement.execute("INSERT INTO users VALUES (2, 'Bob', 'bob@test.com')");
    }
  }

  @Test
  void approve_query() {
    approve(query(dataSource, "SELECT * FROM users"))
        .scrubbedOf(columnValue("id"))
        .printedAs(markdownTable())
        .byFile();
  }
}

package org.approvej.database.jdbc;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javax.sql.DataSource;
import org.jspecify.annotations.NullMarked;

/** Utility for snapshotting database query results into a {@link QueryResult}. */
@NullMarked
public final class DatabaseSnapshot {

  private DatabaseSnapshot() {}

  /**
   * Executes the given SQL query against the {@link DataSource} and returns the result as a {@link
   * QueryResult}.
   *
   * @param dataSource the data source to query
   * @param sql the SQL query to execute
   * @return the query result
   * @throws DatabaseSnapshotException if a SQL error occurs
   */
  public static QueryResult query(DataSource dataSource, String sql) {
    try (Connection connection = dataSource.getConnection();
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery(sql)) {
      return QueryResult.of(resultSet);
    } catch (SQLException e) {
      throw new DatabaseSnapshotException(e);
    }
  }
}

package org.approvej.database;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
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

      ResultSetMetaData metaData = resultSet.getMetaData();
      int columnCount = metaData.getColumnCount();

      List<String> columnNames = new ArrayList<>(columnCount);
      for (int i = 1; i <= columnCount; i++) {
        columnNames.add(metaData.getColumnLabel(i));
      }

      List<List<String>> rows = new ArrayList<>();
      while (resultSet.next()) {
        List<String> row = new ArrayList<>(columnCount);
        for (int i = 1; i <= columnCount; i++) {
          row.add(Objects.toString(resultSet.getObject(i), "NULL"));
        }
        rows.add(row);
      }

      return new QueryResult(sql, columnNames, rows);
    } catch (SQLException e) {
      throw new DatabaseSnapshotException(e);
    }
  }
}

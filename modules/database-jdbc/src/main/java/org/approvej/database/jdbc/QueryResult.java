package org.approvej.database.jdbc;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.jspecify.annotations.NullMarked;

/**
 * Represents the result of a database query as column names and row data.
 *
 * <p>Use {@link #of(ResultSet)} to create an instance from a JDBC {@link ResultSet}, or construct
 * one directly for testing purposes.
 *
 * @param columnNames the column headers
 * @param rows each row as a list of string-rendered cell values
 */
@NullMarked
public record QueryResult(List<String> columnNames, List<List<String>> rows) {

  /** Compact constructor to ensure immutability. */
  public QueryResult {
    columnNames = List.copyOf(columnNames);
    rows = rows.stream().map(List::copyOf).toList();
  }

  /**
   * Materializes the given {@link ResultSet} into a {@link QueryResult}.
   *
   * <p>The result set is consumed from its current position to the end. It is not closed by this
   * method.
   *
   * @param resultSet the result set to read
   * @return the materialized query result
   * @throws SQLException if a database access error occurs
   */
  public static QueryResult of(ResultSet resultSet) throws SQLException {
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
        row.add(Objects.toString(resultSet.getObject(i), "<null>"));
      }
      rows.add(row);
    }

    return new QueryResult(columnNames, rows);
  }
}

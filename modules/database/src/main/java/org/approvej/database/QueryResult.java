package org.approvej.database;

import java.util.Collections;
import java.util.List;
import org.jspecify.annotations.NullMarked;

/**
 * Represents the result of a database query, including the SQL query string, column names, and row
 * data.
 *
 * @param query the SQL query that produced this result
 * @param columnNames the column headers
 * @param rows each row as a list of string-rendered cell values
 */
@NullMarked
public record QueryResult(String query, List<String> columnNames, List<List<String>> rows) {

  /** Compact constructor to ensure immutability. */
  public QueryResult {
    columnNames = Collections.unmodifiableList(List.copyOf(columnNames));
    rows =
        Collections.unmodifiableList(
            rows.stream().map(row -> Collections.unmodifiableList(List.copyOf(row))).toList());
  }
}

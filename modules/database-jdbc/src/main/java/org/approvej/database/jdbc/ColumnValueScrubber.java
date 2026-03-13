package org.approvej.database.jdbc;

import java.util.ArrayList;
import java.util.List;
import org.approvej.scrub.Replacement;
import org.approvej.scrub.Scrubber;
import org.jspecify.annotations.NullMarked;

/**
 * A {@link Scrubber} for {@link QueryResult}s that replaces all values in a specific column.
 *
 * <p>By default, a column "my_column" will be replaced with "[my_column]". This can be changed via
 * {@link #replacement(Replacement)}.
 *
 * @param columnName the name of the column to be scrubbed
 * @param replacement the replacement function for the column values
 */
@NullMarked
public record ColumnValueScrubber(String columnName, Replacement<String> replacement)
    implements Scrubber<ColumnValueScrubber, QueryResult, String> {

  /**
   * Creates a {@link Scrubber} for the given columnName with the default replacement.
   *
   * @param columnName the name of the column to be scrubbed
   */
  public ColumnValueScrubber(String columnName) {
    this(columnName, (match, count) -> "[%s]".formatted(columnName));
  }

  @Override
  public QueryResult apply(QueryResult result) {
    int columnIndex = -1;
    for (int i = 0; i < result.columnNames().size(); i++) {
      if (result.columnNames().get(i).equalsIgnoreCase(columnName)) {
        columnIndex = i;
        break;
      }
    }
    if (columnIndex < 0) {
      return result;
    }

    List<List<String>> newRows = new ArrayList<>(result.rows().size());
    for (var row : result.rows()) {
      List<String> newRow = new ArrayList<>(row);
      newRow.set(columnIndex, replacement.apply(row.get(columnIndex), 1));
      newRows.add(newRow);
    }

    return new QueryResult(result.columnNames(), newRows);
  }

  @Override
  public ColumnValueScrubber replacement(Replacement<String> replacement) {
    return new ColumnValueScrubber(columnName, replacement);
  }
}

package org.approvej.database.jdbc;

import org.jspecify.annotations.NullMarked;

/** Collection of predefined database-related {@link org.approvej.scrub.Scrubber}s. */
@NullMarked
public final class DatabaseScrubbers {

  private DatabaseScrubbers() {}

  /**
   * Creates a {@link ColumnValueScrubber} for the given column name.
   *
   * @param columnName the name of the column to be scrubbed
   * @return the new {@link ColumnValueScrubber}
   */
  public static ColumnValueScrubber columnValue(String columnName) {
    return new ColumnValueScrubber(columnName);
  }
}

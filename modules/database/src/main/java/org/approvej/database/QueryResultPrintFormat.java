package org.approvej.database;

import org.approvej.print.PrintFormat;
import org.approvej.print.Printer;
import org.jspecify.annotations.NullMarked;

/**
 * {@link PrintFormat} implementation for {@link QueryResult} that prints the result as a
 * Markdown-compatible ASCII table.
 *
 * <p>For example, a query {@code SELECT * FROM users} with two rows would be printed like this:
 *
 * <pre>
 * query: SELECT * FROM users
 *
 * | id | name  | email          |
 * |----|-------|----------------|
 * | 1  | Alice | alice@test.com |
 * | 2  | Bob   | bob@test.com   |
 * </pre>
 */
@NullMarked
public class QueryResultPrintFormat implements PrintFormat<QueryResult> {

  /** Default constructor. */
  public QueryResultPrintFormat() {
    // No initialization needed
  }

  @Override
  public Printer<QueryResult> printer() {
    return (QueryResult result) -> {
      StringBuilder sb = new StringBuilder();
      sb.append("query: %s".formatted(result.query()));

      int columnCount = result.columnNames().size();
      int[] widths = new int[columnCount];
      for (int i = 0; i < columnCount; i++) {
        widths[i] = result.columnNames().get(i).length();
      }
      for (var row : result.rows()) {
        for (int i = 0; i < columnCount; i++) {
          widths[i] = Math.max(widths[i], row.get(i).length());
        }
      }

      sb.append("\n\n");
      appendRow(sb, result.columnNames(), widths);
      sb.append("\n");
      appendSeparator(sb, widths);
      for (var row : result.rows()) {
        sb.append("\n");
        appendRow(sb, row, widths);
      }

      return sb.toString();
    };
  }

  private static void appendRow(StringBuilder sb, java.util.List<String> values, int[] widths) {
    sb.append("|");
    for (int i = 0; i < values.size(); i++) {
      sb.append(" %-*s |".replace("*", String.valueOf(widths[i])).formatted(values.get(i)));
    }
  }

  private static void appendSeparator(StringBuilder sb, int[] widths) {
    sb.append("|");
    for (int width : widths) {
      sb.append("-".repeat(width + 2));
      sb.append("|");
    }
  }

  @Override
  public String filenameExtension() {
    return "md";
  }

  /**
   * Creates and returns a new {@link QueryResultPrintFormat} instance.
   *
   * @return the new instance
   */
  public static QueryResultPrintFormat queryResult() {
    return new QueryResultPrintFormat();
  }
}

package org.approvej.database.jdbc;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.approvej.print.PrintFormat;
import org.approvej.print.Printer;
import org.jspecify.annotations.NullMarked;

/**
 * {@link PrintFormat} implementation for SQL strings that formats them with line breaks and
 * indentation for readability.
 *
 * <p>Major SQL clauses (SELECT, FROM, WHERE, etc.) are placed on their own lines. Column lists and
 * conditions are indented. This makes it easy to review SQL in approved files.
 *
 * <p>For example, {@code SELECT id, name FROM users WHERE active = 1 ORDER BY name} becomes:
 *
 * <pre>
 * SELECT
 *   id,
 *   name
 * FROM
 *   users
 * WHERE
 *   active = 1
 * ORDER BY
 *   name
 * </pre>
 */
@NullMarked
public class SqlPrintFormat implements PrintFormat<String> {

  private static final Set<String> CLAUSE_KEYWORDS =
      Set.of(
          "SELECT",
          "FROM",
          "WHERE",
          "ORDER BY",
          "GROUP BY",
          "HAVING",
          "LIMIT",
          "OFFSET",
          "INSERT INTO",
          "VALUES",
          "UPDATE",
          "SET",
          "DELETE FROM",
          "UNION",
          "UNION ALL",
          "EXCEPT",
          "INTERSECT",
          "RETURNING");

  private static final Set<String> SUB_KEYWORDS =
      Set.of(
          "AND",
          "OR",
          "ON",
          "INNER JOIN",
          "LEFT JOIN",
          "RIGHT JOIN",
          "FULL JOIN",
          "CROSS JOIN",
          "LEFT OUTER JOIN",
          "RIGHT OUTER JOIN",
          "FULL OUTER JOIN",
          "JOIN");

  private static final Pattern TOKEN_PATTERN =
      Pattern.compile(
          "('[^']*+(?:''[^']*+)*+')" // single-quoted string (SQL escapes via '')
              + "|(\"[^\"]*+(?:\"\"[^\"]*+)*+\")" // double-quoted identifier
              + "|(\\()" // open paren
              + "|(\\))" // close paren
              + "|(,)" // comma
              + "|([^\\s()',\"]+)" // word
              + "|(\\s+)"); // whitespace

  /** Default constructor. */
  public SqlPrintFormat() {
    // No initialization needed
  }

  @Override
  public Printer<String> printer() {
    return SqlPrintFormat::formatSql;
  }

  @Override
  public String filenameExtension() {
    return "sql";
  }

  /**
   * Creates and returns a new {@link SqlPrintFormat} instance.
   *
   * @return the new instance
   */
  public static SqlPrintFormat sql() {
    return new SqlPrintFormat();
  }

  static String formatSql(String sql) {
    List<String> tokens = mergeKeywords(tokenize(sql.strip()));
    SqlFormatter formatter = new SqlFormatter();
    for (String token : tokens) {
      formatter.process(token);
    }
    return formatter.toString();
  }

  private static List<String> tokenize(String sql) {
    List<String> tokens = new ArrayList<>();
    Matcher matcher = TOKEN_PATTERN.matcher(sql);
    while (matcher.find()) {
      tokens.add(matcher.group());
    }
    return tokens;
  }

  private static List<String> mergeKeywords(List<String> tokens) {
    List<String> merged = new ArrayList<>();
    for (int index = 0; index < tokens.size(); index++) {
      String token = tokens.get(index);

      if (!token.isBlank()) {
        int secondWordIndex = findSecondWordIndex(tokens, index);
        String twoWordKeyword =
            secondWordIndex > 0 ? (token + " " + tokens.get(secondWordIndex)).toUpperCase() : "";

        if (CLAUSE_KEYWORDS.contains(twoWordKeyword) || SUB_KEYWORDS.contains(twoWordKeyword)) {
          merged.add(twoWordKeyword);
          index = secondWordIndex; // Skip ahead
        } else {
          merged.add(token);
        }
      }
    }
    return merged;
  }

  private static int findSecondWordIndex(List<String> tokens, int position) {
    int next = position + 1;
    while (next < tokens.size() && tokens.get(next).isBlank()) {
      next++;
    }
    if (next < tokens.size() && isWord(tokens.get(next))) {
      return next;
    }
    return -1;
  }

  private static boolean isWord(String token) {
    return !token.equals("(")
        && !token.equals(")")
        && !token.equals(",")
        && !token.startsWith("'")
        && !token.startsWith("\"");
  }

  private static class SqlFormatter {

    private final StringBuilder result = new StringBuilder();
    private boolean afterClauseKeyword = false;
    private boolean needsSpace = false;

    void process(String token) {
      String upper = token.toUpperCase();
      if (CLAUSE_KEYWORDS.contains(upper)) {
        appendClause(upper);
      } else if (SUB_KEYWORDS.contains(upper)) {
        appendSubKeyword(upper);
      } else if (",".equals(token)) {
        appendComma();
      } else if ("(".equals(token)) {
        appendOpenParen();
      } else if (")".equals(token)) {
        appendCloseParen();
      } else {
        appendValue(token);
      }
    }

    private void appendClause(String keyword) {
      if (!result.isEmpty()) {
        result.append("\n");
      }
      result.append(keyword);
      afterClauseKeyword = true;
      needsSpace = false;
    }

    private void appendSubKeyword(String keyword) {
      result.append("\n");
      result.append("  ");
      result.append(keyword);
      afterClauseKeyword = false;
      needsSpace = true;
    }

    private void appendComma() {
      result.append(",\n");
      result.append("  ");
      afterClauseKeyword = false;
      needsSpace = false;
    }

    private void appendOpenParen() {
      if (afterClauseKeyword) {
        result.append("\n");
        result.append("  ");
        afterClauseKeyword = false;
      }
      result.append("(");
      needsSpace = false;
    }

    private void appendCloseParen() {
      result.append(")");
      afterClauseKeyword = false;
      needsSpace = true;
    }

    private void appendValue(String token) {
      if (afterClauseKeyword) {
        result.append("\n");
        result.append("  ");
        afterClauseKeyword = false;
      } else if (needsSpace) {
        result.append(" ");
      }
      result.append(token);
      needsSpace = true;
    }

    @Override
    public String toString() {
      return result.toString();
    }
  }
}

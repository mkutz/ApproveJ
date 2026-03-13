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
    List<String> tokens = tokenize(sql.strip());
    StringBuilder result = new StringBuilder();
    int indent = 0;
    boolean afterClauseKeyword = false;
    boolean needsSpace = false;

    for (int i = 0; i < tokens.size(); i++) {
      String token = tokens.get(i);

      if (token.isBlank()) {
        continue;
      }

      // Check for two-word keywords first
      int secondWordIndex = findSecondWordIndex(tokens, i);
      String twoWordKeyword =
          secondWordIndex > 0 ? (token + " " + tokens.get(secondWordIndex)).toUpperCase() : null;

      if (twoWordKeyword != null
          && (isClauseKeyword(twoWordKeyword) || isSubKeyword(twoWordKeyword))) {
        boolean isClause = isClauseKeyword(twoWordKeyword);
        if (isClause) {
          if (!result.isEmpty()) {
            result.append("\n");
          }
          result.append("  ".repeat(indent));
        } else {
          result.append("\n");
          result.append("  ".repeat(indent + 1));
        }
        result.append(twoWordKeyword);
        i = secondWordIndex;
        afterClauseKeyword = isClause;
        needsSpace = !isClause;
      } else if (isClauseKeyword(token.toUpperCase())) {
        if (!result.isEmpty()) {
          result.append("\n");
        }
        result.append("  ".repeat(indent));
        result.append(token.toUpperCase());
        afterClauseKeyword = true;
        needsSpace = false;
      } else if (isSubKeyword(token.toUpperCase())) {
        result.append("\n");
        result.append("  ".repeat(indent + 1));
        result.append(token.toUpperCase());
        afterClauseKeyword = false;
        needsSpace = true;
      } else if (",".equals(token)) {
        result.append(",");
        result.append("\n");
        result.append("  ".repeat(indent + 1));
        afterClauseKeyword = false;
        needsSpace = false;
      } else if ("(".equals(token)) {
        if (afterClauseKeyword) {
          result.append("\n");
          result.append("  ".repeat(indent + 1));
          afterClauseKeyword = false;
        }
        result.append("(");
        needsSpace = false;
      } else if (")".equals(token)) {
        result.append(")");
        afterClauseKeyword = false;
        needsSpace = true;
      } else {
        if (afterClauseKeyword) {
          result.append("\n");
          result.append("  ".repeat(indent + 1));
          afterClauseKeyword = false;
        } else if (needsSpace) {
          result.append(" ");
        }
        result.append(token);
        needsSpace = true;
      }
    }

    return result.toString();
  }

  private static List<String> tokenize(String sql) {
    List<String> tokens = new ArrayList<>();
    Matcher matcher = TOKEN_PATTERN.matcher(sql);
    while (matcher.find()) {
      tokens.add(matcher.group());
    }
    return tokens;
  }

  /**
   * Finds the index of the second word after position i, skipping whitespace. Returns -1 if no
   * second word is found.
   */
  private static int findSecondWordIndex(List<String> tokens, int i) {
    int next = i + 1;
    while (next < tokens.size() && tokens.get(next).isBlank()) {
      next++;
    }
    if (next < tokens.size() && !tokens.get(next).isBlank() && isWord(tokens.get(next))) {
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

  private static boolean isClauseKeyword(String upper) {
    return CLAUSE_KEYWORDS.contains(upper);
  }

  private static boolean isSubKeyword(String upper) {
    return SUB_KEYWORDS.contains(upper);
  }
}

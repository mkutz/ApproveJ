package org.approvej.approve;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import org.jspecify.annotations.NullMarked;

/**
 * Rewrites {@code byValue()} string arguments in Java source files.
 *
 * <p>When the reviewer is configured as {@code automatic}, this class replaces the string argument
 * of a {@code byValue()} call with a text block containing the new received value. Only the
 * argument itself is replaced — the rest of the file is left untouched.
 */
@NullMarked
public class InlineValueRewriter {

  private static final ConcurrentHashMap<Path, ReentrantLock> FILE_LOCKS =
      new ConcurrentHashMap<>();

  private InlineValueRewriter() {}

  /**
   * Rewrites the {@code byValue()} call in the given test method, replacing the string argument
   * with a text block containing the new value.
   *
   * <p>The method is located in the source file by its name, then the {@code byValue()} call within
   * that method is rewritten. This approach is robust even when earlier rewrites have shifted line
   * numbers in the same file.
   *
   * @param sourcePath path to the Java source file
   * @param methodName the name of the test method containing the {@code byValue()} call
   * @param newValue the new value to write as a text block argument
   */
  public static void rewrite(Path sourcePath, String methodName, String newValue) {
    if (!sourcePath.toString().endsWith(".java")) {
      throw new InlineValueError(
          "Inline value rewriting is only supported in Java source files: " + sourcePath);
    }

    ReentrantLock lock =
        FILE_LOCKS.computeIfAbsent(sourcePath.toAbsolutePath(), path -> new ReentrantLock());
    lock.lock();
    try {
      String content = Files.readString(sourcePath, StandardCharsets.UTF_8);
      String rewritten = rewriteContent(content, methodName, newValue);
      Files.writeString(sourcePath, rewritten, StandardCharsets.UTF_8);
    } catch (IOException exception) {
      throw new InlineValueError("Failed to rewrite inline value in " + sourcePath, exception);
    } finally {
      lock.unlock();
    }
  }

  static String rewriteContent(String content, String methodName, String newValue) {
    int methodOffset = findMethodOffset(content, methodName);
    int methodEnd = findMethodEnd(content, methodOffset);
    int byValueOffset = findByValueOffset(content, methodOffset, methodEnd);

    int argumentStart = byValueOffset + "byValue(".length();
    int argumentEnd = findArgumentEnd(content, argumentStart);

    String indent = detectIndent(content, byValueOffset);
    String indentUnit = detectIndentUnit(content);
    String bodyIndent = indent + indentUnit;
    String textBlock = buildTextBlock(newValue, bodyIndent);

    return content.substring(0, argumentStart) + textBlock + content.substring(argumentEnd);
  }

  private static int findMethodOffset(String content, String methodName) {
    String pattern = methodName + "(";
    int found = content.indexOf(pattern);
    if (found < 0) {
      throw new InlineValueError("Could not find method " + methodName + " in source file");
    }
    return found;
  }

  private static int findMethodEnd(String content, int methodOffset) {
    int depth = 0;
    int position = content.indexOf('{', methodOffset);
    if (position < 0) {
      return content.length();
    }
    while (position < content.length()) {
      char character = content.charAt(position);
      if (character == '{') {
        depth++;
      } else if (character == '}') {
        depth--;
        if (depth == 0) {
          return position;
        }
      }
      position++;
    }
    return content.length();
  }

  private static int findByValueOffset(String content, int searchFrom, int searchTo) {
    for (int i = searchFrom; i < searchTo; i++) {
      if (content.startsWith("byValue(", i)) {
        return i;
      }
    }
    throw new InlineValueError("Could not find byValue( in the test method");
  }

  private static int findArgumentEnd(String content, int start) {
    int depth = 1;
    boolean inString = false;
    boolean inTextBlock = false;
    int position = start;

    if (position + 3 <= content.length() && content.startsWith("\"\"\"", position)) {
      inTextBlock = true;
      position += 3;
    } else if (position < content.length() && content.charAt(position) == '"') {
      inString = true;
      position++;
    }

    while (position < content.length()) {
      char character = content.charAt(position);

      if (inTextBlock) {
        if (position + 3 <= content.length() && content.startsWith("\"\"\"", position)) {
          inTextBlock = false;
          position += 3;
          continue;
        }
        position++;
        continue;
      }

      if (inString) {
        if (character == '\\') {
          position += 2;
          continue;
        }
        if (character == '"') {
          inString = false;
        }
        position++;
        continue;
      }

      if (character == '"') {
        if (position + 3 <= content.length() && content.startsWith("\"\"\"", position)) {
          inTextBlock = true;
          position += 3;
          continue;
        }
        inString = true;
        position++;
        continue;
      }

      if (character == '(') {
        depth++;
      } else if (character == ')') {
        depth--;
        if (depth == 0) {
          return position;
        }
      }
      position++;
    }

    throw new InlineValueError("Could not find closing parenthesis for byValue(");
  }

  private static String detectIndentUnit(String content) {
    int position = 0;
    while (position < content.length()) {
      int newline = content.indexOf('\n', position);
      if (newline < 0) {
        break;
      }
      int lineStart = newline + 1;
      if (lineStart < content.length() && content.charAt(lineStart) == '\t') {
        return "\t";
      }
      int spaces = 0;
      while (lineStart + spaces < content.length() && content.charAt(lineStart + spaces) == ' ') {
        spaces++;
      }
      if (spaces > 0
          && lineStart + spaces < content.length()
          && !Character.isWhitespace(content.charAt(lineStart + spaces))) {
        return " ".repeat(spaces);
      }
      position = lineStart;
    }
    return "    ";
  }

  private static String detectIndent(String content, int byValueOffset) {
    int lineStart = content.lastIndexOf('\n', byValueOffset);
    lineStart = (lineStart < 0) ? 0 : lineStart + 1;
    StringBuilder indent = new StringBuilder();
    for (int i = lineStart; i < byValueOffset && Character.isWhitespace(content.charAt(i)); i++) {
      indent.append(content.charAt(i));
    }
    return indent.toString();
  }

  private static String buildTextBlock(String value, String bodyIndent) {
    String escaped = value.replace("\\", "\\\\").replace("\"\"\"", "\\\"\"\"");
    StringBuilder textBlock = new StringBuilder();
    textBlock.append("\"\"\"\n");
    for (String valueLine : escaped.split("\n", -1)) {
      textBlock.append(bodyIndent).append(valueLine).append("\n");
    }
    textBlock.append(bodyIndent).append("\"\"\"");
    return textBlock.toString();
  }
}

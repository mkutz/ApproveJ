package org.approvej.approve;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import org.jspecify.annotations.NullMarked;

/**
 * Rewrites {@code byValue()} string arguments in test source files.
 *
 * <p>When {@link org.approvej.configuration.Configuration#autoUpdateInlineValues()} is enabled,
 * this class replaces the string argument of a {@code byValue()} call with a text block containing
 * the new received value. Only the argument itself is replaced — the rest of the file is left
 * untouched.
 *
 * <p>Supports Java ({@code .java}), Kotlin ({@code .kt}), Groovy ({@code .groovy}), and Scala
 * ({@code .scala}) source files.
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
   * @param sourcePath path to the source file
   * @param methodName the name of the test method containing the {@code byValue()} call
   * @param newValue the new value to write as a text block argument
   */
  public static void rewrite(Path sourcePath, String methodName, String newValue) {
    Language language = Language.fromPath(sourcePath);

    Path absolutePath = sourcePath.toAbsolutePath();
    ReentrantLock lock = FILE_LOCKS.computeIfAbsent(absolutePath, path -> new ReentrantLock());
    lock.lock();
    try {
      String content = Files.readString(sourcePath, StandardCharsets.UTF_8);
      String rewritten = rewriteContent(content, methodName, newValue, language);
      Files.writeString(sourcePath, rewritten, StandardCharsets.UTF_8);
    } catch (IOException exception) {
      throw new InlineValueError("Failed to rewrite inline value in " + sourcePath, exception);
    } finally {
      lock.unlock();
      FILE_LOCKS.remove(absolutePath);
    }
  }

  static String rewriteContent(String content, String methodName, String newValue) {
    return rewriteContent(content, methodName, newValue, Language.JAVA);
  }

  static String rewriteContent(
      String content, String methodName, String newValue, Language language) {
    int methodOffset = findMethodOffset(content, methodName, language);
    int methodEnd = findMethodEnd(content, methodOffset);
    int byValueOffset = findByValueOffset(content, methodOffset, methodEnd);

    int argumentStart = byValueOffset + "byValue(".length();
    int argumentEnd = findArgumentEnd(content, argumentStart, language);

    String indent = detectIndent(content, byValueOffset);
    String indentUnit = detectIndentUnit(content);
    String bodyIndent = indent + indentUnit;
    String textBlock = buildTextBlock(newValue, bodyIndent, language);

    return content.substring(0, argumentStart) + textBlock + content.substring(argumentEnd);
  }

  private static int findMethodOffset(String content, String methodName, Language language) {
    if (language.supportsBacktickMethodNames()) {
      String backtickPattern = "`" + methodName + "`(";
      int found = findDeclaration(content, backtickPattern);
      if (found >= 0) {
        return found;
      }
    }
    int found = findDeclaration(content, methodName + "(");
    if (found < 0) {
      throw new InlineValueError("Could not find method " + methodName + " in source file");
    }
    return found;
  }

  private static int findDeclaration(String content, String pattern) {
    int position = 0;
    while (position < content.length()) {
      int found = content.indexOf(pattern, position);
      if (found < 0) {
        return -1;
      }
      if (found == 0 || Character.isWhitespace(content.charAt(found - 1))) {
        return found;
      }
      position = found + 1;
    }
    return -1;
  }

  private static int findMethodEnd(String content, int methodOffset) {
    int position = content.indexOf('{', methodOffset);
    if (position < 0) {
      return content.length();
    }
    int depth = 0;
    SourceScanner scanner = new SourceScanner(content, position);
    while (scanner.hasMore()) {
      if (scanner.isInString()) {
        scanner.advance();
        continue;
      }
      char character = scanner.current();
      if (character == '{') {
        depth++;
      } else if (character == '}') {
        depth--;
        if (depth == 0) {
          return scanner.position();
        }
      }
      scanner.advance();
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

  private static int findArgumentEnd(String content, int start, Language language) {
    SourceScanner scanner = new SourceScanner(content, start);
    scanner.tryEnterString();

    int depth = 1;
    while (scanner.hasMore()) {
      if (scanner.isInString()) {
        if (scanner.tryExitString()) {
          String suffix = language.suffix();
          if (!suffix.isEmpty() && content.startsWith(suffix, scanner.position())) {
            scanner.skip(suffix.length());
          }
        } else {
          scanner.advance();
        }
        continue;
      }

      char character = scanner.current();
      if (scanner.tryEnterString()) {
        continue;
      }
      if (character == '(') {
        depth++;
      } else if (character == ')') {
        depth--;
        if (depth == 0) {
          return scanner.position();
        }
      }
      scanner.advance();
    }

    throw new InlineValueError("Could not find closing parenthesis for byValue(");
  }

  private static boolean isQuoteChar(char character) {
    return character == '"' || character == '\'';
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

  private static String buildTextBlock(String value, String bodyIndent, Language language) {
    String delimiter = language.delimiter();
    String escaped = language.escapeValue(value);
    StringBuilder textBlock = new StringBuilder();
    textBlock.append(delimiter).append("\n");
    String linePrefix = language.linePrefix();
    for (String valueLine : escaped.split("\n", -1)) {
      textBlock.append(bodyIndent).append(linePrefix).append(valueLine).append("\n");
    }
    textBlock.append(bodyIndent).append(linePrefix).append(delimiter);
    String suffix = language.suffix();
    if (!suffix.isEmpty()) {
      textBlock.append(suffix);
    }
    return textBlock.toString();
  }

  /**
   * Tracks position in source content and handles skipping over string literals and text blocks.
   */
  private static final class SourceScanner {

    private final String content;
    private int position;
    private char stringQuoteChar;
    private String textBlockDelimiter;

    SourceScanner(String content, int position) {
      this.content = content;
      this.position = position;
    }

    int position() {
      return position;
    }

    char current() {
      return content.charAt(position);
    }

    boolean hasMore() {
      return position < content.length();
    }

    void advance() {
      if (isInRegularString() && current() == '\\') {
        position += 2;
      } else {
        position++;
      }
    }

    void skip(int count) {
      position += count;
    }

    boolean isInString() {
      return stringQuoteChar != 0 || textBlockDelimiter != null;
    }

    boolean tryEnterString() {
      if (!hasMore() || !isQuoteChar(current())) {
        return false;
      }
      if (position + 3 <= content.length()) {
        String tripleQuote = String.valueOf(current()).repeat(3);
        if (content.startsWith(tripleQuote, position)) {
          textBlockDelimiter = tripleQuote;
          position += 3;
          return true;
        }
      }
      stringQuoteChar = current();
      position++;
      return true;
    }

    boolean tryExitString() {
      if (textBlockDelimiter != null) {
        if (position + 3 <= content.length() && content.startsWith(textBlockDelimiter, position)) {
          textBlockDelimiter = null;
          position += 3;
          return true;
        }
        return false;
      }
      if (stringQuoteChar != 0 && current() != '\\' && current() == stringQuoteChar) {
        stringQuoteChar = 0;
        position++;
        return true;
      }
      return false;
    }

    private boolean isInRegularString() {
      return stringQuoteChar != 0;
    }
  }

  /** Language-specific rules for text block delimiters, escaping, and suffixes. */
  enum Language {
    JAVA {
      @Override
      String delimiter() {
        return "\"\"\"";
      }

      @Override
      String escapeValue(String value) {
        return value.replace("\\", "\\\\").replace("\"\"\"", "\\\"\"\"");
      }

      @Override
      String suffix() {
        return "";
      }

      @Override
      String linePrefix() {
        return "";
      }

      @Override
      boolean supportsBacktickMethodNames() {
        return false;
      }
    },

    KOTLIN {
      @Override
      String delimiter() {
        return "\"\"\"";
      }

      @Override
      String escapeValue(String value) {
        if (value.contains("\"\"\"")) {
          throw new InlineValueError(
              "Cannot rewrite inline value: Kotlin raw strings cannot contain \"\"\"");
        }
        return value.replace("$", "${'$'}");
      }

      @Override
      String suffix() {
        return ".trimIndent()";
      }

      @Override
      String linePrefix() {
        return "";
      }

      @Override
      boolean supportsBacktickMethodNames() {
        return true;
      }
    },

    GROOVY {
      @Override
      String delimiter() {
        return "'''";
      }

      @Override
      String escapeValue(String value) {
        return value.replace("\\", "\\\\").replace("'''", "\\'\\'\\'");
      }

      @Override
      String suffix() {
        return ".stripIndent()";
      }

      @Override
      String linePrefix() {
        return "";
      }

      @Override
      boolean supportsBacktickMethodNames() {
        return false;
      }
    },

    SCALA {
      @Override
      String delimiter() {
        return "\"\"\"";
      }

      @Override
      String escapeValue(String value) {
        if (value.contains("\"\"\"")) {
          throw new InlineValueError(
              "Cannot rewrite inline value: Scala raw strings cannot contain \"\"\"");
        }
        return value;
      }

      @Override
      String suffix() {
        return ".stripMargin";
      }

      @Override
      String linePrefix() {
        return "|";
      }

      @Override
      boolean supportsBacktickMethodNames() {
        return true;
      }
    };

    abstract String delimiter();

    abstract String escapeValue(String value);

    abstract String suffix();

    abstract String linePrefix();

    abstract boolean supportsBacktickMethodNames();

    static Language fromPath(Path path) {
      String filename = path.getFileName().toString();
      if (filename.endsWith(".kt")) {
        return KOTLIN;
      }
      if (filename.endsWith(".java")) {
        return JAVA;
      }
      if (filename.endsWith(".groovy")) {
        return GROOVY;
      }
      if (filename.endsWith(".scala")) {
        return SCALA;
      }
      throw new InlineValueError(
          "Inline value rewriting is not supported for "
              + filename
              + ". Supported: .java, .kt, .groovy, .scala");
    }
  }
}

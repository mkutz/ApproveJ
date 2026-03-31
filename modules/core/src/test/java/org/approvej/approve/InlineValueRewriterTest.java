package org.approvej.approve;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class InlineValueRewriterTest {

  @Test
  void rewrite_single_line_string() {
    String content =
        """
        class MyTest {
          @Test
          void test() {
            approve(person).byValue("old value");
          }
        }
        """;

    String result = InlineValueRewriter.rewriteContent(content, "test", "new value");

    assertThat(result)
        .isEqualTo(
            """
            class MyTest {
              @Test
              void test() {
                approve(person).byValue(\"""
                  new value
                  \""");
              }
            }
            """);
  }

  @Test
  void rewrite_existing_text_block() {
    String content =
        """
        class MyTest {
          @Test
          void test() {
            approve(person).byValue(""\"
                old value
                ""\");
          }
        }
        """;

    String result = InlineValueRewriter.rewriteContent(content, "test", "new value");

    assertThat(result)
        .isEqualTo(
            """
            class MyTest {
              @Test
              void test() {
                approve(person).byValue(\"""
                  new value
                  \""");
              }
            }
            """);
  }

  @Test
  void rewrite_multiline_value() {
    String content =
        """
        class MyTest {
          @Test
          void test() {
            approve(person).byValue("old");
          }
        }
        """;

    String result = InlineValueRewriter.rewriteContent(content, "test", "line 1\nline 2\nline 3");

    assertThat(result)
        .isEqualTo(
            """
            class MyTest {
              @Test
              void test() {
                approve(person).byValue(\"""
                  line 1
                  line 2
                  line 3
                  \""");
              }
            }
            """);
  }

  @Test
  void rewrite_multiline_text_block() {
    String content =
        """
        class MyTest {
          @Test
          void test() {
            approve(person).byValue(\"""
                old line 1
                old line 2
                \""");
          }
        }
        """;

    String result = InlineValueRewriter.rewriteContent(content, "test", "new line 1\nnew line 2");

    assertThat(result)
        .isEqualTo(
            """
            class MyTest {
              @Test
              void test() {
                approve(person).byValue(\"""
                  new line 1
                  new line 2
                  \""");
              }
            }
            """);
  }

  @Test
  void rewrite_empty_value() {
    String content =
        """
        class MyTest {
          @Test
          void test() {
            approve(person).byValue("old");
          }
        }
        """;

    String result = InlineValueRewriter.rewriteContent(content, "test", "");

    assertThat(result)
        .isEqualTo(
            """
            class MyTest {
              @Test
              void test() {
                approve(person).byValue(\"""
            \s\s\s\s\s\s
                  \""");
              }
            }
            """);
  }

  @Test
  void rewrite_with_chain() {
    String content =
        """
        class MyTest {
          @Test
          void test() {
            approve(person)
                .printedAs(json())
                .byValue("old");
          }
        }
        """;

    String result = InlineValueRewriter.rewriteContent(content, "test", "new value");

    assertThat(result)
        .isEqualTo(
            """
            class MyTest {
              @Test
              void test() {
                approve(person)
                    .printedAs(json())
                    .byValue(\"""
                      new value
                      \""");
              }
            }
            """);
  }

  @Test
  void rewrite_preserves_surrounding_content() {
    String content =
        """
        class MyTest {
          // important comment
          @Test
          void test() {
            String setup = "some setup";
            approve(person).byValue("old");
            System.out.println("after");
          }
        }
        """;

    String result = InlineValueRewriter.rewriteContent(content, "test", "new value");

    assertThat(result)
        .isEqualTo(
            """
            class MyTest {
              // important comment
              @Test
              void test() {
                String setup = "some setup";
                approve(person).byValue(\"""
                  new value
                  \""");
                System.out.println("after");
              }
            }
            """);
  }

  @Test
  void rewrite_uses_tab_indentation() {
    String content =
        """
        class MyTest {
        \t@Test
        \tvoid test() {
        \t\tapprove(person).byValue("old");
        \t}
        }
        """;

    String result = InlineValueRewriter.rewriteContent(content, "test", "new value");

    assertThat(result)
        .isEqualTo(
            """
            class MyTest {
            \t@Test
            \tvoid test() {
            \t\tapprove(person).byValue(\"""
            \t\t\tnew value
            \t\t\t\""");
            \t}
            }
            """);
  }

  @Test
  void rewrite_uses_four_space_indentation() {
    String content =
        """
        class MyTest {
            @Test
            void test() {
                approve(person).byValue("old");
            }
        }
        """;

    String result = InlineValueRewriter.rewriteContent(content, "test", "new value");

    assertThat(result)
        .isEqualTo(
            """
            class MyTest {
                @Test
                void test() {
                    approve(person).byValue(\"""
                        new value
                        \""");
                }
            }
            """);
  }

  @Test
  void rewrite_file(@TempDir Path directory) throws IOException {
    Path sourceFile = directory.resolve("MyTest.java");
    Files.writeString(
        sourceFile,
        """
        class MyTest {
          @Test
          void test() {
            approve(person).byValue("old value");
          }
        }
        """,
        StandardCharsets.UTF_8);

    InlineValueRewriter.rewrite(sourceFile, "test", "new value");

    String content = Files.readString(sourceFile, StandardCharsets.UTF_8);
    assertThat(content)
        .isEqualTo(
            """
            class MyTest {
              @Test
              void test() {
                approve(person).byValue(\"""
                  new value
                  \""");
              }
            }
            """);
  }

  @Test
  void rewrite_non_java_file(@TempDir Path directory) throws IOException {
    Path sourceFile = directory.resolve("MyTest.kt");
    Files.writeString(sourceFile, "content", StandardCharsets.UTF_8);

    assertThatThrownBy(() -> InlineValueRewriter.rewrite(sourceFile, "test", "new"))
        .isInstanceOf(InlineValueError.class)
        .hasMessageContaining("only supported in Java source files");
  }

  @Test
  void rewrite_concurrent_same_file(@TempDir Path directory) throws Exception {
    Path sourceFile = directory.resolve("MyTest.java");
    String original =
        """
        class MyTest {
          @Test
          void test1() {
            approve(person).byValue("old1");
          }
          @Test
          void test2() {
            approve(person).byValue("old2");
          }
        }
        """;
    Files.writeString(sourceFile, original, StandardCharsets.UTF_8);

    CountDownLatch startLatch = new CountDownLatch(1);
    try (ExecutorService executor = Executors.newFixedThreadPool(2)) {
      executor.submit(
          () -> {
            startLatch.await();
            InlineValueRewriter.rewrite(sourceFile, "test1", "new1");
            return null;
          });
      executor.submit(
          () -> {
            startLatch.await();
            InlineValueRewriter.rewrite(sourceFile, "test2", "new2");
            return null;
          });

      startLatch.countDown();
      executor.shutdown();
      assertThat(executor.awaitTermination(10, TimeUnit.SECONDS)).isTrue();
    }

    String content = Files.readString(sourceFile, StandardCharsets.UTF_8);
    assertThat(content)
        .isEqualTo(
            """
            class MyTest {
              @Test
              void test1() {
                approve(person).byValue(\"""
                  new1
                  \""");
              }
              @Test
              void test2() {
                approve(person).byValue(\"""
                  new2
                  \""");
              }
            }
            """);
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "simple value",
        "line 1\nline 2\nline 3",
        "normal\n  indented\nnormal",
        "",
        "special chars: <>&\"'",
      })
  void rewrite_round_trip(String originalValue) {
    String content =
        """
        class MyTest {
          @Test
          void test() {
            approve(person).byValue("placeholder");
          }
        }
        """;

    String rewritten = InlineValueRewriter.rewriteContent(content, "test", originalValue);
    String textBlockContent = extractTextBlockContent(rewritten);

    assertThat(textBlockContent).isEqualTo(originalValue);
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "simple value",
        "line 1\nline 2\nline 3",
        "normal\n  indented\nnormal",
      })
  void rewrite_round_trip_four_space_indent(String originalValue) {
    String content =
        """
        class MyTest {
            @Test
            void test() {
                approve(person).byValue("placeholder");
            }
        }
        """;

    String rewritten = InlineValueRewriter.rewriteContent(content, "test", originalValue);
    String textBlockContent = extractTextBlockContent(rewritten);

    assertThat(textBlockContent).isEqualTo(originalValue);
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "simple value",
        "line 1\nline 2\nline 3",
      })
  void rewrite_round_trip_tab_indent(String originalValue) {
    String content =
        """
        class MyTest {
        \t@Test
        \tvoid test() {
        \t\tapprove(person).byValue("placeholder");
        \t}
        }
        """;

    String rewritten = InlineValueRewriter.rewriteContent(content, "test", originalValue);
    String textBlockContent = extractTextBlockContent(rewritten);

    assertThat(textBlockContent).isEqualTo(originalValue);
  }

  /**
   * Extracts the text block content from a rewritten source file and applies Java text block
   * semantics ({@link String#stripIndent()} + {@link String#trim()}) to simulate what the JVM would
   * produce at runtime. This mirrors how {@link InplaceApprover} trims both the received and
   * previously approved values.
   */
  private static String extractTextBlockContent(String source) {
    Matcher matcher =
        Pattern.compile("byValue\\(\"\"\"\\n(.*?)\"\"\"\\)", Pattern.DOTALL).matcher(source);
    assertThat(matcher.find()).as("text block in rewritten source").isTrue();
    String rawContent = matcher.group(1);
    return rawContent.stripIndent().trim();
  }
}

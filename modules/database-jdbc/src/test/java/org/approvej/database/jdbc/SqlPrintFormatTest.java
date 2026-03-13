package org.approvej.database.jdbc;

import static org.approvej.database.jdbc.SqlPrintFormat.sql;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class SqlPrintFormatTest {

  @Test
  void printer_select() {
    String formatted =
        sql().printer().apply("SELECT id, name, email FROM users WHERE active = 1 ORDER BY name");

    assertThat(formatted)
        .isEqualTo(
            """
            SELECT
              id,
              name,
              email
            FROM
              users
            WHERE
              active = 1
            ORDER BY
              name\
            """);
  }

  @Test
  void printer_select_with_join() {
    String formatted =
        sql()
            .printer()
            .apply(
                "SELECT u.id, u.name, o.total FROM users u"
                    + " LEFT JOIN orders o ON u.id = o.user_id"
                    + " WHERE u.active = 1");

    assertThat(formatted)
        .isEqualTo(
            """
            SELECT
              u.id,
              u.name,
              o.total
            FROM
              users u
              LEFT JOIN orders o
              ON u.id = o.user_id
            WHERE
              u.active = 1\
            """);
  }

  @Test
  void printer_select_with_and_or() {
    String formatted =
        sql()
            .printer()
            .apply("SELECT * FROM users WHERE active = 1 AND role = 'admin' OR name = 'root'");

    assertThat(formatted)
        .isEqualTo(
            """
            SELECT
              *
            FROM
              users
            WHERE
              active = 1
              AND role = 'admin'
              OR name = 'root'\
            """);
  }

  @Test
  void printer_insert() {
    String formatted =
        sql().printer().apply("INSERT INTO users (id, name, email) VALUES (1, 'Alice', 'a@b.com')");

    assertThat(formatted)
        .isEqualTo(
            """
            INSERT INTO
              users(id,
              name,
              email)
            VALUES
              (1,
              'Alice',
              'a@b.com')\
            """);
  }

  @Test
  void printer_prepared_statement() {
    String formatted =
        sql().printer().apply("INSERT INTO users (id, name, email) VALUES (?, ?, ?)");

    assertThat(formatted)
        .isEqualTo(
            """
            INSERT INTO
              users(id,
              name,
              email)
            VALUES
              (?,
              ?,
              ?)\
            """);
  }

  @Test
  void printer_update() {
    String formatted =
        sql().printer().apply("UPDATE users SET name = 'Bob', email = 'bob@test.com' WHERE id = 1");

    assertThat(formatted)
        .isEqualTo(
            """
            UPDATE
              users
            SET
              name = 'Bob',
              email = 'bob@test.com'
            WHERE
              id = 1\
            """);
  }

  @Test
  void printer_delete() {
    String formatted = sql().printer().apply("DELETE FROM users WHERE id = 1 AND active = 0");

    assertThat(formatted)
        .isEqualTo(
            """
            DELETE FROM
              users
            WHERE
              id = 1
              AND active = 0\
            """);
  }

  @Test
  void printer_group_by_having() {
    String formatted =
        sql()
            .printer()
            .apply(
                "SELECT department, COUNT(*) FROM employees"
                    + " GROUP BY department HAVING COUNT(*) > 5");

    assertThat(formatted)
        .isEqualTo(
            """
            SELECT
              department,
              COUNT(*)
            FROM
              employees
            GROUP BY
              department
            HAVING
              COUNT(*) > 5\
            """);
  }

  @Test
  void printer_preserves_string_literals() {
    String formatted =
        sql().printer().apply("SELECT * FROM users WHERE name = 'O''Brien' AND city = 'New York'");

    assertThat(formatted)
        .isEqualTo(
            """
            SELECT
              *
            FROM
              users
            WHERE
              name = 'O''Brien'
              AND city = 'New York'\
            """);
  }

  @Test
  void filenameExtension() {
    assertThat(sql().filenameExtension()).isEqualTo("sql");
  }
}

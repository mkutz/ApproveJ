package examples.kotlin

import java.sql.Connection
import org.approvej.ApprovalBuilder.approve
import org.approvej.database.DatabaseScrubbers.columnValue
import org.approvej.database.DatabaseSnapshot.query
import org.approvej.database.QueryResultPrintFormat.queryResult
import org.approvej.database.RecordingDataSource
import org.approvej.database.SqlPrintFormat.sql
import org.assertj.core.api.Assertions.assertThat
import org.h2.jdbcx.JdbcDataSource
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class DatabaseDocTest {

  private lateinit var dataSource: JdbcDataSource

  @BeforeEach
  fun setUp() {
    dataSource = JdbcDataSource()
    dataSource.setURL("jdbc:h2:mem:doc_${System.nanoTime()};DB_CLOSE_DELAY=-1")
    dataSource.connection.use { conn: Connection ->
      conn.createStatement().use { stmt ->
        stmt.execute(
          "CREATE TABLE users (id INT PRIMARY KEY, name VARCHAR(100), email VARCHAR(200))"
        )
        stmt.execute("INSERT INTO users VALUES (1, 'Alice', 'alice@test.com')")
        stmt.execute("INSERT INTO users VALUES (2, 'Bob', 'bob@test.com')")
      }
    }
  }

  @Test
  fun recording() {
    // tag::recording[]
    val recordingDs = RecordingDataSource(dataSource)

    // ... pass recordingDs to your code instead of the real DataSource ...
    recordingDs.connection.use { conn ->
      conn.createStatement().use { stmt ->
        stmt.executeQuery("SELECT id, name, email FROM users WHERE id = 1")
      }
    }

    approve(recordingDs.lastRecordedQuery()).printedAs(sql()).byFile()
    // end::recording[]
  }

  @Test
  fun snapshot() {
    // tag::snapshot[]
    val result = query(dataSource, "SELECT * FROM users")
    // end::snapshot[]

    assertThat(result.rows()).hasSize(2)
  }

  @Test
  fun approve_query() {
    // tag::approve[]
    approve(query(dataSource, "SELECT * FROM users"))
      .scrubbedOf(columnValue("id"))
      .printedAs(queryResult())
      .byFile()
    // end::approve[]
  }

  @Test
  fun scrub() {
    // tag::scrub[]
    approve(query(dataSource, "SELECT * FROM users"))
      .scrubbedOf(columnValue("id"))
      .printedAs(queryResult())
      .byFile()
    // end::scrub[]
  }

  @Test
  fun scrub_custom() {
    // tag::scrub_custom[]
    approve(query(dataSource, "SELECT * FROM users"))
      .scrubbedOf(columnValue("id").replacement("***"))
      .printedAs(queryResult())
      .byFile()
    // end::scrub_custom[]
  }
}

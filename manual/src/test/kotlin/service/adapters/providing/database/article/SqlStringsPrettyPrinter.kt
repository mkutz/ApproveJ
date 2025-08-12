package service.adapters.providing.database.article

import org.approvej.print.Printer
import org.hibernate.engine.jdbc.internal.BasicFormatterImpl

class SqlStringsPrettyPrinter : Printer<List<String>> {

  private val formatter = BasicFormatterImpl()

  companion object {
    fun sqlStringPrettyPrinter() = SqlStringsPrettyPrinter()
  }

  override fun apply(sqlStrings: List<String>): String =
    sqlStrings
      .map { formatter.format(it).trimIndent() }
      .joinToString(";\n\n")
      .lines()
      .map { it.trimEnd() }
      .joinToString("\n")

  override fun filenameExtension() = "sql"
}

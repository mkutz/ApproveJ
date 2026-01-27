package examples.kotlin

import org.approvej.print.PrintFormat
import org.approvej.print.PrintFormatProvider
import org.approvej.print.Printer

class ScreamingPrintFormat : PrintFormat<Any>, PrintFormatProvider<Any> {
  override fun printer(): Printer<Any> = { any -> any.toString().uppercase() }

  override fun filenameExtension() = "TXT"

  override fun alias() = "screaming"

  override fun create() = ScreamingPrintFormat()
}

package examples.java;

import org.approvej.print.PrintFormat;
import org.approvej.print.PrintFormatProvider;
import org.approvej.print.Printer;
import org.jspecify.annotations.NonNull;

public class ScreamingPrintFormat implements PrintFormat<Object>, PrintFormatProvider<Object> {

  @Override
  public @NonNull String alias() {
    return "screaming";
  }

  @Override
  public PrintFormat<Object> create() {
    return new ScreamingPrintFormat();
  }

  @Override
  public Printer<Object> printer() {
    return (Object object) -> object.toString().toUpperCase();
  }

  @Override
  public String filenameExtension() {
    return "TXT";
  }
}

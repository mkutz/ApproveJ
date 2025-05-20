package org.approvej.review;

import static java.nio.file.Files.readAllLines;

import com.github.difflib.text.DiffRow;
import com.github.difflib.text.DiffRowGenerator;
import java.io.IOException;
import java.nio.file.Path;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * {@link FileReviewer} implementation prints the diff on the console and waits for user approval.
 */
public class LoggerFileReviewer implements FileReviewer {

  private static final Logger LOGGER = Logger.getLogger(LoggerFileReviewer.class.getName());

  public static final DiffRowGenerator CONSOLE_DIFF_ROW_GENERATOR =
      DiffRowGenerator.create()
          .lineNormalizer(line -> line)
          .showInlineDiffs(true)
          .mergeOriginalRevised(true)
          .inlineDiffByWord(false)
          .oldTag(f -> f ? "\u001B[9;2m" : "\u001B[0m")
          .newTag(f -> f ? "\u001B[1m" : "\u001B[0m")
          .build();

  @Override
  public void trigger(Path receivedPath, Path approvedPath) {
    try {
      LOGGER.warning(
          CONSOLE_DIFF_ROW_GENERATOR
              .generateDiffRows(readAllLines(receivedPath), readAllLines(approvedPath))
              .stream()
              .map(DiffRow::getOldLine)
              .collect(Collectors.joining("\n")));
    } catch (IOException e) {
      throw new ReviewerError("Filed to trigger %s".formatted(this.getClass().getSimpleName()), e);
    }
  }
}

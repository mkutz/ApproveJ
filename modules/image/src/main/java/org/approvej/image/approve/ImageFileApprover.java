package org.approvej.image.approve;

import static java.awt.image.BufferedImage.TYPE_INT_ARGB;
import static java.nio.file.Files.createDirectories;
import static java.nio.file.Files.deleteIfExists;
import static java.nio.file.Files.exists;
import static java.nio.file.Files.getLastModifiedTime;
import static java.nio.file.Files.list;
import static java.nio.file.Files.move;
import static java.nio.file.Files.notExists;
import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;
import static java.util.Comparator.comparing;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.imageio.ImageIO;
import org.approvej.approve.PathProvider;
import org.approvej.image.ImageApprovalResult;
import org.approvej.image.compare.ImageComparator;
import org.approvej.image.compare.ImageComparisonResult;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class ImageFileApprover implements ImageApprover {

  private final PathProvider pathProvider;
  private final ImageComparator comparator;

  /**
   * Creates a new image file approver.
   *
   * @param pathProvider a {@link PathProvider} to determine the paths of the approved and received
   *     files
   * @param comparator the {@link ImageComparator} to use for comparing images
   */
  ImageFileApprover(PathProvider pathProvider, ImageComparator comparator) {
    this.pathProvider = pathProvider;
    this.comparator = comparator;
  }

  @Override
  public ImageApprovalResult apply(BufferedImage received) {
    ensureDirectory();
    handleOldApprovedFiles();
    ensureApprovedFile(received.getWidth(), received.getHeight());
    return check(readApprovedFile(), received);
  }

  private void ensureDirectory() {
    try {
      createDirectories(pathProvider.directory());
    } catch (IOException e) {
      throw new ImageFileApproverError(
          "Creating directories %s failed".formatted(pathProvider.directory()), e);
    }
  }

  private void ensureApprovedFile(int width, int height) {
    Path approvedPath = pathProvider.approvedPath();
    if (notExists(approvedPath)) {
      try {
        BufferedImage missingApprovedImage = new BufferedImage(width, height, TYPE_INT_ARGB);
        ImageIO.write(
            missingApprovedImage,
            pathProvider.filenameExtension(),
            Files.newOutputStream(approvedPath, CREATE));
      } catch (IOException e) {
        throw new ImageFileApproverError(
            "Creating approved file %s failed".formatted(approvedPath), e);
      }
    }
  }

  private void handleOldApprovedFiles() {
    Path approvedPath = pathProvider.approvedPath();
    String filename = approvedPath.getFileName().toString();
    Pattern filenameExtensionPattern =
        Pattern.compile("(?<baseFilename>.+?)(?:\\.(?<extension>[^.]*))?");
    Matcher matcher = filenameExtensionPattern.matcher(filename);
    String baseFilename = matcher.matches() ? matcher.group("baseFilename") : null;
    Pattern baseFilenamePattern = Pattern.compile(baseFilename + "(?:\\.(?<extension>[^.]*))?");
    try (var paths = list(pathProvider.directory())) {
      List<Path> oldApprovedFiles =
          paths
              .filter(
                  path ->
                      !path.toAbsolutePath().equals(approvedPath)
                          && baseFilenamePattern.matcher(path.getFileName().toString()).matches())
              .sorted(
                  comparing(
                      path -> {
                        try {
                          return getLastModifiedTime(path);
                        } catch (IOException ignored) {
                          return FileTime.from(Instant.ofEpochSecond(0));
                        }
                      }))
              .toList();
      if (oldApprovedFiles.isEmpty()) {
        return;
      }
      if (!exists(approvedPath)) {
        move(oldApprovedFiles.getLast(), approvedPath);
      }
      oldApprovedFiles.forEach(
          path -> {
            try {
              deleteIfExists(path);
            } catch (IOException ignored) {
              // this is an optional cleanup
            }
          });
    } catch (IOException ignored) {
      // this is an optional cleanup
    }
  }

  private BufferedImage readApprovedFile() {
    Path approvedPath = pathProvider.approvedPath();
    try {
      return ImageIO.read(approvedPath.toFile());
    } catch (IOException e) {
      throw new ImageFileApproverError(
          "Reading approved file %s failed".formatted(approvedPath), e);
    }
  }

  private ImageApprovalResult check(BufferedImage previouslyApproved, BufferedImage received) {
    ImageComparisonResult comparisonResult = comparator.compare(previouslyApproved, received);
    ImageFileApprovalResult result = new ImageFileApprovalResult(comparisonResult, pathProvider);
    Path receivedPath = pathProvider.receivedPath();
    if (result.needsApproval()) {
      try {
        ImageIO.write(
            received,
            pathProvider.filenameExtension(),
            Files.newOutputStream(receivedPath, CREATE, TRUNCATE_EXISTING));
      } catch (IOException e) {
        throw new ImageFileApproverError(
            "Writing received to %s failed".formatted(receivedPath), e);
      }
    }
    return result;
  }

  public static ImageFileApprover imageFile(PathProvider pathProvider, ImageComparator comparator) {
    return new ImageFileApprover(pathProvider, comparator);
  }
}

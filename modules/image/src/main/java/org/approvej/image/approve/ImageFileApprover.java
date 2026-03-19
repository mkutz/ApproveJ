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

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.imageio.ImageIO;
import org.approvej.approve.PathProvider;
import org.approvej.image.ImageApprovalResult;
import org.approvej.image.compare.ImageComparator;
import org.approvej.image.compare.ImageComparisonResult;
import org.approvej.print.PrintFormat;
import org.jspecify.annotations.NullMarked;

/**
 * Approves a received image by comparing it to an approved file on disk.
 *
 * <p>If the approved file does not exist, a transparent placeholder file is generated. If the
 * received image differs from the approved image according to the configured {@link
 * ImageComparator}, the received image is written to disk alongside the approved file.
 */
@NullMarked
public class ImageFileApprover implements ImageApprover {

  private static final Logger LOGGER = Logger.getLogger(ImageFileApprover.class.getName());

  private final PathProvider pathProvider;
  private final ImageComparator comparator;

  /**
   * Creates a new image file approver.
   *
   * @param pathProvider a {@link PathProvider} to determine the paths of the approved and received
   *     files
   * @param comparator the {@link ImageComparator} to use for comparing images
   */
  private static final String DEFAULT_IMAGE_EXTENSION = "png";

  ImageFileApprover(PathProvider pathProvider, ImageComparator comparator) {
    this.pathProvider =
        pathProvider.filenameExtension().equals(PrintFormat.DEFAULT_FILENAME_EXTENSION)
            ? pathProvider.filenameExtension(DEFAULT_IMAGE_EXTENSION)
            : pathProvider;
    this.comparator = comparator;
  }

  @Override
  public ImageApprovalResult apply(BufferedImage received) {
    ensureDirectory();
    handleOldApprovedFiles();
    ensureApprovedFile(received.getWidth(), received.getHeight());
    BufferedImage approved = readApprovedFile();
    return check(approved, received);
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
      try (var outputStream = Files.newOutputStream(approvedPath, CREATE)) {
        BufferedImage missingApprovedImage = new BufferedImage(width, height, TYPE_INT_ARGB);
        ImageIO.write(missingApprovedImage, pathProvider.filenameExtension(), outputStream);
      } catch (IOException e) {
        throw new ImageFileApproverError(
            "Creating approved file %s failed".formatted(approvedPath), e);
      }
    }
  }

  private void handleOldApprovedFiles() {
    Path approvedPath = pathProvider.approvedPath().toAbsolutePath();
    String filename = approvedPath.getFileName().toString();
    Pattern filenameExtensionPattern =
        Pattern.compile("(?<baseFilename>.+?)(?:\\.(?<extension>[^.]*))?");
    Matcher matcher = filenameExtensionPattern.matcher(filename);
    if (!matcher.matches()) {
      return;
    }
    String baseFilename = matcher.group("baseFilename");
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
                        } catch (IOException e) {
                          LOGGER.fine("Could not read modification time: " + e.getMessage());
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
            } catch (IOException e) {
              LOGGER.fine("Could not delete old approved file: " + e.getMessage());
            }
          });
    } catch (IOException e) {
      LOGGER.fine("Could not clean up old approved files: " + e.getMessage());
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
      try (var outputStream = Files.newOutputStream(receivedPath, CREATE, TRUNCATE_EXISTING)) {
        ImageIO.write(received, pathProvider.filenameExtension(), outputStream);
      } catch (IOException e) {
        throw new ImageFileApproverError(
            "Writing received to %s failed".formatted(receivedPath), e);
      }
    } else {
      try {
        deleteIfExists(receivedPath);
      } catch (IOException e) {
        throw new ImageFileApproverError(
            "Deleting received file %s failed".formatted(receivedPath), e);
      }
    }
    return result;
  }

  /**
   * Creates a new ImageFileApprover.
   *
   * @param pathProvider the provider for approved and received file paths
   * @param comparator the comparator to use
   * @return a new ImageFileApprover
   */
  public static ImageFileApprover imageFile(PathProvider pathProvider, ImageComparator comparator) {
    return new ImageFileApprover(pathProvider, comparator);
  }
}

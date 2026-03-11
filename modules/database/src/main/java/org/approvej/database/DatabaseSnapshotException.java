package org.approvej.database;

import org.jspecify.annotations.NullMarked;

/** An error that occurs when executing a database snapshot query. */
@NullMarked
public class DatabaseSnapshotException extends RuntimeException {

  /**
   * Creates a new {@link DatabaseSnapshotException} with the given cause.
   *
   * @param cause the cause of the error
   */
  public DatabaseSnapshotException(Throwable cause) {
    super(cause);
  }
}

package org.approvej.database;

import java.io.PrintWriter;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import javax.sql.DataSource;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * A {@link DataSource} wrapper that records all SQL statements executed through it.
 *
 * <p>Wrap your application's {@link DataSource} with this to capture the SQL your code sends. Then
 * approve the recorded queries to catch unintended changes.
 *
 * <p>This is analogous to the HTTP module's {@code HttpStubServer}: instead of intercepting HTTP
 * requests, it intercepts SQL statements.
 *
 * <p>SQL is recorded at execution time. For {@link PreparedStatement}s, the SQL template (with
 * {@code ?} placeholders) is recorded each time the statement is executed.
 */
@NullMarked
public class RecordingDataSource implements DataSource {

  private static final Set<String> EXECUTE_METHODS =
      Set.of("execute", "executeQuery", "executeUpdate", "executeLargeUpdate");

  private final DataSource delegate;
  private final List<String> recordedQueries = new ArrayList<>();

  /**
   * Creates a new {@link RecordingDataSource} wrapping the given delegate.
   *
   * @param delegate the real {@link DataSource} to delegate to
   */
  public RecordingDataSource(DataSource delegate) {
    this.delegate = delegate;
  }

  /**
   * Returns the {@link List} of recorded SQL queries in the order they were executed.
   *
   * @return the recorded SQL queries
   */
  public List<String> recordedQueries() {
    return recordedQueries;
  }

  /**
   * Returns the last (most recently) recorded SQL query.
   *
   * @return the last recorded SQL query
   */
  public String lastRecordedQuery() {
    return recordedQueries.getLast();
  }

  /**
   * Resets the recorded queries to an empty list.
   *
   * @return this
   */
  public RecordingDataSource resetRecordedQueries() {
    recordedQueries.clear();
    return this;
  }

  @Override
  public Connection getConnection() throws SQLException {
    return recordingConnection(delegate.getConnection());
  }

  @Override
  public Connection getConnection(String username, String password) throws SQLException {
    return recordingConnection(delegate.getConnection(username, password));
  }

  private Connection recordingConnection(Connection real) {
    return proxy(
        Connection.class,
        (proxy, method, args) -> {
          Object result = method.invoke(real, args);
          if ("createStatement".equals(method.getName()) && result instanceof Statement stmt) {
            return recordingStatement(stmt, null);
          }
          if ("prepareStatement".equals(method.getName())
              && result instanceof PreparedStatement pstmt
              && args != null
              && args.length > 0
              && args[0] instanceof String sql) {
            return recordingStatement(pstmt, sql);
          }
          return result;
        });
  }

  private <S extends Statement> S recordingStatement(S real, @Nullable String preparedSql) {
    @SuppressWarnings("unchecked")
    Class<S> iface =
        real instanceof PreparedStatement
            ? (Class<S>) PreparedStatement.class
            : (Class<S>) Statement.class;
    return proxy(
        iface,
        (proxy, method, args) -> {
          if (EXECUTE_METHODS.contains(method.getName())) {
            if (args != null && args.length > 0 && args[0] instanceof String sql) {
              recordedQueries.add(sql);
            } else if (preparedSql != null) {
              recordedQueries.add(preparedSql);
            }
          }
          return method.invoke(real, args);
        });
  }

  @SuppressWarnings("unchecked")
  private static <T> T proxy(Class<T> iface, InvocationHandler handler) {
    return (T)
        Proxy.newProxyInstance(
            RecordingDataSource.class.getClassLoader(), new Class[] {iface}, handler);
  }

  @Override
  public @Nullable PrintWriter getLogWriter() throws SQLException {
    return delegate.getLogWriter();
  }

  @Override
  public void setLogWriter(PrintWriter out) throws SQLException {
    delegate.setLogWriter(out);
  }

  @Override
  public void setLoginTimeout(int seconds) throws SQLException {
    delegate.setLoginTimeout(seconds);
  }

  @Override
  public int getLoginTimeout() throws SQLException {
    return delegate.getLoginTimeout();
  }

  @Override
  public Logger getParentLogger() {
    return Logger.getLogger(RecordingDataSource.class.getName());
  }

  @Override
  public <T> T unwrap(Class<T> iface) throws SQLException {
    return delegate.unwrap(iface);
  }

  @Override
  public boolean isWrapperFor(Class<?> iface) throws SQLException {
    return delegate.isWrapperFor(iface);
  }
}

package org.approvej;

import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * Tracks approval tokens to detect dangling approvals — {@link ApprovalBuilder#approve(Object)
 * approve()} calls that are never concluded with a terminal method.
 *
 * <p>Each call to {@link ApprovalBuilder#approve(Object)} registers a token scoped to the current
 * thread. After each test, the {@link DanglingApprovalExtension} calls {@link #checkAndReset()} to
 * detect unconcluded tokens on that thread. Additionally, a JVM shutdown hook performs a final
 * check and reports any remaining dangling approvals at process shutdown.
 *
 * <p>Thread-scoping ensures parallel test execution does not cause interference between tests.
 */
@NullMarked
final class DanglingApprovalTracker {

  private static final ThreadLocal<List<AtomicBoolean>> threadTokens =
      ThreadLocal.withInitial(java.util.ArrayList::new);
  private static final ConcurrentLinkedDeque<AtomicBoolean> allTokens =
      new ConcurrentLinkedDeque<>();
  private static final AtomicReference<@Nullable Thread> shutdownHook = new AtomicReference<>();

  private DanglingApprovalTracker() {}

  static AtomicBoolean register() {
    AtomicBoolean token = new AtomicBoolean(false);
    List<AtomicBoolean> tokens = threadTokens.get();
    tokens.removeIf(AtomicBoolean::get);
    tokens.add(token);
    allTokens.removeIf(AtomicBoolean::get);
    allTokens.add(token);
    shutdownHook.updateAndGet(
        existing -> {
          if (existing != null) {
            return existing;
          }
          Thread hook =
              new Thread(
                  DanglingApprovalTracker::reportDanglingOnShutdown,
                  "ApproveJ-Dangling-Approval-Check");
          Runtime.getRuntime().addShutdownHook(hook);
          return hook;
        });
    return token;
  }

  /**
   * Checks for unconcluded tokens on the current thread and resets. Throws {@link
   * DanglingApprovalError} if any dangling tokens are found.
   */
  static void checkAndReset() {
    List<AtomicBoolean> tokens = threadTokens.get();
    boolean hasDangling = tokens.stream().anyMatch(token -> !token.get());
    allTokens.removeAll(tokens);
    tokens.clear();
    if (hasDangling) {
      throw new DanglingApprovalError();
    }
  }

  static void reset() {
    threadTokens.get().clear();
    allTokens.clear();
    Thread hook = shutdownHook.getAndSet(null);
    if (hook != null) {
      try {
        Runtime.getRuntime().removeShutdownHook(hook);
      } catch (IllegalStateException e) {
        // JVM is already shutting down
      }
    }
  }

  private static void reportDanglingOnShutdown() {
    allTokens.removeIf(AtomicBoolean::get);
    if (!allTokens.isEmpty()) {
      System.err.println(
          "WARNING: Dangling approval detected."
              + " Call by(), byFile(), or byValue() to conclude the approval.");
    }
    allTokens.clear();
  }
}

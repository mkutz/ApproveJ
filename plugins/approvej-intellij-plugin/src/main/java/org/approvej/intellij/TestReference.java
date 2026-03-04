package org.approvej.intellij;

import org.jetbrains.annotations.NotNull;

/** A reference to a test method: fully qualified class name and method name. */
record TestReference(@NotNull String className, @NotNull String methodName) {}

package org.approvej.configuration;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/** Internal interface for configuration sources. */
@NullMarked
@FunctionalInterface
interface ConfigurationSource {
  @Nullable String getValue(String key);
}

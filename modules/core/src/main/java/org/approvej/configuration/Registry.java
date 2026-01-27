package org.approvej.configuration;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.ServiceLoader;
import org.jspecify.annotations.NullMarked;

/**
 * Registry for components that can be configured via aliases.
 *
 * <p>Components are discovered via the Java {@link ServiceLoader} mechanism. Providers register
 * themselves by implementing the {@link Provider} interface and adding a corresponding entry in
 * {@code META-INF/services/org.approvej.configuration.Provider}.
 *
 * <p>The registry supports both alias-based lookups (e.g., "json", "yaml") and fallback to
 * fully-qualified class names for backward compatibility.
 */
@NullMarked
public final class Registry {

  private static final Map<Class<?>, Map<String, Provider<?>>> providersByType = new HashMap<>();

  static {
    loadProviders();
  }

  private Registry() {}

  @SuppressWarnings("rawtypes")
  private static void loadProviders() {
    ServiceLoader<Provider> loader = ServiceLoader.load(Provider.class);
    for (Provider<?> provider : loader) {
      providersByType
          .computeIfAbsent(provider.type(), k -> new HashMap<>())
          .put(provider.alias(), provider);
    }
  }

  /**
   * Resolves a component by its alias or fully-qualified class name.
   *
   * <p>First attempts to find a registered provider with the given alias. If no provider is found,
   * falls back to creating an instance via reflection using the alias as a class name.
   *
   * @param aliasOrClassName the alias (e.g., "json") or fully-qualified class name
   * @param type the expected component type
   * @param <T> the component type
   * @return the resolved component instance
   * @throws ConfigurationError if the component cannot be resolved
   */
  public static <T> T resolve(String aliasOrClassName, Class<T> type) {
    // First, try to find by alias
    return findByAlias(aliasOrClassName, type)
        .orElseGet(() -> createByReflection(aliasOrClassName, type));
  }

  /**
   * Finds a provider by its alias for the given type.
   *
   * @param alias the alias to look up
   * @param type the expected component type
   * @param <T> the component type
   * @return an Optional containing the component if found
   */
  @SuppressWarnings("unchecked")
  public static <T> Optional<T> findByAlias(String alias, Class<T> type) {
    Map<String, Provider<?>> providers = providersByType.get(type);
    if (providers != null && providers.containsKey(alias)) {
      return Optional.of((T) providers.get(alias).create());
    }
    return Optional.empty();
  }

  @SuppressWarnings("unchecked")
  private static <T> T createByReflection(String className, Class<T> type) {
    try {
      Object instance = Class.forName(className).getDeclaredConstructor().newInstance();
      if (!type.isInstance(instance)) {
        throw new ConfigurationError(
            "Class %s is not an instance of %s".formatted(className, type.getName()), null);
      }
      return (T) instance;
    } catch (ReflectiveOperationException e) {
      throw new ConfigurationError("Failed to create %s %s".formatted(toHumanReadable(type), className), e);
    }
  }

  private static String toHumanReadable(Class<?> type) {
    String simpleName = type.getSimpleName();
    // Convert camelCase to "camel case" (e.g., PrintFormat -> print format)
    return simpleName
        .replaceAll("([a-z])([A-Z])", "$1 $2")
        .toLowerCase();
  }
}

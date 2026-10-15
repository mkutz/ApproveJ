package org.approvej.spring;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;
import org.jspecify.annotations.NullMarked;
import org.springframework.test.context.MergedContextConfiguration;
import org.springframework.test.context.web.WebMergedContextConfiguration;
import org.springframework.util.StringUtils;

/**
 * Prints a {@link MergedContextConfiguration} as stable, human-readable lines.
 *
 * <p>Only details that are relevant to the configuration's equality are printed, so the same
 * context is always printed the same way, no matter which test class it was determined from.
 */
@NullMarked
final class ContextConfigurationPrinter {

  private static final String SPRING_BOOT_MARKER_PROPERTY =
      "org.springframework.boot.test.context.SpringBootTestContextBootstrapper=true";

  private ContextConfigurationPrinter() {}

  static List<String> print(MergedContextConfiguration configuration) {
    List<String> lines = new ArrayList<>();
    lines.add("loader: %s".formatted(configuration.getContextLoader().getClass().getSimpleName()));
    if (configuration.getClass() != MergedContextConfiguration.class) {
      lines.add("type: %s".formatted(configuration.getClass().getSimpleName()));
    }
    if (configuration instanceof WebMergedContextConfiguration webConfiguration
        && StringUtils.hasText(webConfiguration.getResourceBasePath())) {
      lines.add("resource base path: %s".formatted(webConfiguration.getResourceBasePath()));
    }
    addList(lines, "classes", Arrays.stream(configuration.getClasses()).map(Class::getName));
    addList(lines, "locations", Arrays.stream(configuration.getLocations()));
    addList(
        lines,
        "initializers",
        configuration.getContextInitializerClasses().stream().map(Class::getName).sorted());
    addList(lines, "profiles", Arrays.stream(configuration.getActiveProfiles()));
    addList(
        lines,
        "property sources",
        configuration.getPropertySourceDescriptors().stream().map(Object::toString));
    addList(
        lines,
        "properties",
        Arrays.stream(configuration.getPropertySourceProperties())
            .filter(property -> !property.equals(SPRING_BOOT_MARKER_PROPERTY)));
    addList(
        lines,
        "customizers",
        configuration.getContextCustomizers().stream()
            .flatMap(ContextCustomizerPrinter::print)
            .sorted());
    MergedContextConfiguration parent = configuration.getParent();
    if (parent != null) {
      lines.add("parent:");
      print(parent).forEach(line -> lines.add("  %s".formatted(line)));
    }
    return lines;
  }

  private static void addList(List<String> lines, String key, Stream<String> values) {
    List<String> valueList = values.toList();
    if (valueList.isEmpty()) {
      return;
    }
    lines.add("%s:".formatted(key));
    valueList.forEach(value -> lines.add("  - %s".formatted(value)));
  }
}

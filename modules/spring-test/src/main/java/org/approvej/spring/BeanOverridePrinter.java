package org.approvej.spring;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.stream.Stream;
import org.jspecify.annotations.NullMarked;
import org.springframework.test.context.bean.override.BeanOverrideHandler;

/**
 * Prints the {@link BeanOverrideHandler}s of a bean override context customizer, e.g. for
 * {@code @MockitoBean}, {@code @MockitoSpyBean}, or {@code @TestBean}.
 *
 * <p>This is a separate class so {@link BeanOverrideHandler} is only loaded if bean overrides are
 * used, as it does not exist in older Spring versions.
 */
@NullMarked
final class BeanOverridePrinter {

  private BeanOverridePrinter() {}

  static Stream<String> print(Collection<?> handlers) {
    return handlers.stream().map(BeanOverrideHandler.class::cast).map(BeanOverridePrinter::print);
  }

  private static String print(BeanOverrideHandler handler) {
    StringBuilder line =
        new StringBuilder("%s %s".formatted(annotation(handler), handler.getBeanType()));
    String beanName = handler.getBeanName();
    Field field = handler.getField();
    if (beanName != null) {
      line.append(" name=%s".formatted(beanName));
    } else if (field != null) {
      line.append(" field=%s".formatted(field.getName()));
    }
    if (!handler.getContextName().isEmpty()) {
      line.append(" context=%s".formatted(handler.getContextName()));
    }
    return line.toString();
  }

  private static String annotation(BeanOverrideHandler handler) {
    return switch (handler.getClass().getSimpleName()) {
      case "MockitoBeanOverrideHandler" -> "@MockitoBean";
      case "MockitoSpyBeanOverrideHandler" -> "@MockitoSpyBean";
      case "TestBeanOverrideHandler" -> "@TestBean";
      default -> handler.getClass().getName();
    };
  }
}

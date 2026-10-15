package org.approvej.spring.fixtures;

import java.util.List;
import org.jspecify.annotations.Nullable;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.test.context.ContextConfigurationAttributes;
import org.springframework.test.context.ContextCustomizer;
import org.springframework.test.context.ContextCustomizerFactory;
import org.springframework.test.context.MergedContextConfiguration;

public class FixtureContextCustomizerFactory implements ContextCustomizerFactory {

  @Override
  public @Nullable ContextCustomizer createContextCustomizer(
      Class<?> testClass, List<ContextConfigurationAttributes> configurationAttributes) {
    FixtureCustomization customization =
        AnnotatedElementUtils.findMergedAnnotation(testClass, FixtureCustomization.class);
    return customization == null ? null : new FixtureContextCustomizer(customization.value());
  }

  record FixtureContextCustomizer(String value) implements ContextCustomizer {

    @Override
    public void customizeContext(
        ConfigurableApplicationContext context, MergedContextConfiguration configuration) {}
  }
}

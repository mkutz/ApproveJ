package org.approvej.spring.fixtures;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.test.context.ContextCustomizerFactories;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@ContextCustomizerFactories(FixtureContextCustomizerFactory.class)
public @interface FixtureCustomization {

  String value();
}

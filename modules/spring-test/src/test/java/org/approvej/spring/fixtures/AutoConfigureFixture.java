package org.approvej.spring.fixtures;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.boot.test.context.PropertyMapping;

@Target(java.lang.annotation.ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@PropertyMapping("fixture")
public @interface AutoConfigureFixture {

  boolean enabled() default true;
}

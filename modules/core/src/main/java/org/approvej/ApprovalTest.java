package org.approvej;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * Enables dangling approval detection for the annotated test class.
 *
 * <p>After each test, any {@link ApprovalBuilder#approve(Object) approve()} call that was not
 * concluded with a terminal method ({@link ApprovalBuilder#by(java.util.function.Function) by()},
 * {@link ApprovalBuilder#byFile() byFile()}, or {@link ApprovalBuilder#byValue(String) byValue()})
 * will cause a {@link DanglingApprovalError}.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@ExtendWith(DanglingApprovalExtension.class)
public @interface ApprovalTest {}

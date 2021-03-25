package com.arjunsk.codekrypt.di.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This is used to resolve Bean conflicts. If multiple implementations are available for an
 * interface, then we use @Qualifier(value="ClassName") to resolve the conflict.
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface Qualifier {
  String value() default "";
}

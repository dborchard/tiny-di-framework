package com.arjunsk.codekrypt.di.core.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This is used to resolve Bean conflicts. If multiple implementations are available for an
 * interface, then we use @Qualifier(value="ClassName") to resolve the conflict.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Qualifier {
  String value() default "";
}

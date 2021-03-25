package com.arjunsk.codekrypt.di.utils;

import com.arjunsk.codekrypt.di.annotation.Autowire;
import com.arjunsk.codekrypt.di.annotation.PostConstruct;
import com.arjunsk.codekrypt.di.annotation.Qualifier;
import com.arjunsk.codekrypt.di.core.BeanManager;
import com.arjunsk.codekrypt.di.exceptions.BeanInjectException;
import com.arjunsk.codekrypt.di.exceptions.InvokeException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public final class BeanOperationsUtils {

  private BeanOperationsUtils() {}

  /**
   * Inject instance to @Autowire Field.
   *
   * @param beanManager Bean Manager for fetching the @Autowire Field instance.
   * @param implementationClass Implementation class for @Autowire Field.
   * @param classInstance @Component class instance using @Autowire Field.
   */
  public static void invokeAutowire(
      BeanManager beanManager, Class<?> implementationClass, Object classInstance) {

    // 1. Get all the @Autowire Fields.
    List<Field> fieldList = new ArrayList<>();
    fieldList.addAll(Arrays.asList(implementationClass.getDeclaredFields()));

    // 2. Get the fields from Super Class if it exist.
    Class<?> superclass = implementationClass.getSuperclass();
    if (superclass != null) fieldList.addAll(Arrays.asList(superclass.getDeclaredFields()));

    List<Field> injectableFieldList =
        fieldList.stream()
            .filter(field -> field.isAnnotationPresent(Autowire.class))
            .collect(Collectors.toList());

    try {

      // 2. Inject ClassInstance to all the Autowire fields.
      for (Field injectableField : injectableFieldList) {

        String qualifier =
            injectableField.isAnnotationPresent(Qualifier.class)
                ? injectableField.getAnnotation(Qualifier.class).value()
                : null;

        Object fieldInstance =
            beanManager.getBeanInstance(
                injectableField.getType(), injectableField.getName(), qualifier);

        // Recursive calling. We call invokeAutowire on the Field class to ensure that the @Autowire
        // inside field class is resolved before hand. [Snip 1]
        invokeAutowire(beanManager, injectableField.getClass(), fieldInstance);

        // If the Field Class @Autowire's are resolved, we set field value as the object. [Snip 2]
        injectableField.setAccessible(true);
        injectableField.set(classInstance, fieldInstance);

        // NOTE: since we are currently only supporting setter injection, it really doesn't matter
        // if we interchange [snip 1] & [snip 2].
      }

      // 3. Post Construct call.
      invokePostConstruct(implementationClass, classInstance);

    } catch (Exception ex) {
      throw new BeanInjectException("Unable to Inject bean", ex);
    }
  }

  /**
   * Invoke all the post Construct calls.
   *
   * @param implementationClass @Component Class.
   * @param classInstance Class Instance.
   */
  private static void invokePostConstruct(Class<?> implementationClass, Object classInstance) {

    try {
      for (Method declaredMethod : implementationClass.getDeclaredMethods()) {
        if (declaredMethod.isAnnotationPresent(PostConstruct.class)) {
          declaredMethod.invoke(classInstance);
        }
      }
    } catch (Exception ex) {
      throw new InvokeException("Invoke Exception", ex);
    }
  }
}

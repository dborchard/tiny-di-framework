package com.arjunsk.codekrypt.di.utils;

import com.arjunsk.codekrypt.di.annotation.Autowire;
import com.arjunsk.codekrypt.di.core.BeanManager;
import com.arjunsk.codekrypt.di.annotation.PostConstruct;
import com.arjunsk.codekrypt.di.annotation.Qualifier;
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
    fieldList.addAll(Arrays.asList(implementationClass.getSuperclass().getDeclaredFields()));

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

        injectableField.setAccessible(true);
        injectableField.set(classInstance, fieldInstance);

        invokeAutowire(beanManager, injectableField.getClass(), fieldInstance);
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

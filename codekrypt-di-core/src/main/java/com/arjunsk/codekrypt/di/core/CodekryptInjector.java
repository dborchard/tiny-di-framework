package com.arjunsk.codekrypt.di.core;

import com.arjunsk.codekrypt.di.annotation.Autowire;
import com.arjunsk.codekrypt.di.annotation.Component;
import com.arjunsk.codekrypt.di.annotation.Qualifier;
import com.arjunsk.codekrypt.di.exceptions.BeanInitiateException;
import com.arjunsk.codekrypt.di.utils.ClassObjectUtils;
import java.lang.reflect.Constructor;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class CodekryptInjector {

  private static final BeanManager beanManager = new BeanManager();

  private CodekryptInjector() {}

  /**
   * Entry point for Codekrypt Injector.
   *
   * @param mainClass Main class of the program.
   */
  public static void run(Class<?> mainClass) {

    // 1. Get all the Component classes
    String packageToScan = mainClass.getPackage().getName();

    ClassScanner classScanner = new ClassScanner(packageToScan);

    Set<Class<?>> componentClasses =
        classScanner.getLocatedClasses().stream()
            .filter(item -> item.isAnnotationPresent(Component.class))
            .collect(Collectors.toSet());

    // 2. Initialize Bean-Inheritance-Map
    for (Class<?> componentClass : componentClasses) {
      Class<?>[] interfaces = componentClass.getInterfaces();
      if (interfaces.length == 0) {
        beanManager.addBeanInheritanceMapping(componentClass, componentClass);
      } else {

        // TODO: To fix, multiple interface for 1 impl class (Diamond problem)
        for (Class<?> interfaceClass : interfaces) {
          beanManager.addBeanInheritanceMapping(componentClass, interfaceClass);
        }
      }
    }

    // 3. Instantiate all the component class.
    try {
      for (Class<?> componentClass : componentClasses) {
        // Create a bean
        Object beanClassObject = initBeanClass(componentClass, null);

        // TODO: Fix post-construct invocation order.

        // Post Construct call.
        ClassObjectUtils.invokePostConstruct(beanClassObject);
      }
    } catch (Exception ex) {
      throw new BeanInitiateException("Unable to Initiate Class", ex);
    }
  }

  /**
   * Initiates Bean Class.
   *
   * @param beanClass Bean Class to be initiated
   * @param qualifier For @Autowire Field, Qualifier would be present, in case of multiple
   *     implementations. For @Component Class, Qualifier is null.
   * @param <T> Class Type
   */
  private static <T> Object initBeanClass(Class<T> beanClass, String qualifier) {

    boolean isOneConstructorInjectionDefined = false;

    // Used for class.newInstance(class[],objects[])
    List<Class<?>> argClassList = new LinkedList<>();
    List<Object> argObjectList = new ArrayList<>();

    for (Constructor<?> constructor : beanClass.getConstructors()) {

      // Fail if we have, more than 1 @Autowire Constructor.
      if (isOneConstructorInjectionDefined) {
        throw new IllegalArgumentException("Only supports 1 Autowire constructor.");
      }

      if (constructor.isAnnotationPresent(Autowire.class)) {

        isOneConstructorInjectionDefined = true;

        for (Parameter field : constructor.getParameters()) {
          argClassList.add(field.getType());

          // Getting @Qualifier attribute from the Field.
          String qualifierVal =
              field.isAnnotationPresent(Qualifier.class)
                  ? field.getAnnotation(Qualifier.class).value()
                  : null;

          // Create object for this @Autowire Field.
          Object fieldArgumentObject = initBeanClass(field.getType(), qualifierVal);
          argObjectList.add(fieldArgumentObject);
        }
      }
    }
    Object beanClassObject;
    String beanClassName = beanClass.getName();

    if (!isOneConstructorInjectionDefined) {

      // 3.1 Here it will be interface, ie @Autowire Field Or a concrete class, with no constructor
      // injection.
      beanClassObject = beanManager.getBeanInstance(beanClass, beanClassName, qualifier);

    } else {

      // 3.2 Here it will be @Component class, with constructor injection.
      beanClassObject =
          beanManager.getBeanInstance(
              beanClass, beanClassName, qualifier, argClassList, argObjectList);
    }

    // 4. Autowire + recursion.
    ClassObjectUtils.invokeAutowire(beanManager, beanClass, beanClassObject);

    return beanClassObject;
  }
}

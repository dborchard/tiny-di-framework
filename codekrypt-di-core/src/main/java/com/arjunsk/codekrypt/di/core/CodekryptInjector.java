package com.arjunsk.codekrypt.di.core;

import com.arjunsk.codekrypt.di.annotation.Autowire;
import com.arjunsk.codekrypt.di.annotation.Component;
import com.arjunsk.codekrypt.di.annotation.Qualifier;
import com.arjunsk.codekrypt.di.exceptions.BeanInitiateException;
import com.arjunsk.codekrypt.di.utils.BeanOperationsUtils;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
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
        /**
         * 1. For @Component class, Qualifier is null.
         *
         * <p>2.For @Autowire Field, Qualifier is not null. (Passed via {@link
         * #initComponentClass(Class, String)} })
         */
        initComponentClass(componentClass, null);
      }
    } catch (Exception ex) {
      throw new BeanInitiateException("Unable to Initiate Class", ex);
    }
  }

  private static <T> Object initComponentClass(Class<T> componentClass, String qualifier)
      throws InstantiationException, IllegalAccessException, NoSuchMethodException,
          InvocationTargetException {

    boolean isConstructorInjection = false;

    // Used for class.newInstance(class[],objects[])
    List<Class<?>> argClassList = new LinkedList<>();
    List<Object> argObjectList = new ArrayList<>();

    for (Constructor<?> constructor : componentClass.getConstructors()) {

      // Fail if we have, more than 1 @Autowire Constructor.
      if (isConstructorInjection) {
        throw new IllegalArgumentException("Only supports 1 Autowire constructor.");
      }

      if (constructor.isAnnotationPresent(Autowire.class)) {

        isConstructorInjection = true;

        for (Parameter field : constructor.getParameters()) {
          argClassList.add(field.getType());

          // Getting @Qualifier attribute from the Field.
          String qualifierVal =
              field.isAnnotationPresent(Qualifier.class)
                  ? field.getAnnotation(Qualifier.class).value()
                  : null;

          // Create object for this @Autowire Field.
          Object fieldArgumentObject = initComponentClass(field.getType(), qualifierVal);
          argObjectList.add(fieldArgumentObject);
        }
      }
    }

    Object componentClassObject;

    if (!isConstructorInjection) {

      // 3.1 Here it will be interface, ie @Autowire Field. So we get the correct bean from the
      // IoC container (based on qualifier if any).

      // NOTE: It will create an instance if not present and add it to the IoC container.
      componentClassObject =
          beanManager.getBeanInstance(componentClass, componentClass.getName(), qualifier);

    } else {

      // 3.2 Here it will be @Component class, with constructor injection.

      // NOTE: Here we are explicitly adding the component instance to the IoC container.
      componentClassObject =
          componentClass
              .getDeclaredConstructor(argClassList.toArray(new Class[0]))
              .newInstance(argObjectList.toArray());

      beanManager.addClassInstancesMapping(componentClass, componentClassObject);
    }

    // 3.2 Autowire + recursion.
    BeanOperationsUtils.invokeAutowire(beanManager, componentClass, componentClassObject);

    return componentClassObject;
  }
}

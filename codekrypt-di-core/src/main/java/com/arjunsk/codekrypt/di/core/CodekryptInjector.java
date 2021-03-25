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
        // For @Component class, Qualifier is null.
        // For @Autowire Field, Qualifier is not null.
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

    List<Class<?>> argClassList = new LinkedList<>();
    List<Object> argObjectList = new ArrayList<>();

    for (Constructor<?> constructor : componentClass.getConstructors()) {
      if (constructor.isAnnotationPresent(Autowire.class)) {

        isConstructorInjection = true;

        for (Parameter parameter : constructor.getParameters()) {
          argClassList.add(parameter.getType());

          String qualifierVal =
              parameter.isAnnotationPresent(Qualifier.class)
                  ? parameter.getAnnotation(Qualifier.class).value()
                  : null;

          // Initializing the object for the @Autowire Field.
          Object fieldArgumentObject = initComponentClass(parameter.getType(), qualifierVal);
          argObjectList.add(fieldArgumentObject);
        }
      }
    }

    // Once the dependent fields are initialized, it is ready to be passed to the component
    // constructor.

    Object componentClassObject;

    if (!isConstructorInjection) {

      // 3.1 Here it will be interface, ie @Autowire Field. So get the correct impl class and create
      // the object.
      Class<?> implementationClass =
          beanManager.getImplementationClass(componentClass, componentClass.getName(), qualifier);

      componentClassObject = implementationClass.newInstance();
    } else {

      // 3.2 Here it will be @component class, with constructor injection.
      componentClassObject =
          componentClass
              .getDeclaredConstructor(argClassList.toArray(new Class[0]))
              .newInstance(argObjectList.toArray());
    }

    beanManager.addClassInstancesMapping(componentClass, componentClassObject);

    // 3.2 Autowire + recursion.
    BeanOperationsUtils.invokeAutowire(beanManager, componentClass, componentClassObject);

    return componentClassObject;
  }
}

package com.arjunsk.codekrypt.di.core;

import com.arjunsk.codekrypt.di.annotation.Component;
import com.arjunsk.codekrypt.di.exceptions.BeanInitiateException;
import com.arjunsk.codekrypt.di.utils.BeanOperationsUtils;
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
        // 3.1 Add class instance to Class-Instance-Map
        Object componentClassObject = componentClass.newInstance();
        beanManager.addClassInstancesMapping(componentClass, componentClassObject);

        // 3.2 Autowire + recursion.
        BeanOperationsUtils.invokeAutowire(beanManager, componentClass, componentClassObject);
      }
    } catch (Exception ex) {
      throw new BeanInitiateException("Unable to Initiate Class", ex);
    }
  }
}

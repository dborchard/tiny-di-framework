package com.arjunsk.codekrypt.di.core;

import com.arjunsk.codekrypt.di.exceptions.BeanFetchException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/** Will manage the beans and there relationships. */
public class BeanManager {

  // This Map will hold the Class and its corresponding Interface/Class name.
  private final Map<Class<?>, Class<?>> beanInheritanceMap;

  // This Map will hold the Class and its corresponding Singleton Instance.
  private final Map<Class<?>, Object> classInstancesMap;

  public BeanManager() {
    this.beanInheritanceMap = new HashMap<>();
    this.classInstancesMap = new HashMap<>();
  }

  public void addBeanInheritanceMapping(Class<?> componentClass, Class<?> interfaceClass) {
    beanInheritanceMap.put(componentClass, interfaceClass);
  }

  public void addClassInstancesMapping(Class<?> componentClass, Object componentClassObject) {
    classInstancesMap.put(componentClass, componentClassObject);
  }

  /**
   * Fetch Bean Instance based on the input criteria.
   *
   * @param interfaceClass interface of the @Autowire field.
   * @param fieldName Used for match by field name.
   * @param qualifier Used for match by qualifier name.
   * @return Object of the class
   */
  public <T> Object getBeanInstance(
      Class<T> interfaceClass, final String fieldName, final String qualifier) {

    Class<?> implementationClass = getImplementationClass(interfaceClass, fieldName, qualifier);

    // If Impl class instance already available, return that.
    if (classInstancesMap.containsKey(implementationClass))
      return classInstancesMap.get(implementationClass);

    // else, create a new instance and insert into map and return the Object.
    try {
      synchronized (classInstancesMap) {
        Object service = implementationClass.newInstance();
        classInstancesMap.put(implementationClass, service);
        return service;
      }
    } catch (Exception ex) {
      throw new BeanFetchException("Bean Initialization error", ex);
    }
  }

  /**
   * Returns Implementation class for an interface.
   *
   * @param interfaceClass interface class
   * @param fieldName @Autowire field name
   * @param qualifier @Qualifier value
   * @return Implementation class of the interface.
   */
  private Class<?> getImplementationClass(
      Class<?> interfaceClass, String fieldName, String qualifier) {

    String errorMessage;

    // Get all the implementation classes for the interface.
    Set<Entry<Class<?>, Class<?>>> implementationClasses =
        beanInheritanceMap.entrySet().stream()
            .filter(entry -> entry.getValue() == interfaceClass)
            .collect(Collectors.toSet());

    if (implementationClasses.isEmpty()) {
      errorMessage = "No impl found";
    } else if (implementationClasses.size() == 1) {
      return implementationClasses.stream().findFirst().get().getKey();
    } else {

      // If there are multiple impl, we use qualifier or field name for resolving.
      String findBy = (qualifier == null || qualifier.isEmpty()) ? fieldName : qualifier;
      Optional<Entry<Class<?>, Class<?>>> optional =
          implementationClasses.stream()
              .filter(entry -> entry.getKey().getSimpleName().equalsIgnoreCase(findBy))
              .findAny();
      if (optional.isPresent()) {
        return optional.get().getKey();
      } else {
        // If findBy is not matched, we reach this else block.
        errorMessage =
            String.format(
                "%s implementations of %s found. Use @Qualifier to resolve it.",
                implementationClasses.size(), interfaceClass.getName());
      }
    }

    throw new BeanFetchException(errorMessage);
  }
}

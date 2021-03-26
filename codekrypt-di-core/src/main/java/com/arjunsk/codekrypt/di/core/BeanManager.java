package com.arjunsk.codekrypt.di.core;

import com.arjunsk.codekrypt.di.exceptions.BeanFetchException;
import java.util.HashMap;
import java.util.List;
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

  /**
   * Fetch Bean Instance based on the input criteria. (For Beans without constructor injection)
   *
   * @param interfaceClass interface of the @Autowire field.
   * @param fieldName Used for match by field name.
   * @param qualifier Used for match by qualifier name.
   * @return Object of bean class.
   */
  public <T> Object getBeanInstance(
      Class<T> interfaceClass, final String fieldName, final String qualifier) {
    return getBeanInstance(interfaceClass, fieldName, qualifier, null, null);
  }

  /**
   * Fetch Bean Instance based on the input criteria. (For Beans with constructor injection)
   *
   * @param interfaceClass interface of the @Autowire field.
   * @param fieldName Used for match by field name.
   * @param qualifier Used for match by qualifier name.
   * @param argClassList list of Classes to instantiate a class with parametrized constructor.
   * @param argObjectList list of Object to instantiate a class with parametrized constructor.
   * @return Object of bean class.
   */
  public <T> Object getBeanInstance(
      Class<T> interfaceClass,
      final String fieldName,
      final String qualifier,
      List<Class<?>> argClassList,
      List<Object> argObjectList) {

    Class<?> implementationClass = getImplementationClass(interfaceClass, fieldName, qualifier);

    Object classInstance;

    // 1. If Impl class instance already available, return that.
    if (classInstancesMap.containsKey(implementationClass)) {
      classInstance = classInstancesMap.get(implementationClass);
    } else {

      // 2. else, create a new instance and insert into map and return the Object.
      try {
        synchronized (classInstancesMap) {
          if (argClassList == null || argClassList.isEmpty()) {
            // 2.1 Create a new Object with empty constructor.
            classInstance = implementationClass.newInstance();
          } else {

            // 2.2 Create a new object with parameterized constructor.
            Class<?>[] argClassArray = argClassList.toArray(new Class[0]);
            Object[] argObjectArray = argObjectList.toArray();

            classInstance =
                implementationClass
                    .getDeclaredConstructor(argClassArray)
                    .newInstance(argObjectArray);
          }

          // 3. save to the IoC Map.
          classInstancesMap.put(implementationClass, classInstance);
        }

      } catch (Exception ex) {
        throw new BeanFetchException("Bean Initialization error", ex);
      }
    }
    // 4. Return the instance with the flag.
    return classInstance;
  }

  /**
   * Returns Implementation class for an interface/class.
   *
   * @param inputClass interface/concrete class
   * @param fieldName @Autowire field name
   * @param qualifier @Qualifier value
   * @return Implementation class of the interface.
   */
  private Class<?> getImplementationClass(Class<?> inputClass, String fieldName, String qualifier) {

    /* 1. if this is a concrete class, then return the same class.*/
    if (!inputClass.isInterface()) {
      return inputClass;
    }

    /* 2. Else if it is an interface, return the implementation class. */
    String errorMessage;

    // Get all the implementation classes for the interface.
    Set<Entry<Class<?>, Class<?>>> implementationClasses =
        beanInheritanceMap.entrySet().stream()
            .filter(entry -> entry.getValue() == inputClass)
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
                implementationClasses.size(), inputClass.getName());
      }
    }

    throw new BeanFetchException(errorMessage);
  }
}

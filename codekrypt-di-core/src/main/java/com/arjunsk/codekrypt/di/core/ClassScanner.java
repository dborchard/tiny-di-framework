package com.arjunsk.codekrypt.di.core;

import com.arjunsk.codekrypt.di.exceptions.ClassLoadException;
import java.io.File;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

/** Fetches all the classes in the package. */
public class ClassScanner {

  private final Set<Class<?>> locatedClasses;

  public ClassScanner(String packagePath) {
    this.locatedClasses = new HashSet<>();
    this.locateClasses(packagePath);
  }

  /**
   * Entry point for calling recursive function.
   *
   * @param packagePath base path (path of the Driver class).
   */
  public void locateClasses(String packagePath) {
    URL root =
        Thread.currentThread().getContextClassLoader().getResource(packagePath.replace(".", "/"));

    // We invoke scanDirectory and recurse to traverse the tree.
    scanDirectory(new File(root.getFile()), packagePath);
  }

  /**
   * Scan Directory for the classes.
   *
   * @param file Class File
   * @param path The current package path.
   */
  private void scanDirectory(File file, String path) {
    try {
      // If Directory
      if (file.isDirectory()) {
        for (File innerFile : file.listFiles()) {
          scanDirectory(innerFile, path + "." + innerFile.getName());
        }
      } else {
        // If .class file.
        if (file.getName().endsWith(".class")) {
          Class<?> classInstance = Class.forName(path.replace(".class", ""));
          this.locatedClasses.add(classInstance);
        }
      }
    } catch (Exception ex) {
      throw new ClassLoadException("Class Load Exception", ex);
    }
  }

  /** Get all the located classes. */
  public Set<Class<?>> getLocatedClasses() {
    return locatedClasses;
  }
}

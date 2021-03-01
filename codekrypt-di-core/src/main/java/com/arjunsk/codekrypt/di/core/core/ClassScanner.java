package com.arjunsk.codekrypt.di.core.core;

import com.arjunsk.codekrypt.di.core.exceptions.ClassLoadException;
import java.io.File;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

public class ClassScanner {

  private final Set<Class<?>> locatedClasses;

  public ClassScanner(String packagePath) {
    this.locatedClasses = new HashSet<>();
    this.locateClasses(packagePath);
  }

  public void locateClasses(String packagePath) {
    URL root =
        Thread.currentThread().getContextClassLoader().getResource(packagePath.replace(".", "/"));
    scanDirectory(new File(root.getFile()), packagePath);
  }

  private void scanDirectory(File file, String path) {
    try {
      if (file.isDirectory()) {
        for (File innerFile : file.listFiles()) {
          scanDirectory(innerFile, path + "." + innerFile.getName());
        }
      } else {
        if (file.getName().endsWith(".class")) {
          Class<?> classInstance = Class.forName(path.replace(".class", ""));
          this.locatedClasses.add(classInstance);
        }
      }
    } catch (Exception ex) {
      throw new ClassLoadException("Class Load Exception", ex);
    }
  }

  public Set<Class<?>> getLocatedClasses() {
    return locatedClasses;
  }
}

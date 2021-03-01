package com.arjunsk.codekrypt.di.core.exceptions;

public class ClassLoadException extends RuntimeException {

  public ClassLoadException(String message) {
    super(message);
  }

  public ClassLoadException(String message, Throwable cause) {
    super(message, cause);
  }
}

package com.arjunsk.codekrypt.di.core.exceptions;

public class BeanInjectException extends RuntimeException {

  public BeanInjectException(String message) {
    super(message);
  }

  public BeanInjectException(String message, Throwable cause) {
    super(message, cause);
  }
}

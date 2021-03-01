package com.arjunsk.codekrypt.di.core.exceptions;

public class BeanInitiateException extends RuntimeException {

  public BeanInitiateException(String message) {
    super(message);
  }

  public BeanInitiateException(String message, Throwable cause) {
    super(message, cause);
  }
}

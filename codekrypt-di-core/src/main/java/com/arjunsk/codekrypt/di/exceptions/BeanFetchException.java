package com.arjunsk.codekrypt.di.exceptions;

public class BeanFetchException extends RuntimeException {

  public BeanFetchException(String message) {
    super(message);
  }

  public BeanFetchException(String message, Throwable cause) {
    super(message, cause);
  }
}

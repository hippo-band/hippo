package com.github.hippo.exception;

public class HippoRuntimeException extends RuntimeException {

  private static final long serialVersionUID = -309132048507267819L;

  public HippoRuntimeException(String msg) {
    super(msg);
  }

  public HippoRuntimeException(String msg, Throwable cause) {
    super(msg, cause);
  }
}

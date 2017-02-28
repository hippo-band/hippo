package com.github.hippo.exception;

/**
 * 服务服务异常
 * 
 * @author sl
 *
 */
public class HippoServiceException extends HippoRuntimeException {


  /**
   * 
   */
  private static final long serialVersionUID = 1850002644637772940L;

  public HippoServiceException(String msg) {
    super(msg);

  }

  public HippoServiceException(String msg, Throwable throwable) {
    super(msg, throwable);
  }

}

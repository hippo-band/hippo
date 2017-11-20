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

  private int code;

  public HippoServiceException(String msg) {
    super(msg);

  }

  public HippoServiceException(int code, String msg) {
    super(msg);
    this.code = code;
  }

  public HippoServiceException(String msg, Throwable throwable) {
    super(msg, throwable);
  }

  public HippoServiceException(int code, String msg, Throwable throwable) {
    super(msg, throwable);
    this.code = code;
  }

  public int getCode() {
    return code;
  }

  public void setCode(int code) {
    this.code = code;
  }


}

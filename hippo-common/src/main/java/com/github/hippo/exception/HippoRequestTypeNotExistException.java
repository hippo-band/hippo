package com.github.hippo.exception;

/**
 * 请求类型不存在
 * 
 * @author sl
 *
 */
public class HippoRequestTypeNotExistException extends HippoRuntimeException {


  /**
   * 
   */
  private static final long serialVersionUID = 5734324880718980024L;

  public HippoRequestTypeNotExistException(String msg) {
    super(msg);

  }

  public HippoRequestTypeNotExistException(String msg, Throwable throwable) {
    super(msg, throwable);
  }



}

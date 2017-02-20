package com.github.hippo.exception;

/**
 * 服务名称冲突
 * 
 * @author sl
 *
 */
public class HippoServiceNameDuplicationException extends HippoRuntimeException {
  /**
   * 
   */
  private static final long serialVersionUID = 6552079356728112880L;

  /**
   * ServiceName重复异常
   * @param msg 异常msg
   */
  public HippoServiceNameDuplicationException(String msg) {
    super(msg);

  }

  /**
   * ServiceName重复异常
   * @param msg 异常msg
   * @param throwable 具体异常
   */
  public HippoServiceNameDuplicationException(String msg, Throwable throwable) {
    super(msg, throwable);
  }



}

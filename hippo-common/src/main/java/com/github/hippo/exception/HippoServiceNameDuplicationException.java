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
   * 
   */

  public HippoServiceNameDuplicationException(String msg) {
    super(msg);

  }

  public HippoServiceNameDuplicationException(String msg, Throwable throwable) {
    super(msg, throwable);
  }



}

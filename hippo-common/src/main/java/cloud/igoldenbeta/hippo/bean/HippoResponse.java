package cloud.igoldenbeta.hippo.bean;

import java.io.Serializable;


/**
 * 
 * 返回结果包装类
 * 
 * @author sl
 *
 */
public class HippoResponse implements Serializable {
  /**
   * 
   */
  private static final long serialVersionUID = -3924875427337306692L;
  private String requestId;
  private Object result;
  private Throwable throwable;
  private boolean isError=false;//default

  public String getRequestId() {
    return requestId;
  }

  public void setRequestId(String requestId) {
    this.requestId = requestId;
  }

  public Object getResult() {
    return result;
  }

  public void setResult(Object result) {
    this.result = result;
  }

  public Throwable getThrowable() {
    return throwable;
  }

  public void setThrowable(Throwable throwable) {
    this.throwable = throwable;
  }

  public boolean isError() {
    return isError;
  }

  public void setError(boolean isError) {
    this.isError = isError;
  }
}

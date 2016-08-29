package com.github.hippo.monitor;

import com.github.hippo.bean.HippoResponse;

/**
 * log监控
 * 
 * @author sl
 *
 */
public interface LogMonitor {
  public void errorLogMonitor(HippoResponse response);
}

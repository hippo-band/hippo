package cloud.igoldenbeta.hippo.govern;

/**
 * 服务管理
 * 
 * @author sl
 *
 */
public interface ServiceGovern {
  /**
   * 服务注册
   * 
   * @param serviceName
   * @return 注册时的端口号
   */
  public int register(String serviceName);

  /**
   * 获取服务地址 ip:port 
   * 127.0.0.1:7070
   * 
   * @param serviceName 注册服务名
   * @return
   */
  public String getServiceAddress(String serviceName);

}

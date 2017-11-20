package com.github.hippo.enums;
/**
 * 调用方式枚举
 * @author sl
 *
 */
public enum HippoRequestEnum {
  RPC(0, "RPC调用"), API(1, "API调用"), PING(2, "长连接PING");
  private int type;
  private String desc;

  private HippoRequestEnum(int type, String desc) {
    this.type = type;
    this.desc = desc;
  }

  public int getType() {
    return type;
  }

  public String getDesc() {
    return desc;
  }

  public static HippoRequestEnum getByType(int type) {
    for (HippoRequestEnum hippoRequestEnum : HippoRequestEnum.values()) {
      if (hippoRequestEnum.type == type) {
        return hippoRequestEnum;
      }
    }
    return null;
  }
}

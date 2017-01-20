package com.github.hippo.cache;

import org.apache.commons.lang3.StringUtils;

import java.util.Objects;
import java.util.UUID;

/**
 * Created by hanruofei on 16/8/30. 串起rpc调用链
 */
public enum MsgThreadLocal {
	Instance;

	/**
	 * 存放msgId
	 */
	private ThreadLocal<String> msgId = new ThreadLocal<>();
	/**
	 * 存放msgLevel (被rpc调用的次数)
	 */
	private ThreadLocal<Integer> msgLevel = new ThreadLocal<>();

	/**
	 * 获取msgId ThreadLocal对象
	 * 
	 * @return ThreadLocal
	 */
	protected ThreadLocal<String> getMsgIdItem() {
		return msgId;
	}

	/**
	 * 获取msgLevel ThreadLocal对象
	 *
	 * @return ThreadLocal
	 */
	protected ThreadLocal<Integer> getMsgLevelItem() {
		return msgLevel;
	}

	/**
	 * 获取ThreadLocal中的msgId value
	 * 
	 * @return msgId
	 */
	public String getMsgId() {
		if (StringUtils.isBlank(msgId.get()))
			msgId.set(UUID.randomUUID().toString());
		return msgId.get();
	}

	/**
	 * 获取ThreadLocal中的msgLevel value
	 *
	 * @return msgLevel
	 */
	public Integer getMsgLevel() {
		if (Objects.isNull(msgLevel.get()))
			msgLevel.set(0);
		return msgLevel.get();
	}

	/**
	 * 初始化ThreadLocal中的value
	 */
	public void setMsgId() {
		this.msgId.set(UUID.randomUUID().toString());
	}

	/**
	 * 插入或更新ThreadLocal中的value
	 * 
	 * @param msgId msgId
	 */
	public void setMsgId(String msgId) {
		this.msgId.set(msgId);
	}

	/**
	 * 初始化ThreadLocal中的value or 每次rpc调用都会 +1 msg level
	 */
	public void setMsgLevel() {
		if (Objects.isNull(msgLevel.get()))
			this.msgLevel.set(0);
		else
			this.msgLevel.set(msgLevel.get() + 1);
	}

	/**
	 * 插入或更新ThreadLocal中的value
	 *
	 * @param msgLevel msgLevel
	 */
	public void setMsgLevel(int msgLevel) {
		this.msgLevel.set(msgLevel);
	}

	/**
	 * logger调用
	 *
	 * @return 现有的msgId
	 */
	public String loggerGetMsgId() {
		return StringUtils.isBlank(msgId.get()) ? "" : msgId.get();
	}

	/**
	 * logger调用
	 *
	 * @return 现有的msgLevel
	 */
	public String loggerGetMsgLevel() {
		return Objects.isNull(msgLevel.get()) ? "" : String.valueOf(msgLevel.get());
	}
}

package com.github.hippo.logger;

import ch.qos.logback.classic.PatternLayout;

/**
 * Created by hanruofei on 16/8/30.
 */
public class HippoPatternLayout extends PatternLayout {

	static {
		defaultConverterMap.put("msgId", ChainIdConvert.class.getName());
		defaultConverterMap.put("msgLevel", ChainOrderConvert.class.getName());
	}

}

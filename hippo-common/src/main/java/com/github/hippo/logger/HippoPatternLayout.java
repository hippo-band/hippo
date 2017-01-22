package com.github.hippo.logger;

import ch.qos.logback.classic.PatternLayout;

/**
 * Created by hanruofei on 16/8/30.
 */
public class HippoPatternLayout extends PatternLayout {

	static {
		defaultConverterMap.put("ChainId", ChainIdConvert.class.getName());
		defaultConverterMap.put("ChainOrder", ChainOrderConvert.class.getName());
	}

}

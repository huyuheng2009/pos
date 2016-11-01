package com.yogapay.core.domain;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 * @Title ToString.java
 * @Description 重写toString方法，打印方便
 * @author dongjie.wang,<a href="mailto:wangdj@cnepay.com">wangdj@cnepay.com</a>
 * @date 2012-5-17 下午10:08:55
 * @version V1.0
 */
public class ToString{
	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
	}
}

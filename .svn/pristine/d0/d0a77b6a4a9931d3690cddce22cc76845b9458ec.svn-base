package com.yogapay.core.server.jpos;

import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;
import org.jpos.q2.iso.QMUX;

/**
 * 扩展QMUX，为了防止交易并发情况下交易混乱，使用报文Key作为唯一约束，但是签到报文构不成默认组成Key的域，故扩张此类。
 * 
 * @author dj
 * 
 */
public class EptokQMux extends QMUX {

	@Override
	public String getKey(ISOMsg m) throws ISOException {
		String mti = m.getMTI();
		if ("0900".equals(mti) || "0200".equals(mti)) {
			StringBuilder sb = new StringBuilder(out);
			sb.append('.');
			sb.append(mapMTI(m.getMTI()));
			sb.append(m.getString(31).trim());
			return sb.toString();
		} else {
			return super.getKey(m);
		}
	}

	private String mapMTI(String mti) {
		StringBuilder sb = new StringBuilder();
		if (mti != null && mti.length() == 4) {
			for (int i = 0; i < mtiMapping.length; i++) {
				int c = mti.charAt(i) - '0';
				if (c >= 0 && c < 10)
					sb.append(mtiMapping[i].charAt(c));
			}
		}
		return sb.toString();
	}

}

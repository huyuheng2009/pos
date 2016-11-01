package com.yogapay.core.server.op;

import org.jpos.iso.ISOMsg;

import com.yogapay.core.server.OpBase;

public class DownloadEnd extends OpBase implements IOperator {

	@Override
	public ISOMsg doTrx(ISOMsg request) throws Exception {
		System.out.println("============DownloadEnd=================");
		boolean isValidation = true;
		isValidation = isValidation(request.getString(42),
				request.getString(41));
		ISOMsg response = (ISOMsg) request.clone();
		response.setResponseMTI();
		if (!isValidation) {
			response.set(39, "03");
			return response;
		} 
		response.set(39, "00");
		return response;
	}
}

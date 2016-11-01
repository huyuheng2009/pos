package com.yogapay.core.server.op;

import org.jpos.iso.ISOMsg;

import com.yogapay.core.domain.ResponseMsg;
import com.yogapay.core.server.OpBase;
import com.yogapay.core.service.ReversalService;

//撤销
public class Reversal extends OpBase implements IOperator {
	private ReversalService reversalService;

	@Override
	public ISOMsg doTrx(ISOMsg request) throws Exception {
		
		boolean isValidation = true;
		isValidation = isValidation(request.getString(42),
				request.getString(41));
		if (!isValidation) {
			ISOMsg response = (ISOMsg) request.clone();
			response.set(39, "03");
			response.setResponseMTI();
			return response;
		} 
		
		reversalService = applicationContext.getBean(ReversalService.class);
		ResponseMsg rm = reversalService.doTrx(request);
		request = buildResult(request, rm);
		return request;
	}

}

package com.yogapay.core.server.op;

import org.jpos.iso.ISOMsg;

import com.yogapay.core.domain.ResponseMsg;
import com.yogapay.core.server.OpBase;
import com.yogapay.core.service.SaleService;

//消费
public class Sale extends OpBase implements IOperator {
	private SaleService saleService;

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
		saleService = applicationContext.getBean(SaleService.class);
		ResponseMsg rm = saleService.doTrx(request);
		if(null!=rm){
			 request = buildResult(request, rm);
		}
		return request;
	}

}

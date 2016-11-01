package com.yogapay.core.server.op;

import org.jpos.iso.ISOMsg;

import com.yogapay.core.server.OpBase;

public class Settle  extends OpBase implements IOperator{

	@Override
	public ISOMsg doTrx(ISOMsg request) throws Exception {
		// TODO Auto-generated method stub
		request.setMTI(request.getMTI());
		request.setResponseMTI();
		request.set(39,"00");
		return request;
	}

}

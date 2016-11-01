package com.yogapay.core.server.op;

import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;

import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;
import org.jpos.util.NameRegistrar.NotFoundException;

import com.yogapay.core.domain.ResponseMsg;
import com.yogapay.core.exception.CardBinNotFoundException;
import com.yogapay.core.exception.HsmException;
import com.yogapay.core.exception.RouteNotFindException;
import com.yogapay.core.server.OpBase;
import com.yogapay.core.service.QueryService;

/**
 * 余额查询
 * 
 * @author dj
 * 
 */
public class Query extends OpBase implements IOperator {

	private QueryService queryService;

	@Override
	public ISOMsg doTrx(ISOMsg request) throws ISOException, HsmException,
			NotFoundException, IOException, SQLException,
			RouteNotFindException, CardBinNotFoundException, ParseException {
		
		boolean isValidation = true;
		isValidation = isValidation(request.getString(42),
				request.getString(41));
		if (!isValidation) {
			ISOMsg response = (ISOMsg) request.clone();
			response.set(39, "03");
			response.setResponseMTI();
			return response;
		} 
		
		queryService = applicationContext.getBean(QueryService.class);
		ResponseMsg rm = queryService.doTrx(request);
		request.set(39, rm.getAcqResponseCode());
		request.set(54, rm.getIosMsg().getString(54));
		request.setResponseMTI();
		return request;
	}
}

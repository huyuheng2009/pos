package com.yogapay.core.server.op;

import java.sql.SQLException;

import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;

import com.yogapay.core.domain.ResponseMsg;
import com.yogapay.core.server.OpBase;
import com.yogapay.core.service.RollbackService;

/**
 * 冲正
 * 
 * @author donjek
 * 
 */
public class Rollback extends OpBase implements IOperator {
	private RollbackService rollbackService;

	@Override
	public ISOMsg doTrx(ISOMsg request) throws SQLException, ISOException {
		
		boolean isValidation = true;
		isValidation = isValidation(request.getString(42),
				request.getString(41));
		if (!isValidation) {
			ISOMsg response = (ISOMsg) request.clone();
			response.set(39, "03");
			response.setResponseMTI();
			return response;
		} 

		rollbackService = applicationContext.getBean(RollbackService.class);
		try {
            ResponseMsg rm = rollbackService.doTrx(request);
            request.set(39, rm.getAcqResponseCode());
            request.set(54, rm.getIosMsg().getString(54));
            request.setResponseMTI();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
		}
        return request;
	}
}

package com.yogapay.core.server.op;

import java.util.List;

import javax.annotation.Resource;

import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOUtil;
import org.jpos.tlv.TLVList;

import com.yogapay.core.domain.IcAid;
import com.yogapay.core.domain.IcRid;
import com.yogapay.core.server.OpBase;
import com.yogapay.core.server.tlv.GBKTLVList;
import com.yogapay.core.service.SaleService;
import com.yogapay.core.service.StatusSendService;

public class StatusSend extends OpBase implements IOperator {
	private StatusSendService statusSendService;

	@Override
	public ISOMsg doTrx(ISOMsg request) throws Exception {
		System.out.println("================StatusSend=============");
		boolean isValidation = true;
		isValidation = isValidation(request.getString(42),
				request.getString(41));
		ISOMsg response = (ISOMsg) request.clone();
		response.setResponseMTI();
		if (!isValidation) {
			response.set(39, "03");
			return response;
		} 
		String f603 = request.getString(60).substring(8, 11);
		List<IcRid> ridList = null;
		List<IcAid> aidList= null;
		statusSendService = applicationContext.getBean(StatusSendService.class);
		//公钥下载
		if("372".equals(f603)){
			ridList = statusSendService.getIcRidPartl();
			 
			TLVList tlvlist = new TLVList();
			for(IcRid rid : ridList){
				tlvlist.append(0X9F06, rid.getRid());
				tlvlist.append(0X9F22, rid.getInd());
				tlvlist.append(0XDF05, rid.getExp());
			}
			response.set(62, ISOUtil.hex2byte("31"+ISOUtil.hexString(tlvlist.pack())));
		}else{
			//参数下载
			try {
				aidList = statusSendService.getIcAidPartl();
			} catch (Exception e) {
				e.printStackTrace();
			}
			TLVList tlvlist = new TLVList();
			for(IcAid aid : aidList){
				tlvlist.append(0X9F06, aid.getAid());
			}
			response.set(62, ISOUtil.hex2byte("31"+ISOUtil.hexString(tlvlist.pack())));
		}
		response.set(39, "00");
		return response;
	}

}

package com.yogapay.core.server.op;

import java.util.List;

import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOUtil;
import org.jpos.tlv.TLVList;

import com.yogapay.core.domain.IcAid;
import com.yogapay.core.domain.IcRid;
import com.yogapay.core.server.OpBase;
import com.yogapay.core.server.tlv.GBKTLVList;
import com.yogapay.core.service.StatusSendService;

public class Download extends OpBase implements IOperator {
	private StatusSendService statusSendService;

	@Override
	public ISOMsg doTrx(ISOMsg request) throws Exception {
		System.out.println("============Download=============");
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
		if("370".equals(f603)){
			//公钥下载
			TLVList tlvList = new TLVList();
			tlvList.unpack(request.getBytes(62));
			String tlv_9f06 = tlvList.getString(0x9f06);
			String tlv_9f22 = tlvList.getString(0x9f22);
			ridList = statusSendService.getIcRidDetail(tlv_9f06, tlv_9f22);
			TLVList tlvlist = new TLVList();
			for(IcRid rid : ridList){
				tlvlist.append(0X9F06, rid.getRid());
				tlvlist.append(0X9F22, rid.getInd());
				tlvlist.append(0XDF05, rid.getExp());
				tlvlist.append(0XDF06, rid.getHashAlg());
				tlvlist.append(0XDF07, rid.getRidAlg());
				tlvlist.append(0XDF02, rid.getMod());
				tlvlist.append(0XDF04, rid.getIdx());
				tlvlist.append(0XDF03, rid.getCk());
			}
			response.set(62, ISOUtil.hex2byte("31"+ISOUtil.hexString(tlvlist.pack())));
		}else if("380".equals(f603)){
			//参数下载
			TLVList tlvList = new TLVList();
			tlvList.unpack(request.getBytes(62));
			String tlv_9f06 = tlvList.getString(0x9f06);
			aidList = statusSendService.getIcAidDetail(tlv_9f06);
			TLVList tlvlist = new TLVList();
			for(IcAid aid : aidList){
				tlvlist.append(0X9F06, aid.getAid());
				tlvlist.append(0XDF01, aid.getAsi());
				tlvlist.append(0X9F08, aid.getVer());
				tlvlist.append(0XDF11, aid.getTacDefault());
				tlvlist.append(0XDF12, aid.getTacOnline());
				tlvlist.append(0XDF13, aid.getTacDeninal());
				tlvlist.append(0X9F1B, aid.getFloorLimit());
				tlvlist.append(0XDF15, aid.getThreshold());
				tlvlist.append(0XDF16, aid.getThresholdPercent());
				tlvlist.append(0XDF17, aid.getThresholdVal());
				tlvlist.append(0XDF14, aid.getDdol());
				tlvlist.append(0XDF18, aid.getOnlinePin());
				tlvlist.append(0X9F7B, aid.getTerminalLimit());
				tlvlist.append(0XDF19, aid.getNoTouchLowestLimit());
				tlvlist.append(0XDF20, aid.getNoTouchSaleLimit());
				tlvlist.append(0XDF21, aid.getValidateMethod());
				
			}
			response.set(62, ISOUtil.hex2byte("31"+ISOUtil.hexString(tlvlist.pack())));
		}
		response.set(39, "00");
		return response;
	}

}

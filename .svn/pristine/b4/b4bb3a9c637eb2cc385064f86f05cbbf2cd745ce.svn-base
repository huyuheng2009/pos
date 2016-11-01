package com.yogapay.core.server.op;

import java.util.Date;

import org.apache.commons.lang.StringUtils;
import org.jpos.iso.ISODate;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOUtil;
import org.springframework.stereotype.Service;

import com.yogapay.core.domain.PosTerminal;
import com.yogapay.core.security.JCEHandler;
import com.yogapay.core.security.IHsm.DES_TYPE;
import com.yogapay.core.server.GenSyncNo;
import com.yogapay.core.server.OpBase;
import com.yogapay.core.service.PosService;

@Service
public class CheckIn extends OpBase implements IOperator {

	public ISOMsg doTrx(ISOMsg request) throws ISOException {

		ISOMsg response = new ISOMsg();
		response.setMTI(request.getMTI());
		response.setResponseMTI();

		try {
			boolean isValidation = true;
			isValidation = isValidation(request.getString(42),
					request.getString(41));
			if (!isValidation) {
				response.set(39, "03");
                return response;
			} else {
				PosTerminal pos = getPos(request.getString(42),request.getString(41));
				// 交易流水号
				response.set(11, request.getString(11));
				// 时间
				response.set(12, ISODate.getTime(new Date()));
				// 日期
				response.set(13, ISODate.getDate(new Date()));
				// 参考号12位
				String referNo = "100000000000";
				try {
					referNo = GenSyncNo.getInstance().getNextPosReferNo();
				} catch (Exception e) {
					e.printStackTrace();
				}
				// 应答码
				response.set(39, "00");
				response.set(37, referNo);
				// 终端号
				response.set(41, request.getString(41));
				// 商户号
				response.set(42, request.getString(42));
				// 交易类型// 批次号// 单倍长度密钥001
				response.set(60, request.getString(60));
				String f571 = request.getString("57.1");
				if (StringUtils.isNotEmpty(f571)) {
					response.set("57.1", f571);
				}
				String f572 = request.getString("57.2");
				if (StringUtils.isNotEmpty(f571)) {
					response.set("57.2", f572);
				}
				String f573 = request.getString("57.3");
				if (StringUtils.isNotEmpty(f573)) {
					response.set("57.3", f573);
				}
				String clearTpk = hsm.randomClearKey(DES_TYPE.DES);
				String clearTak = hsm.randomClearKey(DES_TYPE.DES);
				// 用zmk加密明文的zpk
				String desTpk = JCEHandler.encryptData(clearTpk, pos.getTmk());
				String desTak = JCEHandler.encryptData(clearTak, pos.getTmk());
				posService = applicationContext.getBean(PosService.class);
				pos.setVoucherNo(request.getString(11));
				pos.setBatchNo(request.getString(60).substring(2, 8));
				pos.setTmkTak(desTak);
				pos.setTmkTpk(desTpk);
				posService.update(pos);
				// 用明文的tpk作为key，16个0作为data进行des，得到的值为checkvalue,取前8位
				String desTpkCheckValue = JCEHandler.encryptData(
						"0000000000000000", clearTpk).substring(0, 8);
				String desTakCheckValue = JCEHandler.encryptData(
						"0000000000000000", clearTak).substring(0, 8);
				String wk = desTpk + desTpkCheckValue + desTak
						+ desTakCheckValue;
				response.set(62, ISOUtil.hex2byte(wk));
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return response;
	}
}

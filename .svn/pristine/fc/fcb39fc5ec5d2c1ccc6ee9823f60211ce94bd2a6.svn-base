package com.yogapay.core.server;

import java.sql.SQLException;
import java.util.*;

import org.apache.commons.lang.StringUtils;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;
import org.jpos.q2.QBeanSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.yogapay.core.domain.BizType;
import com.yogapay.core.domain.PosMerchant;
import com.yogapay.core.domain.PosTerminal;
import com.yogapay.core.domain.ResponseMsg;
import com.yogapay.core.exception.BizPermissionException;
import com.yogapay.core.security.IHsm;
import com.yogapay.core.security.Vhsm;
import com.yogapay.core.service.PosService;
import com.yogapay.core.utils.Dao;

/**
 * 交易基础类，提供mac计算，pin计算等常用方法，子类继承后直接调用接口，这里不做和具体业务相关的工作
 * 
 * @author dj
 * 
 */
public class OpBase extends QBeanSupport implements ApplicationContextAware {
	protected static ApplicationContext applicationContext;
	protected IHsm hsm = new Vhsm();
	protected PosService posService = null;
	private static final Logger log = LoggerFactory.getLogger(OpBase.class);
	/**
	 * 银行终端号
	 */
	protected String f41 = "";
	/**
	 * 银行商户号
	 */
	protected String f42 = "";

	/**
	 * 过滤掉不需要计算mac的交易
	 */
	private static String[] filterMacMti = { "0800", "0900", "0500" };
	/**
	 * 在过滤掉不需要计算mac的基础上过滤掉不需要计算pin的交易
	 */
	private static String[] filterPinMti = { "0400" };

	static {
		Arrays.sort(filterMacMti);
		Arrays.sort(filterPinMti);
	}

	/**
	 * 对响应的报文进行处理，以便于自动设置响应的mti，如请求0200，响应会自动设置成0210
	 * 
	 * @param isoReq
	 *            请求报文
	 * @return 响应报文
	 * @throws ISOException
	 */
	public ISOMsg makeResponse(ISOMsg isoReq) throws ISOException {
		ISOMsg response = new ISOMsg();
		// 先设置mti，后面会根据这个mti进行转换成响应的mti
		response.setMTI(isoReq.getMTI());
		response.setResponseMTI();
		return response;
	}

	// 将返回的结果进行必要域的设置
	public ISOMsg buildResult(ISOMsg response, ResponseMsg rm)
			throws ISOException {
		response.set(12, rm.getIosMsg().getString(12));
		response.set(13, rm.getIosMsg().getString(13));
		// 检索参考号
		if (StringUtils.isNotEmpty(rm.getIosMsg().getString(37))) {
			response.set(37, rm.getIosMsg().getString(37));
		}
		// 授权码
		if (StringUtils.isNotEmpty(rm.getIosMsg().getString(38))) {
			response.set(38, rm.getIosMsg().getString(38));
		}

		response.set(39, rm.getAcqResponseCode());
		// 附加响应数据，发卡行代码，收单行代码
		if (StringUtils.isNotEmpty(rm.getIosMsg().getString(44))) {
			response.set(44, rm.getIosMsg().getString(44));
		}
		//55域ic卡数据
		if (StringUtils.isNotEmpty(rm.getIosMsg().getString(55))) {
			response.set(55, rm.getIosMsg().getString(55));
		}
		response.set(63, "CUP");
        response.setResponseMTI();
		return response;
	}

	/**
	 * 设置请求报文的mac，签到，签退，批结无需设置mac，具体详见文档是否需要64域
	 * 
	 * @param isoRes
	 *            请求报文
	 * @param oak
	 *            收单机构lmk下加密的oak
	 * @throws Exception
	 */
	public void setMac(ISOMsg isoRes, String tak) throws Exception {
		isoRes.set(64, new byte[8]);
		log.info("packager:{}", isoRes.getPackager().getDescription());
		/**
		 * 加密设备选择，如果替换加密设备，可以直接更换接口的实现即可，具体交易无需变动
		 */
		PosTerminal pos = getPos(isoRes.getString(42), isoRes.getString(41));
		hsm.setMk(pos.getTmk());
		byte[] mac = hsm.genECBMAC(isoRes.pack(), IHsm.KEY_TYPE.TAK, tak);
		isoRes.set(64, mac);
	}

	//
	// public String trasnPin(String posPinBlock, String tmk, String tmkTpk,
	// String srcAccountNo) {
	// String pinBlock = "";
	// try {
	// String clearPin = "000000";
	// if (StringUtils.isNotEmpty(posPinBlock)) {
	// String clearTak = JCEHandler.decryptData(tmkTpk, tmk);
	// String dest = JCEHandler.decryptData(posPinBlock, clearTak);
	// String accountNo = srcAccountNo.substring(
	// srcAccountNo.length() - 13, srcAccountNo.length() - 1);
	//
	// byte[] x = ISOUtil.xor(ISOUtil.hex2byte(dest),
	// ISOUtil.hex2byte("0000" + accountNo));
	// clearPin = ISOUtil.hexString(x);
	// clearPin = clearPin.substring(2, clearPin.indexOf("F"));
	// if (clearPin.length() != 6) {
	// clearPin = "000000";
	// }
	// }
	// pinBlock = new SHJ0902().genPinBlock(clearPin, srcAccountNo,
	// SHJ0902.KEY_TYPE.ZPK, Constants.HSM_SPK);
	// return pinBlock;
	//
	// } catch (Exception e) {
	// e.printStackTrace();
	// }
	// return pinBlock;
	// }

	public void hasPermission(BizType rt, String merchantNo)
			throws SQLException, BizPermissionException {
		posService = applicationContext.getBean(PosService.class);
		PosMerchant m = posService.findByMno(merchantNo);

		String permission = m.getPermission();
		if (StringUtils.isEmpty(permission)) {
			throw new BizPermissionException(merchantNo);
		}
		String flag = "0";
		switch (rt) {
		// 签到
		case CHECK_IN:
			flag = permission.substring(0, 1);
			break;
		case QUERY:
			flag = permission.substring(1, 2);
			break;
		case SALE:
			flag = permission.substring(2, 3);
			break;
		case REVERSAL:
			flag = permission.substring(3, 4);
			break;
		case ROLLBACK:
			flag = permission.substring(4, 5);
			break;
		case REFUND:
			flag = permission.substring(5, 6);
			break;
		case BATCH:
			flag = permission.substring(6, 7);
			break;
		case ECHO:
			flag = permission.substring(8, 9);
			break;
		case INIT:
			flag = permission.substring(9, 10);
			break;
		}
		if (flag.equals("0")) {
			throw new BizPermissionException(merchantNo);
		}
	}

	public PosTerminal getPos(String merchantNo, String terminalNo) {
		PosTerminal pos = null;
		try {
			posService = applicationContext.getBean(PosService.class);
			pos = posService.findByTno(merchantNo, terminalNo);
			if (null == pos || StringUtils.isEmpty(pos.getTmk())) {
				pos = null;
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return pos;
	}

	// 出现异常时更新订单信息
	public void updateOrderError(ISOMsg response) {
		// 账户
		String accountNo = response.getString(35);
		accountNo = accountNo.replace("D", "=");
		accountNo = accountNo.substring(0, accountNo.indexOf("="));
		Dao dao = applicationContext.getBean(Dao.class);
		try {
			String requestMsg = "";
			if (StringUtils.isNotEmpty(response.getString(39))) {
				String sql = "select response_text from response_code where response_code=?";
				List<Map<String, Object>> result = dao.find(sql,
						response.getString(39));
				if (StringUtils.isNotEmpty(result.get(0).get("response_text")
						.toString())) {
					requestMsg = result.get(0).get("response_text").toString();
				}
			}

			List<Object> params = new ArrayList<Object>();
			StringBuffer sb = new StringBuffer();
			// sb.append(" trans_status=?,");
			// params.add(TransStatus.FAIL.toString());
			sb.append(" response_code=?, ");
			params.add(response.getString(39));
			sb.append(" response_msg=? ");
			params.add(requestMsg);

			params.add(response.getString(42));
			params.add(response.getString(41));
			params.add(response.getString(60).substring(2, 8));
			params.add(response.getString(11));
			params.add(accountNo);
			params.add(response.getString(3));
			String mti = "";
			try {
				mti = response.getMTI();
			} catch (ISOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			params.add(mti);
            params.add(new Date());
			String sql = "update trans_info set "
					+ sb.toString()
					+ " where merchant_no=? and terminal_no=? and terminal_batch_no=? and terminal_voucher_no=? and card_no=? and trans_code=? and mti=? and to_days(create_time) = to_days(?)";
			dao.update(sql, params.toArray());

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// 验证pos是否可用，商户是否开通，true正常，false无效商户
	public boolean isValidation(String merchantNo, String terminalNo)
			throws SQLException {
		posService = applicationContext.getBean(PosService.class);
		return posService.isValidation(merchantNo, terminalNo);
	}

	@Override
	public void setApplicationContext(ApplicationContext arg0)
			throws BeansException {
		applicationContext = arg0;
	}

	public String renameCode(String code) {
		if (code.equals("YFB01")) {
			return "25";
		}
		if (code.equals("YFB02")) {
			return "94";
		}
		if (code.equals("YFB03")) {
			return "25";
		}
		if (code.equals("YFB04")) {
			return "01";
		}
		if (code.equals("YFB05")) {
			return "35";
		}
		if (code.equals("YFB06")) {
			return "34";
		}
		if (code.equals("YFB07")) {
			return "03";
		}
		if (code.equals("YFB08")) {
			return "03";
		}
		if (code.equals("YFB09")) {
			return "25";
		}
		if (code.equals("YFB10")) {
			return "25";
		}
		if (code.equals("YFB22")) {
			return "03";
		}
		if (code.equals("YFB33")) {
			return "28";
		}
		if (code.equals("YFB44")) {
			return "21";
		}
		return code;
	}
}
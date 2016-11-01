package com.yogapay.core.server;

import java.net.SocketTimeoutException;

import com.yogapay.core.server.op.*;
import org.apache.commons.lang.StringUtils;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISORequestListener;
import org.jpos.iso.ISOSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.yogapay.core.domain.BizType;
import com.yogapay.core.domain.PosTerminal;
import com.yogapay.core.exception.BizException;
import com.yogapay.core.exception.BizPermissionException;
import com.yogapay.core.exception.CardBinNotFoundException;
import com.yogapay.core.exception.MacException;
import com.yogapay.core.exception.MaxAmountException;
import com.yogapay.core.exception.RouteNotFindException;
import com.yogapay.core.exception.TransInfoNotFindException;
import com.yogapay.core.server.jpos.Utils;

/**
 * 交易分发器，所有的交易先通过这个类来接收，然后根据MTI进行找到对应的具体处理类
 * 
 * @author dj
 * 
 */
public class RequestListener extends OpBase implements ISORequestListener {
	private static Logger log = LoggerFactory.getLogger(RequestListener.class);

	public boolean process(ISOSource isoSource, ISOMsg isoReq) {
		// 最终返回给pos机的
		ISOMsg response = null;
		BizType rt = null;
		IOperator op = null;
		try {
			log.info(Utils.dump(isoReq));
			log.info("recieve data,terminalNo:{},mti:{}", isoReq.getString(41),
					isoReq.getMTI());
			rt = bizType(isoReq);
			// 判断是否有相应的功能权限
			//hasPermission(rt, isoReq.getString(42));

			switch (rt) {
			// 签到
			case CHECK_IN:
				op = new CheckIn();
				break;
			case QUERY:
				op = new Query();
				break;
			case SALE:
				op = new Sale();
				break;
			case REVERSAL:
				op = new Reversal();
				break;
			case REFUND:
				op = new Refund();
				break;
			case ROLLBACK:
				op = new Rollback();
				break;
			case INIT:
				op = new Init();
				break;
			case BATCH:
				op = new Batch();
				break;
			case ECHO:
				op = new Echo();
				break;
			case SETTLE:
				op = new Settle();
				break;
			case STATUS_SEND:
				op = new StatusSend();
				break;
			case DOWNLOAD:
				op = new Download();
				break;
			case DOWNLOAD_END:
				op = new DownloadEnd();
				break;
			default:
				throw new BizPermissionException();
			}
			response = op.doTrx(isoReq);
			log.info(Utils.dump(response));
		} catch (MacException e) {
			e.printStackTrace();
			response = error(isoReq, "A0");
			updateOrderError(response);
		} catch (SocketTimeoutException e) {
			e.printStackTrace();
			response = error(isoReq, "96");
			updateOrderError(response);
        } catch (RouteNotFindException e) {
			e.printStackTrace();
			response = error(isoReq, "92");
			updateOrderError(response);
		} catch (CardBinNotFoundException e) {
			e.printStackTrace();
			response = error(isoReq, "14");
			updateOrderError(response);
		} catch (BizPermissionException e) {
			e.printStackTrace();
			response = error(isoReq, "40");
			updateOrderError(response);
		} catch (BizException e) {
			e.printStackTrace();
			response = error(isoReq, "40");
			updateOrderError(response);
		} catch (TransInfoNotFindException e) {
			e.printStackTrace();
			response = error(isoReq, "25");
			updateOrderError(response);
		} catch (MaxAmountException e) {
			e.printStackTrace();
			response = error(isoReq, "61");
			updateOrderError(response);
		} catch (Exception e) {
			e.printStackTrace();
			response = error(isoReq, "96");
			updateOrderError(response);
		}

		// 开始返回POS机
		send(rt, isoSource, response);
		return true;
	}

	public ISOMsg error(ISOMsg isoReq, String code) {
		ISOMsg response = (ISOMsg) isoReq.clone();
		try {
			response.set(39, code);
			response.setResponseMTI();
		} catch (ISOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return response;
	}

	public void send(BizType rt, ISOSource isoSource, ISOMsg isoRes) {
		// 返回
		try {
			if (rt != BizType.CHECK_IN && rt != BizType.INIT
					&& rt != BizType.ECHO) {
				log.info("set mac");
				String tak = "D33D736B2A2A9E13";
				PosTerminal pos = getPos(isoRes.getString(42),
						isoRes.getString(41));
				if (null != pos) {
					tak = pos.getTmkTak();
				}
				setMac(isoRes, tak);
			}
			isoSource.send(isoRes);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 根据交易类型的Mti转换成枚举类型
	 * 
	 * @param isoReq
	 *            请求报文
	 * @return BizType 交易类型
	 * @throws Exception
	 */
	public BizType bizType(ISOMsg isoReq) throws Exception {
		String mti = isoReq.getMTI();
		String f60 = isoReq.getString(60);
		String f601 = "";
		if (StringUtils.isNotEmpty(f60)) {
			f601 = f60.substring(0, 2);
		}
		String f603 = "";
		if("0800".equals(mti) || "0820".equals(mti)){
			f603 = f60.substring(8, 11);
		}
		log.info("receive request:mti:{},f601:{}", mti, f601);
		log.info("f603="+f603);
		// 签到
		if ("0800".equals(mti) && ("001".equals(f603) || "003".equals(f603) || "004".equals(f603)))
			return BizType.CHECK_IN;
		//IC卡公钥/参数/TMS参数/卡bin黑名单下载
		if("0800".equals(mti) && ("370".equals(f603) || "380".equals(f603) || "364".equals(f603) || "390".equals(f603)))
			return BizType.DOWNLOAD;
		//IC卡公钥/参数/TMS参数/卡bin黑名单下载结束
		if("0800".equals(mti) && ("371".equals(f603) || "381".equals(f603) || "365".equals(f603) || "391".equals(f603)))
			return BizType.DOWNLOAD_END;
		// 消费
		if ("0200".equals(mti) && "22".equals(f601))
			return BizType.SALE;
		// 查询余额
		if ("0200".equals(mti) && "01".equals(f601))
			return BizType.QUERY;
		// 退货
		if ("0220".equals(mti) && "25".equals(f601))
			return BizType.REFUND;
		// 撤销
		if ("0200".equals(mti) && "23".equals(f601))
			return BizType.REVERSAL;
		// 冲正
		if ("0400".equals(mti))
			return BizType.ROLLBACK;
		// 批结
		if ("0500".equals(mti))
			return BizType.SETTLE;
		// 初始化
		if ("0900".equals(mti))
			return BizType.INIT;
		// 批上送
		if ("0320".equals(mti))
			return BizType.BATCH;
		// 回响
		if ("0820".equals(mti) && "301".equals(f603))
			return BizType.ECHO;
		//POS状态上送
		if ("0820".equals(mti) && ("362".equals(f603) || "372".equals(f603) || "382".equals(f603)))
			return BizType.STATUS_SEND;
		
		throw new BizException("biz error");
	}

}
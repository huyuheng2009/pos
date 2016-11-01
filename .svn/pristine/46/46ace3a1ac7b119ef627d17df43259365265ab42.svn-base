package com.yogapay.mobile.controller;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.jpos.iso.ISOUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.alibaba.fastjson.JSON;
import com.yogapay.core.domain.CardBin;
import com.yogapay.core.security.rsa.RSAUtils;
import com.yogapay.core.utils.Sms;
import com.yogapay.mobile.domain.AcqResult;
import com.yogapay.mobile.domain.Alipay;
import com.yogapay.mobile.domain.Credit;
import com.yogapay.mobile.domain.DeviceRel;
import com.yogapay.mobile.domain.House;
import com.yogapay.mobile.domain.LoginLog;
import com.yogapay.mobile.domain.Order;
import com.yogapay.mobile.domain.Receive;
import com.yogapay.mobile.domain.UserCard;
import com.yogapay.mobile.domain.UserInfo;
import com.yogapay.mobile.enums.ProcessStatus;
import com.yogapay.mobile.enums.TransStatus;
import com.yogapay.mobile.enums.TransType;
import com.yogapay.mobile.service.CommonService;
import com.yogapay.mobile.service.MobileFinanceService;
import com.yogapay.mobile.service.MobileManagerService;
import com.yogapay.mobile.utils.Constants;

/**
 * 手机客户端接口,url方式进行传值，以json格式进行返回
 * 
 * @author donjek
 * 
 */
@Controller
@RequestMapping("/v1")
public class MobileController extends BaseController {

	private static final Logger log = LoggerFactory
			.getLogger(MobileFinanceService.class);
	@Resource
	private MobileFinanceService mobileFinanceService;
	@Resource
	private MobileManagerService mobileManagerService;
	@Resource
	private CommonService commonService;
	private Map<Object, Object> result = null;

	// 构造结果返回
	public Map<Object, Object> buildResult(String errorCode, String msg,
			Map<Object, Object> content) {
		Map<Object, Object> head = new HashMap<Object, Object>();
		head.put("status", errorCode);
		head.put("error", msg);

		Map<Object, Object> result = new HashMap<Object, Object>();
		result.put("head", head);
		result.put("content", content);
		return result;
	}

	// 构造结果返回
	public Map<Object, Object> buildAcqResult(Receive receive, AcqResult ar) {
		String bankPath = commonService.getValue("bank_images_path");
		Map<Object, Object> content = new HashMap<Object, Object>();
		content.put("cardNo", ar.getCb().getCardNo());
		content.put("cardName", ar.getCb().getCardName());
		content.put("bankName", ar.getCb().getBankName());
		content.put("cardType", ar.getCb().getCardType());
		if (StringUtils.isNotEmpty(ar.getCb().getImageName())) {
			String name = ar.getCb().getImageName();
			name = name.substring(0, name.lastIndexOf("."));
			content.put("image", bankPath + name + ".png");
		}

		content.put("transFee", ar.getFee());
		content.put("transAmount", ar.getAmount());
		SimpleDateFormat formatD = new SimpleDateFormat(" E ");
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		content.put("settleText", "预计(" + formatD.format(ar.getSettleTime())
				+ ") " + sdf.format(ar.getSettleTime()) + " 到账");
		content.put("responseCode", ar.getResponseCode());
		content.put("responseMsg", ar.getResponseMsg());

		return content;
	}

	// 公共数据获取
	@RequestMapping(value = "global")
	public void base(HttpServletResponse response, Receive receive) {
		Map<Object, Object> map = new HashMap<Object, Object>();
		map.put("version",
				commonService.getValue(receive.getAppName().toUpperCase()
						+ "_VERSION"));
		map.put("versionDesc",
				commonService.getValue(receive.getAppName().toUpperCase()
						+ "_VERSION_DESC"));
		map.put("clientUrl", receive.getAppName().toUpperCase()
				+ "_CLIENT_URL");
		map.put("appKey", commonService.getValue("HIPAY_YEAHKA_KEY"));

		List<Map<Object, String>> list = new ArrayList<Map<Object, String>>();
		Map<Object, String> map2 = new HashMap<Object, String>();

		map2 = new HashMap<Object, String>();
		map2.put("img", "http://121.199.47.51/pos/images/ad.jpg");

		list.add(map2);

		map.put("ads", list);

		Map<Object, Object> result = buildResult(Constants.ErrorCode.SUCCESS,
				"", map);

		String text = JSON.toJSONString(result);
		outJson(text, response);
	}

	// 发送短信
	@RequestMapping(value = "sms")
	public void sms(HttpServletResponse response, @RequestParam String mobile,
			@RequestParam String text) {
		Sms.sendMsg(mobile, text);
		result = buildResult(Constants.ErrorCode.SUCCESS, null, null);
		outJson(JSON.toJSONString(result), response);
	}

	// 用户注册
	@RequestMapping(value = "reg")
	public void reg(HttpServletResponse response,
			@RequestParam String userName, @RequestParam String password,
			Receive receive) {
		log.info("用户注册");
		try {
			UserInfo ui = mobileManagerService.login(userName,
					receive.getAppName());
			// 用户存在
			if (null != ui) {
				result = buildResult(Constants.ErrorCode.FAIL, "注册失败，手机号码已经注册",
						null);
			} else {
				password = decodedData(password);

				String devModel = commonService.getValue(receive.getAppName()
						.toUpperCase() + "_DEFAULT_DEV_MODEL");
				int r = mobileManagerService.reg(userName, password, devModel,
						receive.getAppName(), receive.getSettleName(),
						receive.getSettleNo(), receive.getRealName(),
						receive.getIdCard());
				Map<Object, Object> content = new HashMap<Object, Object>();
				if (r == 1) {
					content.put("status", 1);
					// Sms.sendMsg("18600002270", "新用户注册：" + username);
					result = buildResult(Constants.ErrorCode.SUCCESS, null,
							content);
				} else {
					content.put("status", 0);
					result = buildResult(Constants.ErrorCode.FAIL, "注册失败",
							content);
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
			result = buildResult(Constants.ErrorCode.FAIL,
					Constants.ErrorMsg.SYS_ERROR, null);
		}
		outJson(JSON.toJSONString(result), response);
	}

	// 修改密码
	@RequestMapping(value = "person")
	public void person(HttpServletResponse response, Receive receive,
			@RequestParam Map<String, String> params) {
		log.info("修改密码");
		try {
			String username = params.get("userName");
			String oldpassword = params.get("oldPassword");
			String newpassword = params.get("newPassword");
			UserInfo ui = mobileManagerService.login(username,
					receive.getAppName());
			// 用户存在
			if (!ui.getUserPassword().equals(decodedData(oldpassword))) {
				result = buildResult(Constants.ErrorCode.FAIL, "原密码不正确", null);
			} else {
				ui.setUserPassword(decodedData(newpassword));
				int r = mobileManagerService.updateUser(ui);
				Map<Object, Object> content = new HashMap<Object, Object>();
				if (r == 1) {
					content.put("status", 1);
					result = buildResult(Constants.ErrorCode.SUCCESS, null,
							content);
				} else {
					content.put("status", 0);
					result = buildResult(Constants.ErrorCode.FAIL, "密码修改",
							content);
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
			result = buildResult(Constants.ErrorCode.FAIL,
					Constants.ErrorMsg.SYS_ERROR, null);
		}
		outJson(JSON.toJSONString(result), response);
	}

	// 忘记密码
	@RequestMapping(value = "forget")
	public void forget(HttpServletResponse response, Receive receive,
			@RequestParam Map<String, String> params) {
		log.info("忘记密码");
		try {
			String username = params.get("userName");
			String password = params.get("password");
			String code = params.get("code");
			UserInfo ui = mobileManagerService.login(username,
					receive.getAppName());
			if (null == ui) {
				result = buildResult(Constants.ErrorCode.FAIL, "手机号码不存在", null);
			} else {
				Random rm = new Random();
				if (StringUtils.isNotEmpty(code)) {
					ui.setCode(code);
					List<UserInfo> ul = mobileManagerService.findUser(ui);
					if (ul.size() == 0) {
						result = buildResult(Constants.ErrorCode.FAIL,
								"验证码错误，请输入正确验证码", null);
					} else {
						password = decodedData(password);
						ui.setUserPassword(password);
						ui.setCode("");
						mobileManagerService.updateUser(ui);
						result = buildResult(Constants.ErrorCode.SUCCESS, null,
								null);
					}
				} else {
					double pross = (1 + rm.nextDouble()) * Math.pow(10, 6);
					String fixLenthString = String.valueOf(pross);
					String verifyCode = fixLenthString.substring(1, 6 + 1);
					ui.setCode(verifyCode);
					mobileManagerService.updateUser(ui);
					Sms.sendMsg(username, "正在进行" + Constants.APP_NAME
							+ "重置密码，验证码:" + verifyCode);
					result = buildResult(Constants.ErrorCode.SUCCESS, null,
							null);
				}

			}

		} catch (Exception e) {
			e.printStackTrace();
			result = buildResult(Constants.ErrorCode.FAIL,
					Constants.ErrorMsg.SYS_ERROR, null);
		}
		outJson(JSON.toJSONString(result), response);
	}

	// 日志记录
	@RequestMapping(value = "location")
	public void location(HttpServletResponse response, Receive receive,
			LoginLog loginLog) {
		log.info("用户登录日志记录");
		try {
			mobileManagerService.saveLoginLog(loginLog, receive.getAppName());
		} catch (Exception e) {
			e.printStackTrace();
		}
		result = buildResult(Constants.ErrorCode.SUCCESS, null, null);
		outJson(JSON.toJSONString(result), response);
	}

	// 用户登录
	@RequestMapping(value = "login")
	public void login(HttpServletResponse response, Receive receive,
			@RequestParam Map<String, String> params) {
		log.info("用户登录");
		try {

			String username = params.get("userName");
			String appName = params.get("appName");
			String password = params.get("password");
			String uuid = params.get("uuid");

			UserInfo ui = mobileManagerService.login(username,
					receive.getAppName());
			// 用户不存在
			if (null == ui) {
				result = buildResult(Constants.ErrorCode.FAIL, "验证失败，用戶名不存在",
						null);
			} else {
				password = decodedData(password);
				if (!ui.getUserPassword().equals(password)) {
					result = buildResult(Constants.ErrorCode.FAIL,
							"验证失败，用戶名与密码不匹配", null);
				} else {

					if (!appName.equalsIgnoreCase(ui.getAppName())) {
						result = buildResult(Constants.ErrorCode.FAIL,
								"验证失败，用戶名与密码不匹配", null);
					} else {
						if (ui.getStatus() == 1) {
							DeviceRel dr = mobileManagerService.activate(
									username, uuid, receive.getAppName());
							Map<Object, Object> content = new HashMap<Object, Object>();

							if (username.equals(Constants.TEST_USERNAME)) {
								content.put("activate", 1);
							} else {
								if (null == dr) {
									content.put("activate", 0);
								} else {
									content.put("activate", 1);
								}
							}
							Map<String, String> requestParams = new HashMap<String, String>();
							requestParams.put("startDate", "2012-01-10");
							requestParams.put("endDate", "2024-01-10");
							requestParams.put("transStatus",
									TransStatus.已成功.toString());
							requestParams.put("processStatus",
									ProcessStatus.待处理.toString());
							List<Order> orderList = mobileManagerService
									.orderList(username, requestParams, 1,
											appName);
							content.put("process", orderList.size());
							content.put("status", ui.getStatus());
							// content.put("devModel", "itronbox");
							content.put("devModel", ui.getDevModel());
							content.put("rate", ui.getRate());
							content.put("verify", ui.getVerify());
							content.put("verifyMsg", ui.getVerifyMsg());
							content.put("singleMaxAmount",
									ui.getSingleMaxAmount());
							content.put("singleMinAmount",
									ui.getSingleMinAmount());
							result = buildResult(Constants.ErrorCode.SUCCESS,
									null, content);
						} else {
							result = buildResult(Constants.ErrorCode.FAIL,
									"登陆失败，用户已关闭", null);
						}
					}
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
			result = buildResult(Constants.ErrorCode.FAIL,
					Constants.ErrorMsg.SYS_ERROR, null);
		}
		outJson(JSON.toJSONString(result), response);
	}

	// 收藏删除
	@RequestMapping(value = "delFav")
	public void delFav(HttpServletResponse response, Receive receive,
			@RequestParam Map<String, String> params) {
		log.info("收藏删除");
		try {
			Long id = Long.valueOf(params.get("id"));
			String transType = params.get("transType");
			int r = 0;
			if (StringUtils.isNotEmpty(transType)
					&& transType.equals(TransType.余额查询.toString())) {
				r = mobileManagerService.delFav(receive.getUserName(), id,
						TransType.余额查询);
			} else {
				r = mobileManagerService
						.delFav(receive.getUserName(), id, null);
			}

			Map<Object, Object> content = new HashMap<Object, Object>();
			content.put("status", r);
			result = buildResult(Constants.ErrorCode.SUCCESS, "取消收藏成功", content);
		} catch (Exception e) {
			e.printStackTrace();
			result = buildResult(Constants.ErrorCode.FAIL,
					Constants.ErrorMsg.SYS_ERROR, null);
		}
		outJson(JSON.toJSONString(result), response);
	}

	// 用户激活
	@RequestMapping(value = "activate")
	public void activate(HttpServletResponse response, Receive receive,
			@RequestParam Map<String, String> params) {
		log.info("用户激活");
		try {
			String sn = params.get("sn");
			String uuid = params.get("uuid");
			String osName = params.get("os_name");
			String osVersion = params.get("os_version");
			String location = params.get("location");
			int act = mobileManagerService.activate(sn, uuid,
					receive.getUserName(), osName, osVersion, location,
					receive.getAppName());
			Map<Object, Object> content = new HashMap<Object, Object>();
			// 用户不存在
			if (act == 0) {
				content.put("activate", 0);
				result = buildResult(Constants.ErrorCode.FAIL, "激活失败，请正确输入激活码",
						content);
			}
			if (act == 1) {
				UserInfo ui = new UserInfo();
				ui.setUserName(receive.getUserName());
				ui = mobileManagerService.findUser(ui).get(0);
				content.put("activate", 1);
				content.put("verify", ui.getVerify());
				result = buildResult(Constants.ErrorCode.SUCCESS, "激活成功",
						content);
			}
		} catch (Exception e) {
			e.printStackTrace();
			result = buildResult(Constants.ErrorCode.FAIL,
					Constants.ErrorMsg.SYS_ERROR, null);
		}
		outJson(JSON.toJSONString(result), response);
	}

	// cardbin查询
	@RequestMapping(value = "cardBin")
	public void cardBin(HttpServletResponse response, Receive receive) {
		CardBin cb = commonService.cardBin(receive);
		Map<Object, Object> result = null;
		try {
			Map<Object, Object> card = new HashMap<Object, Object>();
			String bankPath = commonService.getValue("bank_images_path");
			card.put("bankName", cb.getBankName());
			card.put("cardName", cb.getCardName());
			card.put("cardType", cb.getCardType());
			if (StringUtils.isNotEmpty(cb.getImageName())) {
				String name = cb.getImageName();
				name = name.substring(0, name.lastIndexOf("."));
				card.put("image", bankPath + name + ".png");
			}

			if (null != cb && StringUtils.isNotEmpty(cb.getBankName())) {
				result = buildResult(Constants.ErrorCode.SUCCESS, null, card);
			} else {
				result = buildResult(Constants.ErrorCode.FAIL, "无法识别的卡", null);
			}

		} catch (Exception e) {
			e.printStackTrace();
			result = buildResult(Constants.ErrorCode.FAIL,
					Constants.ErrorMsg.SYS_ERROR, null);
		}
		outJson(JSON.toJSONString(result), response);
	}

	// 快捷账号
	@RequestMapping(value = "cardList")
	public void cardList(HttpServletResponse response, Receive receive,
			@RequestParam(defaultValue = "1") Integer page) {
		Map<Object, Object> result = null;
		try {
			List<UserCard> ul = mobileManagerService.cardList(
					receive.getUserName(), page, receive.getAppName());
			Long count = mobileManagerService.cardCount(receive.getUserName(),
					receive.getAppName());
			List<Map<Object, Object>> list = new ArrayList<Map<Object, Object>>();
			String bankPath = commonService.getValue("bank_images_path");
			for (UserCard u : ul) {
				Map<Object, Object> card = new HashMap<Object, Object>();
				card.put("cardNo", u.getCardNo());
				card.put("id", u.getId());
				card.put("nick", u.getNick());
				card.put("track", u.getTrack());
				CardBin cb = commonService.cardBin(u.getCardNo());
				card.put("bankName", cb.getBankName());
				card.put("cardName", cb.getCardName());
				card.put("cardType", cb.getCardType());
				if (StringUtils.isNotEmpty(cb.getImageName())) {
					String name = cb.getImageName();
					name = name.substring(0, name.lastIndexOf("."));
					card.put("image", bankPath + name + ".png");
				}

				list.add(card);
			}
			Map<Object, Object> content = new HashMap<Object, Object>();

			content.put("countRecord", count);
			content.put("currentPage", page);
			content.put("pageSize", Constants.PAGE_SIZE);
			long totalPage = (count + Constants.PAGE_SIZE - 1)
					/ Constants.PAGE_SIZE;
			content.put("totalPage", totalPage);
			if (totalPage < page) {
				content.put("hasNextPage", true);
			} else {
				content.put("hasNextPage", false);
			}
			content.put("cards", list);
			result = buildResult(Constants.ErrorCode.SUCCESS, null, content);
		} catch (SQLException e) {
			e.printStackTrace();
			result = buildResult(Constants.ErrorCode.FAIL,
					Constants.ErrorMsg.SYS_ERROR, null);
		}
		outJson(JSON.toJSONString(result), response);
	}

	// 账号历史记录
	@RequestMapping(value = "accountFavorite")
	public void accountFavorite(HttpServletResponse response, Receive receive,
			@RequestParam Map<String, String> params,
			@RequestParam(defaultValue = "1") Integer page) {
		Map<Object, Object> result = null;
		try {
			List<UserCard> ul = mobileManagerService.accountFavorite(
					receive.getUserName(), receive.getAppName(),
					params.get("transType").toString(), page);
			Long count = mobileManagerService.accountFavoriteCount(
					receive.getUserName(), params.get("transType").toString(),
					receive.getAppName());
			List<Map<Object, Object>> list = new ArrayList<Map<Object, Object>>();
			String bankPath = commonService.getValue("bank_images_path");
			for (UserCard u : ul) {
				Map<Object, Object> card = new HashMap<Object, Object>();
				card.put("cardNo", u.getCardNo());
				card.put("id", u.getId());
				card.put("owner", u.getOwner());

				if (!params.get("transType").toString()
						.equals(TransType.支付宝充值.toString())) {
					CardBin cb = commonService.cardBin(u.getCardNo());
					card.put("bankName", cb.getBankName());
					card.put("cardName", cb.getCardName());
					card.put("cardType", cb.getCardType());
					if (StringUtils.isNotEmpty(cb.getImageName())) {
						String name = cb.getImageName();
						name = name.substring(0, name.lastIndexOf("."));
						card.put("image", bankPath + name + ".png");
					}

				}

				list.add(card);
			}
			Map<Object, Object> content = new HashMap<Object, Object>();

			content.put("countRecord", count);
			content.put("currentPage", page);
			content.put("pageSize", Constants.PAGE_SIZE);
			long totalPage = (count + Constants.PAGE_SIZE - 1)
					/ Constants.PAGE_SIZE;
			content.put("totalPage", totalPage);
			if (totalPage > page) {
				content.put("hasNextPage", true);
			} else {
				content.put("hasNextPage", false);
			}
			content.put("cards", list);
			result = buildResult(Constants.ErrorCode.SUCCESS, null, content);
		} catch (SQLException e) {
			e.printStackTrace();
			result = buildResult(Constants.ErrorCode.FAIL,
					Constants.ErrorMsg.SYS_ERROR, null);
		}
		outJson(JSON.toJSONString(result), response);
	}

	// 余额查询保存账号
	@RequestMapping(value = "queryFavorite")
	public void queryFavorite(HttpServletResponse response, Receive receive,
			@RequestParam Map<String, String> params) {
		Map<Object, Object> result = null;
		CardBin cb = null;
		try {
			if (StringUtils.isEmpty(receive.getCardNo())) {
				cb = commonService.cardBin(receive);
			} else {
				cb = commonService.cardBin(receive.getCardNo());
			}

			int flag = mobileManagerService.saveQueryFavorite(
					receive.getUserName(), params.get("nick"), cb,
					receive.getAppName());
			if (flag > 0) {
				result = buildResult(Constants.ErrorCode.SUCCESS, null, null);
			} else {
				result = buildResult(Constants.ErrorCode.FAIL, "别名保存失败", null);
			}

		} catch (Exception e) {
			e.printStackTrace();
			result = buildResult(Constants.ErrorCode.FAIL,
					Constants.ErrorMsg.SYS_ERROR, null);
		}
		outJson(JSON.toJSONString(result), response);
	}

	// 查询余额
	@RequestMapping(value = "query")
	public void query(HttpServletResponse response, Receive receive) {
		log.info("余额查询");
		try {
			AcqResult ar = mobileFinanceService.query(receive);
			Map<Object, Object> content = null;
			if (ar.getResponseCode().equals("00")) {
				content = buildAcqResult(receive, ar);
				content.put("type", TransType.余额查询.toString());
				result = buildResult(Constants.ErrorCode.SUCCESS, null, content);
			} else {
				result = buildResult(Constants.ErrorCode.FAIL,
						"交易失败 " + ar.getResponseMsg(), null);
			}

		} catch (Exception e) {
			e.printStackTrace();
			result = buildResult(Constants.ErrorCode.FAIL,
					Constants.ErrorMsg.SYS_ERROR, null);
		}
		outJson(JSON.toJSONString(result), response);
	}

	// 交房租
	@RequestMapping(value = "house")
	public void house(HttpServletResponse response,
			@RequestParam Map<String, String> params, Receive receive,
			House house) {
		log.info("交房租");
		try {

			CardBin cb = mobileFinanceService.cardBin(house.getCardNo());
			if (StringUtils.isEmpty(cb.getBankName())) {
				result = buildResult(Constants.ErrorCode.FAIL, "收款账号有误", null);
			} else {
				AcqResult ar = mobileFinanceService.house(receive, house);
				Map<Object, Object> content = buildAcqResult(receive, ar);
				content.put("houseAmount", house.getAmount());
				content.put("owner", house.getOwner());
				content.put("accountNo", house.getCardNo());
				content.put("transAmount", ar.getAmount());
				content.put("type", TransType.交房租.toString());

				if (ar.getResponseCode().equals("00")) {
					result = buildResult(Constants.ErrorCode.SUCCESS, null,
							content);
					if (house.getSave()) {
						mobileManagerService.saveFavorite(
								receive.getUserName(), house.getOwner(),
								house.getCardNo(), TransType.交房租,
								receive.getAppName());
					}
				} else {
					result = buildResult(Constants.ErrorCode.FAIL,
							ar.getResponseMsg(), null);
				}

			}
		} catch (Exception e) {
			e.printStackTrace();
			result = buildResult(Constants.ErrorCode.FAIL,
					Constants.ErrorMsg.FINANCE_ERROR, null);
		}
		outJson(JSON.toJSONString(result), response);

	}

	// 收款
	@RequestMapping(value = "payment")
	public void payment(HttpServletResponse response,
			@RequestParam Map<String, String> params, Receive receive,
			BigDecimal amount) {
		log.info("收款");
		try {
			AcqResult ar = mobileFinanceService.payment(receive, amount);
			Map<Object, Object> content = buildAcqResult(receive, ar);
			content.put("amount", amount);
			content.put("type", TransType.收款.toString());
			if (null != ar.getIsoMsg()) {
				content.put("merchantNo", ar.getIsoMsg().getString(42));
				String time = ar.getIsoMsg().getString(12);
				String date = ar.getIsoMsg().getString(13);
				content.put("transDate", date);
				content.put("transTime", time);
				content.put("batchNo", ar.getIsoMsg().getString(60));
				content.put("referenceNo", ar.getIsoMsg().getString(37));
				content.put("authNo", ar.getIsoMsg().getString(38));
			}

			if (ar.getResponseCode().equals("00")) {
				result = buildResult(Constants.ErrorCode.SUCCESS, null, content);
			}
		} catch (Exception e) {
			e.printStackTrace();
			result = buildResult(Constants.ErrorCode.FAIL,
					Constants.ErrorMsg.FINANCE_ERROR, null);
		}
		outJson(JSON.toJSONString(result), response);

	}

	// 支付宝充值
	@RequestMapping(value = "alipay")
	public void alipay(HttpServletResponse response, Receive receive,
			Alipay alipay) {
		log.info("支付宝充值");
		try {
			AcqResult ar = mobileFinanceService.alipay(receive, alipay);
			Map<Object, Object> content = buildAcqResult(receive, ar);
			content.put("accountName", alipay.getAccountName());
			content.put("accountNo", alipay.getAccountNo());
			content.put("alipayAmount", alipay.getAmount());
			content.put("type", TransType.支付宝充值.toString());
			if (ar.getResponseCode().equals("00")) {

				if (!receive.getUserName().equals(Constants.TEST_USERNAME)) {
					Sms.sendMsg(
							Constants.TRANS_AGENT_MOBILE,
							"支付宝充值,用户名：" + receive.getUserName() + "，收款账号："
									+ alipay.getAccountNo() + "，账户名："
									+ alipay.getAccountName() + "，金额："
									+ alipay.getAmount());
				}

				result = buildResult(Constants.ErrorCode.SUCCESS, null, content);
				if (alipay.getSave()) {
					mobileManagerService.saveFavorite(receive.getUserName(),
							alipay.getAccountName(), alipay.getAccountNo(),
							TransType.支付宝充值, receive.getAppName());
				}
			} else {
				result = buildResult(Constants.ErrorCode.FAIL,
						ar.getResponseMsg(), null);
			}

		} catch (Exception e) {
			result = buildResult(Constants.ErrorCode.FAIL,
					Constants.ErrorMsg.FINANCE_ERROR, null);
			e.printStackTrace();
		}
		outJson(JSON.toJSONString(result), response);

	}

	// 信用卡还款
	@RequestMapping(value = "credit")
	public void credit(HttpServletResponse response, Receive receive,
			Credit credit) {
		log.info("信用卡还款");
		try {

			CardBin cb = mobileFinanceService.cardBin(credit.getCardNo());
			if (StringUtils.isEmpty(cb.getBankName())) {
				result = buildResult(Constants.ErrorCode.FAIL, "收款账号有误", null);
			} else {
				AcqResult ar = mobileFinanceService.credit(receive, credit);
				Map<Object, Object> content = buildAcqResult(receive, ar);
				content.put("creditAmount", credit.getAmount());
				content.put("accountNo", credit.getCardNo());
				content.put("owner", credit.getOwner());
				content.put("fee", credit.getFee());
				content.put("type", TransType.信用卡还款.toString());
				if (ar.getResponseCode().equals("00")) {
					result = buildResult(Constants.ErrorCode.SUCCESS, null,
							content);
					if (credit.getSave()) {
						mobileManagerService.saveFavorite(
								receive.getUserName(), credit.getOwner(),
								credit.getCardNo(), TransType.信用卡还款,
								receive.getAppName());
					}
				} else {
					result = buildResult(Constants.ErrorCode.FAIL,
							ar.getResponseMsg(), null);
				}
			}
		} catch (Exception e) {
			result = buildResult(Constants.ErrorCode.FAIL,
					Constants.ErrorMsg.FINANCE_ERROR, null);
			e.printStackTrace();
		}
		outJson(JSON.toJSONString(result), response);

	}

	// 订单查询
	@RequestMapping(value = "orderList")
	public void orderList(HttpServletResponse response,
			@RequestParam Map<String, String> params, Receive receive,
			@RequestParam(defaultValue = "1") Integer page) {
		try {
			List<Order> ul = mobileManagerService.orderList(
					receive.getUserName(), params, page, receive.getAppName());
			Long count = mobileManagerService.orderCount(receive.getUserName(),
					params);
			List<Map<Object, Object>> list = new ArrayList<Map<Object, Object>>();
			for (Order u : ul) {
				Map<Object, Object> card = new HashMap<Object, Object>();
				card.put("id", u.getId());
				card.put("cardNo", u.getCardNo());
				card.put("transAmount", u.getTransAmount());
				card.put("transStatus", u.getTransStatus());
				card.put("transType", u.getTransType());
				card.put("merchantFee", u.getMerchantFee());
				SimpleDateFormat sdf = new SimpleDateFormat(
						"yyyy-MM-dd HH:mm:ss");
				card.put("createTime", sdf.format(u.getCreateTime()));
				card.put("username", u.getUserName());
				list.add(card);
			}
			Map<Object, Object> content = new HashMap<Object, Object>();

			content.put("countRecord", count);
			content.put("currentPage", page);
			content.put("pageSize", Constants.PAGE_SIZE);
			long totalPage = (count + Constants.PAGE_SIZE - 1)
					/ Constants.PAGE_SIZE;
			content.put("totalPage", totalPage);
			if (totalPage > page) {
				content.put("hasNextPage", true);
			} else {
				content.put("hasNextPage", false);
			}
			content.put("orders", list);
			result = buildResult(Constants.ErrorCode.SUCCESS, null, content);
		} catch (Exception e) {
			e.printStackTrace();
			result = buildResult(Constants.ErrorCode.FAIL,
					Constants.ErrorMsg.SYS_ERROR, null);
		}
		outJson(JSON.toJSONString(result), response);

	}

	// 订单详情
	@RequestMapping(value = "orderDetail")
	public void orderDetail(HttpServletResponse response,
			HttpServletRequest request, Receive receive, @RequestParam Long id) {
		try {
			Order order = mobileManagerService.order(receive.getUserName(), id,
					receive.getAppName());

			Map<Object, Object> content = new HashMap<Object, Object>();

			String transType = order.getTransType();
			content.put("cardNo", order.getCardNo());
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			content.put("createTime", sdf.format(order.getCreateTime()));
			content.put("transAmount", order.getTransAmount());
			content.put("transStatus", order.getTransStatus());
			content.put("acqResponseMsg", order.getAcqResponseMsg());
			content.put("acqResponseCode", order.getAcqResponseCode());
			content.put("transType", transType);
			content.put("username", order.getUserName());

			CardBin cb = commonService.cardBin(order.getCardNo());
			content.put("cardName", cb.getCardName());
			content.put("cardType", cb.getCardType());
			content.put("bankName", cb.getBankName());
			String signPath = commonService.getValue("sign_path");
			// 判断签名是否存在
			String uploadDir = request.getSession().getServletContext()
					.getRealPath("/upload/sign");
			String filename = order.getSyncNo() + ".png";
			File file = new File(uploadDir + "/" + filename);
			if (file.exists()) {
				content.put("image", signPath + filename);
			} else {
				content.put("image", signPath + "no.png");
			}

			content.put("bizAmount", order.getSettleAmount());
			content.put("bizAccount", order.getSettleAccount());
			content.put("bizOwner", order.getSettleName());
			content.put("bizFee", order.getMerchantFee());

			result = buildResult(Constants.ErrorCode.SUCCESS, null, content);
		} catch (Exception e) {
			e.printStackTrace();
			result = buildResult(Constants.ErrorCode.FAIL,
					Constants.ErrorMsg.SYS_ERROR, null);
		}
		outJson(JSON.toJSONString(result), response);

	}

	// 电子签名上传
	@RequestMapping(value = "signUpload", method = RequestMethod.POST)
	public void upload(HttpServletRequest request, Receive receive,
			HttpServletResponse response) throws Exception {
		Map<Object, Object> content = new HashMap<Object, Object>();
		log.info("sign upload request SignUpload");
		String module = request.getParameter("module");
		String uploadPath = "/upload/sign";
		log.info("module:{}", module);
		if (StringUtils.isNotEmpty(module) && module.equals("verify")) {
			uploadPath = "/upload/verify";
			String username = request.getParameter("userName");
			mobileManagerService.verify(username, receive.getAppName());
		}
		try {
			MultipartHttpServletRequest mRequest = (MultipartHttpServletRequest) request;
			Map<String, MultipartFile> fileMap = mRequest.getFileMap();

			String uploadDir = request.getSession().getServletContext()
					.getRealPath(uploadPath);
			File file = new File(uploadDir);

			if (!file.exists()) {
				file.mkdir();
			}

			String fileName = null;
			int i = 0;
			for (Iterator<Map.Entry<String, MultipartFile>> it = fileMap
					.entrySet().iterator(); it.hasNext(); i++) {

				Map.Entry<String, MultipartFile> entry = it.next();
				MultipartFile mFile = entry.getValue();

				fileName = mFile.getOriginalFilename();

				String storeName = fileName;

				InputStream stream = mFile.getInputStream();

				FileOutputStream fs = new FileOutputStream(uploadDir + "/"
						+ storeName);
				byte[] buffer = new byte[1024 * 1024];
				int bytesum = 0;
				int byteread = 0;
				while ((byteread = stream.read(buffer)) != -1) {
					bytesum += byteread;
					fs.write(buffer, 0, byteread);
					fs.flush();
				}
				fs.close();
				stream.close();

				log.info("upload file " + storeName + " success");
			}
			content.put("flag", "1");
			result = buildResult(Constants.ErrorCode.SUCCESS, null, content);

		} catch (Exception e) {
			e.printStackTrace();
			log.info(e.getMessage());
			content.put("flag", "0");
			result = buildResult(Constants.ErrorCode.FAIL, null, content);
		}

		outJson(JSON.toJSONString(result), response);
	}

	// 解密数据
	public String decodedData(String ecodedData) throws Exception {
		byte[] decodedData = RSAUtils.decryptByPrivateKey(
				ISOUtil.hex2byte(ecodedData), Constants.RSA_PRI_KEY);
		return new String(decodedData);
	}

	public static void main(String[] args) {
		String s = "a.jpg";
		System.out.println(s.substring(0, s.lastIndexOf(".")));
	}
}

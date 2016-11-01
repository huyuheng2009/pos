package com.yogapay.mobile.service;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import com.yogapay.core.domain.CardBin;
import com.yogapay.core.utils.Dao;
import com.yogapay.mobile.domain.Alipay;
import com.yogapay.mobile.domain.Credit;
import com.yogapay.mobile.domain.DeviceRel;
import com.yogapay.mobile.domain.House;
import com.yogapay.mobile.domain.LoginLog;
import com.yogapay.mobile.domain.Order;
import com.yogapay.mobile.domain.UserCard;
import com.yogapay.mobile.domain.UserInfo;
import com.yogapay.mobile.enums.TransType;
import com.yogapay.mobile.utils.Constants;

/**
 * 客户端非金融类管理
 * 
 * @author donjek
 * 
 */
@Service
public class MobileManagerService {

	@Resource
	private Dao dao;

	// 用户登陆验证获取信息
	public UserInfo login(String username, String appName) throws SQLException {
		String sql = "select * from user_info where user_name = ? and app_name=?";
		List<UserInfo> result = dao.find(UserInfo.class, sql, new Object[] {
				username, appName });
		if (null != result && result.size() == 1) {
			return result.get(0);
		}
		return null;
	}

	// 用户注册
	public int reg(String username, String password, String devModel,
			String appName, String settleName, String settleNo,String realName,String idCard)
			throws SQLException {
		String sql = "insert user_info(app_name,user_name,user_password,create_time,single_max_amount,single_min_amount,rate,verify,dev_model,settle_name,settle_no,real_name,id_card) values(?,?,?,?,?,?,?,?,?,?,?,?,?)";

		return dao.update(sql, new Object[] { appName, username, password,
				new Date(), 20000, 2, "1-50", 0, devModel,settleName,settleNo,realName,idCard });
	}

	// 更改状态为以提交
	public int verify(String username, String appName) throws SQLException {
		String sql = "update user_info set verify = 1 where user_name=? and app_name=?";
		return dao.update(sql, new Object[] { username, appName });
	}

	// 更新用户
	public int updateUser(UserInfo ui) throws SQLException {
		String sql = "update user_info set user_password=?,code = ? where id=?";
		return dao
				.update(sql, new Object[] { ui.getUserPassword(), ui.getCode(),
						ui.getId() });
	}

	// 用户查询
	public List<UserInfo> findUser(UserInfo ui) throws SQLException {
		String sql = "select * from user_info where 1=1  ";
		List<Object> params = new ArrayList<Object>();
		if (StringUtils.isNotEmpty(ui.getCode())) {
			sql += "and code =?";
			params.add(ui.getCode());
		}
		if (StringUtils.isNotEmpty(ui.getUserName())) {
			sql += " and user_name =?";
			params.add(ui.getUserName());
		}
		return dao.find(UserInfo.class, sql, params.toArray());
	}

	// 查询激活信息
	public DeviceRel activate(String username, String uuid, String appName)
			throws SQLException {
		String sql = "select * from device_rel where user_name = ? and mobile_uuid=? and app_name=?";
		List<DeviceRel> result = dao.find(DeviceRel.class, sql, new Object[] {
				username, uuid, appName });
		if (null != result && result.size() > 0) {
			return result.get(0);
		}
		return null;
	}

	// 用户激活,0未购买,1激活成功
	public int activate(String sn, String uuid, String username, String osName,
			String osVersion, String location, String appName)
			throws SQLException {
		String sql = "select * from device_rel where    device_sn=? and app_name=? and (mobile_uuid is null or mobile_uuid ='')";
		List<DeviceRel> result = dao.find(DeviceRel.class, sql, new Object[] {
				sn, appName });
		if (null != result && result.size() < 1) {
			return 0;
		} else {
			sql = "update device_rel set mobile_uuid=?,activate_time=?,user_name=?,os_name=?,os_version=?,location=? where device_sn=? and app_name=?";
			return dao.update(sql, new Object[] { uuid, new Date(), username,
					osName, osVersion, location, sn, appName });
		}
	}

	// 删除收藏
	public int delFav(String username, Long id, TransType tt)
			throws SQLException {
		String sql = "";
		if (null == tt) {
			sql = "delete from account_favorite where user_name=? and id=?";
		} else if (tt.equals(TransType.余额查询)) {
			sql = "delete from user_card where user_name=? and id=?";
		}

		return dao.update(sql, new Object[] { username, id });
	}

	// 根据用户名获取用户保存的快捷账号
	public List<UserCard> cardList(String username, Integer page, String appName)
			throws SQLException {
		int start = (page - 1) * Constants.PAGE_SIZE;
		int end = (page - 1) * Constants.PAGE_SIZE + Constants.PAGE_SIZE;
		String sql = "select * from user_card where user_name=? and app_name=?  order by id desc limit ?,?";
		Object[] params = { username, appName, start, end };
		List<UserCard> ul = dao.find(UserCard.class, sql, params);
		return ul;
	}

	// 根据用户名获取用户保存的账号
	public List<UserCard> accountFavorite(String username, String appName,
			String transType, Integer page) throws SQLException {
		int start = (page - 1) * Constants.PAGE_SIZE;
		int end = (page - 1) * Constants.PAGE_SIZE + Constants.PAGE_SIZE;
		String sql = "select * from account_favorite where user_name=? and app_name=? and trans_type=?   order by id desc limit ?,?";
		Object[] params = { username, appName, transType, start, end };
		List<UserCard> ul = dao.find(UserCard.class, sql, params);
		return ul;
	}

	// 保存账号信息
	public void saveFavorite(String username, String owner, String cardNo,
			TransType transType, String appName) {
		String sql = "select * from account_favorite where user_name=?  and card_no=? and trans_type=? and app_name=?";
		try {
			List<Map<String, Object>> have = dao.find(sql, new Object[] {
					username, cardNo, transType.name(), appName });
			if (have.size() == 0) {
				sql = "insert into account_favorite(user_name,owner,card_no,trans_type,create_time,app_name) values(?,?,?,?,?,?)";
				dao.update(sql, new Object[] { username, owner, cardNo,
						transType.name(), new Date(), appName });
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	// 保存余额查询快捷信息
	public int saveQueryFavorite(String username, String nick, CardBin cb,
			String appName) throws SQLException {
		String sql = "select * from user_card where card_no=? and user_name=? and app_name=?";
		String sqlinsert = "insert into user_card(user_name,card_no,track,nick,create_time,app_name) values(?,?,?,?,?,?)";
		String sqlupdate = "update   user_card set nick=? where user_name=? and card_no=? and app_name=?";

		List<Map<String, Object>> have = dao.find(sql,
				new Object[] { cb.getCardNo(), username, appName });
		if (have.size() == 0) {
			return dao.update(sqlinsert,
					new Object[] { username, cb.getCardNo(), cb.getTrackMsg(),
							nick, new Date(), appName });
		} else {
			return dao.update(sqlupdate,
					new Object[] { nick, username, cb.getCardNo(), appName });
		}

	}

	// 订单查询
	public List<Order> orderList(String username,
			Map<String, String> requestParams, Integer page, String appName)
			throws SQLException {
		int start = (page - 1) * Constants.PAGE_SIZE;
		int end = (page - 1) * Constants.PAGE_SIZE + Constants.PAGE_SIZE;
		String startDate = requestParams.get("startDate") + " 00:00:00";
		String endDate = requestParams.get("endDate") + " 23:59:59";
		String transStatus = requestParams.get("transStatus");
		String transType = requestParams.get("transType");
		String processStatus = requestParams.get("processStatus");
		String appsql = "";
		if (StringUtils.isNotEmpty(transStatus)) {
			appsql += " and trans_status='" + transStatus + "'";
		}
		if (StringUtils.isNotEmpty(transType)) {
			appsql += " and trans_type='" + transType + "'";
		}
		if (StringUtils.isNotEmpty(processStatus)) {
			appsql += " and processing='" + processStatus + "'";
		}
		String sql = "select * from trans_info_hipay where user_name=? and trans_type <>'余额查询' "
				+ appsql
				+ " and trans_status <>'初始化' and create_time between ? and ? and app_name=? order by id desc limit ?,?";
		Object[] params = { username, startDate, endDate, appName, start, end };
		List<Order> ul = dao.find(Order.class, sql, params);
		return ul;
	}

	// 订单查询
	public Order order(String username, Long id, String appName)
			throws SQLException {
		String sql = "select * from trans_info_hipay where user_name=? and id=? and app_name=?";
		Object[] params = { username, id, appName };
		Order ul = dao.findFirst(Order.class, sql, params);
		return ul;
	}

	public Long orderCount(String username, Map<String, String> requestParams) {
		String startDate = requestParams.get("startDate") + " 00:00:00";
		String endDate = requestParams.get("endDate") + " 23:59:59";
		String transStatus = requestParams.get("transStatus");
		String transType = requestParams.get("transType");
		String appsql = "";
		if (StringUtils.isNotEmpty(transStatus)) {
			appsql += " and trans_status='" + transStatus + "'";
		}
		if (StringUtils.isNotEmpty(transType)) {
			appsql += " and trans_type='" + transType + "'";
		}
		String sql = "select count(*) count from trans_info_hipay where user_name=? "
				+ appsql
				+ " and trans_type <>'余额查询' and trans_status <>'初始化'  and create_time between ? and ? ";
		Object[] params = { username, startDate, endDate };
		return dao.count(sql, params);
	}

	public Long cardCount(String username, String appName) {
		String sql = "select count(*) count  from user_card where user_name=? and app_name=?";
		return dao.count(sql, new Object[] { username, appName });
	}

	public Long accountFavoriteCount(String username, String transType,
			String appName) {
		String sql = "select count(*) count from account_favorite where user_name=? and trans_type=? and app_name=?";
		return dao.count(sql, new Object[] { username, transType, appName });
	}

	public void saveLoginLog(LoginLog loginLog, String appName) {
		// TODO Auto-generated method stub
		String sql = "insert into login_log(app_name,user_name,uuid,login_code,os_version,os_name,app_version,lat,lng,province,city,district,radius,address,poi_name,create_time) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		Object[] params = { appName, loginLog.getUsername(),
				loginLog.getUuid(), loginLog.getLoginCode(),
				loginLog.getOsVersion(), loginLog.getOsName(),
				loginLog.getAppVersion(), loginLog.getLat(), loginLog.getLng(),
				loginLog.getProvince(), loginLog.getCity(),
				loginLog.getDistrict(), loginLog.getRadius(),
				loginLog.getAddress(), loginLog.getPoiName(), new Date() };
		try {
			dao.update(sql, params);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}

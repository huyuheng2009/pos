package com.yogapay.core.service;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.yogapay.core.domain.IcAid;
import com.yogapay.core.domain.IcRid;
import com.yogapay.core.utils.Dao;
@Service
public class StatusSendService extends BizBaseService{
	@Resource
	private Dao dao;
	
	public List<IcAid> getIcAidDetail(String aid) throws SQLException{
		String sql ="select * from aid_struct where aid=?";
		List<IcAid> list= dao.find(IcAid.class,sql, aid);
		return list;
	}
	
	public List<IcAid> getIcAidPartl() throws SQLException{
		String sql ="select aid from aid_struct";
		List<IcAid> list= dao.find(IcAid.class, sql);
		System.out.println(list);
		return list;
	}
	
	public List<IcRid> getIcRidDetail(String rid, String ind) throws SQLException{
		String sql ="select * from rid_struct where rid=? and ind=?";
		List<IcRid> list= dao.find(IcRid.class, sql, new Object[]{rid, ind});
		return list;
	}
	
	public List<IcRid> getIcRidPartl() throws SQLException{
		String sql ="select rid,ind,exp from rid_struct";
		List<IcRid> list= dao.find(IcRid.class, sql);
		return list;
	}
}

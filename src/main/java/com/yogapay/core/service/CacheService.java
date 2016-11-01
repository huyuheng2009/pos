package com.yogapay.core.service;

import com.yogapay.core.domain.SysDict;
import com.yogapay.core.utils.Dao;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class CacheService {
	@Resource
	private Dao dao;
	private static List<SysDict> allDict = null;
    //暂时用于发短信
    public static ExecutorService smpools = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

	public List<SysDict> findByName(String dictName) throws SQLException {
		if (null == allDict) {
			loadAllDict();
		}
		List<SysDict> revList = new ArrayList<SysDict>();
		for (SysDict d : allDict) {
			if (d.getDictName().equals(dictName.toUpperCase())) {
				revList.add(d);
			}
		}
		return revList;
	}
	
	public List<SysDict> findByKey(String key) throws SQLException {
		if (null == allDict) {
			loadAllDict();
		}
		List<SysDict> revList = new ArrayList<SysDict>();
		for (SysDict d : allDict) {
			if (d.getDictKey().equals(key.toUpperCase())) {
				revList.add(d);
			}
		}
		return revList;
	}

	public void loadAllDict() throws SQLException {
		String sql = "select * from sys_dict";
		allDict = dao.find(SysDict.class, sql);
	}

	public SysDict findByNameKey(String dictName, String key)
			throws SQLException {
		SysDict reval = null;
		if (null == allDict) {
			loadAllDict();
		}
		for (SysDict d : allDict) {
			if (d.getDictName().equals(dictName.toUpperCase())) {
				if (d.getDictKey().equals(key.toUpperCase())) {
					reval = d;
					break;
				}
			}
		}
		return reval;
	}
}

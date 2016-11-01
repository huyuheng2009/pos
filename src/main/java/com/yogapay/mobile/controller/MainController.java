package com.yogapay.mobile.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 
 * @author dj
 * 
 */
@Controller
public class MainController extends BaseController {
	// 首页
	@RequestMapping(value = "/")
	public String main(Model model) {
		return "index";
	}

	// 产品优势
	@RequestMapping(value = "/advantages")
	public String advantages(Model model) {
		return "advantages";
	}

	// 功能展示
	@RequestMapping(value = "/show")
	public String show(Model model) {
		return "show";
	}

	// 开始使用
	@RequestMapping(value = "/start")
	public String start(Model model) {
		return "start";
	}

	// 客户端下载
	@RequestMapping(value = "/download")
	public String download(Model model) {
		return "download";
	}

	// 在线购买
	@RequestMapping(value = "/buy")
	public String buy(Model model) {
		return "buy";
	}

	// 联系我们
	@RequestMapping(value = "/link")
	public String link(Model model) {
		return "link";
	}
}

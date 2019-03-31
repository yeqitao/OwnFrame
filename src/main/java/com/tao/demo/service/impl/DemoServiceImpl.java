package com.tao.demo.service.impl;

import com.own.annotation.OwnService;
import com.tao.demo.service.DemoService;

@OwnService
public class DemoServiceImpl implements DemoService{

	public String query(String name) {
		System.out.println("query·½·¨");
		return "+++++++++++++"+name;
	}

	public String edit(Integer id) {
		return "----------------------";
	}

	public void delete(Integer id) {
		System.out.println("deleteeeeeeeeee");
	}

}

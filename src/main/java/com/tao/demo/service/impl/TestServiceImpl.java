package com.tao.demo.service.impl;

import com.own.annotation.OwnService;
import com.tao.demo.service.DemoService;

@OwnService
public class TestServiceImpl{

	public String query(String name) {
		System.out.println("Testquery����");
		return "test��"+name;
	}

}

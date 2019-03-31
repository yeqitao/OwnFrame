package com.tao.demo.mvc.action;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.own.annotation.OwnAutowired;
import com.own.annotation.OwnController;
import com.own.annotation.OwnRequestMapping;
import com.own.annotation.OwnRequestParam;
import com.own.view.OwnModelAndView;
import com.tao.demo.entity.User;
import com.tao.demo.service.DemoService;
import com.tao.demo.service.impl.TestServiceImpl;

@OwnController
@OwnRequestMapping("/web")
public class DemoAction {
	
	@OwnAutowired
	private DemoService demoService;
	
	@OwnAutowired
	private TestServiceImpl testServiceImpl;
	
	@OwnRequestMapping("/query.do")
	public void query(HttpServletRequest request,HttpServletResponse response,
			Integer age,@OwnRequestParam("name")String nameeee,Double money){
		String str = demoService.query(nameeee);
		System.out.println(str+"=="+age+"=="+money);
		out(response,str);
	}
	
	@OwnRequestMapping("/edit.do")
	public String edit(HttpServletRequest request,HttpServletResponse response,
			@OwnRequestParam("id")Integer id){
		/*demoService.edit(id);*/
		/*HttpDownLoad downLoad = new HttpDownLoad(request, response);
		
		downLoad.down("D:/my/test/ÕÕÆ¬°¡.jpg");*/
		return "forward:/index.jsp";
		//return "forward:/web/test.do?name=xxxxx&age=22";
	}
	@OwnRequestMapping("/edit2.do")
	public OwnModelAndView edit2(HttpServletRequest request,HttpServletResponse response,
			@OwnRequestParam("id")Integer id){
		OwnModelAndView mv = new OwnModelAndView("/index.jsp");
		mv.addAtt("time", "11:55");
		mv.addAtt("user", new User(1, "abaa"));
		
		//mv.setUrl("/index.jsp");
		
		return mv;
	}
	
	@OwnRequestMapping("/edit3.do")
	public String edit3(HttpServletRequest request,HttpServletResponse response,
			@OwnRequestParam("id")Integer id){
		OwnModelAndView mv = new OwnModelAndView();
		
		return "Hello Word";
	}
	
	@OwnRequestMapping("/edit4.do")
	public JSON edit4(HttpServletRequest request,HttpServletResponse response,
			@OwnRequestParam("id")Integer id){
		OwnModelAndView mv = new OwnModelAndView();
		JSONObject js = new JSONObject();
		js.put("name", "xiaoxiao");
		js.put("age", 21);
		return js;
	}
	
	@OwnRequestMapping("/test.do")
	public void test(HttpServletRequest request,HttpServletResponse response,
			Integer age,@OwnRequestParam("name")String nameeee){
		System.out.println(nameeee+"=="+age);
		String str = testServiceImpl.query(nameeee);
		out(response,str);
	}
	
	/*@TAORequestMapping("/file")
	public void file(HttpServletRequest request,HttpServletResponse response,
			String name,TAOMultipartFile upload){
		System.out.println("-------------->"+name);
	}*/
	
	private void out(HttpServletResponse resp,String str){
		try {
			resp.getWriter().write(str);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}

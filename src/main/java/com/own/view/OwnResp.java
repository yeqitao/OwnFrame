package com.own.view;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
/**
 * ·µ»Ø×Ö·û´®²Ù×÷
 * @author User_Troy
 *
 */
public class OwnResp {
	
	/**
	 * Ìø×ª
	 * @param req
	 * @param resp
	 * @param url
	 */
	public static void onResponse(HttpServletRequest req, HttpServletResponse resp, String url){
		String path = req.getContextPath();  
		String basePath = req.getScheme()+"://"+req.getServerName()+":"+req.getServerPort()+path;
		
		if(url.contains("http")){
			url = url.substring(url.indexOf("http"));
		}else{
			url = url.substring(url.indexOf("/"));
		}
		
		String PathUrl = basePath+url;
		try {
			resp.sendRedirect(PathUrl);
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("Redirect:"+PathUrl);
	}
	
	public static void onRequest(HttpServletRequest req, HttpServletResponse resp, String url){
		String path = req.getContextPath();  
		String basePath = req.getScheme()+"://"+req.getServerName()+":"+req.getServerPort()+path;
		
		url = url.substring(url.indexOf("/"));
		//String PathUrl = basePath+url;
		try {
			req.getRequestDispatcher(url).forward(req,resp);
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("forward:"+url);
	}
}

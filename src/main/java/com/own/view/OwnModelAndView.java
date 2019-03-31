package com.own.view;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.alibaba.fastjson.JSONObject;

/**
 * 如果没有设置url 参数以json格式直接写到页面
 * 
 * @author TAO
 *
 */
public class OwnModelAndView {
	
	private Map<String,Object> map = new LinkedHashMap<String,Object>();
	
	private String url;
	
	public void onModelAndView(HttpServletRequest req, HttpServletResponse resp,Object obj){
		try {
			map = ((OwnModelAndView)obj).getMap();
			url = ((OwnModelAndView)obj).getUrl();
			
			if(url == null || url.trim().equals("")){
				//直接输出
				resp.getWriter().print(JSONObject.toJSON(map));
			}else{
				for (Entry<String,Object>ent : map.entrySet()) {
					req.setAttribute(ent.getKey(), ent.getValue());
				}
				url=url.trim();
				//resp.sendRedirect("http://localhost:9999/index.jsp");
				req.getRequestDispatcher(url).forward(req,resp);
			}
				
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
	}
	
	
	public void addAtt(String key, Object value){
		map.put(key, value);
	}
	
	
	
	
	/**
	 * ===================================分割线============================================
	 * @return
	 */
	
	
	public OwnModelAndView(String url) {
		super();
		this.url = url;
	}
	
	public OwnModelAndView() {
		super();
	}


	public Map<String, Object> getMap() {
		return map;
	}

	public void setMap(Map<String, Object> map) {
		this.map = map;
	}


	public String getUrl() {
		return url;
	}


	public void setUrl(String url) {
		this.url = url;
	}
	
	
	
}

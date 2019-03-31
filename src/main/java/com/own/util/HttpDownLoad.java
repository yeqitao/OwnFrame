package com.own.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 文件下载
 * @author TAO
 *
 */
public class HttpDownLoad {
	
	private HttpServletRequest request;
	private HttpServletResponse response;
	
	
	public HttpDownLoad(HttpServletRequest request, HttpServletResponse response) {
		super();
		this.request = request;
		this.response = response;
	}
	
	/**
	 * 下载
	 * @param path  文件路径
	 */
	public void down(String path){
		FileInputStream fileInputStream = null;
		ServletOutputStream  out = null;
		try {
			//读取要下载的文件  
			File f = new File(path);
			//检查文件目录是否存在
			if(f.exists()){
				//文件读入FileInputStream
				fileInputStream = new FileInputStream(f);
				//编码问题。下载乱码问题
				String filename = URLEncoder.encode(f.getName(),"UTF-8");
				
				byte[] b = new byte[fileInputStream.available()];
				fileInputStream.read(b);
				//设置编码
				response.setCharacterEncoding("UTF-8");
				//设置响应头，告诉浏览器要下载文件
				response.setContentType("application/x-download");
				/*response.setContentType("application/force-download");*/
				response.setHeader("Content-Disposition","attachment; filename="+filename+"");
				response.addHeader("Content-Length", (new Long(fileInputStream.available())).toString());//设置大小  
				
				//获取响应报文输出流
	            out = response.getOutputStream();  
	            //写出
	            out.write(b);
			}
		}catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("HttpDownLoad.down出错");
		}finally{
			try {
				fileInputStream.close();
				out.flush();  
				out.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}




















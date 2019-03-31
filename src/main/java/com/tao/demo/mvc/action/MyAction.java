package com.tao.demo.mvc.action;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import com.own.annotation.OwnAutowired;
import com.own.annotation.OwnController;
import com.own.annotation.OwnRequestMapping;
import com.own.annotation.OwnRequestParam;
import com.tao.demo.service.DemoService;

@OwnController
@MultipartConfig
public class MyAction{
	
	@OwnAutowired
	private DemoService demoService;
	
	
	@OwnRequestMapping("/delete")
	public void delete(HttpServletRequest request,HttpServletResponse response,
			@OwnRequestParam("id")Integer id){
		try {
			System.out.println(">>>"+request.getParts());
		}catch (Exception e) {
			e.printStackTrace();
		}
		demoService.delete(id);
	}
	
	@OwnRequestMapping("/file")
	public void file(HttpServletRequest request,HttpServletResponse response,
			@OwnRequestParam("name")String name,Part upload){
		try {
			System.out.println("0---->"+name);
			//System.out.println("0---->"+Arrays.toString(upload));
			System.out.println("0---->"+upload);
			System.out.println(">"+request.getParameter("name"));
			
			
			Collection<Part> co= request.getParts();
			for (Part part : co) {
				System.out.println(">>>"+part.getContentType());
			}
			
			
			Part part = upload;
			if(part!=null){
				System.out.println("-------------->"+part);
				String contentDisposition = part.getHeader("Content-Disposition");  
				System.out.println("-------------->"+contentDisposition);
				int filenameIndex = contentDisposition.indexOf("filename=");
				System.out.println("-------------->"+filenameIndex);
		        String filename = contentDisposition.substring(filenameIndex+10, contentDisposition.length()-1);  
		        System.out.println("-------------->"+filename);
	        
	       
	        	upload.write("D:/my/"+filename);
			}
				
			
	       
	        
		}catch (Exception e) {
			e.printStackTrace();
		}
	}

}

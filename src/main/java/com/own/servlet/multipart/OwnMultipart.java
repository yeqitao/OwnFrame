package com.own.servlet.multipart;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import com.own.servlet.OwnDispatcherServlet;

public class OwnMultipart {
	
	//上传文件处理
	public static List<Part> doMultipart(HttpServletRequest req, HttpServletResponse resp){
		List<Part> partFiles = null;
		try {
			//中文乱码问题
			req.setCharacterEncoding("UTF-8");
			Collection<Part> parts= req.getParts();
			
			partFiles = new ArrayList<Part>(parts.size());
			for (Part part : parts) {
				String contentType = part.getContentType();
				if(contentType != null && !contentType.equals("application/octet-stream")){
					Part file = (Part) OwnDispatcherServlet.convert(Part.class,part);
					if(file != null){
						partFiles.add(file);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} 
		return partFiles;
	}
}

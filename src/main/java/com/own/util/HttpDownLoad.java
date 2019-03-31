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
 * �ļ�����
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
	 * ����
	 * @param path  �ļ�·��
	 */
	public void down(String path){
		FileInputStream fileInputStream = null;
		ServletOutputStream  out = null;
		try {
			//��ȡҪ���ص��ļ�  
			File f = new File(path);
			//����ļ�Ŀ¼�Ƿ����
			if(f.exists()){
				//�ļ�����FileInputStream
				fileInputStream = new FileInputStream(f);
				//�������⡣������������
				String filename = URLEncoder.encode(f.getName(),"UTF-8");
				
				byte[] b = new byte[fileInputStream.available()];
				fileInputStream.read(b);
				//���ñ���
				response.setCharacterEncoding("UTF-8");
				//������Ӧͷ�����������Ҫ�����ļ�
				response.setContentType("application/x-download");
				/*response.setContentType("application/force-download");*/
				response.setHeader("Content-Disposition","attachment; filename="+filename+"");
				response.addHeader("Content-Length", (new Long(fileInputStream.available())).toString());//���ô�С  
				
				//��ȡ��Ӧ���������
	            out = response.getOutputStream();  
	            //д��
	            out.write(b);
			}
		}catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("HttpDownLoad.down����");
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




















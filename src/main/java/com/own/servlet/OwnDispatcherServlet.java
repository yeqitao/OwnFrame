package com.own.servlet;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import com.own.servlet.handler.OwnHandler;
import com.own.servlet.init.InitMethod;
import com.own.servlet.multipart.OwnMultipart;
import com.own.view.OwnModelAndView;
import com.own.view.OwnResp;

/**
 * �Զ���DispatcherServlet ��Ҫ�̳��� HttpServlet
 * @author TAO
 *
 */
@MultipartConfig
public class OwnDispatcherServlet extends HttpServlet{
	private InitMethod init = new InitMethod();
	
	@Override
	public void init(ServletConfig config) throws ServletException {
		
		//���������ļ�,ͨ����application.xml,����ʹ��application.xml����
		init.doLoadConfig(config.getInitParameter("contextConfigLocation"));
		
		//ɨ������������������,@controller,@Service��
		init.doScanner("");
		
		//�����ʼ��,��װ�ص�ioc������(�Զ�ɨ��)
		init.doInstance();
		
		//��������ע��
		init.doAutowired();
		
		//����handlerMappingӳ���ϵ,��һ��urlӳ��һ��method
		init.initHandlerMapping();
		
		System.out.println("�����˰���" + config);
		
	}
	

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		this.doPost(req, resp);
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		
		try {
			//�ȴ��û�����,ƥ��url,�ҵ����Ӧ�ķ���,�������ִ��
			Object obj = doDispatch(req,resp);
			//���ؽ��
			if(obj != null){
				//ת��
				if(obj.getClass() == String.class && obj.toString().contains("redirect")){
					OwnResp.onResponse(req,resp,obj.toString());
				}else if(obj.getClass() == String.class && obj.toString().contains("forward")){
					//�ض���
					OwnResp.onRequest(req,resp,obj.toString());
				}else if(obj.getClass() == OwnModelAndView.class){  //ModelAndView
					OwnModelAndView omav = new OwnModelAndView();
					omav.onModelAndView(req,resp,obj);
				}else{
					//ֱ�����
					resp.getWriter().print(obj);
				}
			}
			
		} catch (Exception e) {
			e.getStackTrace();
			resp.setStatus(500);
			resp.getWriter().write("500 Exception,Detail:\r\n" + Arrays.toString(e.getStackTrace()));
		}
	}
	
	
	private Object doDispatch(HttpServletRequest req, HttpServletResponse resp) throws Exception{
		System.out.println("doDispatch����");
		try {
			//��ȡ��������url��ȡ���Ӧ��Handler
			OwnHandler handler = init.getHandler(req);
			
			//HandlerΪ�� ֱ�ӷ���404����
			if (handler == null){
				resp.setStatus(404);
				resp.getWriter().write("404 Not Found");
			}
			
			//��ȡ�÷����Ĳ�������
			Class<?>[] paramTypes = handler.method.getParameterTypes();
			//System.out.println("paramTypes:"+paramTypes);
			//����һ��object���飬��װ����������ڴ�������
			Object[] paramValues = new Object[paramTypes.length];
			
			//�ļ��ϴ�
			if (req.getHeader("Content-Type") != null && req.getHeader("Content-Type").contains("multipart/form-data")) {
	            //�ж��Ƿ�multipart/form-data����
				List<Part> files = OwnMultipart.doMultipart(req, resp);
				
				//����һ������  ���ڶ��ļ��ϴ�
				Part[] parts = new Part[files.size()]; int ind = 0;
				//System.out.println("paramTypes:"+Arrays.toString(paramTypes));
				for (Part part : files) {
					int index = handler.paramIndexMapping.get(part.getName());
					//�ж��Ƕ��ļ��ϴ����ǵ��ļ��ϴ�
					if(paramTypes[index].isArray()){
						//System.out.println(paramTypes[index].isArray());
						paramValues[index] = part;
						if(paramValues[index] != null && !paramValues[index].equals("")){
							if(ind == 0){
								parts[ind] = (Part) paramValues[index];
								ind++;
								paramValues[index] = parts;
							}else{
								parts[ind] = part;
								ind++;
								paramValues[index] = parts;
							}
						}
					}else{
						paramValues[index] = part;
					}
				}
	        }
			
			//��ȡ�����������װ��Map<String,String[]>
			Map<String,String[]> params = req.getParameterMap();
			
			//System.out.println("params:"+params);
			for (Entry<String,String[]>param : params.entrySet()) {
				String value = Arrays.toString(param.getValue()).replaceAll("\\[|\\]", "").replaceAll(",\\s", ",");
				//�鿴handler.paramIndexMapping��û�ж�Ӧ��key
				if(!handler.paramIndexMapping.containsKey(param.getKey())){continue;}
				//��ȡ��Ӧ���������ı�������Ͳ�����paramValues��
				int index = handler.paramIndexMapping.get(param.getKey());
				paramValues[index] = convert(paramTypes[index],value);
			}
			//��ȡ��Ӧ�����������Ѷ�Ӧֵ����paramValues��
			int reqindex = handler.paramIndexMapping.get(HttpServletRequest.class.getName());
			paramValues[reqindex] = req;
			//��ȡ��Ӧ�����������Ѷ�Ӧֵ����paramValues��
			int respindex = handler.paramIndexMapping.get(HttpServletResponse.class.getName());
			paramValues[respindex] = resp;
			//���ж�Ӧ�ķ���
			Object obj = handler.method.invoke(handler.controller,paramValues);
			return obj;
		} catch (Exception e) {
			throw e;
		}
		
	}
	
	
	
	/**
	 * ��������ת��
	 * @param type
	 * @param value
	 * @return
	 */
	public static Object convert(Class<?>type,Object value){
		if(Integer.class == type){
			//System.out.println("int:"+value);
			return Integer.valueOf(value.toString());
		}
		if(Double.class == type){
			//System.out.println("dou:"+value);
			return Double.valueOf(value.toString());
		}
		if(Part.class == type){
			return (Part)value;
		}
		return value;
	}
}







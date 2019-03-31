package com.own.servlet.init;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import com.own.annotation.OwnAutowired;
import com.own.annotation.OwnController;
import com.own.annotation.OwnRequestMapping;
import com.own.annotation.OwnService;
import com.own.servlet.handler.OwnHandler;
import com.own.util.OwnStringUtil;

public class InitMethod {
	//��ȡ�����ļ�����
	private Properties prop = new Properties();
	
	//����һ������
	private List<String> classes = new ArrayList<String>();
	
	//����һ��ioc����
	private Map<String, Object> ioc = new HashMap<String, Object>();
	
	//����HandlerMapping����
//	private Map<String,Method> handlerMapping = new HashMap<String, Method>();
	private List<OwnHandler>handlerMapping = new ArrayList<OwnHandler>();
	
	
	/**
	 * ���������ļ���Properties��
	 * @param location
	 */
	public void doLoadConfig(String location){
		//��ȡ�������ļ������ݵ���������
		InputStream in = this.getClass().getClassLoader().getResourceAsStream(location);
		try {
			prop.load(in);	//���������ļ����ݵ�Properties��
		} catch (IOException e) {
			e.printStackTrace();
		}finally {
			try {
				in.close();	 //�ر�������
			} catch (IOException e) {
				e.printStackTrace();
			} 
		}
	}
	
	
	/**
	 * ɨ������������������,@controller,@Service��
	 * װ��classes������
	 * @param packageName  ��Ҫɨ��İ�·��
	 */
	public void doScanner(String packageName){
		//�Զ���ɨ��(Ϊ�ձ�ʾ���Σ���Ҫ��ȡɨ��ĳ�ʼ·��)
		if(packageName.trim().equals("") || packageName == null){
			packageName = prop.getProperty("scanPackage");
		}
		//��class�ļ���Ŀ¼���ҵ����е�class�ļ�
		URL url = this.getClass().getClassLoader().getResource(packageName.replaceAll("\\.", "/"));
		//��ȡ���ļ�
		File dir = new File(url.getFile());
		
		//��ȡ���ļ��������ļ�(���ļ���)
		for (File file : dir.listFiles()) {
			if(file.isDirectory()){	//�жϸ��ļ��ǲ����ļ���(�����,�ݹ����)
				doScanner(packageName + "." + file.getName());
			}else{
				String className = packageName + "." +file.getName().replace(".class", "");
				classes.add(className);
			}
		}
	}
	
	/**
	 * �����ʼ��,��װ�ص�ioc������(�Զ�ɨ��)
	 */
	public void doInstance(){
		//���classes����Ϊ��,ֱ�ӷ�����ֹ�ò���
		if(classes.isEmpty()) return;
		
		try {
			//����ÿһ��className
			for (String className : classes) {
				Class<?> clazz = Class.forName(className);
				//�ж��Ƿ�Ϊ�Զ����ע��
				if(clazz.isAnnotationPresent(OwnController.class)){
					//beanName Ĭ��Ϊ����ĸСд clazz.getSimpleName()��ȡ����
					String beanName = OwnStringUtil.lowerFirst(clazz.getSimpleName());
					
					//���浽ioc������
					ioc.put(beanName, clazz.newInstance());
					
				}else if(clazz.isAnnotationPresent(OwnService.class)){
					//Service�нӿ�
					//����ĸСд.AutoWired��ֵ
					//ͨ��ClassType������
					OwnService service = clazz.getAnnotation(OwnService.class);
					
					String beanName = service.value();
					
					//�ж�beanName�Ƿ�Ϊ��
					if (!beanName.trim().equals("")) {   //�ж�������
						ioc.put(beanName, clazz.newInstance());
						
					}else{    //û�ж������� Ĭ������,����ĸСд
						beanName = OwnStringUtil.lowerFirst(clazz.getSimpleName());
						ioc.put(beanName, clazz.newInstance());
					}
					
					//�ж���û��ʵ�ֽӿ�(���ǲ���impl��)��ȡһ���ӿ�����(��ȡ���нӿ�)
					Class<?>[] interfaces = clazz.getInterfaces();
					//��ʵ�ֽ��
					for (Class<?> i : interfaces) {
						ioc.put(i.getName(), clazz.newInstance());
					}
				}else{
					continue;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * ��������ע��
	 */
	public void doAutowired(){
		//�����ioc����û��ֵ,ֱ�ӷ���
		if (ioc.isEmpty()) return;
		for (Entry<String,Object> entry : ioc.entrySet()) {
			//�Ѹ����������������ȡ��(getValue;��ȡ����getClass��ȡ�����;getDeclaredFields��ȡ��������)
			Field[] fields = entry.getValue().getClass().getDeclaredFields();
			
			for (Field field : fields) {
				//û��OwnAutowiredע�� ֱ����һ��
				if(!field.isAnnotationPresent(OwnAutowired.class)) continue;
				//��OwnAutowiredע��
				OwnAutowired autowired = field.getAnnotation(OwnAutowired.class);

				//�ж������ֵ�ֱ��ʹ�ö��������
				String beanName = autowired.value().trim();
				//û�ж������ֵ� ��Ĭ������
				if(beanName.equals("")){ 
					beanName = field.getType().getName();
					
					//ioc���Ҳ������beanName��ʹ��Ĭ��������ƥ��
					if(!ioc.containsKey(beanName)){
						beanName = field.getName();
					}
				}
				
				//˽������Ҳֱ��ע��
				field.setAccessible(true);
				
				try {
					//�Ѷ���ֱ��ע�뵽�����У������Ը�ֵ��
					field.set(entry.getValue(), ioc.get(beanName));
				}catch (Exception e) {
					e.printStackTrace();
					continue;
				}
			}
		}
	}
	
	/**
	 * ����handlerMappingӳ���ϵ,��һ��urlӳ��һ��method
	 */
	public void initHandlerMapping(){
		//�����ioc����û��ֵ,ֱ�ӷ���
		if (ioc.isEmpty()) return;
		for (Entry<String,Object> entry : ioc.entrySet()) {
			Class<?> clazz = entry.getValue().getClass();
			
			//ֻ�м���TAOControllerע�����TAORequestMappingע��
			if(!clazz.isAnnotationPresent(OwnController.class)) continue;
			
			//���TAORequestMapping
			String url = "";
			if(clazz.isAnnotationPresent(OwnRequestMapping.class)){
				//��������TAORequestMappingע�⣬��ȡֵ
				OwnRequestMapping mapping = clazz.getAnnotation(OwnRequestMapping.class);
				url = mapping.value().trim();
			}
			
			//������OwnRequestMapping
			Method[] methods = clazz.getMethods();
			//����ÿһ�������鿴�Ƿ���TAORequestMappingע��
			for (Method method : methods) {
				//û��OwnRequestMappingע�� ֱ����һ��
				if(!method.isAnnotationPresent(OwnRequestMapping.class)) continue;
				
				//��OwnRequestMappingע��
				OwnRequestMapping mapping = method.getAnnotation(OwnRequestMapping.class);
//				String methodUrl = ("/" + url + mapping.value().trim()).replaceAll("/+", "/");
				
				//����handlerMapping������
//				handlerMapping.put(methodUrl, method);
				//System.out.println("handlerMapping:"+methodUrl+","+method);
			
				String regex = ("/" + url + mapping.value().trim()).replaceAll("/+", "/");
				//����ƥ��ģʽ
				Pattern pattern = Pattern.compile(regex);
				//����Handler������handlerMapping�У�ƥ��ģʽ���������ڵ�controller����Ӧ�ķ�����
				handlerMapping.add(new OwnHandler(pattern,entry.getValue(),method));
			}
			
		}
		
	}
	
	
	/**
	 * ��ȡOwnHandler����
	 * @param req
	 * @return
	 * @throws Exception
	 */
	public OwnHandler getHandler(HttpServletRequest req)throws Exception{
		if(handlerMapping.isEmpty())return null;
		
		//��ȡ����urlȫ·��
		String url = req.getRequestURL().toString();
		//��Ŀurl
		String contextPath = req.getScheme()+"://"+req.getServerName()+":"+req.getServerPort()+req.getContextPath(); 
		//�Ѹ�·��ȥ��ֻ����ӳ��·��.�Լ������//���滻����
		url = url.replace(contextPath, "").replaceAll("/+", "/");
		
		//����handlerMapping ������ƥ��url
		for (OwnHandler handler : handlerMapping) {
			try {
				//ƥ��������ʽ
				Matcher matcher = handler.pattern.matcher(url);
				//������Ŀ���ַ�չ��ƥ���⣬���û�У�������һ��
				if(!matcher.matches()){continue;}
				//�ҵ����Ӧ��Handler
				return handler;
			} catch (Exception e) {
				throw e;
			}
		}
		
		return null;
	}
	
	
	
}

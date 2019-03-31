package com.tao.servlet;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import com.tao.annotation.TAOAutowired;
import com.tao.annotation.TAOController;
import com.tao.annotation.TAORequestMapping;
import com.tao.annotation.TAORequestParam;
import com.tao.annotation.TAOService;
import com.tao.entity.TAOMultipartFile;

/**
 * �Զ���DispatcherServlet ��Ҫ�̳��� HttpServlet
 * @author TAO
 *
 */
@MultipartConfig
public class MyDispatcherServlet extends HttpServlet{
	//��ȡ�����ļ�����
	private Properties prop = new Properties();
	
	//����һ������
	private List<String> classes = new ArrayList<String>();
	
	//����һ��ioc����
	private Map<String, Object> ioc = new HashMap<String, Object>();
	
	//����HandlerMapping����
//	private Map<String,Method> handlerMapping = new HashMap<String, Method>();
	List<Handler>handlerMapping = new ArrayList<Handler>();
	
	@Override
	public void init(ServletConfig config) throws ServletException {
		
		//���������ļ�,ͨ����application.xml,����ʹ��application.xml����
		doLoadConfig(config.getInitParameter("contextConfigLocation"));
		
		//ɨ������������������,@controller,@Service��
		doScanner(prop.getProperty("scanPackage"));
		
		//�����ʼ��,��װ�ص�ioc������(�Զ�ɨ��)
		doInstance();
		
		//��������ע��
		doAutowired();
		
		//����handlerMappingӳ���ϵ,��һ��urlӳ��һ��method
		initHandlerMapping();
		
		//�ȴ��û�����,ƥ��url,�ҵ����Ӧ�ķ���,�������ִ��
		
		//���ؽ��
		
		System.out.println("�����˰���" + config);
	}
	
	private void doLoadConfig(String location){
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
	
	private void doScanner(String packageName){
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
	
	private void doInstance(){
		//���classes����Ϊ��,ֱ�ӷ�����ֹ�ò���
		if(classes.isEmpty()) return;
		
		try {
			//����ÿһ��className
			for (String className : classes) {
				Class<?> clazz = Class.forName(className);
				//�ж��Ƿ�Ϊ�Զ����ע��
				if(clazz.isAnnotationPresent(TAOController.class)){
					//beanName Ĭ��Ϊ����ĸСд clazz.getSimpleName()��ȡ����
					String beanName = lowerFirst(clazz.getSimpleName());
					
					//���浽ioc������
					ioc.put(beanName, clazz.newInstance());
					
				}else if(clazz.isAnnotationPresent(TAOService.class)){
					//Service�нӿ�
					//����ĸСд.AutoWired��ֵ
					//ͨ��ClassType������
					TAOService service = clazz.getAnnotation(TAOService.class);
					
					String beanName = service.value();
					
					//�ж�beanName�Ƿ�Ϊ��
					if (!beanName.trim().equals("")) {   //�ж�������
						ioc.put(beanName, clazz.newInstance());
						
					}else{    //û�ж������� Ĭ������,����ĸСд
						beanName = lowerFirst(clazz.getSimpleName());
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
	
	private void doAutowired(){
		//�����ioc����û��ֵ,ֱ�ӷ���
		if (ioc.isEmpty()) return;
		for (Entry<String,Object> entry : ioc.entrySet()) {
			//�Ѹ����������������ȡ��(getValue;��ȡ����getClass��ȡ�����;getDeclaredFields��ȡ��������)
			Field[] fields = entry.getValue().getClass().getDeclaredFields();
			
			for (Field field : fields) {
				//û��TAOAutowiredע�� ֱ����һ��
				if(!field.isAnnotationPresent(TAOAutowired.class)) continue;
				//��TAOAutowiredע��
				TAOAutowired autowired = field.getAnnotation(TAOAutowired.class);

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
	
	private void initHandlerMapping(){
		//�����ioc����û��ֵ,ֱ�ӷ���
		if (ioc.isEmpty()) return;
		for (Entry<String,Object> entry : ioc.entrySet()) {
			Class<?> clazz = entry.getValue().getClass();
			
			//ֻ�м���TAOControllerע�����TAORequestMappingע��
			if(!clazz.isAnnotationPresent(TAOController.class)) continue;
			
			//���TAORequestMapping
			String url = "";
			if(clazz.isAnnotationPresent(TAORequestMapping.class)){
				//��������TAORequestMappingע�⣬��ȡֵ
				TAORequestMapping mapping = clazz.getAnnotation(TAORequestMapping.class);
				url = mapping.value().trim();
			}
			
			//������TAORequestMapping
			Method[] methods = clazz.getMethods();
			//����ÿһ�������鿴�Ƿ���TAORequestMappingע��
			for (Method method : methods) {
				//û��TAORequestMappingע�� ֱ����һ��
				if(!method.isAnnotationPresent(TAORequestMapping.class)) continue;
				
				//��TAORequestMappingע��
				TAORequestMapping mapping = method.getAnnotation(TAORequestMapping.class);
//				String methodUrl = ("/" + url + mapping.value().trim()).replaceAll("/+", "/");
				
				//����handlerMapping������
//				handlerMapping.put(methodUrl, method);
				//System.out.println("handlerMapping:"+methodUrl+","+method);
			
				String regex = ("/" + url + mapping.value().trim()).replaceAll("/+", "/");
				//����ƥ��ģʽ
				Pattern pattern = Pattern.compile(regex);
				//����Handler������handlerMapping�У�ƥ��ģʽ���������ڵ�controller����Ӧ�ķ�����
				handlerMapping.add(new Handler(pattern,entry.getValue(),method));
				//System.out.println("mapping:"+regex+","+method);
			}
			
		}
		
	}
	

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		this.doPost(req, resp);
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		
		try {
			doDispatch(req,resp);
		} catch (Exception e) {
			resp.setStatus(500);
			resp.getWriter().write("500 Exception,Detail:\r\n" + Arrays.toString(e.getStackTrace()));
		}
	}
	
	
	private void doDispatch(HttpServletRequest req, HttpServletResponse resp) throws Exception{
		try {
			//��ȡ��������url��ȡ���Ӧ��Handler
			Handler handler = getHandler(req);
			
			//HandlerΪ�� ֱ�ӷ���404����
			if (handler == null){
				resp.setStatus(404);
				resp.getWriter().write("404 Not Found");
				return;
			}
			
			//��ȡ�÷����Ĳ�������
			Class<?>[] paramTypes = handler.method.getParameterTypes();
			//����һ��object���飬��װ����������ڴ�������
			Object[] paramValues = new Object[paramTypes.length];
			
			//�ļ��ϴ�
			if (req.getHeader("Content-Type").contains("multipart/form-data")) {
	            //�ж��Ƿ�multipart/form-data����
				List<Part> files = doMultipart(req, resp);
				
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
						//System.out.println(paramTypes[index].isArray());
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
			handler.method.invoke(handler.controller,paramValues);
		} catch (Exception e) {
			throw e;
		}
		
	}
	
	
	private Handler getHandler(HttpServletRequest req)throws Exception{
		if(handlerMapping.isEmpty())return null;
		
		//��ȡ����urlȫ·��
		String url = req.getRequestURL().toString();
		//��Ŀurl
		String contextPath = req.getScheme()+"://"+req.getServerName()+":"+req.getServerPort()+req.getContextPath(); 
		//�Ѹ�·��ȥ��ֻ����ӳ��·��.�Լ������//���滻����
		url = url.replace(contextPath, "").replaceAll("/+", "/");
		
		//����handlerMapping ������ƥ��url
		for (Handler handler : handlerMapping) {
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
	
	private Object convert(Class<?>type,Object value){
		System.out.println(type);
		if(Integer.class == type){
			return Integer.valueOf(value.toString());
		}
		if(Double.class == type){
			return Double.valueOf(value.toString());
		}
		if(Part.class == type){
			return (Part)value;
		}
		return value;
	}
	
	/**
	 * ����ĸСд
	 */
	private String lowerFirst(String str){
		char[]chars = str.toCharArray();
		chars[0] += 32;
		return String.valueOf(chars);
	}
	
	//�ϴ��ļ�����
	private List<Part> doMultipart(HttpServletRequest req, HttpServletResponse resp){
		List<Part> partFiles = null;
		try {
			
			Collection<Part> parts= req.getParts();
			
			partFiles = new ArrayList<Part>(parts.size());
			for (Part part : parts) {
				String contentType = part.getContentType();
				if(contentType != null && !contentType.equals("application/octet-stream")){
					Part file = (Part) convert(Part.class,part);
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




/**
 * �ڲ��� ��¼ӳ���ϵ
 * @author TAO
 *
 */
class Handler{
	protected Object controller;
	protected Method method;
	protected Pattern pattern;
	protected Map<String,Integer> paramIndexMapping;
	
	public Handler(Pattern pattern, Object controller, Method method) {
		this.controller = controller;
		this.method = method;
		this.pattern = pattern;
		
		paramIndexMapping = new HashMap<String, Integer>();
		putParamIndexMapping(method);
	}
	
	private void putParamIndexMapping(Method method){
		//ͨ��������ȡ����ע��
		Annotation[][]pa = method.getParameterAnnotations();
		
		for (int i = 0; i < pa.length; i++) {
			for (Annotation a : pa[i]) {
				//�����ʹ��TAORequestParamע�� ��ȡֵ������paramIndexMapping��
				if(a instanceof TAORequestParam){
					String paramName = ((TAORequestParam) a).value();
					//System.out.println("paramName:"+paramName + " , index:" + i);
					if(!paramName.trim().equals("")){
						paramIndexMapping.put(paramName, i);
					}
				}
			}
		}
		
		//��ȡ������������
		Class<?>[]paramsTypes = method.getParameterTypes();
		//���ڻ�ȡ������Ĭ�ϲ�����
		Parameter[]methodNames = method.getParameters();
		//System.out.println(Arrays.toString(paramsTypes));
		//�������ж��������ΪHttpServletRequest��class����HttpServletResponse�Ļ� ֱ�Ӵ���paramIndexMapping��
		for (int i = 0; i < paramsTypes.length; i++) {
			Class<?>type = paramsTypes[i];
			if(type == HttpServletRequest.class ||
				 type == HttpServletResponse.class){
				paramIndexMapping.put(type.getName(), i);
			}else{
				if(pa[i].length==0){
					//û�д�TAORequestParamע�� Ĭ�ϴ���������
					//System.out.println(type.isArray()+"--"+methodNames[i].getName());
					paramIndexMapping.put(methodNames[i].getName(), i);
				}
			}
		}
	}
}





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
 * 自定义DispatcherServlet 需要继承自 HttpServlet
 * @author TAO
 *
 */
@MultipartConfig
public class MyDispatcherServlet extends HttpServlet{
	//获取配置文件内容
	private Properties prop = new Properties();
	
	//定义一个容器
	private List<String> classes = new ArrayList<String>();
	
	//定义一个ioc容器
	private Map<String, Object> ioc = new HashMap<String, Object>();
	
	//定义HandlerMapping容器
//	private Map<String,Method> handlerMapping = new HashMap<String, Method>();
	List<Handler>handlerMapping = new ArrayList<Handler>();
	
	@Override
	public void init(ServletConfig config) throws ServletException {
		
		//加载配置文件,通常是application.xml,我们使用application.xml代替
		doLoadConfig(config.getInitParameter("contextConfigLocation"));
		
		//扫描所有满足条件的类,@controller,@Service等
		doScanner(prop.getProperty("scanPackage"));
		
		//把类初始化,并装载到ioc容器中(自动扫描)
		doInstance();
		
		//进行依赖注入
		doAutowired();
		
		//构造handlerMapping映射关系,将一个url映射一个method
		initHandlerMapping();
		
		//等待用户请求,匹配url,找到相对应的方法,反射调用执行
		
		//返回结果
		
		System.out.println("启动了啊：" + config);
	}
	
	private void doLoadConfig(String location){
		//获取该配置文件的内容到输入流中
		InputStream in = this.getClass().getClassLoader().getResourceAsStream(location);
		try {
			prop.load(in);	//加载配置文件内容到Properties中
		} catch (IOException e) {
			e.printStackTrace();
		}finally {
			try {
				in.close();	 //关闭输入流
			} catch (IOException e) {
				e.printStackTrace();
			} 
		}
	}
	
	private void doScanner(String packageName){
		//在class文件的目录下找到所有的class文件
		URL url = this.getClass().getClassLoader().getResource(packageName.replaceAll("\\.", "/"));
		//获取该文件
		File dir = new File(url.getFile());
		
		//获取该文件下所有文件(和文件夹)
		for (File file : dir.listFiles()) {
			if(file.isDirectory()){	//判断该文件是不是文件夹(如果是,递归继续)
				doScanner(packageName + "." + file.getName());
			}else{
				String className = packageName + "." +file.getName().replace(".class", "");
				classes.add(className);
			}
		}
	}
	
	private void doInstance(){
		//如果classes容器为空,直接返回终止该操作
		if(classes.isEmpty()) return;
		
		try {
			//遍历每一个className
			for (String className : classes) {
				Class<?> clazz = Class.forName(className);
				//判断是否为自定义的注解
				if(clazz.isAnnotationPresent(TAOController.class)){
					//beanName 默认为首字母小写 clazz.getSimpleName()获取类名
					String beanName = lowerFirst(clazz.getSimpleName());
					
					//保存到ioc容器中
					ioc.put(beanName, clazz.newInstance());
					
				}else if(clazz.isAnnotationPresent(TAOService.class)){
					//Service有接口
					//首字母小写.AutoWired的值
					//通过ClassType起名字
					TAOService service = clazz.getAnnotation(TAOService.class);
					
					String beanName = service.value();
					
					//判断beanName是否为空
					if (!beanName.trim().equals("")) {   //有定义名字
						ioc.put(beanName, clazz.newInstance());
						
					}else{    //没有定义名字 默认类名,首字母小写
						beanName = lowerFirst(clazz.getSimpleName());
						ioc.put(beanName, clazz.newInstance());
					}
					
					//判断有没有实现接口(即是不是impl类)获取一个接口数组(获取所有接口)
					Class<?>[] interfaces = clazz.getInterfaces();
					//有实现借口
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
		//如果该ioc容器没有值,直接返回
		if (ioc.isEmpty()) return;
		for (Entry<String,Object> entry : ioc.entrySet()) {
			//把该类下面的所有属性取出(getValue;获取对象getClass获取类对象;getDeclaredFields获取所有属性)
			Field[] fields = entry.getValue().getClass().getDeclaredFields();
			
			for (Field field : fields) {
				//没有TAOAutowired注解 直接下一个
				if(!field.isAnnotationPresent(TAOAutowired.class)) continue;
				//有TAOAutowired注解
				TAOAutowired autowired = field.getAnnotation(TAOAutowired.class);

				//有定义名字的直接使用定义的名字
				String beanName = autowired.value().trim();
				//没有定义名字的 用默认名字
				if(beanName.equals("")){ 
					beanName = field.getType().getName();
					
					//ioc中找不到这个beanName，使用默认名字再匹配
					if(!ioc.containsKey(beanName)){
						beanName = field.getName();
					}
				}
				
				//私有属性也直接注入
				field.setAccessible(true);
				
				try {
					//把对象直接注入到属性中（给属性赋值）
					field.set(entry.getValue(), ioc.get(beanName));
				}catch (Exception e) {
					e.printStackTrace();
					continue;
				}
			}
		}
	}
	
	private void initHandlerMapping(){
		//如果该ioc容器没有值,直接返回
		if (ioc.isEmpty()) return;
		for (Entry<String,Object> entry : ioc.entrySet()) {
			Class<?> clazz = entry.getValue().getClass();
			
			//只有加了TAOController注解才有TAORequestMapping注解
			if(!clazz.isAnnotationPresent(TAOController.class)) continue;
			
			//类的TAORequestMapping
			String url = "";
			if(clazz.isAnnotationPresent(TAORequestMapping.class)){
				//如果类加了TAORequestMapping注解，获取值
				TAORequestMapping mapping = clazz.getAnnotation(TAORequestMapping.class);
				url = mapping.value().trim();
			}
			
			//方法的TAORequestMapping
			Method[] methods = clazz.getMethods();
			//遍历每一个方法查看是否有TAORequestMapping注解
			for (Method method : methods) {
				//没有TAORequestMapping注解 直接下一个
				if(!method.isAnnotationPresent(TAORequestMapping.class)) continue;
				
				//有TAORequestMapping注解
				TAORequestMapping mapping = method.getAnnotation(TAORequestMapping.class);
//				String methodUrl = ("/" + url + mapping.value().trim()).replaceAll("/+", "/");
				
				//存入handlerMapping容器中
//				handlerMapping.put(methodUrl, method);
				//System.out.println("handlerMapping:"+methodUrl+","+method);
			
				String regex = ("/" + url + mapping.value().trim()).replaceAll("/+", "/");
				//创建匹配模式
				Pattern pattern = Pattern.compile(regex);
				//创建Handler并存入handlerMapping中（匹配模式，方法所在的controller，对应的方法）
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
			//获取根据请求url获取相对应的Handler
			Handler handler = getHandler(req);
			
			//Handler为空 直接返回404错误
			if (handler == null){
				resp.setStatus(404);
				resp.getWriter().write("404 Not Found");
				return;
			}
			
			//获取该方法的参数类型
			Class<?>[] paramTypes = handler.method.getParameterTypes();
			//创建一个object数组，封装请求参数用于创建方法
			Object[] paramValues = new Object[paramTypes.length];
			
			//文件上传
			if (req.getHeader("Content-Type").contains("multipart/form-data")) {
	            //判断是否multipart/form-data请求
				List<Part> files = doMultipart(req, resp);
				
				//创建一个数组  用于多文件上传
				Part[] parts = new Part[files.size()]; int ind = 0;
				//System.out.println("paramTypes:"+Arrays.toString(paramTypes));
				for (Part part : files) {
					int index = handler.paramIndexMapping.get(part.getName());
					//判断是多文件上传还是单文件上传
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
			
			//获取请求参数并封装到Map<String,String[]>
			Map<String,String[]> params = req.getParameterMap();
			
			//System.out.println("params:"+params);
			for (Entry<String,String[]>param : params.entrySet()) {
				String value = Arrays.toString(param.getValue()).replaceAll("\\[|\\]", "").replaceAll(",\\s", ",");
				
				//查看handler.paramIndexMapping有没有对应的key
				if(!handler.paramIndexMapping.containsKey(param.getKey())){continue;}
				//获取对应的索引，改变参数类型并存人paramValues中
				int index = handler.paramIndexMapping.get(param.getKey());
				paramValues[index] = convert(paramTypes[index],value);
			
			}
			//获取对应的索引，并把对应值存人paramValues中
			int reqindex = handler.paramIndexMapping.get(HttpServletRequest.class.getName());
			paramValues[reqindex] = req;
			//获取对应的索引，并把对应值存人paramValues中
			int respindex = handler.paramIndexMapping.get(HttpServletResponse.class.getName());
			paramValues[respindex] = resp;
			
			//运行对应的方法
			handler.method.invoke(handler.controller,paramValues);
		} catch (Exception e) {
			throw e;
		}
		
	}
	
	
	private Handler getHandler(HttpServletRequest req)throws Exception{
		if(handlerMapping.isEmpty())return null;
		
		//获取请求url全路径
		String url = req.getRequestURL().toString();
		//项目url
		String contextPath = req.getScheme()+"://"+req.getServerName()+":"+req.getServerPort()+req.getContextPath(); 
		//把根路径去掉只留下映射路径.以及如果有//则替换以下
		url = url.replace(contextPath, "").replaceAll("/+", "/");
		
		//遍历handlerMapping 用正则匹配url
		for (Handler handler : handlerMapping) {
			try {
				//匹配正则表达式
				Matcher matcher = handler.pattern.matcher(url);
				//对整个目标字符展开匹配检测，如果没有，继续下一个
				if(!matcher.matches()){continue;}
				//找到相对应的Handler
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
	 * 首字母小写
	 */
	private String lowerFirst(String str){
		char[]chars = str.toCharArray();
		chars[0] += 32;
		return String.valueOf(chars);
	}
	
	//上传文件处理
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
 * 内部类 记录映射关系
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
		//通过方法获取参数注解
		Annotation[][]pa = method.getParameterAnnotations();
		
		for (int i = 0; i < pa.length; i++) {
			for (Annotation a : pa[i]) {
				//如果有使用TAORequestParam注解 获取值并存入paramIndexMapping中
				if(a instanceof TAORequestParam){
					String paramName = ((TAORequestParam) a).value();
					//System.out.println("paramName:"+paramName + " , index:" + i);
					if(!paramName.trim().equals("")){
						paramIndexMapping.put(paramName, i);
					}
				}
			}
		}
		
		//获取方法参数类型
		Class<?>[]paramsTypes = method.getParameterTypes();
		//用于获取方法的默认参赛名
		Parameter[]methodNames = method.getParameters();
		//System.out.println(Arrays.toString(paramsTypes));
		//遍历，判断如果类型为HttpServletRequest。class或者HttpServletResponse的话 直接存入paramIndexMapping中
		for (int i = 0; i < paramsTypes.length; i++) {
			Class<?>type = paramsTypes[i];
			if(type == HttpServletRequest.class ||
				 type == HttpServletResponse.class){
				paramIndexMapping.put(type.getName(), i);
			}else{
				if(pa[i].length==0){
					//没有带TAORequestParam注解 默认存入属性名
					//System.out.println(type.isArray()+"--"+methodNames[i].getName());
					paramIndexMapping.put(methodNames[i].getName(), i);
				}
			}
		}
	}
}





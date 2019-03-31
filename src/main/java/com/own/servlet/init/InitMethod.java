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
	//获取配置文件内容
	private Properties prop = new Properties();
	
	//定义一个容器
	private List<String> classes = new ArrayList<String>();
	
	//定义一个ioc容器
	private Map<String, Object> ioc = new HashMap<String, Object>();
	
	//定义HandlerMapping容器
//	private Map<String,Method> handlerMapping = new HashMap<String, Method>();
	private List<OwnHandler>handlerMapping = new ArrayList<OwnHandler>();
	
	
	/**
	 * 加载配置文件到Properties中
	 * @param location
	 */
	public void doLoadConfig(String location){
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
	
	
	/**
	 * 扫描所有满足条件的类,@controller,@Service等
	 * 装入classes容器中
	 * @param packageName  需要扫描的包路径
	 */
	public void doScanner(String packageName){
		//自动包扫描(为空表示初次，需要获取扫描的初始路径)
		if(packageName.trim().equals("") || packageName == null){
			packageName = prop.getProperty("scanPackage");
		}
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
	
	/**
	 * 把类初始化,并装载到ioc容器中(自动扫描)
	 */
	public void doInstance(){
		//如果classes容器为空,直接返回终止该操作
		if(classes.isEmpty()) return;
		
		try {
			//遍历每一个className
			for (String className : classes) {
				Class<?> clazz = Class.forName(className);
				//判断是否为自定义的注解
				if(clazz.isAnnotationPresent(OwnController.class)){
					//beanName 默认为首字母小写 clazz.getSimpleName()获取类名
					String beanName = OwnStringUtil.lowerFirst(clazz.getSimpleName());
					
					//保存到ioc容器中
					ioc.put(beanName, clazz.newInstance());
					
				}else if(clazz.isAnnotationPresent(OwnService.class)){
					//Service有接口
					//首字母小写.AutoWired的值
					//通过ClassType起名字
					OwnService service = clazz.getAnnotation(OwnService.class);
					
					String beanName = service.value();
					
					//判断beanName是否为空
					if (!beanName.trim().equals("")) {   //有定义名字
						ioc.put(beanName, clazz.newInstance());
						
					}else{    //没有定义名字 默认类名,首字母小写
						beanName = OwnStringUtil.lowerFirst(clazz.getSimpleName());
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
	
	/**
	 * 进行依赖注入
	 */
	public void doAutowired(){
		//如果该ioc容器没有值,直接返回
		if (ioc.isEmpty()) return;
		for (Entry<String,Object> entry : ioc.entrySet()) {
			//把该类下面的所有属性取出(getValue;获取对象getClass获取类对象;getDeclaredFields获取所有属性)
			Field[] fields = entry.getValue().getClass().getDeclaredFields();
			
			for (Field field : fields) {
				//没有OwnAutowired注解 直接下一个
				if(!field.isAnnotationPresent(OwnAutowired.class)) continue;
				//有OwnAutowired注解
				OwnAutowired autowired = field.getAnnotation(OwnAutowired.class);

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
	
	/**
	 * 构造handlerMapping映射关系,将一个url映射一个method
	 */
	public void initHandlerMapping(){
		//如果该ioc容器没有值,直接返回
		if (ioc.isEmpty()) return;
		for (Entry<String,Object> entry : ioc.entrySet()) {
			Class<?> clazz = entry.getValue().getClass();
			
			//只有加了TAOController注解才有TAORequestMapping注解
			if(!clazz.isAnnotationPresent(OwnController.class)) continue;
			
			//类的TAORequestMapping
			String url = "";
			if(clazz.isAnnotationPresent(OwnRequestMapping.class)){
				//如果类加了TAORequestMapping注解，获取值
				OwnRequestMapping mapping = clazz.getAnnotation(OwnRequestMapping.class);
				url = mapping.value().trim();
			}
			
			//方法的OwnRequestMapping
			Method[] methods = clazz.getMethods();
			//遍历每一个方法查看是否有TAORequestMapping注解
			for (Method method : methods) {
				//没有OwnRequestMapping注解 直接下一个
				if(!method.isAnnotationPresent(OwnRequestMapping.class)) continue;
				
				//有OwnRequestMapping注解
				OwnRequestMapping mapping = method.getAnnotation(OwnRequestMapping.class);
//				String methodUrl = ("/" + url + mapping.value().trim()).replaceAll("/+", "/");
				
				//存入handlerMapping容器中
//				handlerMapping.put(methodUrl, method);
				//System.out.println("handlerMapping:"+methodUrl+","+method);
			
				String regex = ("/" + url + mapping.value().trim()).replaceAll("/+", "/");
				//创建匹配模式
				Pattern pattern = Pattern.compile(regex);
				//创建Handler并存入handlerMapping中（匹配模式，方法所在的controller，对应的方法）
				handlerMapping.add(new OwnHandler(pattern,entry.getValue(),method));
			}
			
		}
		
	}
	
	
	/**
	 * 获取OwnHandler对象
	 * @param req
	 * @return
	 * @throws Exception
	 */
	public OwnHandler getHandler(HttpServletRequest req)throws Exception{
		if(handlerMapping.isEmpty())return null;
		
		//获取请求url全路径
		String url = req.getRequestURL().toString();
		//项目url
		String contextPath = req.getScheme()+"://"+req.getServerName()+":"+req.getServerPort()+req.getContextPath(); 
		//把根路径去掉只留下映射路径.以及如果有//则替换以下
		url = url.replace(contextPath, "").replaceAll("/+", "/");
		
		//遍历handlerMapping 用正则匹配url
		for (OwnHandler handler : handlerMapping) {
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
	
	
	
}

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
 * 自定义DispatcherServlet 需要继承自 HttpServlet
 * @author TAO
 *
 */
@MultipartConfig
public class OwnDispatcherServlet extends HttpServlet{
	private InitMethod init = new InitMethod();
	
	@Override
	public void init(ServletConfig config) throws ServletException {
		
		//加载配置文件,通常是application.xml,我们使用application.xml代替
		init.doLoadConfig(config.getInitParameter("contextConfigLocation"));
		
		//扫描所有满足条件的类,@controller,@Service等
		init.doScanner("");
		
		//把类初始化,并装载到ioc容器中(自动扫描)
		init.doInstance();
		
		//进行依赖注入
		init.doAutowired();
		
		//构造handlerMapping映射关系,将一个url映射一个method
		init.initHandlerMapping();
		
		System.out.println("启动了啊：" + config);
		
	}
	

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		this.doPost(req, resp);
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		
		try {
			//等待用户请求,匹配url,找到相对应的方法,反射调用执行
			Object obj = doDispatch(req,resp);
			//返回结果
			if(obj != null){
				//转发
				if(obj.getClass() == String.class && obj.toString().contains("redirect")){
					OwnResp.onResponse(req,resp,obj.toString());
				}else if(obj.getClass() == String.class && obj.toString().contains("forward")){
					//重定向
					OwnResp.onRequest(req,resp,obj.toString());
				}else if(obj.getClass() == OwnModelAndView.class){  //ModelAndView
					OwnModelAndView omav = new OwnModelAndView();
					omav.onModelAndView(req,resp,obj);
				}else{
					//直接输出
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
		System.out.println("doDispatch运行");
		try {
			//获取根据请求url获取相对应的Handler
			OwnHandler handler = init.getHandler(req);
			
			//Handler为空 直接返回404错误
			if (handler == null){
				resp.setStatus(404);
				resp.getWriter().write("404 Not Found");
			}
			
			//获取该方法的参数类型
			Class<?>[] paramTypes = handler.method.getParameterTypes();
			//System.out.println("paramTypes:"+paramTypes);
			//创建一个object数组，封装请求参数用于创建方法
			Object[] paramValues = new Object[paramTypes.length];
			
			//文件上传
			if (req.getHeader("Content-Type") != null && req.getHeader("Content-Type").contains("multipart/form-data")) {
	            //判断是否multipart/form-data请求
				List<Part> files = OwnMultipart.doMultipart(req, resp);
				
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
			Object obj = handler.method.invoke(handler.controller,paramValues);
			return obj;
		} catch (Exception e) {
			throw e;
		}
		
	}
	
	
	
	/**
	 * 参数类型转换
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







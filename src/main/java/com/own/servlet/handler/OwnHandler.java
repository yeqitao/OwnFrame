package com.own.servlet.handler;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import com.own.annotation.OwnRequestParam;

/**
 * 记录映射关系
 * @author TAO
 *
 */
public class OwnHandler {
	public Object controller;
	public Method method;
	public Pattern pattern;
	public Map<String,Integer> paramIndexMapping;
	
	public OwnHandler(Pattern pattern, Object controller, Method method) {
		this.controller = controller;
		this.method = method;
		this.pattern = pattern;
		
		paramIndexMapping = new HashMap<String, Integer>();
		putParamIndexMapping(method);
	}
	/**
	 * 方法参数索引标记
	 * @param method
	 */
	private void putParamIndexMapping(Method method){
		//通过方法获取参数注解
		Annotation[][]pa = method.getParameterAnnotations();
		
		for (int i = 0; i < pa.length; i++) {
			for (Annotation a : pa[i]) {
				//如果有使用OwnRequestParam注解 获取值并存入paramIndexMapping中
				if(a instanceof OwnRequestParam){
					String paramName = ((OwnRequestParam) a).value();
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
		//遍历，判断如果类型为HttpServletRequest。class或者HttpServletResponse的话 直接存入paramIndexMapping中
		for (int i = 0; i < paramsTypes.length; i++) {
			Class<?>type = paramsTypes[i];
			if(type == HttpServletRequest.class ||
				 type == HttpServletResponse.class){
				paramIndexMapping.put(type.getName(), i);
			}else{
				if(pa[i].length==0){
					//没有带OwnRequestParam注解 默认存入属性名
					//System.out.println(type.isArray()+"--"+methodNames[i].getName());
					paramIndexMapping.put(methodNames[i].getName(), i);
				}
			}
		}
	}
	
	
}

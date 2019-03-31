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
 * ��¼ӳ���ϵ
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
	 * ���������������
	 * @param method
	 */
	private void putParamIndexMapping(Method method){
		//ͨ��������ȡ����ע��
		Annotation[][]pa = method.getParameterAnnotations();
		
		for (int i = 0; i < pa.length; i++) {
			for (Annotation a : pa[i]) {
				//�����ʹ��OwnRequestParamע�� ��ȡֵ������paramIndexMapping��
				if(a instanceof OwnRequestParam){
					String paramName = ((OwnRequestParam) a).value();
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
		//�������ж��������ΪHttpServletRequest��class����HttpServletResponse�Ļ� ֱ�Ӵ���paramIndexMapping��
		for (int i = 0; i < paramsTypes.length; i++) {
			Class<?>type = paramsTypes[i];
			if(type == HttpServletRequest.class ||
				 type == HttpServletResponse.class){
				paramIndexMapping.put(type.getName(), i);
			}else{
				if(pa[i].length==0){
					//û�д�OwnRequestParamע�� Ĭ�ϴ���������
					//System.out.println(type.isArray()+"--"+methodNames[i].getName());
					paramIndexMapping.put(methodNames[i].getName(), i);
				}
			}
		}
	}
	
	
}

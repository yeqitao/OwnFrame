package com.own.jdbc;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.own.util.OwnStringUtil;

/**
 * 数据封装类
 *
 */
public class jdbcPackage {
	/**
	 * 封装一个对象
	 */
	public static Object dataPackageOne(ResultSet result,Object obj){
		try {
			//获取对象所有属性
			Field[]fields = obj.getClass().getDeclaredFields();
			
			//创建容器保存数据
			Map<String,Object> map = new HashMap<>();
			//加载数据到容器中
			while(result.next()){
				for (Field field : fields) {
					map.put("set"+OwnStringUtil.UpperFirst(field.getName()), result.getObject(field.getName()));
				}
			}
			obj = setMethods(map,obj);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return obj;
	}
	
	/**
	 * 封装一个集合对象
	 */
	public static List<Object> dataPackageList(ResultSet result,Object obj){
		List<Object>list = new ArrayList<>();
		
		try {
			//获取对象所有属性
			Field[]fields = obj.getClass().getDeclaredFields();
			
			//创建容器保存数据
			Map<String,Object> map = new HashMap<>();
			//加载数据到容器中
			while(result.next()){
				for (Field field : fields) {
					map.put("set"+OwnStringUtil.UpperFirst(field.getName()), result.getObject(field.getName()));
				}
				list.add(setMethods(map,newObject(obj)));
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}
	
	
	/**
	 * 通过反射set数据
	 */
	public static Object setMethods(Map<String,Object> map,Object obj) throws Exception{
		//反射获取所有方法
		Method[]methods = obj.getClass().getMethods();
		for(int i=0;i<methods.length;++i){
			 //判断方法名是否匹配
			if(map.containsKey(methods[i].getName())){
				//执行方法，传入值
				methods[i].invoke(obj, map.get(methods[i].getName()));
			}
			
		}
		return obj;
	}
	
	/**
	 * 获取新的obj对象 
	 */
	public static Object newObject(Object obj) throws Exception{
		return obj.getClass().newInstance();
	}
	
}

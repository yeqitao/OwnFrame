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
 * ���ݷ�װ��
 *
 */
public class jdbcPackage {
	/**
	 * ��װһ������
	 */
	public static Object dataPackageOne(ResultSet result,Object obj){
		try {
			//��ȡ������������
			Field[]fields = obj.getClass().getDeclaredFields();
			
			//����������������
			Map<String,Object> map = new HashMap<>();
			//�������ݵ�������
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
	 * ��װһ�����϶���
	 */
	public static List<Object> dataPackageList(ResultSet result,Object obj){
		List<Object>list = new ArrayList<>();
		
		try {
			//��ȡ������������
			Field[]fields = obj.getClass().getDeclaredFields();
			
			//����������������
			Map<String,Object> map = new HashMap<>();
			//�������ݵ�������
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
	 * ͨ������set����
	 */
	public static Object setMethods(Map<String,Object> map,Object obj) throws Exception{
		//�����ȡ���з���
		Method[]methods = obj.getClass().getMethods();
		for(int i=0;i<methods.length;++i){
			 //�жϷ������Ƿ�ƥ��
			if(map.containsKey(methods[i].getName())){
				//ִ�з���������ֵ
				methods[i].invoke(obj, map.get(methods[i].getName()));
			}
			
		}
		return obj;
	}
	
	/**
	 * ��ȡ�µ�obj���� 
	 */
	public static Object newObject(Object obj) throws Exception{
		return obj.getClass().newInstance();
	}
	
}

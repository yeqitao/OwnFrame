package com.own.util;

public class OwnStringUtil {
	
	/**
	 * ����ĸСд
	 */
	public static String lowerFirst(String str){
		return (str.substring(0, 1).toLowerCase())+(str.substring(1));
	}
	
	/**
	 * ����ĸ��д
	 */
	public static String UpperFirst(String str){
		return (str.substring(0, 1).toUpperCase())+(str.substring(1));
	}
	
}

package com.own.util;

public class OwnStringUtil {
	
	/**
	 * 首字母小写
	 */
	public static String lowerFirst(String str){
		return (str.substring(0, 1).toLowerCase())+(str.substring(1));
	}
	
	/**
	 * 首字母大写
	 */
	public static String UpperFirst(String str){
		return (str.substring(0, 1).toUpperCase())+(str.substring(1));
	}
	
}

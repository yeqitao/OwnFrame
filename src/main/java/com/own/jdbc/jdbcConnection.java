package com.own.jdbc;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import java.util.Properties;

import com.tao.demo.entity.MR;


/**
 * 简易版jdbc框架(对象 属性名 和 数据库字段名 要一致)
 * (通过set方法注入,需要有空参构造)
 */
public class jdbcConnection {
	private static String jdbcDriver;

    private static String jdbcUrl;

    private static String username;

    private static String password;
    
	private static Properties prop = new Properties();
	
	private static Connection connection = null;
	
	static {
		//读取jdbc配置文件
		InputStream in = jdbcConnection.class.getClassLoader().getResourceAsStream("jdbc.properties");
		try {
			prop.load(in);
		} catch (IOException e) {
			e.printStackTrace();
		}
		jdbcDriver = prop.getProperty("jdbcDriver");
        jdbcUrl = prop.getProperty("jdbcUrl");
        username = prop.getProperty("username");
        password = prop.getProperty("password");
        
		try {
			Class.forName(jdbcDriver);
			connection = DriverManager.getConnection(jdbcUrl, username, password);
		} catch (Exception e) {
			System.out.println("数据库链接异常");
		}
        //System.out.println(jdbcUrl);
	}
	/**
	 * 查询一条数据 
	 */
	public static <T> T queryOne(String classname,String sql) throws Exception{
		Object obj = Class.forName(classname).newInstance();
		//接收返回数据
		ResultSet result = executeSQL(sql);
		
		//封装数据
		obj = jdbcPackage.dataPackageOne(result, obj);
		
		return (T)obj;
	}
	/**
	 * 查询多条数据 
	 */
	public static <T> List<?> queryList(String classname,String sql) throws Exception{
		Object obj = Class.forName(classname).newInstance();
		
		ResultSet result = executeSQL(sql);
		
		
		//封装数据
		List<T> list = (List<T>) jdbcPackage.dataPackageList(result, obj);
		return list;
	}
	
	
	/**
	 * 执行查询语句方法
	 */
	public static ResultSet executeSQL(String sql){
		ResultSet result = null;
		try {
			PreparedStatement ps = connection.prepareStatement(sql);
			result = ps.executeQuery();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}
	
	//测试
	public static void main(String[] args) throws Exception {
		MR mr = queryOne("com.tao.demo.entity.MR","select * from mr limit 1");
		System.out.println(mr);
		
		/*List<MR>list = (List<MR>) queryList("com.tao.demo.entity.MR","select * from mr");
		for (MR mr : list) {
			System.out.println(mr);
		}*/
		//System.out.println(list);
	}
}

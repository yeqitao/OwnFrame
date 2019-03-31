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
 * ���װ�jdbc���(���� ������ �� ���ݿ��ֶ��� Ҫһ��)
 * (ͨ��set����ע��,��Ҫ�пղι���)
 */
public class jdbcConnection {
	private static String jdbcDriver;

    private static String jdbcUrl;

    private static String username;

    private static String password;
    
	private static Properties prop = new Properties();
	
	private static Connection connection = null;
	
	static {
		//��ȡjdbc�����ļ�
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
			System.out.println("���ݿ������쳣");
		}
        //System.out.println(jdbcUrl);
	}
	/**
	 * ��ѯһ������ 
	 */
	public static <T> T queryOne(String classname,String sql) throws Exception{
		Object obj = Class.forName(classname).newInstance();
		//���շ�������
		ResultSet result = executeSQL(sql);
		
		//��װ����
		obj = jdbcPackage.dataPackageOne(result, obj);
		
		return (T)obj;
	}
	/**
	 * ��ѯ�������� 
	 */
	public static <T> List<?> queryList(String classname,String sql) throws Exception{
		Object obj = Class.forName(classname).newInstance();
		
		ResultSet result = executeSQL(sql);
		
		
		//��װ����
		List<T> list = (List<T>) jdbcPackage.dataPackageList(result, obj);
		return list;
	}
	
	
	/**
	 * ִ�в�ѯ��䷽��
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
	
	//����
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

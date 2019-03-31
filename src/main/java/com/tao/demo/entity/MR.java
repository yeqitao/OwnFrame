package com.tao.demo.entity;

public class MR {
	private String userID;
	private String date;
	private String money;
	
	public MR() {
		super();
	}

	public MR(String userID, String date, String money) {
		super();
		this.userID = userID;
		this.date = date;
		this.money = money;
	}

	public String getUserID() {
		return userID;
	}

	public void setUserID(String userID) {
		this.userID = userID;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public String getMoney() {
		return money;
	}

	public void setMoney(String money) {
		this.money = money;
	}

	@Override
	public String toString() {
		return "MR [userID=" + userID + ", date=" + date + ", money=" + money + "]";
	}
	
	
}

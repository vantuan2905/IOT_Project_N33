package com.example.springmqttdemo.dao;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class notification {
	@Id
	private String _time;
	private String ten;
	private String mess;
	private boolean seen;
	public notification() {
		
	}
	
	public String getTen() {
		return ten;
	}

	public void setTen(String ten) {
		this.ten = ten;
	}

	public String getMess() {
		return mess;
	}
	public void setMess(String mess) {
		this.mess = mess;
	}
	public String get_time() {
		return _time;
	}
	public void set_time(String _time) {
		this._time = _time;
	}
	public boolean isSeen() {
		return seen;
	}
	public void setSeen(boolean seen) {
		this.seen = seen;
	}
	
}

package com.example.springmqttdemo.dao;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class time_lock {
	@Id
	private String _time;
	private String _status;
	public time_lock() {
		
	}
	public String get_time() {
		return _time;
	}
	public void set_time(String _time) {
		this._time = _time;
	}
	public String get_status() {
		return _status;
	}
	public void set_status(String _status) {
		this._status = _status;
	}
	
}

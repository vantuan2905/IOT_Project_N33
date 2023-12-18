package com.example.springmqttdemo.dao;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class repo_service {
	@Autowired
	private repo_notif r;
	@Autowired
	private repo_lock rl;
	public List<notification> getMess(){
		return r.findAll();
	}
	public void addMess(notification noti) {
		r.save(noti);
	}
	public void setSeen() {
		r.setSeen("lock");
	}
	public void addTime_lock(time_lock t) {
		rl.save(t);
	}
	public List<time_lock> gettime_lock(){
		return rl.findAll();
	}
}

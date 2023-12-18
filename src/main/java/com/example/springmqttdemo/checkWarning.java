package com.example.springmqttdemo;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.client.RestTemplate;

import com.example.springmqttdemo.dao.notification;
import com.example.springmqttdemo.dao.repo_service;
import com.example.springmqttdemo.model.MqttSubscribeModel;

public class checkWarning extends Thread{
	@Autowired
	repo_service r;
	public checkWarning(repo_service r) {
		this.r=r;
	}
	@Override
	public void run() {
		//r=new repo_service();
		RestTemplate rest=new RestTemplate();
		while(true) {
			System.out.println(1);
			 List<MqttSubscribeModel> mqttSubscribeModels = Arrays.asList(
		                rest.getForObject("http://localhost:8080/api/mqtt/subscribe?topic=B20DCCN614/warn_lock&wait_millis=2000",
		                        MqttSubscribeModel[].class));
		        System.out.println(mqttSubscribeModels.size());
		        String warn1= "No Data";
		        if (mqttSubscribeModels.size() >0) warn1 = mqttSubscribeModels.get(mqttSubscribeModels.size()-1).getMessage();
		        System.out.println(warn1);
		        if(warn1.equals("1")) {
		        	notification nf=new notification();
		        	 LocalDateTime now = LocalDateTime.now();
		        	 DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
		 		    String formattedDateTime = now.format(formatter);
		 		    nf.setTen("lock");
		        	nf.set_time(formattedDateTime);
		        	nf.setMess("nhập quá số lần giới hạn.Tạm thời vô hiệu hóa");
		        	nf.setSeen(true);
		        	r.addMess(nf);
		        	break;
		        }
		}
	}
	public static void main(String args[]) {		
		    LocalDateTime now = LocalDateTime.now();
		    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
		    String formattedDateTime = now.format(formatter);
		    System.out.println(formattedDateTime); 
		  //  new checkWarning().start();
	}
	
}

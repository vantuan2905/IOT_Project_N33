package com.example.springmqttdemo;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.client.RestTemplate;

import com.example.springmqttdemo.dao.notification;
import com.example.springmqttdemo.dao.repo_service;
import com.example.springmqttdemo.model.MqttSubscribeModel;

public class checkWarning_led extends Thread{
	LocalDateTime st;
	@Autowired
	repo_service r;
	public checkWarning_led(repo_service r,LocalDateTime st) {
		this.r=r;
		this.st=st;
	}
	@Override
	public void run() {
		RestTemplate rest=new RestTemplate();
		while(true) {
			 List<MqttSubscribeModel> mqttSubscribeModels = Arrays.asList(
		                rest.getForObject("http://localhost:8080/api/mqtt/subscribe?topic=B20DCCN614/led1&wait_millis=2000",
		                        MqttSubscribeModel[].class));
		       String warn="No data";
		       if (mqttSubscribeModels.size() >0) warn= mqttSubscribeModels.get(mqttSubscribeModels.size()-1).getMessage();
		        System.out.println(warn+"-----------");
		        if(warn.equals("OFF")) break;
		        LocalDateTime end=LocalDateTime.now();
				Duration dr=Duration.between(st, end);
				System.out.println(dr.getSeconds()+"----");
		        if(dr.getSeconds()>=30) {
		        	notification nf=new notification();
		        	 DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
		 		    String formattedDateTime = end.format(formatter);
		 		    nf.setTen("led");
		        	nf.set_time(formattedDateTime);
		        	nf.setMess("Quạt bật quá lâu.Cảnh báo quên không tắt");
		        	nf.setSeen(false);
		        	r.addMess(nf);
		        	break;
		        }
		}
	}
	public static void main(String args[]) throws InterruptedException {
		LocalDateTime st=LocalDateTime.now();
		Thread.sleep(1000);
		LocalDateTime end=LocalDateTime.now();
		Duration dr=Duration.between(st, end);
		System.out.println(dr.getSeconds());
	}
}

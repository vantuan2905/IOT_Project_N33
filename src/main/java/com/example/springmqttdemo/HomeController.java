package com.example.springmqttdemo;

import com.example.springmqttdemo.dao.notification;
import com.example.springmqttdemo.dao.repo_service;
import com.example.springmqttdemo.dao.time_lock;
import com.example.springmqttdemo.model.MqttPublishModel;
import com.example.springmqttdemo.model.MqttSubscribeModel;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Controller
@RequestMapping("/")
public class HomeController {
	private  int st=0;
	@Autowired
	repo_service rs;
    private RestTemplate rest = new RestTemplate();
    @ModelAttribute
    private void getData(Model model) {
    	//LED
        List<MqttSubscribeModel> mqttSubscribeModels = Arrays.asList(
                rest.getForObject("http://localhost:8080/api/mqtt/subscribe?topic=B20DCCN614/led1&wait_millis=2000",
                        MqttSubscribeModel[].class));
        System.out.println(mqttSubscribeModels.size());
        String led = "No Data";
        if (mqttSubscribeModels.size() >0) led = mqttSubscribeModels.get(mqttSubscribeModels.size()-1).getMessage();
        
        model.addAttribute("led1",led);
        //LOCK
        List<MqttSubscribeModel>  mqttSubscribeModels2 = Arrays.asList(
                rest.getForObject("http://localhost:8080/api/mqtt/subscribe?topic=B20DCCN614/lock&wait_millis=2000",
                        MqttSubscribeModel[].class));
        System.out.println("..."+mqttSubscribeModels2.size());
        String lock = "No Data";
        if (mqttSubscribeModels2.size() >0) lock = mqttSubscribeModels2.get(mqttSubscribeModels2.size()-1).getMessage();
        model.addAttribute("lock",lock);
        System.out.println(lock);
        if(this.st==0) {
        	st=1;
        	LocalDateTime now = LocalDateTime.now();
       	 	DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
		    String formattedDateTime = now.format(formatter);
		    time_lock t=new time_lock();
		    t.set_time(formattedDateTime);
		    t.set_status(lock);
		    rs.addTime_lock(t);
        }
    }
    @GetMapping
    public String home(Model model){
    	model.addAttribute("paint", "device");
    	
    	new checkWarning(rs).start();
        return "index";
    }
    @GetMapping("/device")
    public String device(Model model){
    	model.addAttribute("paint", "device");
    	return "index";
    }
    @GetMapping("/control")
    public String control(@RequestParam String device,Model model)  {
    	System.out.println(device);
    	MqttPublishModel mq=new MqttPublishModel();
    	String topic="B20DCCN614/"+device;
    	mq.setTopic(topic);
    	String msg="ON";
    	String lastmsg=(String) model.getAttribute(device);
    	if(lastmsg.equals("ON")) msg="OFF";
    	
    	mq.setMessage(msg);
    	mq.setQos(0);mq.setRetained(false);
    	System.out.println(lastmsg+"******************************");
    	rest.postForObject("http://localhost:8080/api/mqtt/publish",mq,MqttPublishModel.class);
    	getData(model);
    	model.addAttribute("paint", "device");
    	if(msg.equals("ON")&&device.equals("led1")) new checkWarning_led(rs, LocalDateTime.now()).start();
    	if(device.equals("lock")) {
    		LocalDateTime now = LocalDateTime.now();
       	 	DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
		    String formattedDateTime = now.format(formatter);
		    time_lock t=new time_lock();
		    t.set_time(formattedDateTime);
		    t.set_status(msg);
		    rs.addTime_lock(t);
    	};
    	return "index";
    }
    @GetMapping("/notification")
    public String notif(Model model) {
    	List<notification> ds=rs.getMess();
    	for(int i=0;i<ds.size();i++) System.out.println(ds.get(i).getMess());
    	Collections.sort(ds,new Comparator<notification>() {

			@Override
			public int compare(notification o1, notification o2) {
				String t1=o1.get_time();String t2=o2.get_time();
				if(t1.compareTo(t2)<0) return 1;
				return -1;
			}
    		
		});
    	System.out.println(ds.size());
    	model.addAttribute("ds_mess", ds);
    	model.addAttribute("paint", "notif");
    	return "notification";
    }
    @GetMapping("/off_warn")
    public String off_warn(Model model) {
    	//publish off warn
    	MqttPublishModel mq=new MqttPublishModel();
    	mq.setTopic("B20DCCN614/warn_lock");
    	mq.setMessage("0");
    	mq.setQos(0);mq.setRetained(false);
    	rest.postForEntity("http://localhost:8080/api/mqtt/publish", mq, MqttPublishModel.class);
    	//set seen==0 in database
    	rs.setSeen();
    	//return
    	List<notification> ds=rs.getMess();
    	for(int i=0;i<ds.size();i++) System.out.println(ds.get(i).getMess());
    	System.out.println(ds.size());
    	Collections.sort(ds,new Comparator<notification>() {

			@Override
			public int compare(notification o1, notification o2) {
				String t1=o1.get_time();String t2=o2.get_time();
				if(t1.compareTo(t2)<0) return 1;
				return -1;
			}
    		
		});
    	
    	model.addAttribute("ds_mess", ds);
    	model.addAttribute("paint", "notif");

    	new checkWarning(rs).start();
    	return "notification";
    }
    @GetMapping("/history")
    public String history(Model model) {
    	List<time_lock> ds_his=rs.gettime_lock();
    	Collections.sort(ds_his,new Comparator<time_lock>() {

			@Override
			public int compare(time_lock o1, time_lock o2) {
				if(o1.get_time().compareTo(o2.get_time())<0) {
					return 1;
				}
				return -1;
			}
		});
    	model.addAttribute("ds_his", ds_his);
    	model.addAttribute("paint", "his");
    	return "history";
    }
}

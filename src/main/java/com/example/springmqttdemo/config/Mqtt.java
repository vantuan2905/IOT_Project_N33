package com.example.springmqttdemo.config;

import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class Mqtt {

    private static final String MQTT_PUBLISHER_ID = "clientId-230sMbdJRJ";
    private static final String MQTT_SERVER_ADDRES= "tcp://broker.hivemq.com:1883";
    private static IMqttClient instance;

    public static IMqttClient getInstance() {
        try {
            if (instance == null) {
                instance = new MqttClient(MQTT_SERVER_ADDRES, MQTT_PUBLISHER_ID);
            }
            
            MqttConnectOptions options = new MqttConnectOptions();
            options.setAutomaticReconnect(true);
            options.setCleanSession(true);
            options.setConnectionTimeout(10);
            //System.out.println(instance.isConnected());
//       //    if(instance.isConnected()) {
//            	MqttMessage message = new MqttMessage();
//                message.setPayload("Hello world from Java".getBytes());
//                instance.publish("B20DCCN614/temp", message);
//                System.out.println("pp");
//            }
            if (!instance.isConnected()) {
                instance.connect(options);
            }
        } catch (MqttException e) {
            e.printStackTrace();
        }

        return instance;
    }

    private Mqtt() {

    }
}
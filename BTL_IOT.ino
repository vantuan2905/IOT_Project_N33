#include <Keypad.h>
#include <LiquidCrystal_I2C.h>
#include <ESP32Servo.h>
#include <WiFi.h>
#include <PubSubClient.h>

#define RELAY_PIN 15 // ESP32 pin GPIO16 connected to the IN pin of relay

#define ROW_NUM     4 // four rows
#define COLUMN_NUM  4 // four columns
LiquidCrystal_I2C lcd= LiquidCrystal_I2C(0x27,16,2);

#define SERVO_PIN 2 // ESP32 pin GPIO26 connected to servo motor
String led1_status="OFF";
String lock_status="OFF";
unsigned long lastMsg = 0;
Servo servoMotor;
int8_t n=3;
const char* ssid = "MinhNgoc";
const char* password_wf = "19052017";
const char* mqtt_server = "broker.hivemq.com";
WiFiClient espClient;
PubSubClient client(espClient);
char keys[ROW_NUM][COLUMN_NUM] = {
  {'1', '2', '3', 'A'},
  {'4', '5', '6', 'B'},
  {'7', '8', '9', 'C'},
  {'*', '0', '#', 'D'}
};

byte pin_rows[ROW_NUM]      = {33, 25, 26, 14}; // GPIO19, GPIO18, GPIO5, GPIO17 connect to the row pins
byte pin_column[COLUMN_NUM] = {27, 13, 18, 4};
Keypad keypad = Keypad( makeKeymap(keys), pin_rows, pin_column, ROW_NUM, COLUMN_NUM );
String input_password;
String password="12345";

void unlock(){
  for (int pos = 65; pos <= 85; pos += 1) { // goes from 0 degrees to 180 degrees
    // in steps of 1 degree
    servoMotor.write(pos);              // tell servo to go to position in variable 'pos'
                  // waits 15ms for the servo to reach the position
  }
}
void lock(){
   for (int pos = 95; pos >= 65; pos -= 1) { // goes from 180 degrees to 0 degrees
    servoMotor.write(pos);              // tell servo to go to position in variable 'pos'
                        // waits 15ms for the servo to reach the position
  }

}

void setup_wifi() { 
  delay(10);
  // We start by connecting to a WiFi network
  Serial.println();
  Serial.print("Connecting to ");
  Serial.println(ssid);

  WiFi.mode(WIFI_STA); 
  WiFi.begin(ssid, password_wf); 

  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print(".");
  }

  randomSeed(micros());

  Serial.println("");
  Serial.println("WiFi connected");
  Serial.println("IP address: ");
  Serial.println(WiFi.localIP());
}


void callback(char* topic, byte* payload, unsigned int length) {
  String msg; 
  Serial.print("Message arrived [");
  Serial.print(topic);
  Serial.print("] ");
  for (int i = 0; i < length; i++) { 
    //Serial.print((char)payload[i]);
    msg += (char)payload[i];
  }
  Serial.println(msg+" "+led1_status.c_str()+" "+lock_status.c_str()+"  "+strcmp(msg.c_str(),led1_status.c_str())+" "+strcmp(msg.c_str(),lock_status.c_str()));

  // Switch on the LED if temprature > 20
  if (strcmp(topic, "B20DCCN614/warn_lock")==0){
      if(strcmp(msg.c_str(),"0")==0) n=2;
  }
  if (strcmp(topic, "B20DCCN614/led1")==0){
    if(strcmp(msg.c_str(),led1_status.c_str())!=0){
      led1_status=msg;
      if(strcmp(msg.c_str(),"ON")==0){
        digitalWrite(RELAY_PIN,HIGH);
      }else{
        digitalWrite(RELAY_PIN,LOW);
      }
    }
  }
  if (strcmp(topic, "B20DCCN614/lock")==0){
    if(strcmp(msg.c_str(),lock_status.c_str())!=0){
      lock_status=msg;
      if(strcmp(msg.c_str(),"ON")!=0){
        unlock();
      }else{
        lock();
      }
    }
  }
  
}

void reconnect() { 
  // Loop until we're reconnected
  while (!client.connected()) {
    Serial.print("Attempting MQTT connection...");
    String clientId = "ESP32Client-";
    clientId += String(random(0xffff), HEX);
    // Attempt to connect
    if (client.connect(clientId.c_str())) {
      Serial.println("Connected to " + clientId);
      // Once connected, publish an announcement...
      client.publish("B20DCCN614/mqtt", "PTIT_Test"); 
      client.subscribe("B20DCCN614/warn_lock"); 
      // ... and resubscribe
      client.subscribe("B20DCCN614/lock"); 
      client.subscribe("B20DCCN614/led1");
    } else {
      Serial.print("failed, rc=");
      Serial.print(client.state());
      Serial.println(" try again in 5 seconds");
      // Wait 5 seconds before retrying
      delay(2000);
    }
  }
}


void setup() {
  // put your setup code here, to run once:
  Serial.begin(19200);
  Serial.println("Hello, ESP32!");
  setup_wifi(); 
  client.setServer(mqtt_server, 1883); 
  client.setCallback(callback);
  lcd.init();
  lcd.backlight();
  lcd.setCursor(0,0);
  lcd.println("enter password:");
  pinMode(RELAY_PIN, OUTPUT);
  servoMotor.attach(SERVO_PIN);

  digitalWrite(RELAY_PIN, LOW);
   pinMode(35, INPUT);//ligh sensor
  pinMode(23,OUTPUT);//led
}


void loop() {
  if (!client.connected()) {
    reconnect();
  }
  client.loop();
  char key = keypad.getKey();
   unsigned long now = millis();
  if (now - lastMsg > 2000) { 
    lastMsg = now;
    client.publish("B20DCCN614/lock", lock_status.c_str());
    client.publish("B20DCCN614/led1", led1_status.c_str());
    if(n==0){
      client.publish("B20DCCN614/warn_lock", "1");
       lcd.setCursor(0,0);
      lcd.println("                         ");
       lcd.setCursor(0,1);
      lcd.println("temporary lock"); 
    }
    }
  
  if (key&&n>0) {
    Serial.println(key);
    if(key=='D'){
       lcd.setCursor(0,1);
        lcd.println("                         ");
    }else{
    if (key == '*') {
      input_password = ""; // clear input password
    } else if (key == '#') {
          if (password == input_password) {
            Serial.print("unlock");
            lcd.setCursor(0,1);
            lcd.println("unlock");
            unlock();
          } else {
            n--;
            lcd.setCursor(0,0);
            lcd.println("                         ");
            lcd.setCursor(0,1);
            lcd.println("incorrect");
            delay(1000);
            lcd.clear();
            lcd.setCursor(0,0);
            lcd.println("enter your password:");
          }

          input_password = ""; // clear input password
        } else {
            input_password += key;
            lcd.setCursor(0,1);
            lcd.println(input_password);
          // append new character to input password string
        }
      }
  }
  delay(10); // this speeds up the simulation
  int gt=digitalRead(35);
  //Serial.println(gt);
  if(gt==HIGH)
  digitalWrite(23,HIGH);
  else digitalWrite(23,LOW);
}

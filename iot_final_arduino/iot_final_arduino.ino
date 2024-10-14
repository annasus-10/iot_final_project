#include <Arduino.h>
#include <WiFi.h>
#include <Firebase_ESP_Client.h>
#include <addons/TokenHelper.h>
#include <addons/RTDBHelper.h>

#define WIFI_SSID     "Susan"       
#define WIFI_PASSWORD "64301830"  
#define API_KEY       "AIzaSyBmDEh-prpoRs2tC9nagTMT4AD6Pkieg1Y" 
#define DATABASE_URL  "https://iot-project-b6673-default-rtdb.asia-southeast1.firebasedatabase.app/" 

FirebaseData fbdo;
FirebaseAuth auth;
FirebaseConfig config;

bool t1Value = true;
bool t2Value = true;

int pinLED = 21;
int pinFan1 = 12;
int pinFan2 = 13;
int pinLamb = 15;
int pinPV = 36;
int setpointValue;
int highAlarmValue;
int lowAlarmValue;
float PV = 0.0;

unsigned long previousMillis1 = 0;
unsigned long previousMillis2 = 0;
const long interval1 = 1000;  // 1 second
const long interval2 = 100;   // 0.1 second

void setup() {
  Serial.begin(115200);

  // Set pin modes
  pinMode(pinFan1, OUTPUT);
  pinMode(pinFan2, OUTPUT);
  pinMode(pinLamb, OUTPUT);
  pinMode(pinLED, OUTPUT);
  pinMode(pinPV, INPUT);
  
  digitalWrite(pinFan1, HIGH);
  digitalWrite(pinFan2, HIGH);
  digitalWrite(pinLamb, LOW);
  
  // Connect to Wi-Fi
  WiFi.begin(WIFI_SSID, WIFI_PASSWORD);
  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print(".");
  }
  Serial.println("Connected to WiFi");

  // Firebase setup
  config.api_key = API_KEY;
  config.database_url = DATABASE_URL;
  Firebase.signUp(&config, &auth, "", "");
  config.token_status_callback = tokenStatusCallback; 
  Firebase.begin(&config, &auth);
  Firebase.reconnectNetwork(true);
}

void loop() {
  // Current time in milliseconds
  unsigned long currentMillis = millis();

  // Check if 1 second passed for task1 (timer1)
  if (currentMillis - previousMillis1 >= interval1) {
    previousMillis1 = currentMillis;
    t1Value = !t1Value;
    Serial.println("Task 1 executed.");
  }

  // Check if 0.1 second passed for task2 (timer2)
  if (currentMillis - previousMillis2 >= interval2) {
    previousMillis2 = currentMillis;
    t2Value = !t2Value;
    Serial.print("t1Value: ");
    Serial.print(t1Value);
    Serial.print(", t2Value: ");
    Serial.println(t2Value);
    Serial.print("PV: ");
    Serial.println(PV);  // Assuming PV is updated elsewhere in your loop
  }

  // Read sensor value
  float PVvolt = analogRead(pinPV) * (3.3 / 4095.0) ;
  float in_min = 0.1;
  float in_max = 1.74;
  float out_min = -40.0;
  float out_max = 125.0;
  
  PV = ((PVvolt - in_min) / (in_max - in_min)) * (out_max - out_min) + out_min;
  
  if (Firebase.ready()) {
    if (Firebase.RTDB.getInt(&fbdo, "/setpointValue")) {
      setpointValue = fbdo.intData();
    } else {
      Serial.println("Failed to get /setpointValue, error: " + fbdo.errorReason());
    }

    if (Firebase.RTDB.getInt(&fbdo, "/highAlarmValue")) {
      highAlarmValue = fbdo.intData();
    } else {
      Serial.println("Failed to get /highAlarmValue, error: " + fbdo.errorReason());
    }

    if (Firebase.RTDB.getInt(&fbdo, "/lowAlarmValue")) {
      lowAlarmValue = fbdo.intData();
    } else {
      Serial.println("Failed to get /lowAlarmValue, error: " + fbdo.errorReason());
    }

    bool timer = t1Value;
    if (PV > highAlarmValue || PV < lowAlarmValue) {
      timer = t2Value;
    }
    digitalWrite(pinLED, timer);

    // Update Firebase with current value
    Firebase.RTDB.setInt(&fbdo, "/currentValue", PV);  
  }
}

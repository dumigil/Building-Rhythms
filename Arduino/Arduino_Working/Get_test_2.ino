#include <WiFiNINA.h>       // use this for MKR1010 and Nano 33 IoT boards
#include <ArduinoHttpClient.h>
#include <RTCZero.h>
#include <ArduinoJson.h>
#include "arduino_secrets_thuis.h"
//#include "arduino_secrets.h"

WiFiSSLClient netSocket;               // network socket to server
const char server[] PROGMEM = "services3.arcgis.com";  // server name
const char route[] PROGMEM= "/jR9a3QtlDyTstZiO/ArcGIS/rest/services/Arduino_Table/FeatureServer/0/addFeatures";              // API route

RTCZero rtc;
const byte seconds = 0;
const byte minutes = 31;
const byte hours = 13;

/* Change these values to set the current initial date */
const byte day = 1;
const byte month = 6;
const byte year = 21;

// request timestamp in ms:
long lastRequest = 0;
// interval between requests:
int interval = 10000;
int status = WL_IDLE_STATUS;     // the WiFi radio's status
HttpClient http(netSocket, server, 443); 


void setup() {
  Serial.begin(9600);               // initialize serial communication
  //while (!Serial);        // wait for serial monitor to open
  rtc.begin();
  rtc.setTime(hours, minutes, seconds);
  rtc.setDate(day, month, year);
  // while you're not connected to a WiFi AP,
  while ( WiFi.status() != WL_CONNECTED) {
    Serial.print("Attempting to connect to Network named: ");
    Serial.println(SECRET_SSID);

    //at home
    WiFi.begin(SECRET_SSID, SECRET_PASS);
    // at uni
    //WiFi.beginEnterprise(SECRET_SSID,SECRET_USER,SECRET_PASS);         // try to connect
    delay(1000);
  }

  // When you're connected, print out the device's network status:
  IPAddress ip = WiFi.localIP();
  //Serial.print("IP Address: ");
  //Serial.println(ip);
}
void loop() {
  WiFi.end();
  String dummyData = listNetworks();
  Serial.println("Wifi scan complete, starting eduroam timeout");
  WiFi.end();
  delay(1000);
  while ( WiFi.status() != WL_CONNECTED) {
    Serial.print("Attempting to connect to Network named: ");
    Serial.println(SECRET_SSID);

    //at home
    WiFi.begin(SECRET_SSID, SECRET_PASS);
    // at uni
    //WiFi.beginEnterprise(SECRET_SSID,SECRET_USER,SECRET_PASS);         // try to connect
    delay(1000);
  }
  
  if (millis() - lastRequest > interval ) {
    Serial.println("making request");
    //http.get(getRoute);  // make a GET request
    String contentType = "application/x-www-form-urlencoded";
    http.post(route, contentType, dummyData);

    while (http.connected()) {       // while connected to the server,
      if (http.available()) {        // if there is a response from the server,
        String result = http.readString();  // read it
        Serial.print(result);               // and print it
      }
    }
    //  // when there's nothing left to the response,
    http.stop();                     // close the request
    lastRequest = millis();

  }
  //Serial.println(freeMemory());
  
}

String listNetworks() {
  String dataString = "";
  const char room []PROGMEM= "Oost_Serre";
  int numSsid = WiFi.scanNetworks();
  if (numSsid == -1)
  {
    Serial.println("Couldn't get a WiFi connection");
    while (true);
  }
   String comma = "";
   
  dataString += "features=[";
  // print the network number and name for each network found:
  for (int thisNet = 0; thisNet < numSsid; thisNet++) {
    if(String(WiFi.SSID(thisNet)) == "eduroam" || 
    String(WiFi.SSID(thisNet)) == "tudelft-dastud" || 
    String(WiFi.SSID(thisNet)) == "Delft Free Wifi" ||
    String(WiFi.SSID(thisNet)) == "TUvisitor"){
      DynamicJsonDocument doc(2048);
      byte bssid[6];
      String address = printMacAddress(WiFi.BSSID(thisNet, bssid));
      String measurement = "";
      dataString += comma;
      doc["attributes"]["RSSI"] = WiFi.RSSI(thisNet);
      doc["attributes"]["MAC"] = String(address);
      doc["attributes"]["Room_ID"] = room;
      doc["attributes"]["Time_Stamp"] = String(rtc.getEpoch());
      doc["attributes"]["BSSID"] = String(WiFi.SSID(thisNet));
      serializeJson(doc, measurement);
      dataString+=measurement;
      comma = ",";
    }
    else{
      continue;
    }
    
  }
  dataString += "]";
  
  return dataString;
}

String print2digits(int number) {
  if (number < 10) {
    return "0" + String(number);
  }
  return String(number);
}

String printMacAddress(byte mac[]) {
  String address;
  for (int i = 5; i >= 0; i--) {
    if (mac[i] < 16) {
      address += "0";
    }
    //Serial.print(mac[i], HEX);
    address += String(mac[i], HEX);
    if (i > 0) {
      address += ":";
    }
  }
  return address;
}

#ifdef __arm__
// should use uinstd.h to define sbrk but Due causes a conflict
extern "C" char* sbrk(int incr);
#else  // __ARM__
extern char *__brkval;
#endif  // __arm__

int freeMemory() {
  char top;
#ifdef __arm__
  return &top - reinterpret_cast<char*>(sbrk(0));
#elif defined(CORE_TEENSY) || (ARDUINO > 103 && ARDUINO != 151)
  return &top - __brkval;
#else  // __arm__
  return __brkval ? &top - __brkval : &top - __malloc_heap_start;
#endif  // __arm__
}

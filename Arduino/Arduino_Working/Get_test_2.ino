#include <WiFiNINA.h>       // use this for MKR1010 and Nano 33 IoT boards
#include <ArduinoHttpClient.h>
#include <RTCZero.h>
#include <ArduinoJson.h>
#include "arduino_secrets_thuis.h"

WiFiSSLClient netSocket;               // network socket to server
const char server[] = "services3.arcgis.com";  // server name
String route = "/jR9a3QtlDyTstZiO/ArcGIS/rest/services/Arduino_Table/FeatureServer/0/addFeatures";              // API route
String getRoute = "/jR9a3QtlDyTstZiO/ArcGIS/rest/services/Arduino_Table/FeatureServer/info/itemInfo?f=pjson";              // API route


// request timestamp in ms:
long lastRequest = 0;
long lastScanMillis = 0;
// interval between requests:
int interval = 10000;
HttpClient http(netSocket, server, 443);


void setup() {
  Serial.begin(9600);               // initialize serial communication
  while (!Serial);        // wait for serial monitor to open

  // while you're not connected to a WiFi AP,
  while ( WiFi.status() != WL_CONNECTED) {
    Serial.print("Attempting to connect to Network named: ");
    Serial.println(SECRET_SSID);

    //at home
    WiFi.begin(SECRET_SSID, SECRET_PASS);
    // at uni
    //WiFi.beginEnterprise(SECRET_SSID,SECRET_USER,SECRET_PASS);         // try to connect
    delay(2000);
  }

  // When you're connected, print out the device's network status:
  IPAddress ip = WiFi.localIP();
  Serial.print("IP Address: ");
  Serial.println(ip);
}
void loop() {
  String dummyData = listNetworks();
  WiFi.end();
  delay(1000);
  while ( WiFi.status() != WL_CONNECTED) {
    Serial.print("Attempting to connect to Network named: ");
    Serial.println(SECRET_SSID);

    //at home
    WiFi.begin(SECRET_SSID, SECRET_PASS);
    // at uni
    //WiFi.beginEnterprise(SECRET_SSID,SECRET_USER,SECRET_PASS);         // try to connect
    delay(2000);
  }

  
  if (millis() - lastRequest > interval ) {
    Serial.println("making request");
    //HttpClient http(netSocket, server, 443); 
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
  
  
}

String listNetworks() {
  String dataString = "";
  String room= "Michiels_Room";
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
    DynamicJsonDocument doc(2048);
    byte bssid[6];
    String address = printMacAddress(WiFi.BSSID(thisNet, bssid));
    String measurement = "";
    dataString += comma;
    doc["attributes"]["RSSI"] = String(WiFi.RSSI(thisNet));
    doc["attributes"]["MAC"] = String(address);
    doc["attributes"]["Room_ID"] = room;
    doc["attributes"]["Time_Stamp"] = String(WiFi.getTime());
    doc["attributes"]["BSSID"] = String(WiFi.SSID(thisNet));
    serializeJson(doc, measurement);
    dataString+=measurement;
    comma = ",";
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

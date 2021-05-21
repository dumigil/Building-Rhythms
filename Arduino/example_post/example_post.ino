#include <RTCZero.h>
#include <ArduinoHttpClient.h>
#include <WiFiNINA.h>     // use this for MKR1010 or Nano 33 IoT
#include <Arduino_JSON.h>
#include "arduino_secrets.h"



WiFiClient netSocket;               // network socket to server
const char server[] = "https://services3.arcgis.com/jR9a3QtlDyTstZiO/ArcGIS/rest/services";  // server name

String route = "/get";// API route
char ssid[] = SECRET_SSID;  // your WPA2 enterprise network SSID (name)
char user[] = SECRET_USER;  // your WPA2 enterprise username
char pass[] = SECRET_PASS;  // your WPA2 enterprise password
// request timestamp in ms:
long lastRequest = 0;
// interval between requests:
int interval = 10000;
String features[32];
RTCZero rtc;


void setup() {
  Serial.begin(9600);               // initialize serial communication
  while (!Serial);        // wait for serial monitor to open
  
  rtc.begin(); // initialize RTC
  rtc.setEpoch(1451606400); // Jan 1, 2016

  // while you're not connected to a WiFi AP,
  while ( WiFi.status() != WL_CONNECTED) {
    Serial.print("Attempting to connect to Network named: ");
    Serial.println(ssid);           // print the network name (SSID)
    WiFi.begin(ssid, pass);         // try to connect
    delay(2000);
  }

    

  // When you're connected, print out the device's network status:
  IPAddress ip = WiFi.localIP();
  Serial.print("IP Address: ");
  Serial.println(ip);

}

void loop() {
  if (millis() - lastRequest > interval ) {
    listNetworks();
    Serial.println("making request");
    HttpClient http(netSocket, server, 80);      // make an HTTP client
    http.post(route, data);  // make a GET request



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
    
  

void listNetworks() {
  JSONVar logElement;
  String room= "ROOM.X";
  String macList;
  Serial.println("** Scan Networks **");
  int numSsid = WiFi.scanNetworks();
  if (numSsid == -1)
  {
    Serial.println("Couldn't get a WiFi connection");
    while (true);
  }


  //construct timestamp
   String timeStamp = "";
   timeStamp += rtc.getDay();
   timeStamp += rtc.getMonth();
   timeStamp += rtc.getYear();

   timeStamp += print2digits(rtc.getHours());
   timeStamp += print2digits(rtc.getMinutes());
   timeStamp += print2digits(rtc.getSeconds());

  // print the list of networks seen:
  Serial.print("number of available networks: ");
  Serial.println(numSsid);

  // print the network number and name for each network found:
  for (int thisNet = 0; thisNet < numSsid; thisNet++) {
    byte bssid[6];
    String address = printMacAddress(WiFi.BSSID(thisNet, bssid));
    logElement["rssi"] = (WiFi.RSSI(thisNet));
    logElement["mac"]=address;
    logElement["unit"]=room;
    logElement["timestamp"]=timeStamp;
    features[thisNet]=JSON.stringify(logElement);

    Serial.println(features[thisNet]);

  }

}

String print2digits(int number) {
  if (number < 10) {
    return "0" + String(number);
  }
  return String(number);
}

void printEncryptionType(int thisType) {
  // read the encryption type and print out the name:
  switch (thisType) {
    case ENC_TYPE_WEP:
      Serial.println("WEP");
      break;
    case ENC_TYPE_TKIP:
      Serial.println("WPA");
      break;
    case ENC_TYPE_CCMP:
      Serial.println("WPA2");
      break;
    case ENC_TYPE_NONE:
      Serial.println("None");
      break;
    case ENC_TYPE_AUTO:
      Serial.println("Auto");
      break;
    case ENC_TYPE_UNKNOWN:
    default:
      Serial.println("Unknown");
      break;
  }
}

String printMacAddress(byte mac[]) {
  String address;
  for (int i = 5; i >= 0; i--) {
    if (mac[i] < 16) {
      //Serial.print("0");
      address += "0";
    }
    //Serial.print(mac[i], HEX);
    address += String(mac[i], HEX);
    if (i > 0) {
      //Serial.print(":");
      address += ":";
    }
  }
  //Serial.println();
  return address;
}

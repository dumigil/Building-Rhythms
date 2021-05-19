#include <ArduinoHttpClient.h>
#include <WiFiNINA.h>     // use this for MKR1010 or Nano 33 IoT
#include <Arduino_JSON.h>
#include "arduino_secrets.h"



WiFiClient netSocket;               // network socket to server
const char server[] = "services3.arcgis.com";  // server name
String route = "/jR9a3QtlDyTstZiO/ArcGIS/rest/services/fingerprint_vals/FeatureServer/info?f=pjson";// API route
char ssid[] = SECRET_SSID;  // your WPA2 enterprise network SSID (name)
char user[] = SECRET_USER;  // your WPA2 enterprise username
char pass[] = SECRET_PASS;  // your WPA2 enterprise password
// request timestamp in ms:
long lastRequest = 0;
// interval between requests:
int interval = 10000;
String features;


void setup() {
  Serial.begin(9600);               // initialize serial communication
  while (!Serial);        // wait for serial monitor to open

  // while you're not connected to a WiFi AP,
  while ( WiFi.status() != WL_CONNECTED) {
    Serial.print("Attempting to connect to Network named: ");
    Serial.println(ssid);           // print the network name (SSID)
    WiFi.beginEnterprise(ssid, user, pass);         // try to connect
    delay(2000);
  }

    

  // When you're connected, print out the device's network status:
  IPAddress ip = WiFi.localIP();
  Serial.print("IP Address: ");
  Serial.println(ip);

}

void loop() {
  int statusCode = 0;
  String contentType = "application/json";
  JSONVar fingerprint = listNetworks();
  String fingerprintString = (JSON.stringify(fingerprint));
  features = String("features=[")+fingerprintString+String("]");
  Serial.println(features);
  if (millis() - lastRequest > interval ) {
    Serial.println("making request");
    HttpClient http(netSocket, server, 443);      // make an HTTP client
    http.get(route);  // make a GET request

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
JSONVar listNetworks() {
  JSONVar list;
  String macList;
  Serial.println("** Scan Networks **");
  int numSsid = WiFi.scanNetworks();
  if (numSsid == -1)
  {
    Serial.println("Couldn't get a WiFi connection");
    while (true);
  }

  // print the list of networks seen:
  Serial.print("number of available networks: ");
  Serial.println(numSsid);

  // print the network number and name for each network found:
  for (int thisNet = 0; thisNet < numSsid; thisNet++) {

    byte bssid[6];
    String address = printMacAddress(WiFi.BSSID(thisNet, bssid));
    list["attributes"][address] = (WiFi.RSSI(thisNet));
    list["geometry"]["x"]=1;
    list["geometry"]["y"]=1;


  }
  return list;
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

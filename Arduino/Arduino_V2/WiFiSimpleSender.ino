
#include <WiFiNINA.h>       // use this for MKR1010 and Nano 33 IoT boards
#include <ArduinoHttpClient.h>
#include "arduino_secrets.h"

WiFiSSLClient netSocket;               // network socket to server
const char server[] = "services3.arcgis.com";  // server name
String route = "/jR9a3QtlDyTstZiO/ArcGIS/rest/services/Arduino_Table/FeatureServer/info/itemInfo?f=pjson";              // API route
// request timestamp in ms:
long lastRequest = 0;
// interval between requests:
int interval = 10000;

void setup() {
  Serial.begin(9600);               // initialize serial communication
  while (!Serial);        // wait for serial monitor to open

  // while you're not connected to a WiFi AP,
  while ( WiFi.status() != WL_CONNECTED) {
    Serial.print("Attempting to connect to Network named: ");
    Serial.println(SECRET_SSID);           // print the network name (SSID)
    WiFi.beginEnterprise(SECRET_SSID,SECRET_USER,SECRET_PASS);         // try to connect
    delay(2000);
  }

  // When you're connected, print out the device's network status:
  IPAddress ip = WiFi.localIP();
  Serial.print("IP Address: ");
  Serial.println(ip);
}

void loop() {
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

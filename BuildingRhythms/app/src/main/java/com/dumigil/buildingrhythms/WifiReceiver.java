package com.dumigil.buildingrhythms;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

class WifiReceiver extends BroadcastReceiver {
    WifiManager wifiManager;
    StringBuilder sb;
    public JSONObject wifiResultList = new JSONObject();
    public ArrayList<knn_methods> knn_test_objs  = new ArrayList<>();
    ListView wifiDeviceList;

    public WifiReceiver(WifiManager wifiManager, ListView wifiDeviceList) {
        this.wifiManager = wifiManager;
        this.wifiDeviceList = wifiDeviceList;
    }


    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (WifiManager.SCAN_RESULTS_AVAILABLE_ACTION.equals(action)) {
            sb = new StringBuilder();
            List<ScanResult> wifiList = wifiManager.getScanResults();
            ArrayList<String> deviceList = new ArrayList<>();
            for (ScanResult scanResult : wifiList) {
                JSONObject currResult = new JSONObject();
                sb.append("\n").append(scanResult.SSID).append(" - ").append(scanResult.capabilities);
                deviceList.add(scanResult.SSID + " - " + scanResult.capabilities);
                //Log.d("WIFI_RESULT",scanResult.BSSID + ": " + scanResult.level);
                try {
                    currResult.put("MAC", scanResult.BSSID);
                    currResult.put("RSSI", scanResult.level);
                    wifiResultList.put("attributes", currResult);
                    // make knn_methods object here itself

                    String wifiName1 = "eduroam";
                    String wifiName2 = "TUvisitor";
                    String wifiName3 = "Delft Free Wifi";
                    String wifiName4 = "tudelft-dastud";

                    if(scanResult.SSID.equals(wifiName1) || scanResult.SSID.equals(wifiName2) || scanResult.SSID.equals(wifiName3) || scanResult.SSID.equals(wifiName4) )
                    {
                        knn_methods knnObj = new knn_methods(scanResult.BSSID, Double.parseDouble( String.valueOf(scanResult.level)) );
                        knn_test_objs.add(knnObj);
                    }


                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            //Log.d("WIFI_RESULT_JSON",wifiResultList.toString());
            //Toast.makeText(context, sb, Toast.LENGTH_SHORT).show();
            ArrayAdapter arrayAdapter = new ArrayAdapter(context, android.R.layout.simple_list_item_1, deviceList.toArray());
            wifiDeviceList.setAdapter(arrayAdapter);
        }
    }

    public JSONObject getWifiResultList()
    {
        return wifiResultList;
    }

    public ArrayList<knn_methods> getKnn_test_objs()
    {
        return knn_test_objs;
    }
}
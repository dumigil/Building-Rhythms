package com.example.buildingrhythms;

import androidx.appcompat.app.AppCompatActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.esri.arcgisruntime.data.ServiceFeatureTable;
import com.esri.arcgisruntime.layers.FeatureLayer;
import com.esri.arcgisruntime.mapping.ArcGISScene;
import com.esri.arcgisruntime.layers.ArcGISSceneLayer;
import com.esri.arcgisruntime.ArcGISRuntimeEnvironment;

import com.esri.arcgisruntime.mapping.ArcGISTiledElevationSource;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.view.Camera;
import com.esri.arcgisruntime.mapping.view.SceneView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private SceneView mSceneView;
    WifiManager wifi;
    private ListView lv;
    private Button buttonScan;
    private int size = 0;
    private List<ScanResult> results;

    private ArrayList<String> arrayList = new ArrayList<>();
    private ArrayAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ArcGISRuntimeEnvironment.setApiKey("AAPKc0448a01c7d842c18fbdae5643d6ed65IgCmfZc9-fdYJPxtPca3UEp8Ft9V0zOulMBtumgxLfXxyG0wPadIluSBowA0JKCD");

        ArcGISScene scene = new ArcGISScene();
        scene.setBasemap(Basemap.createOpenStreetMap());

        mSceneView = (SceneView) findViewById(R.id.sceneView);
        mSceneView.setScene(scene);

        ServiceFeatureTable serviceFeatureTable = new ServiceFeatureTable("https://services3.arcgis.com/jR9a3QtlDyTstZiO/arcgis/rest/services/BK_MAP_WFL1/FeatureServer/4");
        serviceFeatureTable.setFeatureRequestMode(ServiceFeatureTable.FeatureRequestMode.ON_INTERACTION_NO_CACHE);
        FeatureLayer featureLayer = new FeatureLayer(serviceFeatureTable);

        scene.getOperationalLayers().add(featureLayer);

        ArcGISTiledElevationSource elevationSource = new ArcGISTiledElevationSource(
                "http://elevation3d.arcgis.com/arcgis/rest/services/WorldElevation3D/Terrain3D/ImageServer");
        scene.getBaseSurface().getElevationSources().add(elevationSource);

        Camera camera = new Camera(52.0060971, 4.3701068, 1000.0, 0.00, 0.00, 0.0);
        mSceneView.setViewpointCamera(camera);


        buttonScan = (Button) findViewById(R.id.startScan);
        buttonScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view){
                scanWifi();
            }
        });

        lv = (ListView)findViewById(R.id.listView);
        wifi = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        if(!wifi.isWifiEnabled()){
            Toast.makeText(this, "Wifi is disable -- please enable it", Toast.LENGTH_LONG).show();
            wifi.setWifiEnabled(true);
        }
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, arrayList);
        lv.setAdapter(adapter);
        scanWifi();
    }
    private void scanWifi() {
        arrayList.clear();
        registerReceiver(wifiReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        wifi.startScan();
        Toast.makeText(this, "Scanning WiFi ...", Toast.LENGTH_SHORT).show();
    }
    BroadcastReceiver wifiReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            results = wifi.getScanResults();
            unregisterReceiver(this);
            Log.d("RECEIVER", "We have a receiver");

            for (ScanResult scanResult : results) {
                arrayList.add(scanResult.SSID + " - " + scanResult.capabilities);
                adapter.notifyDataSetChanged();
                Log.d("RESULT", "We have some scan results");
            }
        }
    };
}
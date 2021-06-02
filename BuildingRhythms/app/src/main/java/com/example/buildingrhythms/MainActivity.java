package com.example.buildingrhythms;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.esri.arcgisruntime.concurrent.ListenableFuture;
import com.esri.arcgisruntime.data.Feature;
import com.esri.arcgisruntime.data.FeatureQueryResult;
import com.esri.arcgisruntime.data.QueryParameters;
import com.esri.arcgisruntime.data.ServiceFeatureTable;
import com.esri.arcgisruntime.geometry.Envelope;
import com.esri.arcgisruntime.layers.FeatureLayer;
import com.esri.arcgisruntime.mapping.ArcGISScene;
import com.esri.arcgisruntime.layers.ArcGISSceneLayer;
import com.esri.arcgisruntime.ArcGISRuntimeEnvironment;

import com.esri.arcgisruntime.mapping.ArcGISTiledElevationSource;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.TimeExtent;
import com.esri.arcgisruntime.mapping.view.Camera;
import com.esri.arcgisruntime.mapping.view.LayerSceneProperties;
import com.esri.arcgisruntime.mapping.view.SceneView;
import com.esri.arcgisruntime.symbology.Renderer;
import com.esri.arcgisruntime.symbology.SimpleFillSymbol;
import com.esri.arcgisruntime.symbology.SimpleLineSymbol;
import com.esri.arcgisruntime.symbology.SimpleRenderer;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private SceneView mSceneView;
    WifiManager wifiManager;
    private ListView wifiList;
    private JSONArray resultList;
    private Button buttonScan;
    private final int MY_PERMISSIONS_ACCESS_COARSE_LOCATION = 1;
    private List<ScanResult> results;
    WifiReceiver receiverWifi;
    private boolean withExtrusion = true;

    private ArrayList<String> arrayList = new ArrayList<>();
    private ArrayAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ArcGISRuntimeEnvironment.setApiKey(keys.api_key_02062021);

        ArcGISScene scene = new ArcGISScene();
        scene.setBasemap(Basemap.createOpenStreetMap());

        mSceneView = (SceneView) findViewById(R.id.sceneView);
        mSceneView.setScene(scene);

        ServiceFeatureTable serviceFeatureTableUnits = new ServiceFeatureTable("https://services3.arcgis.com/jR9a3QtlDyTstZiO/arcgis/rest/services/BK_MAP_INDOOR_WFL1/FeatureServer/4");
        serviceFeatureTableUnits.setFeatureRequestMode(ServiceFeatureTable.FeatureRequestMode.ON_INTERACTION_NO_CACHE);
        FeatureLayer unitLayer = new FeatureLayer(serviceFeatureTableUnits);

        unitLayer.getSceneProperties().setSurfacePlacement(LayerSceneProperties.SurfacePlacement.RELATIVE);
        unitLayer.setRenderingMode(FeatureLayer.RenderingMode.DYNAMIC);
        unitLayer.setMinScale(0);
        unitLayer.setMaxScale(0);

        scene.getOperationalLayers().add(unitLayer);

        ServiceFeatureTable serviceFeatureTableBK = new ServiceFeatureTable("https://services3.arcgis.com/jR9a3QtlDyTstZiO/arcgis/rest/services/BK_MAP_INDOOR_WFL1/FeatureServer/3");
        serviceFeatureTableBK.setFeatureRequestMode(ServiceFeatureTable.FeatureRequestMode.ON_INTERACTION_NO_CACHE);
        FeatureLayer featureLayer = new FeatureLayer(serviceFeatureTableBK);
        ServiceFeatureTable serviceFeatureTableArduino = new ServiceFeatureTable("https://services3.arcgis.com/jR9a3QtlDyTstZiO/ArcGIS/rest/services/Arduino_Table/FeatureServer/0");

        //serviceFeatureTableArduino.(ServiceFeatureTable.QueryFeatureFields.valueOf(""));
        featureLayer.getSceneProperties().setSurfacePlacement(LayerSceneProperties.SurfacePlacement.RELATIVE);
        featureLayer.setRenderingMode(FeatureLayer.RenderingMode.DYNAMIC);
        featureLayer.setMinScale(0);
        featureLayer.setMaxScale(0);

        SimpleLineSymbol lineSymbol = new SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, 0x80964B00, 1.5F);
        SimpleRenderer renderer = new SimpleRenderer(new SimpleFillSymbol(SimpleFillSymbol.Style.SOLID, 0xFFFFEDCC, lineSymbol));

        renderer.getSceneProperties().setExtrusionMode(Renderer.SceneProperties.ExtrusionMode.ABSOLUTE_HEIGHT);

        featureLayer.setRenderer(renderer);

        scene.getOperationalLayers().add(featureLayer);
        ArcGISTiledElevationSource elevationSource = new ArcGISTiledElevationSource(
                "http://elevation3d.arcgis.com/arcgis/rest/services/WorldElevation3D/Terrain3D/ImageServer");
        scene.getBaseSurface().getElevationSources().add(elevationSource);

        Camera camera = new Camera(52.0063728, 4.3710473, 300.0, 0.00, 0.00, 0.0);
        mSceneView.setViewpointCamera(camera);


        wifiList = findViewById(R.id.wifiList);
        Button buttonScan = findViewById(R.id.scanBtn);
        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (!wifiManager.isWifiEnabled()) {
            Toast.makeText(getApplicationContext(), "Turning WiFi ON...", Toast.LENGTH_LONG).show();
            wifiManager.setWifiEnabled(true);
        }
        buttonScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(
                            MainActivity.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, MY_PERMISSIONS_ACCESS_COARSE_LOCATION);
                } else {
                    wifiManager.startScan();
                    //getData(serviceFeatureTableArduino);

                }
            }
        });
    }
    @Override
    protected void onPostResume() {
        super.onPostResume();
        receiverWifi = new WifiReceiver(wifiManager, wifiList);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        registerReceiver(receiverWifi, intentFilter);
        getWifi();
    }
    private void getWifi() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Toast.makeText(MainActivity.this, "version> = marshmallow", Toast.LENGTH_SHORT).show();
            if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(MainActivity.this, "location turned off", Toast.LENGTH_SHORT).show();
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, MY_PERMISSIONS_ACCESS_COARSE_LOCATION);
            } else {
                Toast.makeText(MainActivity.this, "location turned on", Toast.LENGTH_SHORT).show();
                wifiManager.startScan();
            }
        } else {
            Toast.makeText(MainActivity.this, "scanning", Toast.LENGTH_SHORT).show();
            wifiManager.startScan();
        }
    }
    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(receiverWifi);
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case MY_PERMISSIONS_ACCESS_COARSE_LOCATION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(MainActivity.this, "permission granted", Toast.LENGTH_SHORT).show();
                wifiManager.startScan();
            } else {
                Toast.makeText(MainActivity.this, "permission not granted", Toast.LENGTH_SHORT).show();
                return;
            }
            break;
        }
    }
    private void getData(ServiceFeatureTable serviceFeatureTable){
        QueryParameters queryParameters = new QueryParameters();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        Date fiveMinAgoTime = Calendar.getInstance().getTime();
        Date nowTime = Calendar.getInstance().getTime();

        Calendar now = toCalendar(nowTime);
        Calendar fiveMinAgo = toCalendar(fiveMinAgoTime);
        fiveMinAgo.add(Calendar.DATE, -20);
        Date fiveMinDate = fiveMinAgo.getTime();

        Log.d("NOW", dateFormat.format(nowTime));
        Log.d("NOW-FIVE", dateFormat.format(fiveMinDate));

        TimeExtent timeExtent = new TimeExtent(fiveMinAgo, now);
        queryParameters.setTimeExtent(timeExtent);

        final ListenableFuture<FeatureQueryResult> future = serviceFeatureTable.queryFeaturesAsync(queryParameters);
        // add done loading listener to fire when the selection returns
        future.addDoneListener(() -> {
            try {
                // call get on the future to get the result
                FeatureQueryResult result = future.get();
                // check there are some results
                Iterator<Feature> resultIterator = result.iterator();
                if (resultIterator.hasNext()) {
                    // get the extent of the first feature in the result to zoom to
                    Feature feature = resultIterator.next();
                    Log.d("WIFI_TABLE", feature.getAttributes().toString());

                } else {
                    Toast.makeText(this, "No WiFi data available for localisation ", Toast.LENGTH_LONG).show();
                }
            } catch (Exception e) {
                String error = "Feature search failed for: " + queryParameters + ". Error: " + e.getMessage();
                Toast.makeText(this, error, Toast.LENGTH_LONG).show();
                Log.e("ERROR", error);
            }
        });
    }

    public static Calendar toCalendar(Date date){
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal;
    }
}
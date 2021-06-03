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
import android.os.StrictMode;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.esri.arcgisruntime.concurrent.ListenableFuture;
import com.esri.arcgisruntime.data.ArcGISFeature;
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
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;

import com.google.gson.*;


public class MainActivity extends AppCompatActivity {
    RequestQueue queue = Volley.newRequestQueue(this);


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

    public String currentRoom;
    public String previousRoom;
    public int  roomOccupancy;
    public String currOBJECTID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

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
                    updateOccupancy("08.02.00.760",1);

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
    public void updateOccupancy(String room_id,@NonNull int plusminus ){
        //TODO

        try {
            String sURL = "https://services3.arcgis.com/jR9a3QtlDyTstZiO/ArcGIS/rest/services/BK_MAP_INDOOR_WFL1/FeatureServer/4/query?where=NAME+like+%27%25"+room_id+"%25%27&objectIds=&time=&geometry=&geometryType=esriGeometryEnvelope&inSR=&spatialRel=esriSpatialRelIntersects&resultType=none&distance=0.0&units=esriSRUnit_Meter&returnGeodetic=false&outFields=NAME%2C+OCCUPANCY%2C+OBJECTID&returnGeometry=false&returnCentroid=false&featureEncoding=esriDefault&multipatchOption=xyFootprint&maxAllowableOffset=&geometryPrecision=&outSR=&datumTransformation=&applyVCSProjection=false&returnIdsOnly=false&returnUniqueIdsOnly=false&returnCountOnly=false&returnExtentOnly=false&returnQueryGeometry=false&returnDistinctValues=false&cacheHint=false&orderByFields=&groupByFieldsForStatistics=&outStatistics=&having=&resultOffset=&resultRecordCount=&returnZ=false&returnM=false&returnExceededLimitFeatures=true&quantizationParameters=&sqlFormat=none&f=pjson&token=";
            URL url = new URL(sURL);
            HttpURLConnection request = (HttpURLConnection) url.openConnection();

            request.connect();
            //Log.d("REQUEST", "updateOccupancy: "+ request.getContent());

            JsonParser jp = new JsonParser(); //from gson
            JsonElement root = jp.parse(new InputStreamReader((InputStream) request.getInputStream())); //Convert the input stream to a json element
            JsonObject rootobj = root.getAsJsonObject();


            Gson gson = new GsonBuilder().create();
            indoorModel value = gson.fromJson(root, indoorModel.class);
            indoorModel.Features[] feat_arr = value.features;

            System.out.println("There are so many features: \t"+ feat_arr.length);
            for (indoorModel.Features iter: feat_arr){
                String mRoom = iter.attributes.NAME;
                int currOcc = iter.attributes.OCCUPANCY;
                String objectID = iter.attributes.OBJECTID;
                currOBJECTID = objectID;
                roomOccupancy = currOcc;
                System.out.println("There are " + currOcc +" people in room "+mRoom+" with objectID "+objectID);
                Toast.makeText(MainActivity.this,"You are in room "+mRoom+". There are "+currOcc+" people in the room with you",Toast.LENGTH_SHORT).show()
;
            }
            request.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }



        if(plusminus > 0){
            JSONObject postRequest = new JSONObject();
            JSONObject mBody = new JSONObject();

            try {
                mBody.put("OBJECTID",currOBJECTID);
                mBody.put("OCCUPANCY", (roomOccupancy+1));
                postRequest.put("attributes", mBody);
                String postData = "features=["+postRequest.toString()+"]";
                URL url = new URL("https://services3.arcgis.com/jR9a3QtlDyTstZiO/ArcGIS/rest/services/BK_MAP_INDOOR_WFL1/FeatureServer/4/updateFeatures");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                try {

                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", "application/application/x-www-form-urlencoded;charset=UTF-8");
                    conn.setRequestProperty("Accept","text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9");
                    conn.setDoOutput(true);
                    DataOutputStream os = new DataOutputStream(conn.getOutputStream());
                    os.writeBytes(postData);
                    os.flush();
                    os.close();

                    Log.i("STATUS", String.valueOf(conn.getResponseCode()));

                    conn.disconnect();
                }catch (Exception e){
                    e.printStackTrace();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println(postRequest);
        }else{
            //TODO
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

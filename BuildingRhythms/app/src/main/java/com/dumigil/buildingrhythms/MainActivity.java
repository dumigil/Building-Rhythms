package com.dumigil.buildingrhythms;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;
import com.chaquo.python.android.PyApplication;
import com.esri.arcgisruntime.concurrent.ListenableFuture;
import com.esri.arcgisruntime.data.Feature;
import com.esri.arcgisruntime.data.ServiceFeatureTable;
import com.esri.arcgisruntime.layers.FeatureLayer;
import com.esri.arcgisruntime.loadable.LoadStatus;
import com.esri.arcgisruntime.mapping.ArcGISScene;
import com.esri.arcgisruntime.ArcGISRuntimeEnvironment;

import com.esri.arcgisruntime.mapping.ArcGISTiledElevationSource;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.BasemapStyle;
import com.esri.arcgisruntime.mapping.GeoElement;
import com.esri.arcgisruntime.mapping.view.Camera;
import com.esri.arcgisruntime.mapping.view.DefaultSceneViewOnTouchListener;
import com.esri.arcgisruntime.mapping.view.IdentifyLayerResult;
import com.esri.arcgisruntime.mapping.view.LayerSceneProperties;
import com.esri.arcgisruntime.mapping.view.SceneView;
import com.esri.arcgisruntime.symbology.ClassBreaksRenderer;
import com.esri.arcgisruntime.symbology.Renderer;
import com.esri.arcgisruntime.symbology.SimpleFillSymbol;
import com.esri.arcgisruntime.symbology.SimpleLineSymbol;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import com.google.gson.*;


public class MainActivity extends AppCompatActivity {


    private SceneView mSceneView;
    private ListView wifiList;
    private Button buttonScan;
    private final int MY_PERMISSIONS_ACCESS_COARSE_LOCATION = 1;
    private List<ScanResult> results;

    WifiManager wifiManager;
    private boolean withExtrusion = true;
    public ArrayList<String> resultList = new ArrayList<>();
    private ArrayList<String> arrayList = new ArrayList<>();
    private ArrayAdapter adapter;

    public String currentRoom;
    public String previousRoom;
    public int  roomOccupancy;
    public String currOBJECTID;
    public knnApp kNNObj = new knnApp();

    public ArrayList<knn_methods> arrayAppend( ArrayList<knn_methods> A , ArrayList<knn_methods> B)
    {
        for(knn_methods item: B)
        {
            A.add(item);
        }
        return A;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        if (! Python.isStarted()) {
            Python.start(new AndroidPlatform(this));
        }
        Python py = Python.getInstance();
        PyObject pyobj = py.getModule("python_script_to_port");
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ArcGISRuntimeEnvironment.setApiKey(keys.api_key_02062021);

        ArcGISScene scene = new ArcGISScene();
        Basemap basemap = new Basemap(BasemapStyle.OSM_LIGHT_GRAY_BASE);
        scene.setBasemap(Basemap.createStreetsWithReliefVector());

        mSceneView = (SceneView) findViewById(R.id.sceneView);
        mSceneView.setScene(scene);

        ServiceFeatureTable serviceFeatureTableUnits = new ServiceFeatureTable("https://services3.arcgis.com/jR9a3QtlDyTstZiO/arcgis/rest/services/BK_MAP_INDOOR_WFL1/FeatureServer/4");
        serviceFeatureTableUnits.setFeatureRequestMode(ServiceFeatureTable.FeatureRequestMode.ON_INTERACTION_NO_CACHE);
        FeatureLayer unitLayer = new FeatureLayer(serviceFeatureTableUnits);

        unitLayer.getSceneProperties().setSurfacePlacement(LayerSceneProperties.SurfacePlacement.RELATIVE);
        unitLayer.setRenderingMode(FeatureLayer.RenderingMode.DYNAMIC);
        unitLayer.setMinScale(0);
        unitLayer.setMaxScale(0);


        final ClassBreaksRenderer classBreaksRenderer = createPopulationClassBreaksRenderer();
        classBreaksRenderer.getSceneProperties().setExtrusionMode(Renderer.SceneProperties.ExtrusionMode.ABSOLUTE_HEIGHT);
        classBreaksRenderer.getSceneProperties().setExtrusionExpression("[HEIGHT_RELATIVE] + [ELEVATION_RELATIVE]");

        unitLayer.setRenderer(classBreaksRenderer);

        scene.getOperationalLayers().add(unitLayer);
        ArcGISTiledElevationSource elevationSource = new ArcGISTiledElevationSource(
                "http://elevation3d.arcgis.com/arcgis/rest/services/WorldElevation3D/Terrain3D/ImageServer");
        scene.getBaseSurface().getElevationSources().add(elevationSource);

        Camera camera = new Camera(52.0048404,4.3696593, 150, 30, 50, 0.0);
        mSceneView.setViewpointCamera(camera);
        mSceneView.setFieldOfView(15);

        wifiList = findViewById(R.id.wifiList);
        Button buttonScan = findViewById(R.id.scanBtn);
        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (!wifiManager.isWifiEnabled()) {
            Toast.makeText(getApplicationContext(), "Turning WiFi ON...", Toast.LENGTH_LONG).show();
            wifiManager.setWifiEnabled(true);
        }



        unitLayer.addDoneLoadingListener(() -> {
            if (unitLayer.getLoadStatus() == LoadStatus.LOADED){
                mSceneView.setOnTouchListener(new DefaultSceneViewOnTouchListener(mSceneView){
                    @Override public boolean onSingleTapConfirmed(MotionEvent motionEvent) {
                        Log.d("CLICK", "WE HAVE A TAP");
                        unitLayer.clearSelection();
                        android.graphics.Point screenPoint = new android.graphics.Point(Math.round(motionEvent.getX()),Math.round(motionEvent.getY()));
                        ListenableFuture<IdentifyLayerResult> identify = mSceneView
                                .identifyLayerAsync(unitLayer, screenPoint, 10, false, 1);
                        identify.addDoneListener(() -> {
                            try {
                                // get the identified result and check that it is a feature
                                IdentifyLayerResult result = identify.get();
                                List<GeoElement> geoElements = result.getElements();
                                if (!geoElements.isEmpty()) {
                                    Log.d("EMPTY", "geoelement not empty");
                                    //GeoElement geoElement = geoElements.get(0);
                                    for(GeoElement element : result.getElements()){
                                        Feature feature = (Feature) element;
                                        unitLayer.selectFeature((Feature) feature);
                                        Map<String, Object> attr = feature.getAttributes();
                                        Set<String> keys = attr.keySet();
                                        for(String key : keys) {
                                            Object value = attr.get(key);
                                            if(key.equals("OCCUPANCY")){
                                                Toast.makeText(MainActivity.this, "Occupancy: "+value, Toast.LENGTH_LONG).show();
                                            }
                                        }
                                    }
                                }
                            } catch (InterruptedException | ExecutionException e) {
                                String error = "Error while identifying layer result: " + e.getMessage();
                                Log.e("ERROR", error);
                                Toast.makeText(MainActivity.this, error, Toast.LENGTH_LONG).show();
                            }
                        });
                        return true;
                    }
                });
            } else if (unitLayer.getLoadStatus() == LoadStatus.FAILED_TO_LOAD) {
                String error = "Error loading scene layer " + unitLayer.getLoadStatus();
                Log.e("ERROR", error);
                Toast.makeText(this, error, Toast.LENGTH_LONG).show();

            }
        });
        buttonScan.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                final String[] input = {""};
                if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(
                            MainActivity.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, MY_PERMISSIONS_ACCESS_COARSE_LOCATION);
                } else {
                    BroadcastReceiver wifiScanReceiver = new BroadcastReceiver() {
                        @Override
                        public void onReceive(Context c, Intent intent) {
                            boolean success = intent.getBooleanExtra(
                                    WifiManager.EXTRA_RESULTS_UPDATED, false);
                            if (success) {
                                input[0] = scanSuccess();
                            } else {
                                // scan failure handling
                                scanFailure();
                            }
                        }
                    };
                    IntentFilter intentFilter = new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
                    getApplicationContext().registerReceiver(wifiScanReceiver, intentFilter);
                    boolean success = wifiManager.startScan();
                    if (!success) {
                        scanFailure();
                    }else{
                        input[0] = scanSuccess();
                    }

                    System.out.println(input[0]);

                    if(!input[0].equals("[]")){
                        Log.d("PY","Starting python run");
                        Long tsLong = System.currentTimeMillis()/1000;
                        String ts = tsLong.toString();
                        PyObject obj = pyobj.callAttr("predict_func", input[0], ts);
                        Log.d("PY","Python run done");
                        if(!obj.toString().equals("Couldn't locate the room you are in :(")){
                            previousRoom = currentRoom;
                            currentRoom = obj.toString();
                            Toast.makeText(MainActivity.this,"You, Human, are in room: " + currentRoom, Toast.LENGTH_SHORT).show();
                            System.out.println("You, Human, are in room: "+currentRoom);
                            if(previousRoom !=null){
                                updateOccupancy(previousRoom, -1);
                            }
                            updateOccupancy(currentRoom,1);
                        }else{
                            Toast.makeText(MainActivity.this,"Couldn't locate the room you are in :(", Toast.LENGTH_SHORT).show();

                        }
                    }
                }
            }
        });

    } // end of onCreate




    private String scanSuccess() {
        List<ScanResult> results = null;
        results = wifiManager.getScanResults();
        for(ScanResult res: results){
            //System.out.println(res.BSSID+": "+res.level);
            JSONObject feature = new JSONObject();
            JSONObject output = new JSONObject();
            try {
                feature.put("MAC", res.BSSID);
                feature.put("RSSI", res.level);
                output.put("attributes", feature);
                resultList.add(output.toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
        String input = "[";
        String comma = "";
        System.out.println(resultList.size());
        for(String e: resultList){
            input += comma;
            input += e;
            comma = ",\n";
        }
        input +="]";
        return input;

    }

    private void scanFailure() {
        // handle failure: new scan did NOT succeed
        // consider using old scan results: these are the OLD results!
        List<ScanResult> results = wifiManager.getScanResults();
    }


    public void updateOccupancy(String room_id,@NonNull int plusminus ){
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

            for (indoorModel.Features iter: feat_arr)
            {

                String mRoom = iter.attributes.NAME;
                int currOcc = iter.attributes.OCCUPANCY;
                String objectID = iter.attributes.OBJECTID;
                currOBJECTID = objectID;
                roomOccupancy = currOcc;
                //System.out.println("There are " + currOcc +" people in room "+mRoom+" with objectID "+objectID);
                //Toast.makeText(MainActivity.this,"You are in room "+mRoom+". There are "+currOcc+" people in the room with you",Toast.LENGTH_SHORT).show();

            }

            request.disconnect();
        }

        catch (Exception e)
        {
            e.printStackTrace();
        }



        if(plusminus > 0){
            JSONObject postRequest = new JSONObject();
            JSONObject mBody = new JSONObject();

            try {
                mBody.put("OBJECTID",currOBJECTID);
                mBody.put("OCCUPANCY", (roomOccupancy+1));
                postRequest.put("attributes", mBody);
                String url = "https://services3.arcgis.com/jR9a3QtlDyTstZiO/ArcGIS/rest/services/BK_MAP_INDOOR_WFL1/FeatureServer/4/updateFeatures";
                RequestQueue requestQueue = Volley.newRequestQueue(this);
                final String requestBody = "features=["+postRequest.toString()+"]";
                System.out.println(requestBody);
                StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.i("VOLLEY", response);
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("VOLLEY", error.toString());
                    }
                }) {
                    @Override
                    public String getBodyContentType() {
                        return "application/x-www-form-urlencoded; charset=UTF-8";
                    }
                    @Override
                    public byte[] getBody() throws AuthFailureError {
                        try {
                            return requestBody == null ? null : requestBody.getBytes("utf-8");
                        } catch (UnsupportedEncodingException uee) {
                            VolleyLog.wtf("Unsupported Encoding while trying to get the bytes of %s using %s", requestBody, "utf-8");
                            return null;
                        }
                    }
                    @Override
                    protected Response<String> parseNetworkResponse(NetworkResponse response) {
                        String responseString = "";
                        if (response != null) {
                            responseString = String.valueOf(response.allHeaders);

                            // can get more details such as response.headers

                        }
                        return Response.success(responseString, HttpHeaderParser.parseCacheHeaders(response));
                    }
                };
                requestQueue.add(stringRequest);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }else{
            JSONObject postRequest = new JSONObject();
            JSONObject mBody = new JSONObject();

            try {
                mBody.put("OBJECTID",currOBJECTID);
                mBody.put("OCCUPANCY", (roomOccupancy-1));
                postRequest.put("attributes", mBody);
                String url = "https://services3.arcgis.com/jR9a3QtlDyTstZiO/ArcGIS/rest/services/BK_MAP_INDOOR_WFL1/FeatureServer/4/updateFeatures";
                RequestQueue requestQueue = Volley.newRequestQueue(this);
                final String requestBody = "features=["+postRequest.toString()+"]";
                System.out.println(requestBody);
                StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.i("VOLLEY", response);
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("VOLLEY", error.toString());
                    }
                }) {
                    @Override
                    public String getBodyContentType() {
                        return "application/x-www-form-urlencoded; charset=UTF-8";
                    }
                    @Override
                    public byte[] getBody() throws AuthFailureError {
                        try {
                            return requestBody == null ? null : requestBody.getBytes("utf-8");
                        } catch (UnsupportedEncodingException uee) {
                            VolleyLog.wtf("Unsupported Encoding while trying to get the bytes of %s using %s", requestBody, "utf-8");
                            return null;
                        }
                    }
                    @Override
                    protected Response<String> parseNetworkResponse(NetworkResponse response) {
                        String responseString = "";
                        if (response != null) {
                            responseString = String.valueOf(response.allHeaders);

                            // can get more details such as response.headers

                        }
                        return Response.success(responseString, HttpHeaderParser.parseCacheHeaders(response));
                    }
                };
                requestQueue.add(stringRequest);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }


    @Override
    protected void onPause() {
        super.onPause();

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


    public static Calendar toCalendar(Date date){
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal;
    }

    private static ClassBreaksRenderer createPopulationClassBreaksRenderer() {

        // create colors
        final int gray  = Color.rgb(153, 153, 153);
        final int blue1 = Color.argb(150,255, 236, 204 );
        final int blue2 = Color.argb(150,253, 194, 149);
        final int blue3 = Color.argb(150,240, 125, 132);
        final int blue4 = Color.argb(150,195, 94, 131);
        final int blue5 = Color.argb(150,120, 73, 109);
        final int blue6 = Color.argb(150,61, 49, 87);

        // create 5 fill symbols with different shades of blue and a gray outline
        SimpleLineSymbol outline = new SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, gray, 1);
        SimpleLineSymbol outline1 = new SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, blue1, 1);
        SimpleLineSymbol outline2= new SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, blue2, 1);
        SimpleLineSymbol outline3 = new SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, blue3, 1);
        SimpleLineSymbol outline4 = new SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, blue4, 1);
        SimpleLineSymbol outline5 = new SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, blue5, 1);
        SimpleLineSymbol outline6 = new SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, blue6, 1);

        SimpleFillSymbol classSymbol1 = new SimpleFillSymbol(SimpleFillSymbol.Style.SOLID, blue1, outline1);
        SimpleFillSymbol classSymbol2 = new SimpleFillSymbol(SimpleFillSymbol.Style.SOLID, blue2, outline2);
        SimpleFillSymbol classSymbol3 = new SimpleFillSymbol(SimpleFillSymbol.Style.SOLID, blue3, outline3);
        SimpleFillSymbol classSymbol4 = new SimpleFillSymbol(SimpleFillSymbol.Style.SOLID, blue4, outline4);
        SimpleFillSymbol classSymbol5 = new SimpleFillSymbol(SimpleFillSymbol.Style.SOLID, blue5, outline5);
        SimpleFillSymbol classSymbol6 = new SimpleFillSymbol(SimpleFillSymbol.Style.SOLID, blue6, outline6);

        // create 5 classes for different population ranges
        ClassBreaksRenderer.ClassBreak classBreak1 = new ClassBreaksRenderer.ClassBreak("-99 to 8560", "-99 to 8560", -1,
                0, classSymbol1);
        ClassBreaksRenderer.ClassBreak classBreak2 = new ClassBreaksRenderer.ClassBreak("> 8,560 to 18,109", "> 8,560 to 18,109", 1,
                4, classSymbol2);
        ClassBreaksRenderer.ClassBreak classBreak3 = new ClassBreaksRenderer.ClassBreak("> 18,109 to 35,501", "> 18,109 to 35,501", 4,
                6, classSymbol3);
        ClassBreaksRenderer.ClassBreak classBreak4 = new ClassBreaksRenderer.ClassBreak("> 35,501 to 86,100", "> 35,501 to 86,100", 6,
                8, classSymbol4);
        ClassBreaksRenderer.ClassBreak classBreak5 = new ClassBreaksRenderer.ClassBreak("> 0.9 to 1", "> 86,100 to 10,110,975", 8,
                10, classSymbol5);
        ClassBreaksRenderer.ClassBreak classBreak6 = new ClassBreaksRenderer.ClassBreak("> 0.9 to 1", "> 86,100 to 10,110,975", 10,
                60, classSymbol5);
        // create the renderer for the POP2007 field
        return new ClassBreaksRenderer("OCCUPANCY", Arrays.asList(classBreak1, classBreak2, classBreak3, classBreak4,
                classBreak5, classBreak6));
    }

}

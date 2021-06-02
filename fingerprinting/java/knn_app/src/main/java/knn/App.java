package knn;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import com.google.gson.*;
import knn.malakas;
import knn.knn_methods;
import org.joda.time.LocalTime;

// @SuppressWarnings
public class App {

    // Scanner sc = new Scanner(System.in);
    int knn_value = 5;
	// int totalNumberOfLabel = 0;
    public static double[] a = {1,2,3,4,5,6,7};
    public static double[] b = {10,20,30,40,50,60,70};

    public ArrayList<knn_methods> readJson() throws Exception
    {
        ArrayList<knn_methods> knnObjList = new ArrayList<knn_methods>();
        try
        {

            ////////////////// Read JSON ///////////////////
            String sURL = "https://services3.arcgis.com/jR9a3QtlDyTstZiO/ArcGIS/rest/services/Arduino_Table/FeatureServer/0/query?where=ObjectID%3E%3D0&outFields=MAC%2C+RSSI%2C+BSSID%2C+Room_ID+%2C+ObjectId+%2C+Time_Stamp+&returnIdsOnly=false&returnUniqueIdsOnly=false&returnCountOnly=false&returnDistinctValues=false&cacheHint=false&sqlFormat=none&f=pjson";
            URL url = new URL(sURL);

            // make url connection
            URLConnection request = url.openConnection();
            request.connect();

            // Convert to a JSON object to malakas class object print data
            JsonParser jp = new JsonParser(); //from gson
            JsonElement root = jp.parse(new InputStreamReader((InputStream) request.getContent())); //Convert the input stream to a json element
            JsonObject rootobj = root.getAsJsonObject()  ;

            Gson gson = new GsonBuilder().create();
            malakas value = gson.fromJson(root, malakas.class);

            //reading values for last instance
            malakas.Features[] feat_arr = value.features;
            System.out.println( "There are so many features: \t" +  feat_arr.length );

            // CREATE A list for storing knn object methods


            String wifiName1 = "eduroam";
            String wifiName2 = "TUvisitor";
            String wifiName3 = "Delft Free Wifi";
            String wifiName4 = "tudelft-dastud";

            double currentTime = Double.parseDouble( String.valueOf(TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis())) );
            int timeToPass = 60; // time in minutes
            int timeToPassSeconds =timeToPass*60; // time in minutes

            double timePast = currentTime - timeToPassSeconds;
            System.out.println(currentTime);
            
            for (malakas.Features iter: feat_arr)
            {
                double timeFromObject = Double.parseDouble( iter.attributes.Time_Stamp);


                if(iter.attributes.BSSID.toString().equals(wifiName1) || iter.attributes.BSSID.toString().equals(wifiName2) || iter.attributes.BSSID.toString().equals(wifiName3) || iter.attributes.BSSID.toString().equals(wifiName4) )
                {
                    if(timeFromObject >= timePast)
                    {
                        String mac = iter.attributes.MAC;
                        double signal = Double.parseDouble (iter.attributes.RSSI );
                        double unqID  = Double.parseDouble( iter.attributes.ObjectId );
                        String lbl = iter.attributes.Room_ID;
                        
                        double timestamp = Double.parseDouble(iter.attributes.Time_Stamp);
    
                        knnObjList.add( new knn_methods(mac, signal, unqID, lbl));

                    }
                }
            }

            System.out.println("we have so many " + " training wifi samples " +knnObjList.size());

        } catch (Exception e)
        {
            e.printStackTrace();
        }

        return knnObjList;
    }

    public double[] getMinMaxValue(ArrayList<knn_methods> objList, int type)
    {
        // max and min variable
        double max = -99999;
        double min = 100000;

        //loop through all objects and extract min max value
        for ( knn_methods obj : objList )
        {
            double temp = 0;
            // type 1 is for rssi
            if(type==1) temp = obj.RSSI;

            // type 2 is for uniqueID
            if(type==2) temp = obj.uniqueId;

            if(max <= temp)
            {
                max = temp ;
            }
            if(min >= temp)
            {
                min=temp;
            }
        }
        double[] minmax = {min, max};
        return minmax;
    }

//    public void normalizer(ArrayList<knn_methods> objList)
//    {
//        // normalize signal values
//    }

//    public double getEuclideanDistance(ArrayList<knn.knn_methods> objList,  knn_methods test_feat)
//    {
//        // takes all knn objects and the test object, calculates distances
//        double sum = 0;
//        for (int i = 0; i < features1.length; i++)
//        {  //System.out.println(features1[i]+" "+features2[i]);
//            //applied Euclidean distance formula
//            sum += Math.pow(features1[i] - features2[i], 2);
//        }
//        return Math.sqrt(sum);
//    }

    public static void main(String[] args) throws Exception 
    {

        try
        {
            App mainObj = new App();
            ArrayList<knn.knn_methods> kNN_Objs = new ArrayList<knn.knn_methods>();
            kNN_Objs = mainObj.readJson();

        }
        catch(Exception e)
        {
            e.printStackTrace();
        }

    }
}
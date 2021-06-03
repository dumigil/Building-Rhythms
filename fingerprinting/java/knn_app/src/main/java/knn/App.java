package knn;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.net.URL;
import java.net.URLConnection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.google.gson.*;
import knn.malakas;
import knn.knn_methods;
import org.graalvm.compiler.lir.LIRFrameState;
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


            String wifiName = "eduroam";
            for (malakas.Features iter: feat_arr)
            {

                if(iter.attributes.BSSID.toString().equals(wifiName))
                {
                    String mac = iter.attributes.MAC;
                    double signal = Double.parseDouble (iter.attributes.RSSI );
                    double unqID  = Double.parseDouble( iter.attributes.ObjectId );
                    String lbl = iter.attributes.Room_ID;

                    knnObjList.add( new knn_methods(mac, signal, unqID, lbl));
                }
            }
            uniqueMaker(knnObjList);

            System.out.println("we have so many "+ wifiName + " training wifi samples " +knnObjList.size());

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

//    public void majorityVoter(ArrayList<Double> distList, int k)
//    {
////        HashMap counts = new HashMap<String, Integer>();
////        ArrayList<Double> sortList
//        int max = -100;
//        for(String item : distList)
//        {
////             set sort and get top k max objects
//
////        counts.put(item, Integer.parseInt( counts.get(item) ) + 1  );
//        }
//        // max value is obtained
//
//        // iterate through the hashmap to find the max k objects
//    }

    public void uniqueMaker(ArrayList<knn_methods> listObj)
    {
        // makes unique id for the macids
        // and sets

        //first creates a hashmap
        Map counts = new HashMap<String, Integer>(); //key is macID values is count

        for(knn_methods item : listObj) {
            counts.put(item.MAC, 1);// counts.get(item.MAC) +
        }// hash map is made

        // update counts to store the unique number values
        int  counter=0;
        Iterator<Map.Entry<String, Integer>> iter =  counts.entrySet().iterator();

        while( iter.hasNext() )
        {
            Map.Entry<String, Integer> entry = iter.next();
            String k = (String) entry.getKey();
            counts.put(k, counter );
            counter+=1;
        }
        System.out.println(counts.toString());

        // now set the macno of the objects
        for(knn_methods item : listObj)
        {
            // get mac no of obj
            String mac = item.MAC.toString();

            // check the mac with unique macno
            if(counts.containsKey(mac))
            {
                // get value of this mac
                int macno = (int) counts.get(mac);
                // set the macno of the objects
                item.setMacNo(macno);
            }

        }

//        return counts; //returns for getting number on test dataset
    }


    public ArrayList<Double> getEuclideanDistance(ArrayList<knn.knn_methods> objList, knn_methods test_feat)
    {

        // takes all knn objects and single test object, calculates distances
        ArrayList<Double> distList = new ArrayList<>();
        for (knn_methods item: objList)
        {

            double sum = 0;
            //applied Euclidean distance formula
            sum += Math.pow(item.RSSI - test_feat.RSSI, 2);
            sum += Math.pow(item.uniqueId - test_feat.uniqueId, 2);
            double val = Math.sqrt(sum);
            distList.add(val);

        }
        return distList;
    }


    public static void main(String[] args) throws Exception
    {

        try
        {
            App mainObj = new App();
            ArrayList<knn.knn_methods> kNN_Objs = new ArrayList<knn.knn_methods>();
            kNN_Objs = mainObj.readJson();
            System.out.println("mac no of 7th obj "+ kNN_Objs.get(7).MAC +" and macno is  " +kNN_Objs.get(10).macno);

//            Map h = mainObj.uniqueMaker(kNN_Objs);

//            System.out.println(h.toString());
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }

    }
}


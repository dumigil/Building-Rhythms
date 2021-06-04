package knn;

import java.io.InputStream;
import java.io.InputStreamReader;

import java.net.URL;
import java.net.URLConnection;

import java.util.*;

import com.google.gson.*;


// @SuppressWarnings
public class App {
    static class Tuple<K, V>
    {
        private final K one;
        private final V two;

        public Tuple(K fir, V sec)
        {
            this.one = fir;
            this.two = sec;
        }
        //getters
        public K getOne() {
            return one;
        }

        public V getTwo() {
            return two;
        }
    }

    public Tuple<ArrayList<knn_methods>, Map> readJson() throws Exception //ArrayList<knn_methods>
    {
        // local var to be returned
        ArrayList<knn_methods> knnObjList = new ArrayList<>();
        Map unq = new HashMap();
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
//            JsonObject rootobj = root.getAsJsonObject()  ;

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
            unq = uniqueMaker(knnObjList);

            System.out.println("we have so many "+ wifiName + " training wifi samples " +knnObjList.size());

        } catch (Exception e)
        {
            e.printStackTrace();
        }

        return new Tuple<ArrayList<knn_methods>, Map>(knnObjList, unq);
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



    public Map uniqueMaker(ArrayList<knn_methods> listObj)
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
            String k = entry.getKey();
            counts.put(k, counter );
            counter+=1;
        }

        System.out.println(counts);

        // now set the macno of the objects
        for(knn_methods item : listObj)
        {
            // get mac no of obj
            String mac = item.MAC;

            // check the mac with unique macno
            if(counts.containsKey(mac))
            {
                // get value of this mac
                int macno = (int) counts.get(mac);
                // set the macno of the objects
                item.setMacNo(macno);
            }

        }

        return counts; //returns for getting number on test dataset
    }


    public ArrayList<Double> getEuclideanDistance(ArrayList<knn.knn_methods> objList, knn_methods test_feat)
    {

        // takes all knn objects and single test object, calculates distances
        // returns list of distances of the test from each training sample
        ArrayList<Double> distList = new ArrayList<>();
        for (knn_methods item: objList)
        {

            double sum = 0;
            //applied Euclidean distance formula
            sum += Math.pow(item.RSSI - test_feat.RSSI, 2);
            sum += Math.pow(item.macno - test_feat.macno, 2);
            double val = Math.sqrt(sum);
            // System.out.println(val);
            distList.add(val);
        }

        // return list of distance or the label itself
        // distlist is same size as objlist, so labels can be extracted easily, just know the indices of the min k labels

        //maybe label but make a new function to give
        majorityVoter(distList, objList, 10);

        return distList;
    }


    public int[] majorityVoter(ArrayList<Double> distList, ArrayList<knn.knn_methods> objList ,int k)
    {

        int[] indices = new int[k];
        double[] minDist = new double[k];

        // fill in values for minDist
        for(int i=0; i<k; i++)
        {
            minDist[i] = Double.MAX_VALUE;
        }

        for (int i = 0; i < objList.size(); i++)
        {
            double dist = distList.get(i);
            double max = Double.MIN_VALUE;
            int maxIdx = 0;

            for (int j = 0; j < k; j++)
            {
                // takes the max value from min array and takes its position
                if (max < minDist[j])
                {
                    max = minDist[j];
                    maxIdx = j;
                }
            }
//            System.out.println( maxIdx);
            if (minDist[maxIdx] > dist)
            {
                minDist[maxIdx] = dist;
                indices[maxIdx] = i;
            }
        }
        System.out.println(Arrays.toString( minDist) );
        System.out.println(Arrays.toString( indices) );
        System.out.println(distList);

        //check label of indices
        for (int i :indices)
        {
            System.out.println( objList.get(i).label );
        }

        return indices;
    }


    public static void main(String[] args)
    {

        try
        {
            App mainObj = new App();
            ArrayList<knn_methods> kNN_Objs;
            Tuple<ArrayList<knn_methods> , Map> tup = mainObj.readJson();
            kNN_Objs = tup.getOne();
            Map macnos = tup.getTwo();

            // make test object for testing
            knn_methods test_obj = new knn_methods( "84:3d:c6:c7:ef:50",  -90);

            //set macno of test obj
            if( macnos.containsKey( test_obj.MAC )  )
            {
                test_obj.setMacNo( (Integer) macnos.get(test_obj.MAC) );
            }
            mainObj.getEuclideanDistance(kNN_Objs , test_obj);
            System.out.println(test_obj.macno);

            //            System.out.println("mac no of 7th obj "+ kNN_Objs.get(7).MAC +" and macno is  " +kNN_Objs.get(10).macno);
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }

    }
}


package com.dumigil.buildingrhythms;

import java.io.InputStream;
import java.io.InputStreamReader;

import java.net.URL;
import java.net.URLConnection;

import java.util.*;
import java.util.concurrent.TimeUnit;

import com.google.gson.*;


// @SuppressWarnings
public class knnApp {

    ArrayList<knn_methods> testObjList = new ArrayList<>();

    static class Tuple<K, V>
    {
        private K one;
        private V two;

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
        public void setOneTwo(K one_, V two_)
        {
            this.one = one_;
            this.two = two_;
        }
    }

    public Tuple<ArrayList<knn_methods>, Map> readJson() throws Exception //ArrayList<knn_methods>
    {
        // local var to be returned
        ArrayList<knn_methods> knnObjList = new ArrayList<>();
        knnObjList.clear();
        Map unq = new HashMap();
        try
        {

            //////////////////////////////////// Read JSON /////////////////////////////////////
            String sURL = "https://services3.arcgis.com/jR9a3QtlDyTstZiO/ArcGIS/rest/services/Arduino_Table/FeatureServer/0/query?where=ObjectID%3E%3D0&outFields=MAC%2C+RSSI%2C+BSSID%2C+Room_ID+%2C+ObjectId+%2C+Time_Stamp+&returnIdsOnly=false&returnUniqueIdsOnly=false&returnCountOnly=false&returnDistinctValues=false&cacheHint=false&sqlFormat=none&f=pjson";
            URL url = new URL(sURL);

            // make url connection
            URLConnection request = url.openConnection();
            request.connect();

            // Convert to a JSON object to malakas class object print data
            JsonParser jp = new JsonParser(); //from gson
            JsonElement root = jp.parse(new InputStreamReader((InputStream) request.getContent())); //Convert the input stream to a json element
            // JsonObject rootobj = root.getAsJsonObject()  ;

            Gson gson = new GsonBuilder().create();
            malakas value = gson.fromJson(root, malakas.class);

            //reading values for last instance
            malakas.Features[] feat_arr = value.features;
            System.out.println( "There are so many features: \t" +  feat_arr.length );
            request.getInputStream().close();
            // CREATE A list for storing knn object methods

            String wifiName1 = "eduroam";
            String wifiName2 = "TUvisitor";
            String wifiName3 = "Delft Free Wifi";
            String wifiName4 = "tudelft-dastud";

            // read current time and calculate time t minutes ago
            double currentTime = Double.parseDouble( String.valueOf(TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis())) );
            double timeToPass = 7200;//60; // time in minutes
            double timeToPassSeconds =timeToPass*60; // time in minutes
            double timePast = currentTime - timeToPassSeconds;

//            System.out.println(currentTime);
//            System.out.println(timePast);


            for (malakas.Features iter: feat_arr)
            {
                String timeFromObjectAsString = ( iter.attributes.Time_Stamp);
//                Double timeFromObject = Double.parseDouble(timeFromObjectAsString);

//                System.out.println( timeFromObjectAsString );

                if(iter.attributes.BSSID.equals(wifiName1) || iter.attributes.BSSID.equals(wifiName2) || iter.attributes.BSSID.equals(wifiName3) || iter.attributes.BSSID.equals(wifiName4) )
                {
//                     if(timeFromObject >= timePast)
//                     {
//                         String mac = iter.attributes.MAC;
//                         double signal = Double.parseDouble (iter.attributes.RSSI );
//                         double unqID  = Double.parseDouble( iter.attributes.ObjectId );
//                         String lbl = iter.attributes.Room_ID;
//                         knnObjList.add( new knn_methods(mac, signal, unqID, lbl));
//                     }

                    String mac = iter.attributes.MAC;
                    double signal = Double.parseDouble (iter.attributes.RSSI );
                    double unqID  = Double.parseDouble( iter.attributes.ObjectId );
                    String lbl = iter.attributes.Room_ID;

                    knnObjList.add( new knn_methods(mac, signal, unqID, lbl));

                }
            }
            unq = uniqueMaker(knnObjList);

            System.out.println("we have "+ knnObjList.size() + " training wifi samples ");


        }
        catch (Exception e)
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

//        System.out.println(counts);

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

    public String getEuclideanDistance(ArrayList<knn_methods> objList, knn_methods test_feat)
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

        return majorityVoter(distList, objList, 7);
    }

    public String classifier(ArrayList<String> labelsList)
    {
        // takes list of String labels and finds count of each
        Map lablMap = new HashMap<String, Integer>();

        for(String item : labelsList)
        {
            int count = (int) lablMap.getOrDefault(item, 0); // ensure count will be one of 0,1,2,3,...
            lablMap.put(item, count + 1);

        }// hash map is made


        //use tuple to set label and max value
        Iterator<Map.Entry<String, Integer>> iter =  lablMap.entrySet().iterator();
        int max = Integer.MIN_VALUE;
        Tuple t_max = new Tuple("lol_what_room_is_this", -10 );
        while( iter.hasNext() )
        {
            Map.Entry<String, Integer> entry = iter.next();
            // get key value
            String k = entry.getKey();
            Integer v = entry.getValue();
            //store as tuple / update tuple tmax



//            System.out.println("k is: "+k +" and v is: "+  v);
            if(max < (int) t_max.getTwo())
            {
                max = v;
//                System.out.println("max is : "+ max);
                // if the max value is defeated, then update max value and also the label with this value
                // update tuple
                t_max.setOneTwo(k, v);

//                System.out.println("k_ is: "+ t_max.getOne() +" and v_ is: "+  t_max.getTwo());

            } // this should give us the max value in the hashmap

//            System.out.println(t_max.getOne());
        }

//        System.out.println("label counts dict is \n" + lablMap.toString());
//        System.out.println("max label is: " + t_max.getOne());
        return (String) t_max.getOne();
    }

    public String majorityVoter(ArrayList<Double> distList, ArrayList<knn_methods> objList ,int k)
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
//        System.out.println(Arrays.toString( minDist) );
//        System.out.println(Arrays.toString( indices) );
//        System.out.println(distList);

        // make list of labels and then send to label voter
        ArrayList<String> labelsList = new ArrayList<>();

        // check label of indices
        for (int i :indices)
        {
            labelsList.add(objList.get(i).label);
//            System.out.println( objList.get(i).label );
        }
        String feature_label =  classifier(labelsList);
        return feature_label;
    }

    public String knn_test_features(ArrayList<knn_methods> testObjList) throws Exception // run this function to get final label
    {
        ///////////// read training set
        Tuple<ArrayList<knn_methods> , Map> tup = readJson();
        ArrayList<knn_methods> trainingObjList = tup.getOne();
        Map macnos = tup.getTwo();


        ///////////////////// TESTING set

        ///////// read test obj and load as knn_methods objects
//        String testJson = "{'attributes':{'MAC':'02:15:b2:00:01:00','RSSI':-50}}, {'attributes':{'MAC':'02:15:b2:00:01:00','RSSI':-50}}, {'attributes':{'MAC':'02:15:b2:00:01:00','RSSI':-50}}" ;
//
//        JsonElement root = new JsonParser().parse(testJson); //Convert the input stream to a json element
//        Gson gson = new GsonBuilder().create();
//
//        malakas.Attributes value = gson.fromJson(root, malakas.Attributes.class);

//        System.out.println(value);

        /////////////////// assuming that the knn obj list is made

        // make list of labels from
        ArrayList<String> testLblList = new ArrayList<>();
        for(knn_methods testerObj : testObjList)
        {
            //add macno to test features
            if( macnos.containsKey( testerObj.MAC ) )
            {
                testerObj.setMacNo((Integer) macnos.get(testerObj.MAC));
            String lbl = getEuclideanDistance(trainingObjList , testerObj);
            testerObj.label = lbl;
            testLblList.add(lbl) ;
            }
//            else
//            {
//                // set the macno to get large distance from the others, so set as max value of int
////                testerObj.setMacNo( Integer.MAX_VALUE );
//                //or not consider this value
//            }

            //get label and update label in object
        }

        String finLabel = classifier(testLblList);
        return finLabel;
    }

    public static void main_kNNApp(String[] args)
    {
        try
        {
            knnApp mainObj = new knnApp();

            ///////////// 4 lines below wont be needed
            ArrayList<knn_methods> kNN_Objs;
            Tuple<ArrayList<knn_methods> , Map> tup = mainObj.readJson();
            kNN_Objs = tup.getOne();
            Map macnos = tup.getTwo();

            // make test object for testing
            knn_methods test_obj = new knn_methods( "84:3d:c6:c7:ef:50",  -70);

            //set macno of test obj
            if( macnos.containsKey( test_obj.MAC )  )
            {
                test_obj.setMacNo( (Integer) macnos.get(test_obj.MAC) );
            }

            System.out.println( "the label is "+ mainObj.getEuclideanDistance(kNN_Objs , test_obj)  );

            // to run the final code only below line is needed
//            mainObj.knn_test_features();


        }
        catch(Exception e)
        {
            e.printStackTrace();
        }

    }
}

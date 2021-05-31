package knn;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.*;
import knn.malakas;
import knn.knn_methods;
import org.joda.time.LocalTime;

// @SuppressWarnings
public class App {
    
    private List<double[]> trainfeatures = new ArrayList<>();
	private List<String> trainlabel = new ArrayList<>();

	private List<double[]> testfeatures = new ArrayList<>();
	private List<String> testlabel = new ArrayList<>();
    
    // Scanner sc = new Scanner(System.in);
	// int knn_value = 1;
	// int totalNumberOfLabel = 0;
    public static double[] a = {1,2,3,4,5,6,7};
    public static double[] b = {10,20,30,40,50,60,70};
    
    public static void main(String[] args) throws Exception 
    {
        ////////////////// Read JSON ///////////////////
        String sURL = "https://services3.arcgis.com/jR9a3QtlDyTstZiO/ArcGIS/rest/services/Arduino_Table/FeatureServer/0/query?where=ObjectID%3E%3D0&outFields=MAC%2C+RSSI%2C+BSSID%2C+Room_ID+%2C+ObjectId+%2C+Time_Stamp+&returnIdsOnly=false&returnUniqueIdsOnly=false&returnCountOnly=false&returnDistinctValues=false&cacheHint=false&sqlFormat=none&f=pjson";
        URL url = new URL(sURL);

        // make url connection
        URLConnection request = url.openConnection();
        request.connect();
        System.out.println(request);

        // Convert to a JSON object to malakas class object print data
        JsonParser jp = new JsonParser(); //from gson
        JsonElement root = jp.parse(new InputStreamReader((InputStream) request.getContent())); //Convert the input stream to a json element
        JsonObject rootobj = root.getAsJsonObject()  ;

        String jsonString = root.toString();
        System.out.println("printing rootobj\n");
        Gson gson = new GsonBuilder().create();
        malakas value = gson.fromJson(root, malakas.class);

        malakas.Features[] text = value.features;
        System.out.println(text[text.length-1].attributes.MAC);
        System.out.println(text[text.length-1].attributes.Room_ID);
        System.out.println(text[text.length-1].attributes.RSSI);
        System.out.println(text[text.length-1].attributes.ObjectId);

        ////////////////////////////////////////////////////////// kNN implementation ////////////////////
        knn.knn_methods obj = new knn_methods();
        App ob = new App();

        double c = obj.getEuclideanDistance( App.a , App.b );
        System.out.println(c);
        // knn_methods obj = new knn_methods();
        // obj.printer();

    }
}


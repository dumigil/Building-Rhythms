package knn;

public class malakas
{


    public String objectIdFieldName;
    public uniqueIdField_ uniqueIdField;
    public String globalIdFieldName;
    public fields_[] fields;
    public Features[] features;


    static public class uniqueIdField_
    {
        public String name;
        public String isSystemMaintained;
    }

    static public class fields_
    {
        public String name;
        public String type;
        public String alias;
        public String sqlType;
        public String length;
        public String domain;
        public String defaultValue;
    }
    static class Features
    {
        public Attributes attributes;
    }

    static class Attributes
    {
        public String MAC;
        public String RSSI;
        public String BSSID;
        public String Room_ID;
        public String ObjectId;
        public String Time_Stamp;
    }
}




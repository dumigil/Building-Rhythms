package knn;
public class knn_methods {
    public String MAC;
    public double RSSI;
    public double uniqueId;
    public String label;
    public int macno;
    // constructor for those with label
    knn_methods(String mac, double signal, double unqID, String lbl)
    {
        this.MAC = mac;
        this.RSSI = signal;
        this.uniqueId = unqID;
        this.label = lbl;
    }
    //constructor without label
    knn_methods(String mac, double signal)
    {
        this.MAC = mac;
        this.RSSI = signal;
//        this.uniqueId = unqID;

    }

    public void setMacNo(int n)
    {
        this.macno = n;
    }

    public double getManhattanDistance(final double[] features1, final double[] features2)
    {
        double sum = 0;
        for (int i = 0; i < features1.length; i++)
        	//Applied Manhattan distance formula
            sum += Math.abs(features1[i] - features2[i]);
        return sum;
    }



    public void printer()
    {
        System.out.println("\n wtf dude! i am in this method \n"); //prints integer  
    }
    public void setMacNo(Integer n) {
    }



}

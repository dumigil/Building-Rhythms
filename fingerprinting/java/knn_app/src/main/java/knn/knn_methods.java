package knn;
public class knn_methods {
    public double getManhattanDistance(final double[] features1, final double[] features2)
    {
        double sum = 0;
        for (int i = 0; i < features1.length; i++)
        	//Applied Manhattan distance formula
            sum += Math.abs(features1[i] - features2[i]);
        return sum;
    }

    public double getEuclideanDistance( double[] features1,  double[] features2) 
    {
        double sum = 0;
        for (int i = 0; i < features1.length; i++)
        {  //System.out.println(features1[i]+" "+features2[i]);
            //applied Euclidean distance formula
            sum += Math.pow(features1[i] - features2[i], 2);
        }
        return Math.sqrt(sum);
    }

    public void printer()
    {
        System.out.println("\n wtf dude! i am in this method \n"); //prints integer  
    }



}

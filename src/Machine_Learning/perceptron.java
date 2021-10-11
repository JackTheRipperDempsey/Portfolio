import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

public class perceptron {

	public static void main (String[] args) throws IOException{
		
		// Parse the csv file and store the rows
		ArrayList<String[]> rows = new ArrayList<String[]>();
        BufferedReader reader = new BufferedReader(new FileReader(args[0]));
        String nextLine;
        while ((nextLine = reader.readLine()) != null){
        	String[] row = nextLine.split(",");
        	rows.add(row);
        }
        
        /* Store the attributes in one string array and the values in
        a 2D int array. Note that the indexing in the value array is
        such that the first index corresponds to that index's attribute
        in the attribute array.  There is one extra column at the end of
        the value array, which is the actual classification for that example.*/
       int numExamples = rows.size()-1;
       int numAttr = rows.get(0).length-1;
       String[] attributes = new String[numAttr];
       int[][] valueColumns = new int[rows.get(1).length][numExamples];
       for (int i = 0; i < rows.get(0).length; ++i){
    	   if (i < rows.get(0).length-1){
    		   attributes[i] = rows.get(0)[i];
       		}
       		for (int j = 1; j < numExamples+1; ++j){
       			valueColumns[i][j-1] = Integer.parseInt(rows.get(j)[i]);
       			
       			// Rescale to -1 v. 1 instead of 0 v. 1
       			if ((i == numAttr) && (valueColumns[i][j-1]==0)){
           			valueColumns[i][j-1] = -1;
       			}
       		}
       }	 
       
       reader.close();
       
       // Get the learning rate
       double learningRate = Double.parseDouble(args[2]);
       
       // Initialize the weights and bias to 0
       int numWeights = numAttr;
       double[] weights = new double[numWeights];
       Arrays.fill(weights, 0.0);
       double bias = 0.0;
       
       // Apply the perceptron learning algorithm
       // The outermost loop is just the number of iterations
       for (int i = 0; i < 100; ++i){
    	   
    	   int allRight = 1;
    	   // Loop over number of examples
    	   for (int j = 0; j < numExamples; ++j){
    		   
    		   // Perceptron activation
    		   double a = 0;
    		   // Expected value
    		   int y = valueColumns[numAttr][j];
    		   
    		   // Compute activation
    		   for (int k = 0; k < numAttr; ++k){
    			   a += valueColumns[k][j]*weights[k];
    		   }
    		   
    		   a += bias;
    		   
    		   // Determine whether activation was right or wrong for example
    		   if (a*valueColumns[numAttr][j] <= 0){
    			   if (a <= 0){
    				   a = -1;
    			   }
    			   else if (a > 0){
    				   a = 1;
    			   }
    			   allRight = 0;
    			   // If wrong ...
    			   // Update bias and weights
        		   for (int k = 0; k < numAttr; ++k){
        			  // weights[k] += learningRate*valueColumns[k][j]*y;
        			   weights[k] += learningRate*(y-a)*valueColumns[k][j];
        		   }
        		   bias += learningRate*(y-a);	   
    		   }
    	   }
    	   
    	   // Break out of loop if all examples activated correctly
    	   if (allRight == 1){
    		   break;
    	   }
       }
       
       // Write weights to file
       BufferedWriter writer = new BufferedWriter(new FileWriter(args[3]));
       nextLine = "";
       nextLine += String.format("%.5g", bias);
       writer.write(nextLine);
       
       for (int i = 0; i < numAttr; ++i){
    	   writer.newLine();
    	   nextLine = "";
    	   nextLine += attributes[i];
    	   nextLine += " ";
    	   if ((weights[i] < .0001) && (weights[i] > -.0001)){
    		   nextLine += String.format("0.0000");
    	   }
    	   else if ((weights[i] < 1) && (weights[i] > -1)){
    		   nextLine += String.format("%.4g", weights[i]);
    	   }
    	   else if ((weights[i] >= 10) || (weights[i] <= -10)){
    		   nextLine += String.format("%.6g", weights[i]);
    	   }
    	   else {
    		   nextLine += String.format("%.5g", weights[i]);
    	   }
    	   writer.write(nextLine);
       }
       
       writer.flush();
       writer.close();
       
       // Determine accuracy on training data
       int numCorrect = 0;
       int activation = 0;
       for (int i = 0; i < numExamples; ++i){
    	   nextLine = "";
    	   double testActivation = 0;
    	   for (int j = 0; j < numAttr; ++j){
    		   testActivation += weights[j]*valueColumns[j][i];
    	   }
    	   testActivation += bias;
    	   nextLine += testActivation;
    	   if (testActivation > 0){
    		   activation = 1;
    	   }
    	   else {
    		   activation = -1;
    	   }
    	   if (activation == valueColumns[numAttr][i]){
    		   ++numCorrect;
    	   }
       }
       double trainingAccuracy = ((double) numCorrect)/numExamples;
       
       // Read in test instances
		ArrayList<String[]> testRows = new ArrayList<String[]>();
        BufferedReader reader2 = new BufferedReader(new FileReader(args[1]));
        nextLine = "";
        while ((nextLine = reader2.readLine()) != null){
        	String[] row = nextLine.split(",");
        	testRows.add(row);
        }
        
        int numTests = testRows.size()-1;
        int[][] testColumns = new int[testRows.get(1).length][numTests];
        for (int i = 0; i < testRows.get(0).length; ++i){
        	for (int j = 1; j < numTests+1; ++j){
        		testColumns[i][j-1] = Integer.parseInt(testRows.get(j)[i]);
       			
       			// Rescale to -1 v. 1 instead of 0 v. 1
        		if ((i == numAttr) && (testColumns[i][j-1]==0)){
           			testColumns[i][j-1] = -1;
       			}
       		}
        }
        
       reader2.close();
       
       String testFile = "testPerformance.txt";
       if (args.length >= 5){
    	   testFile = args[4];
       }
       
       BufferedWriter writer2 = new BufferedWriter(new FileWriter(testFile));
       nextLine = "";
       nextLine += "Activations for test instances are";
       writer2.write(nextLine);
       writer2.newLine();
       
       // Run test cases and count how many are activated correctly
       numCorrect = 0;
       activation = 0;
       for (int i = 0; i < numTests; ++i){
    	   nextLine = "";
    	   double testActivation = 0;
    	   for (int j = 0; j < numAttr; ++j){
    		   testActivation += weights[j]*testColumns[j][i];
    	   }
    	   testActivation += bias;
    	   if ((testActivation < .0001) && (testActivation > -.0001)){
    		   nextLine += String.format("0.0000");
    	   }
    	   else if ((testActivation < 1) && (testActivation > -1)){
    		   nextLine += String.format("%.4g", testActivation);
    	   }
    	   else if ((testActivation >= 10) || (testActivation <= -10)){
    		   nextLine += String.format("%.6g", testActivation);
    	   }
    	   else {
    		   nextLine += String.format("%.5g", testActivation);
    	   }
    	   writer2.write(nextLine);
    	   if (testActivation > 0){
    		   activation = 1;
    	   }
    	   else {
    		   activation = -1;
    	   }
    	   if (activation == testColumns[numAttr][i]){
    		   ++numCorrect;
    	   }
    	   writer2.newLine();
       }
       
       // Write accuracy to file

       double testAccuracy = ((double) numCorrect)/numTests;
       writer2.newLine();
       nextLine = "";
       nextLine += "Training accuracy is";
       writer2.write(nextLine);
       writer2.newLine();
       nextLine = "";
       nextLine += trainingAccuracy;
       writer2.write(nextLine);
       writer2.newLine();
       writer2.newLine();
       nextLine = "";
       nextLine += "Test accuracy is";
       writer2.write(nextLine);
       writer2.newLine();
       nextLine = "";
       nextLine += testAccuracy;
       writer2.write(nextLine);
       
       writer2.flush();
       writer2.close();
       
       
	}
	
}

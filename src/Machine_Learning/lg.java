import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

public class lg {

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
       		}
       }	 
       
       reader.close();
       
       // Get the learning rate
       double learningRate = Double.parseDouble(args[2]);
       double sigma = Double.parseDouble(args[3]);
       
       // Initialize the weights and bias to 0
       int numWeights = numAttr;
       double[] weights = new double[numWeights];
       Arrays.fill(weights, 0.0);
       double bias = 0.0;
	   double gradient = 0;
	   
       // Apply the logistic regression training algorithm
       // Loop until gradient small enough to stop
       while(true){
    	   double gradientMag = 0;
    	   for (int i = 0; i < numWeights; ++i){
    		   gradient = 0;
    		   gradient -= weights[i]/Math.pow(sigma, 2);
    		   double weightSum = 0;
    		   double biasSum = 0;
    		   gradientMag = 0;
    		   for (int j = 0; j < numExamples; ++j){
        		   double sum2 = 0;
    			   for (int k = 0; k < numAttr; ++k){
        			   sum2 += weights[k]*valueColumns[k][j];		   
        		   }
    			   double probability = 0;
    			   probability += Math.exp(bias + sum2);
    			   probability /= (1+Math.exp(bias + sum2));
    			   double difference = valueColumns[numAttr][j]-probability;
    			   weightSum += valueColumns[i][j]*difference;
    			   biasSum += difference;
    		   }
			   gradient += learningRate*weightSum;
			   double oldWeight = weights[i];
			   weights[i] += gradient;
			   gradientMag += Math.pow(weights[i]-oldWeight, 2);
			  // Don't add w/sigma^2 to bias, only computed sum
			   bias += learningRate*biasSum;
    	   }
    	   // Second condition breaks out of loop if gradient == NaN
    	   if ((Math.abs(gradientMag) < .00001) ||(gradient!=gradient)){
    		   break;
    	   }
       }
       
       // Write weights to file
       BufferedWriter writer = new BufferedWriter(new FileWriter(args[4]));
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
       int trainCorrect = 0;
       double trainProbability = 0;
       for (int i = 0; i < numExamples; ++i){
      		nextLine = "";
      		trainProbability = 0;
      		double sum = 0;
      		for (int k = 0; k < numAttr; ++k){
      			sum += weights[k]*valueColumns[k][i];	
      		}
      		trainProbability = 0;
      		trainProbability += Math.exp(bias + sum);
      		trainProbability /= (1+Math.exp(bias + sum)); 
       		if (trainProbability > .5){
       			if (valueColumns[numAttr][i] == 1){
       				++trainCorrect;
       			}
       		}
       		else {
       			if (valueColumns[numAttr][i] == 0){
       				++trainCorrect;
       			}
       		}
       }
       double trainingAccuracy = ((double) trainCorrect)/numExamples;
       
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
        	}
        }
        
       reader2.close();
       
       String testFile = "testPerformance.txt";
       if (args.length >= 6){
    	   testFile = args[5];
       }
       
       BufferedWriter writer2 = new BufferedWriter(new FileWriter(testFile));
       nextLine = "";
       
       nextLine += "Probabilities that test instances are labeled 1 are:";
       System.out.println(nextLine);
       writer2.write(nextLine);
       writer2.newLine();
       
       int numCorrect = 0;
       double probability = 0;
       
       for (int i = 0; i < numTests; ++i){
       		nextLine = "";
       		probability = 0;
       		double sum = 0;
       		for (int k = 0; k < numAttr; ++k){
       			sum += weights[k]*testColumns[k][i];	
       		}
       		probability = 0;
       		probability += Math.exp(bias + sum);
       		probability /= (1+Math.exp(bias + sum)); 
       		
       		if ((probability < .0001) && (probability > -.0001)){
       			nextLine += String.format("0.0000");
       		}
       		else if (probability < 1){
       			if (probability > .99999){
       				nextLine += String.format("%.5g", probability);
       			}

       			else {
       				if (probability > .1){
       					nextLine += String.format("%.4g", probability);

       				}
       				else {
       					if (probability > .01){
       						nextLine += String.format("%.3g", probability);
       					}
       					else {
       						if (probability > .001){
       							nextLine += String.format("%.2g", probability);
       						}
       						else {
       							nextLine += String.format("%.1g", probability);
       						}
       					}
       				}
       			}
       		}
       		else if (probability >= 10){
       			nextLine += String.format("%.6g", probability);
       		}
       		else {
       			nextLine += String.format("%.5g", probability);
       		}
       		System.out.println(nextLine);
       		writer2.write(nextLine);
       		writer2.newLine();
       		if (probability > .5){
       			if (testColumns[numAttr][i] == 1){
       				++numCorrect;
       			}
       		}
       		else {
       			if (testColumns[numAttr][i] == 0){
       				++numCorrect;
       			}
       		}
       }
       
       double accuracy = ((double) numCorrect)/numTests;
       nextLine = "Assuming we assign a label of 1 to test instances with a probability" +
       		" greater than .5 and a label of 0 to those with a probability less than .5," +
       		" our accuracy is:";
       writer2.newLine();
       writer2.write(nextLine);
       writer2.newLine();
       System.out.println(nextLine);
       nextLine = "" + accuracy;
       System.out.println(nextLine);
       writer2.write(nextLine);
       writer2.newLine();
       nextLine = "";
       nextLine += "Training set accuracy is " + trainingAccuracy;
       System.out.println(nextLine);
       writer2.write(nextLine);
       writer2.flush();
       writer2.close();
	}
	
}


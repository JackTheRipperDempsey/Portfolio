import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

public class nb {
	
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
       
       double beta = Double.parseDouble(args[2]);
       
       // Set up arrays to hold weights and probabilities
       double[] weights = new double[numAttr];
       double[] probOneOne = new double[numAttr];
       double[] probOneZero = new double[numAttr];
       double[] probZeroZero = new double[numAttr];
       double[] probZeroOne = new double[numAttr];

       Arrays.fill(weights, 0.0);
       Arrays.fill(probOneOne, 0.0);
       Arrays.fill(probOneZero, 0.0);
       Arrays.fill(probZeroZero, 0.0);
       Arrays.fill(probZeroOne, 0.0);
       
       double baseLog = 0;
       
       int numOnes = 0;
       int numZeros = 0;
	   for (int j = 0; j < numExamples; ++j){
		   if (valueColumns[numAttr][j] == 1){
			   ++numOnes;
		   }
		   
		   else if (valueColumns[numAttr][j] == 0){
			   ++numZeros;
		   }
	   }
       
	   double oneProb = 0;
	   double zeroProb = 0;
	   
   	// Determine baseline probabilities

	   oneProb = numOnes + beta - 1;
	   oneProb /= (numOnes+numZeros+(2*beta)-2);
	   zeroProb = 1.0-oneProb;
	   
	   int numZeroZero = 0;
	   int numZeroOne = 0;
	   int numOneZero = 0;
	   int numOneOne = 0;
       
        for (int i = 0; i < numAttr; ++i){
        	numZeroZero = 0;
        	numZeroOne = 0;
        	numOneZero = 0;
        	numOneOne = 0;
        	
        	for (int j = 0; j < numExamples; ++j){
        		if (valueColumns[numAttr][j] == 1){
    			   if (valueColumns[i][j] == 1){
    				   ++numOneOne;
    			   }
    			   else if (valueColumns[i][j] == 0){
    				   ++numZeroOne;
    			   }
        		}
    		   
        		else if (valueColumns[numAttr][j] == 0){
     			   if (valueColumns[i][j] == 1){
    				   ++numOneZero;
    			   }
    			   else if (valueColumns[i][j] == 0){
    				   ++numZeroZero;
    			   }
        		}
        	}
        	
        	double pOneOne = 0;
        	double pZeroOne = 0;
        	double pOneZero= 0;
        	double pZeroZero = 0;
        	
        	// Determine conditional probabilities for features
        	pOneOne = numOneOne + beta - 1;
        	pOneOne /= (numOneOne+numZeroOne+(2*beta)-2);
        	pZeroOne = numZeroOne + beta - 1;
        	pZeroOne /= (numOneOne+numZeroOne+(2*beta)-2);

        	pOneZero = numOneZero + beta - 1;
        	pOneZero /= (numOneZero+numZeroZero+(2*beta)-2);
        	pZeroZero = numZeroZero + beta - 1;
        	pZeroZero /= (numOneZero+numZeroZero+(2*beta)-2);
        	
        	probOneOne[i] = pOneOne;
        	probZeroOne[i] = pZeroOne;
        	probZeroZero[i] = pZeroZero;
        	probOneZero[i] = pOneZero;
        	
       	} 
        
        // Update weights and baselog
        baseLog += Math.log(oneProb/zeroProb);
        for (int i = 0; i < numAttr;++i){
        	baseLog += Math.log(probZeroOne[i]/probZeroZero[i]);
        	weights[i]+=Math.log(probOneOne[i]/probOneZero[i]);
        	weights[i]-=Math.log(probZeroOne[i]/probZeroZero[i]);
        }
        
        BufferedWriter writer = new BufferedWriter(new FileWriter(args[3]));
        nextLine = "";
        nextLine += String.format("%.5g", baseLog);
        writer.write(nextLine);
        
        // Output weights
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
            if (args.length >= 5){
         	   testFile = args[4];
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
            	probability += baseLog;
            	for (int j = 0; j < numAttr; ++j){
            		if (testColumns[j][i] == 1){
            			probability += weights[j];
            		}
            	}
            	probability = 1/(1+Math.exp(-1*probability));
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
            writer2.flush();
            writer2.close();
	}

}

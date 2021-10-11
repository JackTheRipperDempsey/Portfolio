/*
 This is the classic naive Bayes algorithm - the only modification is that binary and continuously valued features are handled differently from one another, for obvious reasons.
 */

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

public class nbcombined {
	
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
       double[] probClass1One = new double[numAttr-11];
       double[] probClass1Zero = new double[numAttr-11];
       double[] probClass2One = new double[numAttr-11];
       double[] probClass2Zero = new double[numAttr-11];
       double[] probClass3One = new double[numAttr-11];
       double[] probClass3Zero = new double[numAttr-11];
       double[] probClass4One = new double[numAttr-11];
       double[] probClass4Zero = new double[numAttr-11];
       double[] probClass5One = new double[numAttr-11];
       double[] probClass5Zero = new double[numAttr-11];
       double[] probClass6One = new double[numAttr-11];
       double[] probClass6Zero = new double[numAttr-11];
       double[] probClass7One = new double[numAttr-11];
       double[] probClass7Zero = new double[numAttr-11];

       Arrays.fill(probClass1One, 0.0);
       Arrays.fill(probClass1Zero, 0.0);
       Arrays.fill(probClass2One, 0.0);
       Arrays.fill(probClass2Zero, 0.0);
       Arrays.fill(probClass3One, 0.0);
       Arrays.fill(probClass3Zero, 0.0);
       Arrays.fill(probClass4One, 0.0);
       Arrays.fill(probClass4Zero, 0.0);
       Arrays.fill(probClass5One, 0.0);
       Arrays.fill(probClass5Zero, 0.0);
       Arrays.fill(probClass6One, 0.0);
       Arrays.fill(probClass6Zero, 0.0);
       Arrays.fill(probClass7One, 0.0);
       Arrays.fill(probClass7Zero, 0.0);
              
       int numClass1 = 0;
       int numClass2 = 0;
       int numClass3 = 0;
       int numClass4 = 0;
       int numClass5 = 0;
       int numClass6 = 0;
       int numClass7 = 0;

	   for (int j = 0; j < numExamples; ++j){
		   if (valueColumns[numAttr][j] == 1){
			   ++numClass1;
		   }
		   
		   else if (valueColumns[numAttr][j] == 2){
			   ++numClass2;
		   }
		   
		   else if (valueColumns[numAttr][j] == 3){
			   ++numClass3;
		   }
		   
		   else if (valueColumns[numAttr][j] == 4){
			   ++numClass4;
		   }
		   
		   else if (valueColumns[numAttr][j] == 5){
			   ++numClass5;
		   }
		   
		   else if (valueColumns[numAttr][j] == 6){
			   ++numClass6;
		   }
		   
		   else if (valueColumns[numAttr][j] == 7){
			   ++numClass7;
		   }
	   }
       
	   double Class1Prob = 0;
	   double Class2Prob = 0;
	   double Class3Prob = 0;
	   double Class4Prob = 0;
	   double Class5Prob = 0;
	   double Class6Prob = 0;
	   double Class7Prob = 0;
	   
   	// Determine baseline probabilities

	   Class1Prob = numClass1 + beta - 1;
	   Class1Prob /= (numExamples+(7*beta)-7);
	   Class2Prob = numClass2 + beta - 1;
	   Class2Prob /= (numExamples+(7*beta)-7);
	   Class3Prob = numClass3 + beta - 1;
	   Class3Prob /= (numExamples+(7*beta)-7);
	   Class4Prob = numClass4 + beta - 1;
	   Class4Prob /= (numExamples+(7*beta)-7);
	   Class5Prob = numClass5 + beta - 1;
	   Class5Prob /= (numExamples+(7*beta)-7);
	   Class6Prob = numClass6 + beta - 1;
	   Class6Prob /= (numExamples+(7*beta)-7);
	   Class7Prob = numClass7 + beta - 1;
	   Class7Prob /= (numExamples+(7*beta)-7); 
	   
	 /*  Class1Prob = numClass1;
	   Class1Prob /= (numExamples);
	   Class2Prob = numClass2;
	   Class2Prob /= (numExamples);
	   Class3Prob = numClass3;
	   Class3Prob /= (numExamples);
	   Class4Prob = numClass4;
	   Class4Prob /= (numExamples);
	   Class5Prob = numClass5;
	   Class5Prob /= (numExamples);
	   Class6Prob = numClass6;
	   Class6Prob /= (numExamples);
	   Class7Prob = numClass7;
	   Class7Prob /= (numExamples);*/
	   
	   int numClass1One = 0;
	   int numClass1Zero = 0;
	   int numClass2One = 0;
	   int numClass2Zero = 0;
	   int numClass3One = 0;
	   int numClass3Zero = 0;
	   int numClass4One = 0;
	   int numClass4Zero = 0;
	   int numClass5One = 0;
	   int numClass5Zero = 0;
	   int numClass6One = 0;
	   int numClass6Zero = 0;
	   int numClass7One = 0;
	   int numClass7Zero = 0;
	   
	   double[] Class1Means = new double[10];
	   double[] Class2Means = new double[10];
	   double[] Class3Means = new double[10];
	   double[] Class4Means = new double[10];
	   double[] Class5Means = new double[10];
	   double[] Class6Means = new double[10];
	   double[] Class7Means = new double[10];
	   
	   double[] Class1Variance = new double[10];
	   double[] Class2Variance = new double[10];
	   double[] Class3Variance = new double[10];
	   double[] Class4Variance = new double[10];
	   double[] Class5Variance = new double[10];
	   double[] Class6Variance = new double[10];
	   double[] Class7Variance = new double[10];
	   
	   Arrays.fill(Class1Means,0.0);
	   Arrays.fill(Class2Means,0.0);
	   Arrays.fill(Class3Means,0.0);
	   Arrays.fill(Class4Means,0.0);
	   Arrays.fill(Class5Means,0.0);
	   Arrays.fill(Class6Means,0.0);
	   Arrays.fill(Class7Means,0.0);
	   
	   Arrays.fill(Class1Variance,0.0);
	   Arrays.fill(Class2Variance,0.0);
	   Arrays.fill(Class3Variance,0.0);
	   Arrays.fill(Class4Variance,0.0);
	   Arrays.fill(Class5Variance,0.0);
	   Arrays.fill(Class6Variance,0.0);
	   Arrays.fill(Class7Variance,0.0);
	   
	   for (int i = 1; i < 11; ++i){
		   for (int j = 0; j < numExamples; ++j){
			   if (valueColumns[numAttr][j] == 1){
				   Class1Means[i-1] += valueColumns[i][j];
			   }
			   else if (valueColumns[numAttr][j] == 2){
				   Class2Means[i-1] += valueColumns[i][j];
			   }
			   else if (valueColumns[numAttr][j] == 3){
				   Class3Means[i-1] += valueColumns[i][j];
			   }
			   else if (valueColumns[numAttr][j] == 4){
				   Class4Means[i-1] += valueColumns[i][j];
			   }
			   else if (valueColumns[numAttr][j] == 5){
				   Class5Means[i-1] += valueColumns[i][j];
			   }
			   else if (valueColumns[numAttr][j] == 6){
				   Class6Means[i-1] += valueColumns[i][j];
			   }
			   else if (valueColumns[numAttr][j] == 7){
				   Class7Means[i-1] += valueColumns[i][j];
			   }
		   }
	   }
	   
	   for (int i = 0; i < 10; ++i){
		   Class1Means[i] /= numClass1;
		   Class2Means[i] /= numClass2;
		   Class3Means[i] /= numClass3;
		   Class4Means[i] /= numClass4;
		   Class5Means[i] /= numClass5;
		   Class6Means[i] /= numClass6;
		   Class7Means[i] /= numClass7;
	   }
	   
	   for (int i = 1; i < 11; ++i){
		   for (int j = 0; j < numExamples; ++j){
			   if (valueColumns[numAttr][j] == 1){
				   double difference = Class1Means[i-1]-valueColumns[i][j];
				   Class1Variance[i-1] += Math.pow(difference,2);
			   }
			   else if (valueColumns[numAttr][j] == 2){
				   double difference = Class2Means[i-1]-valueColumns[i][j];
				   Class2Variance[i-1] += Math.pow(difference,2);
			   }
			   else if (valueColumns[numAttr][j] == 3){
				   double difference = Class3Means[i-1]-valueColumns[i][j];
				   Class3Variance[i-1] += Math.pow(difference,2);
			   }
			   else if (valueColumns[numAttr][j] == 4){
				   double difference = Class4Means[i-1]-valueColumns[i][j];
				   Class4Variance[i-1] += Math.pow(difference,2);
			   }
			   else if (valueColumns[numAttr][j] == 5){
				   double difference = Class5Means[i-1]-valueColumns[i][j];
				   Class5Variance[i-1] += Math.pow(difference,2);
			   }
			   else if (valueColumns[numAttr][j] == 6){
				   double difference = Class6Means[i-1]-valueColumns[i][j];
				   Class6Variance[i-1] += Math.pow(difference,2);
			   }
			   else if (valueColumns[numAttr][j] == 7){
				   double difference = Class7Means[i-1]-valueColumns[i][j];
				   Class7Variance[i-1] += Math.pow(difference,2);
				  // System.out.println(difference);
			   }
		   }
	   }
	   
	   for (int i = 0; i < 10; ++i){
		   Class1Variance[i] /= numClass1;
		   Class2Variance[i] /= numClass2;
		   Class3Variance[i] /= numClass3;
		   Class4Variance[i] /= numClass4;
		   Class5Variance[i] /= numClass5;
		   Class6Variance[i] /= numClass6;
		   Class7Variance[i] /= numClass7;
		   //System.out.println(Class7Variance[i]);
	   }
       
        for (int i = 11; i < numAttr; ++i){
        	numClass1One = 0;
     	   	numClass1Zero = 0;
     	   	numClass2One = 0;
     	   	numClass2Zero = 0;
     	   	numClass3One = 0;
     	   	numClass3Zero = 0;
     	   	numClass4One = 0;
     	   	numClass4Zero = 0;
     	   	numClass5One = 0;
     	   	numClass5Zero = 0;
     	   	numClass6One = 0;
     	   	numClass6Zero = 0;
     	   	numClass7One = 0;
     	   	numClass7Zero = 0;
        	
        	for (int j = 0; j < numExamples; ++j){
        		if (valueColumns[numAttr][j] == 1){
    			   if (valueColumns[i][j] == 1){
    				   ++numClass1One;
    			   }
    			   else if (valueColumns[i][j] == 0){
    				   ++numClass1Zero;
    			   }
        		}
    		   
        		else if (valueColumns[numAttr][j] == 2){
     			   if (valueColumns[i][j] == 1){
    				   ++numClass2One;
    			   }
    			   else if (valueColumns[i][j] == 0){
    				   ++numClass2Zero;
    			   }
        		}
        		
        		else if (valueColumns[numAttr][j] == 3){
      			   if (valueColumns[i][j] == 1){
     				   ++numClass3One;
     			   }
     			   else if (valueColumns[i][j] == 0){
     				   ++numClass3Zero;
     			   }
         		}
        		
        		else if (valueColumns[numAttr][j] == 4){
      			   if (valueColumns[i][j] == 1){
     				   ++numClass4One;
     			   }
     			   else if (valueColumns[i][j] == 0){
     				   ++numClass4Zero;
     			   }
         		}
        		
        		else if (valueColumns[numAttr][j] == 5){
      			   if (valueColumns[i][j] == 1){
     				   ++numClass5One;
     			   }
     			   else if (valueColumns[i][j] == 0){
     				   ++numClass5Zero;
     			   }
         		}
        		
        		else if (valueColumns[numAttr][j] == 6){
       			   if (valueColumns[i][j] == 1){
      				   ++numClass6One;
      			   }
      			   else if (valueColumns[i][j] == 0){
      				   ++numClass6Zero;
      			   }
          		}
        		
        		else if (valueColumns[numAttr][j] == 7){
       			   if (valueColumns[i][j] == 1){
      				   ++numClass7One;
      			   }
      			   else if (valueColumns[i][j] == 0){
      				   ++numClass7Zero;
      			   }
          		}
        	}
        	
        	double pClass1One = 0;
        	double pClass1Zero = 0;
        	double pClass2One = 0;
        	double pClass2Zero = 0;
        	double pClass3One = 0;
        	double pClass3Zero = 0;
        	double pClass4One = 0;
        	double pClass4Zero = 0;
        	double pClass5One = 0;
        	double pClass5Zero = 0;
        	double pClass6One = 0;
        	double pClass6Zero = 0;
        	double pClass7One = 0;
        	double pClass7Zero = 0;

        	
        	// Determine conditional probabilities for features
        	pClass1One = numClass1One + beta - 1;
        	pClass1One /= (numClass1+(2*beta)-2);
        	
        	pClass1Zero = numClass1Zero + beta - 1;
        	pClass1Zero /= (numClass1+(2*beta)-2);
        	
        	pClass2One = numClass2One + beta - 1;
        	pClass2One /= (numClass2+(2*beta)-2);
        	
        	pClass2Zero = numClass2Zero + beta - 1;
        	pClass2Zero /= (numClass2+(2*beta)-2);
        	
        	pClass3One = numClass3One + beta - 1;
        	pClass3One /= (numClass3+(2*beta)-2);
        	
        	pClass3Zero = numClass3Zero + beta - 1;
        	pClass3Zero /= (numClass3+(2*beta)-2);
        	
        	pClass4One = numClass4One + beta - 1;
        	pClass4One /= (numClass4+(2*beta)-2);
        	
        	pClass4Zero = numClass4Zero + beta - 1;
        	pClass4Zero /= (numClass4+(2*beta)-2);

        	pClass5One = numClass5One + beta - 1;
        	pClass5One /= (numClass5+(2*beta)-2);
        	
        	pClass5Zero = numClass5Zero + beta - 1;
        	pClass5Zero /= (numClass5+(2*beta)-2);
        	
        	pClass6One = numClass6One + beta - 1;
        	pClass6One /= (numClass6+(2*beta)-2);
        	
        	pClass6Zero = numClass6Zero + beta - 1;
        	pClass6Zero /= (numClass6+(2*beta)-2);
        	
        	pClass7One = numClass7One + beta - 1;
        	pClass7One /= (numClass7+(2*beta)-2);
        	
        	pClass7Zero = numClass7Zero + beta - 1;
        	pClass7Zero /= (numClass7+(2*beta)-2);
        	
        	probClass1One[i-11] = pClass1One;
        	probClass1Zero[i-11] = pClass1Zero;
        	
        	probClass2One[i-11] = pClass2One;
        	probClass2Zero[i-11] = pClass2Zero;
        	
        	probClass3One[i-11] = pClass3One;
        	probClass3Zero[i-11] = pClass3Zero;
        	
        	probClass4One[i-11] = pClass4One;
        	probClass4Zero[i-11] = pClass4Zero;
        	
        	probClass5One[i-11] = pClass5One;
        	probClass5Zero[i-11] = pClass5Zero;
        	
        	probClass6One[i-11] = pClass6One;
        	probClass6Zero[i-11] = pClass6Zero;
        	
        	probClass7One[i-11] = pClass7One;
        	probClass7Zero[i-11] = pClass7Zero;
	
       	} 
        
        int numCorrect = 0;
        double trainingAccuracy = 0;

        for (int i = 0; i < numExamples; ++i){
        	 double probability1 = 0;
             double probability2 = 0;
             double probability3 = 0;
             double probability4 = 0;
             double probability5 = 0;
             double probability6 = 0;
             double probability7 = 0;
             
             probability1 += Math.log(Class1Prob);
             probability2 += Math.log(Class2Prob);
             probability3 += Math.log(Class3Prob);
             probability4 += Math.log(Class4Prob);
             probability5 += Math.log(Class5Prob);
             probability6 += Math.log(Class6Prob);
             probability7 += Math.log(Class7Prob);
             
             for (int j = 1; j < 11; ++j){
             	double difference = Class1Means[j-1]-valueColumns[j][i];
           	 	probability1 -= (Math.pow(difference,2)/(2*Class1Variance[j-1]));
           	 	probability1 += Math.log(1/Math.sqrt(2*Math.PI*Class1Variance[j-1]));
           	 	
           	 	
           	 	difference = Class2Means[j-1]-valueColumns[j][i];
           	 	probability2 -= (Math.pow(difference,2)/(2*Class2Variance[j-1]));
           	 	probability2 += Math.log(1/Math.sqrt(2*Math.PI*Class2Variance[j-1]));
           	 	
           	 	
           	 	difference = Class3Means[j-1]-valueColumns[j][i];
           	 	probability3 -= (Math.pow(difference,2)/(2*Class3Variance[j-1]));
           	 	probability3 += Math.log(1/Math.sqrt(2*Math.PI*Class3Variance[j-1]));
           	 	
           	 	
           	 	difference = Class4Means[j-1]-valueColumns[j][i];
           	 	probability4 -= (Math.pow(difference,2)/(2*Class4Variance[j-1]));
           	 	probability4 += Math.log(1/Math.sqrt(2*Math.PI*Class4Variance[j-1]));
           	 	
           	 	
           	 	difference = Class5Means[j-1]-valueColumns[j][i];
           	 	probability5 -= (Math.pow(difference,2)/(2*Class5Variance[j-1]));
           	 	probability5 += Math.log(1/Math.sqrt(2*Math.PI*Class5Variance[j-1]));
           	 	
           	 	
           	 	difference = Class6Means[j-1]-valueColumns[j][i];
           	 	probability6 -= (Math.pow(difference,2)/(2*Class6Variance[j-1]));
           	 	probability6 += Math.log(1/Math.sqrt(2*Math.PI*Class6Variance[j-1]));
           	 	
           	 	
           	 	difference = Class7Means[j-1]-valueColumns[j][i];
           	 	probability7 -= (Math.pow(difference,2)/(2*Class7Variance[j-1]));
           	 	probability7 += Math.log(1/Math.sqrt(2*Math.PI*Class7Variance[j-1]));
           	 	
             }

             for (int j = 11; j < numAttr; ++j){
            	 if (valueColumns[j][i] == 1){
            		 probability1 += Math.log(probClass1One[j-11]);
            		 probability2 += Math.log(probClass2One[j-11]);
            		 probability3 += Math.log(probClass3One[j-11]);
            		 probability4 += Math.log(probClass4One[j-11]);
            		 probability5 += Math.log(probClass5One[j-11]);
            		 probability6 += Math.log(probClass6One[j-11]);
            		 probability7 += Math.log(probClass7One[j-11]);
            	 }
            	/* else if (valueColumns[j][i] == 0){
            		 probability1 += Math.log(probClass1Zero[j-11]);
            		 probability2 += Math.log(probClass2Zero[j-11]);
            		 probability3 += Math.log(probClass3Zero[j-11]);
            		 probability4 += Math.log(probClass4Zero[j-11]);
            		 probability5 += Math.log(probClass5Zero[j-11]);
            		 probability6 += Math.log(probClass6Zero[j-11]);
            		 probability7 += Math.log(probClass7Zero[j-11]);
            	 } */
             }
             
             int label = 0;
             
             if ((probability1 >= probability2) && (probability1 >= probability3) &&
            		 (probability1 >= probability4) && (probability1 >= probability5) &&
            		 (probability1 >= probability6) && (probability1 >= probability7)){
            			 
            	 label = 1;
             }
             
             else if ((probability2 >= probability1) && (probability2 >= probability3) &&
            		 (probability2 >= probability4) && (probability2 >= probability5) &&
            		 (probability2 >= probability6) && (probability2 >= probability7)){
            	 
            	 label = 2;
             }
             
             else if ((probability3 >= probability1) && (probability3 >= probability2) &&
            		 (probability3 >= probability4) && (probability3 >= probability5) &&
            		 (probability3 >= probability6) && (probability3 >= probability7)){
            	 
            	 label = 3;
             }
             
             else if ((probability4 >= probability1) && (probability4 >= probability3) &&
            		 (probability4 >= probability2) && (probability4 >= probability5) &&
            		 (probability4 >= probability6) && (probability4 >= probability7)){
            	 
            	 label = 4;
             }
             
             else if ((probability5 >= probability1) && (probability5 >= probability3) &&
            		 (probability5 >= probability4) && (probability5 >= probability2) &&
            		 (probability5 >= probability6) && (probability5 >= probability7)){
            	 
            	 label = 5;
             }
             
             else if ((probability6 >= probability1) && (probability6 >= probability3) &&
            		 (probability6 >= probability4) && (probability6 >= probability2) &&
            		 (probability6 >= probability5) && (probability6 >= probability7)){
            	 
            	 label = 6;
             }
             
             else if ((probability7 >= probability1) && (probability7 >= probability3) &&
            		 (probability7 >= probability4) && (probability7 >= probability2) &&
            		 (probability7 >= probability6) && (probability7 >= probability5)){
            	 
            	 label = 7;
             }
             
             if (label == valueColumns[numAttr][i]){
            	 ++numCorrect;
             }
             
        }
        
        trainingAccuracy += ((double) numCorrect)/numExamples;
        System.out.println("Training accuracy is " + trainingAccuracy);
        
        BufferedWriter writer = new BufferedWriter(new FileWriter(args[3]));
        nextLine = "";
        nextLine += "Id,Cover_Type";
        writer.write(nextLine);
        writer.newLine();
        
        rows = null;
        valueColumns = null;
        
     // Read in test instances
     		//ArrayList<String[]> testRows = new ArrayList<String[]>();
             BufferedReader reader2 = new BufferedReader(new FileReader(args[1]));
           /*  nextLine = "";
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
             } */
            
            for (int l = 0; l < 12; ++l){ 
            
     		String[][] testRows = new String[50000][55];
             
     		boolean outBreak = false;
     		boolean inBreak = false;

             nextLine = "";
             for (int i = 0; i < 50000; ++i){
             	nextLine = reader2.readLine();
             	if (nextLine == null){
             		outBreak = true;
             		break;
             	}
              	String[] row = nextLine.split(",");
              	testRows[i] = row;
             }
             
             int counter = 0;
             int numTests = 50000-1;
             int[][] testColumns = new int[testRows[1].length][numTests+1];
             for (int i = 0; i < testRows[0].length-1; ++i){
            	 int x;
            	 int y;
            	 if (l == 0){
            		 x = 1;
            		 y = numTests + 1;
            	 }
            	 else {
            		 x = 0;
            		 y = numTests + 1;
            	 }
             	for (int j = x; j < y; ++j){
             		if (testRows[j][i] == null){
             			//System.out.println(testRows[j-1][0]);
             			inBreak = true;
             			break;
             			/*System.out.println(i);
             			System.out.println(j);
             			System.out.println(l);*/
             		}
             		if (l == 0){
             			testColumns[i][j-1] = Integer.parseInt(testRows[j][i]);
             		}
             		else {
             			testColumns[i][j] = Integer.parseInt(testRows[j][i]);
             		}
             		if (i == 0){
             			++counter;
             		}
            	}
             	if (inBreak == true){
             		break;
             	} 
             }

            System.out.println(testColumns[0][counter-1]);
            
            numCorrect = 0;
           
            // for (int i = 0; i < numTests; ++i){
            
            for (int i = 0; i < counter; ++i){
           	 	double probability1 = 0;
                double probability2 = 0;
                double probability3 = 0;
                double probability4 = 0;
                double probability5 = 0;
                double probability6 = 0;
                double probability7 = 0;
                
                probability1 += Math.log(Class1Prob);
                probability2 += Math.log(Class2Prob);
                probability3 += Math.log(Class3Prob);
                probability4 += Math.log(Class4Prob);
                probability5 += Math.log(Class5Prob);
                probability6 += Math.log(Class6Prob);
                probability7 += Math.log(Class7Prob);
                
                for (int j = 1; j < 11; ++j){
          	 		
            	   	double difference = Class1Means[j-1]-testColumns[j][i];
              	 	probability1 -= (Math.pow(difference,2)/(2*Class1Variance[j-1]));
              	 	probability1 += Math.log(1/Math.sqrt(2*Math.PI*Class1Variance[j-1]));
              	 	
              	 	
              	 	difference = Class2Means[j-1]-testColumns[j][i];
              	 	probability2 -= (Math.pow(difference,2)/(2*Class2Variance[j-1]));
              	 	probability2 += Math.log(1/Math.sqrt(2*Math.PI*Class2Variance[j-1]));
              	 	
              	 	
              	 	difference = Class3Means[j-1]-testColumns[j][i];
              	 	probability3 -= (Math.pow(difference,2)/(2*Class3Variance[j-1]));
              	 	probability3 += Math.log(1/Math.sqrt(2*Math.PI*Class3Variance[j-1]));
              	 	
              	 	
              	 	difference = Class4Means[j-1]-testColumns[j][i];
              	 	probability4 -= (Math.pow(difference,2)/(2*Class4Variance[j-1]));
              	 	probability4 += Math.log(1/Math.sqrt(2*Math.PI*Class4Variance[j-1]));
              	 	
              	 	
              	 	difference = Class5Means[j-1]-testColumns[j][i];
              	 	probability5 -= (Math.pow(difference,2)/(2*Class5Variance[j-1]));
              	 	probability5 += Math.log(1/Math.sqrt(2*Math.PI*Class5Variance[j-1]));
              	 	
              	 	
              	 	difference = Class6Means[j-1]-testColumns[j][i];
              	 	probability6 -= (Math.pow(difference,2)/(2*Class6Variance[j-1]));
              	 	probability6 += Math.log(1/Math.sqrt(2*Math.PI*Class6Variance[j-1]));
              	 	
              	 	
              	 	difference = Class7Means[j-1]-testColumns[j][i];
              	 	probability7 -= (Math.pow(difference,2)/(2*Class7Variance[j-1]));
              	 	probability7 += Math.log(1/Math.sqrt(2*Math.PI*Class7Variance[j-1]));
              	 	
               }
               

                for (int j = 11; j < numAttr; ++j){
               	 if (testColumns[j][i] == 1){
               		 probability1 += Math.log(probClass1One[j-11]);
               		 probability2 += Math.log(probClass2One[j-11]);
               		 probability3 += Math.log(probClass3One[j-11]);
               		 probability4 += Math.log(probClass4One[j-11]);
               		 probability5 += Math.log(probClass5One[j-11]);
               		 probability6 += Math.log(probClass6One[j-11]);
               		 probability7 += Math.log(probClass7One[j-11]);
               	 } 
               	/* else if (valueColumns[j][i] == 0){
               		 probability1 += Math.log(probClass1Zero[j-11]);
               		 probability2 += Math.log(probClass2Zero[j-11]);
               		 probability3 += Math.log(probClass3Zero[j-11]);
               		 probability4 += Math.log(probClass4Zero[j-11]);
               		 probability5 += Math.log(probClass5Zero[j-11]);
               		 probability6 += Math.log(probClass6Zero[j-11]);
               		 probability7 += Math.log(probClass7Zero[j-11]);
               	 	} */
                }
                
                int label = 0;
                
                if ((probability1 >= probability2) && (probability1 >= probability3) &&
               		 (probability1 >= probability4) && (probability1 >= probability5) &&
               		 (probability1 >= probability6) && (probability1 >= probability7)){
               			 
               	 	label = 1;
                }
                
                else if ((probability2 >= probability1) && (probability2 >= probability3) &&
               		 (probability2 >= probability4) && (probability2 >= probability5) &&
               		 (probability2 >= probability6) && (probability2 >= probability7)){
               	 
               	 	label = 2;
                }
                
                else if ((probability3 >= probability1) && (probability3 >= probability2) &&
               		 (probability3 >= probability4) && (probability3 >= probability5) &&
               		 (probability3 >= probability6) && (probability3 >= probability7)){
               	 
               	 	label = 3;
                }
                
                else if ((probability4 >= probability1) && (probability4 >= probability3) &&
               		 (probability4 >= probability2) && (probability4 >= probability5) &&
               		 (probability4 >= probability6) && (probability4 >= probability7)){
               	 
                	label = 4;
                }
                
                else if ((probability5 >= probability1) && (probability5 >= probability3) &&
               		 (probability5 >= probability4) && (probability5 >= probability2) &&
               		 (probability5 >= probability6) && (probability5 >= probability7)){
               	 
               	 	label = 5;
                }
                
                else if ((probability6 >= probability1) && (probability6 >= probability3) &&
               		 (probability6 >= probability4) && (probability6 >= probability2) &&
               		 (probability6 >= probability5) && (probability6 >= probability7)){
               	 
                	label = 6;
                }
                
                else if ((probability7 >= probability1) && (probability7 >= probability3) &&
               		 (probability7 >= probability4) && (probability7 >= probability2) &&
               		 (probability7 >= probability6) && (probability7 >= probability5)){
               	 
               	 	label = 7;
                }
                
             /*   if (label == testColumns[numAttr][i]){
               	 	++numCorrect;
                }  */
                
                nextLine = "";
                nextLine += testColumns[0][i];
                nextLine += ",";
                nextLine += label;
               // System.out.println(label);
                writer.write(nextLine);
                if (!((l==11) && i == (counter-1))){
                	writer.newLine();
                }
                
           }
            
            if (outBreak == true){
              	 reader2.close();
              	 break;
               }
             }
            
          //  double testAccuracy = ((double) numCorrect)/numTests; 
           // System.out.println("Test accuracy is " + testAccuracy); 

            writer.flush();
            writer.close();
	}

}
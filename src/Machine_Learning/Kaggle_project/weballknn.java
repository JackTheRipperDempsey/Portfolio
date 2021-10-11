/*
 This is a combination of the weighting and e-ball adaptations of the k-nearest neighbor algorithm, where more predictive features are weighted more heavily than less predictive ones, and only neighbors within a certain range of the test instance are allowed to vote.
 */
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

public class weballknn {
    
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
        double[][] valueColumns = new double[rows.get(1).length][numExamples];
        for (int i = 0; i < rows.get(0).length; ++i){
            if (i < rows.get(0).length-1){
                attributes[i] = rows.get(0)[i];
            }
            for (int j = 1; j < numExamples+1; ++j){
                valueColumns[i][j-1] = Double.parseDouble(rows.get(j)[i]);
            }
        }
        
        reader.close();
        
        int K = Integer.parseInt(args[2]);
        double[][] nearestNeighbors = new double[K][2];
        double[] weights = new double[12];
        weights[0] = 6;
        weights[1] = 2;
        weights[2] = 20;
        weights[3] = 2;
        weights[4] = 2;
        weights[5] = 2;
        weights[6] = 2;
        weights[7] = 2;
        weights[8] = 2;
        weights[9] = 2;
        weights[10] = 600;
        weights[11] = 600;
        
       /* int numCorrect = 0;
        
        double trainingAccuracy = 0;
        for (int i = 0; i < numExamples; ++i){
            for (int m = 0; m < K; ++m){
                Arrays.fill(nearestNeighbors[m], -1.0);
            }
            for (int j = 0; j < numExamples; ++j){
                if (i == j){
                    continue;
                }
                double distance = 0;
                for (int k = 1; k < numAttr; ++k){
                    double weight;
                    if (k > 14){
                        weight = weights[11];
                    }
                    else if (k > 10){
                        weight = weights[10];
                    }
                    else {
                        weight = weights[k-1];
                    } 
                    double difference = valueColumns[k][i]-valueColumns[k][j];
                    difference *= weight;
                    double nextDistance = Math.pow(difference, 2);
                    distance += nextDistance;
                }
                distance = Math.sqrt(distance);
                int worstNeighbor = -1;
                double worstDistance = -1;
                boolean assigned  = false;
                for (int l = 0; l < K; ++l){
                    if (nearestNeighbors[l][0] == -1.0){
                        nearestNeighbors[l][0] = j;
                        nearestNeighbors[l][1] = distance;
                        assigned = true;
                        break;
                    }
                    else if (nearestNeighbors[l][1] > worstDistance){
                        worstNeighbor = l;
                        worstDistance = nearestNeighbors[l][1];
                    }
                }
                if ((!assigned) && (distance < worstDistance)){
                    nearestNeighbors[worstNeighbor][0] = j;
                    nearestNeighbors[worstNeighbor][1] = distance;
                }
            }
            
            double bestDistance = -1;
            for (int l = 0; l < K; ++l){
            	if ((bestDistance == -1)|| (nearestNeighbors[l][1] < bestDistance)){
            		bestDistance = nearestNeighbors[l][1];
            	}
            }
            
            int numClass1 = 0;
            int numClass2 = 0;
            int numClass3 = 0;
            int numClass4 = 0;
            int numClass5 = 0;
            int numClass6 = 0;
            int numClass7 = 0;
            
            for (int j = 0; j < K; ++j){
            	if (nearestNeighbors[j][1] > (1.05*bestDistance)){
            		continue;
            	}
                int index = (int) nearestNeighbors[j][0];
                if (valueColumns[numAttr][index] == 1){
                    ++numClass1;
                }
                else if (valueColumns[numAttr][index] == 2){
                    ++numClass2;
                }
                else if (valueColumns[numAttr][index] == 3){
                    ++numClass3;
                }
                else if (valueColumns[numAttr][index] == 4){
                    ++numClass4;
                }
                else if (valueColumns[numAttr][index] == 5){
                    ++numClass5;
                }
                else if (valueColumns[numAttr][index] == 6){
                    ++numClass6;
                }
                else if (valueColumns[numAttr][index] == 7){
                    ++numClass7;
                }
            }
            
            int label = 0;
            
            if ((numClass1 >= numClass2) && (numClass1 >= numClass3) &&
                (numClass1 >= numClass4) && (numClass1 >= numClass5) &&
                (numClass1 >= numClass6) && (numClass1 >= numClass7)){
                
                label = 1;
            }
            
            else if ((numClass2 >= numClass1) && (numClass2 >= numClass3) &&
                     (numClass2 >= numClass4) && (numClass2 >= numClass5) &&
                     (numClass2 >= numClass6) && (numClass2 >= numClass7)){
                
                label = 2;
            }
            
            else if ((numClass3 >= numClass1) && (numClass3 >= numClass2) &&
                     (numClass3 >= numClass4) && (numClass3 >= numClass5) &&
                     (numClass3 >= numClass6) && (numClass3 >= numClass7)){
                
                label = 3;
            }
            
            else if ((numClass4 >= numClass1) && (numClass4 >= numClass3) &&
                     (numClass4 >= numClass2) && (numClass4 >= numClass5) &&
                     (numClass4 >= numClass6) && (numClass4 >= numClass7)){
                
                label = 4;
            }
            
            else if ((numClass5 >= numClass1) && (numClass5 >= numClass3) &&
                     (numClass5 >= numClass4) && (numClass5 >= numClass2) &&
                     (numClass5 >= numClass6) && (numClass5 >= numClass7)){
                
                label = 5;
            }
            
            else if ((numClass6 >= numClass1) && (numClass6 >= numClass3) &&
                     (numClass6 >= numClass4) && (numClass6 >= numClass2) &&
                     (numClass6 >= numClass5) && (numClass6 >= numClass7)){
                
                label = 6;
            }
            
            else if ((numClass7 >= numClass1) && (numClass7 >= numClass3) &&
                     (numClass7 >= numClass4) && (numClass7 >= numClass2) &&
                     (numClass7 >= numClass6) && (numClass7 >= numClass5)){
                label = 7;
            }
            
            if (valueColumns[numAttr][i] == label){
                ++numCorrect;
            }
            
            System.out.println(i);
        }
        
        trainingAccuracy = ((double) numCorrect)/numExamples;
        System.out.println("Training accuracy is " + trainingAccuracy); */
        
         BufferedWriter writer = new BufferedWriter(new FileWriter(args[3]));
         nextLine = "";
         nextLine += "Id,Cover_Type";
         writer.write(nextLine);
         
         BufferedReader reader2 = new BufferedReader(new FileReader(args[1]));
         
         nextLine = reader2.readLine();
         
       //  numCorrect = 0;
      //   int numTests = 0;
         
         while ((nextLine = reader2.readLine()) != null){
         
        	// ++numTests;
        	 String[] row = nextLine.split(",");
        	 for (int m = 0; m < K; ++m){
        		 Arrays.fill(nearestNeighbors[m], -1.0);
        	 }
        	 for (int j = 0; j < numExamples; ++j){
         
        		 double distance = 0;
        		 for (int k = 1; k < numAttr; ++k){
                     double weight;
                     if (k > 14){
                         weight = weights[11];
                     }
                     else if (k > 10){
                         weight = weights[10];
                     }
                     else {
                         weight = weights[k-1];
                     }
        			 double difference = Double.parseDouble(row[k])-valueColumns[k][j];
        			 difference *= weight;
        			 double nextDistance = Math.pow(difference, 2);
        			 distance += nextDistance;
        		 }
        		 distance = Math.sqrt(distance);
        		 int worstNeighbor = -1;
        		 double worstDistance = -1;
        		 boolean assigned  = false;
        		 for (int l = 0; l < K; ++l){
        			 if (nearestNeighbors[l][0] == -1.0){
        				 nearestNeighbors[l][0] = j;
        				 nearestNeighbors[l][1] = distance;
        				 assigned = true;
        				 break;
        			 }
        			 else if (nearestNeighbors[l][1] > worstDistance){
        				 worstNeighbor = l;
        				 worstDistance = nearestNeighbors[l][1];
        			 }
        		 }
        	 if ((!assigned) && (distance < worstDistance)){
        		 nearestNeighbors[worstNeighbor][0] = j;
        		 nearestNeighbors[worstNeighbor][1] = distance;
        	 }
         }
        	 
         double bestDistance = -1;
         	for (int l = 0; l < K; ++l){
         		if ((bestDistance == -1)|| (nearestNeighbors[l][1] < bestDistance)){
             		bestDistance = nearestNeighbors[l][1];
             	}
             }
         
         int numClass1 = 0;
         int numClass2 = 0;
         int numClass3 = 0;
         int numClass4 = 0;
         int numClass5 = 0;
         int numClass6 = 0;
         int numClass7 = 0;
         
         for (int j = 0; j < K; ++j){
         	if (nearestNeighbors[j][1] > (1.01*bestDistance)){
        		continue;
        	}
         int index = (int) nearestNeighbors[j][0];
         if (valueColumns[numAttr][index] == 1){
         ++numClass1;
         }
         else if (valueColumns[numAttr][index] == 2){
         ++numClass2;
         }
         else if (valueColumns[numAttr][index] == 3){
         ++numClass3;
         }
         else if (valueColumns[numAttr][index] == 4){
         ++numClass4;
         }
         else if (valueColumns[numAttr][index] == 5){
         ++numClass5;
         }
         else if (valueColumns[numAttr][index] == 6){
         ++numClass6;
         }
         else if (valueColumns[numAttr][index] == 7){
         ++numClass7;
         }
         }
         
         int label = 0;
         
         if ((numClass1 >= numClass2) && (numClass1 >= numClass3) &&
         (numClass1 >= numClass4) && (numClass1 >= numClass5) &&
         (numClass1 >= numClass6) && (numClass1 >= numClass7)){
         
         label = 1;
         }
         
         else if ((numClass2 >= numClass1) && (numClass2 >= numClass3) &&
         (numClass2 >= numClass4) && (numClass2 >= numClass5) &&
         (numClass2 >= numClass6) && (numClass2 >= numClass7)){
       	 
         label = 2;
         }
         
         else if ((numClass3 >= numClass1) && (numClass3 >= numClass2) &&
         (numClass3 >= numClass4) && (numClass3 >= numClass5) &&
         (numClass3 >= numClass6) && (numClass3 >= numClass7)){
       	 
         label = 3;
         }
         
         else if ((numClass4 >= numClass1) && (numClass4 >= numClass3) &&
         (numClass4 >= numClass2) && (numClass4 >= numClass5) &&
         (numClass4 >= numClass6) && (numClass4 >= numClass7)){
       	 
        	label = 4;
         }
         
         else if ((numClass5 >= numClass1) && (numClass5 >= numClass3) &&
         (numClass5 >= numClass4) && (numClass5 >= numClass2) &&
         (numClass5 >= numClass6) && (numClass5 >= numClass7)){
       	 
         label = 5;
         }
         
         else if ((numClass6 >= numClass1) && (numClass6 >= numClass3) &&
         (numClass6 >= numClass4) && (numClass6 >= numClass2) &&
         (numClass6 >= numClass5) && (numClass6 >= numClass7)){
       	 
        	label = 6;
         }
         
         else if ((numClass7 >= numClass1) && (numClass7 >= numClass3) &&
         (numClass7 >= numClass4) && (numClass7 >= numClass2) &&
         (numClass7 >= numClass6) && (numClass7 >= numClass5)){
         label = 7;
         }
         
       /*  if (label == Integer.parseInt(row[numAttr])){
        	++numCorrect;
         }  */
         
         nextLine = "";
         nextLine += Integer.parseInt(row[0]);
         nextLine += ",";
         nextLine += label;
         writer.newLine();
         writer.write(nextLine);
         writer.flush();
         }
         
         
       /*  double testAccuracy = ((double) numCorrect)/numTests; 
         System.out.println("Test accuracy is " + testAccuracy); */
         
         writer.flush();
         writer.close();
         reader2.close(); 
    }
    
}
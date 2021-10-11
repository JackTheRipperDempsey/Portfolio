/*
 This is a simple algorithm for getting the best of three other learning methods by taking their combined outputs and having them vote the classification for a set of test instances.
 */

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.FileReader;

public class blender {

	public static void main(String[] args) throws IOException{
        
        BufferedReader reader = new BufferedReader(new FileReader(args[0]));
        BufferedReader reader2 = new BufferedReader(new FileReader(args[1]));
        BufferedReader reader3 = new BufferedReader(new FileReader(args[2]));

        
 		String nextLine1 = reader.readLine();
 		String nextLine2 = reader2.readLine();
 		String nextLine3 = reader3.readLine();

        BufferedWriter writer = new BufferedWriter(new FileWriter(args[3]));
        String nextLine = "Id,Cover_Type";
        writer.write(nextLine);
        
      //  int numSame = 0;
      //  int numDiff = 0;
        for (int i = 15121; i <= 581012; ++i){
        	writer.newLine();
        	nextLine1 = reader.readLine();
        	nextLine2 = reader2.readLine();
        	nextLine3 = reader3.readLine();

        	//System.out.println(nextLine1);
        	String[] rows1 = nextLine1.split(",");
        	String[] rows2 = nextLine2.split(",");
        	String[] rows3 = nextLine3.split(",");

        	nextLine = "";
        	nextLine += i;
        	nextLine += ",";
        	/*if (Integer.parseInt(rows1[1]) == Integer.parseInt(rows2[1])){
        		nextLine += Integer.parseInt(rows1[1]);
        		//++numSame;
        	}
        	else {
        		if (Double.parseDouble(rows2[2]) >= .9995){
        			nextLine += Integer.parseInt(rows2[1]);
        		}
        		else {
            		nextLine += Integer.parseInt(rows1[1]);
        		}
        	} */
        	
        	if (Integer.parseInt(rows1[1]) == Integer.parseInt(rows2[1])){
        		nextLine += Integer.parseInt(rows1[1]);
        	}
        	
        	else {
        		if (Integer.parseInt(rows1[1]) == Integer.parseInt(rows3[1])){
        			nextLine += Integer.parseInt(rows1[1]);
        		}
        		else if (((Integer.parseInt(rows2[1]) == 2) || (Integer.parseInt(rows2[1]) == 1)) && 
        				!((Integer.parseInt(rows1[1]) == 2) )){
        		//else if ((Integer.parseInt(rows2[1]) == Integer.parseInt(rows3[1])) && (Double.parseDouble(rows3[2]) > .8)){
        		//else if (Integer.parseInt(rows2[1]) == Integer.parseInt(rows3[1])){
        			nextLine += Integer.parseInt(rows2[1]);
        		}
        		else if (((Integer.parseInt(rows3[1]) == 2) || (Integer.parseInt(rows3[1]) == 1)) && 
        				!((Integer.parseInt(rows1[1]) == 2) ||  (Integer.parseInt(rows1[1]) == 1))){
        		//else if ((Integer.parseInt(rows2[1]) == Integer.parseInt(rows3[1])) && (Double.parseDouble(rows3[2]) > .8)){
        		//else if (Integer.parseInt(rows2[1]) == Integer.parseInt(rows3[1])){
        			nextLine += Integer.parseInt(rows3[1]);
        		}
        		else {
        			nextLine += Integer.parseInt(rows1[1]);
        		}
        	}
        	writer.write(nextLine); 
        }
        reader.close();
        reader2.close();
        reader3.close();
    /*    double accuracy = ((double) numSame)/(numSame+numDiff);
        System.out.println(numSame);
        System.out.println(numDiff);

        System.out.println(accuracy); */
        writer.flush();
        writer.close();
	}
	
}


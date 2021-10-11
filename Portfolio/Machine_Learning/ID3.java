import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;

/* 
 * This is the class where we implement the ID3 algorithm
 * and its helper methods.
 * */

public class ID3 {

	public static void main(String[] args) throws IOException{
		
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
        
        // Knowing how many zeros and ones are in the original classification 
        // set will allow us to perform a chi-square test later
    	int baseLineZeros = 0;
    	int baseLineOnes = 0;

    	for (int k = 0; k < numExamples; ++k){
    		if (valueColumns[numAttr][k] == 0){
    			++baseLineZeros;
    		}
    		else if (valueColumns[numAttr][k] ==1){
    			++baseLineOnes;
    		}
    	}
		
        BufferedWriter writer = new BufferedWriter(new FileWriter(args[2]));
		
    	// Looked this value up - it's the 1% threshold for cases
    	// where there's one degree of freedom
    	double alpha = 6.635;
    	
    	// Allow different values to be entered as arguments if desired
    	// alpha = Integer.parseInt(args[3]);
        
        // Form our tree
        ID3TreeNode tree = RunID3(valueColumns, attributes, baseLineZeros, baseLineOnes, alpha);
    	// Write our tree to file
        saveModel(0,writer,tree); 
        BufferedReader reader2 = new BufferedReader(new FileReader(args[1]));
        // Find the accuracy of our tree
        double accuracy = testID3(tree, reader2);
        reader2.close();

        nextLine = "";
        nextLine += accuracy;
        writer.newLine();
        writer.write(nextLine);
        
        // Clean up our writer
        writer.flush();
    	writer.close();
	}
	
	
	// This is the algorithm where we actual build our tree using the ID3 algorithm
	static ID3TreeNode RunID3(int[][] data, String[] attr, int baseLineZeros, int baseLineOnes, double alpha) throws IOException{
        int numPoints = data[0].length;
		int numAttr = attr.length;
		ID3TreeNode root = new ID3TreeNode();
        
        int allPos = 1;
        int allNeg = 1;
        int zeros = 0;
        int ones = 0;
        
        // Determine how many examples are classified as zero
        // and how many as one for this subset
        for (int i = 0; i < numPoints; ++i){
        	if (data[attr.length][i] == 0){
        		allPos = 0;
        		++zeros;
        	}
        	if (data[attr.length][i] == 1){
        		allNeg = 0;
        		++ones;
        	}
        }
        
        // Handle cases where all examples have the same value or 
        // there are no (more) attributes to split on
        if (allPos == 1){
        		root.setLabel(1);
        		root.setAttribute("leaf");
        		return root;
        }
        
        else if (allNeg == 1){
        		root.setLabel(0);
        		root.setAttribute("leaf");
        		return root;
        }
        
        else if (numAttr == 0){
        	if (zeros > ones){
        		root.setLabel(0);
        		root.setAttribute("leaf");
        		return root;
        	}
        	else {
        		root.setLabel(1);
        		root.setAttribute("leaf");
        		return root;
        	}
        }
        
        // Otherwise, we compute the maximum information gain and then determine
        // whether we should split again, and if so, on which attribute
        else{
        	double maxGain = calcGain(data[0],data[numAttr]);
        	int maxIndex = 0;
        	// Find attribute with max information gain
        	for (int k = 0; k < attr.length; ++k){
        		double tempGain = calcGain(data[k],data[numAttr]);
        		if (tempGain > maxGain){
        			maxGain = tempGain;
        			maxIndex = k;
        		}
        	}
        	root.setAttribute(attr[maxIndex]);
        	
        	// Find set of attributes we haven't split on yet
        	String[] newAttr = new String[numAttr-1];
        	int index = 0;
        	for (int k = 0; k < attr.length; ++k){
        		if (k == maxIndex){
        			continue;
        		}
        		else{
        			newAttr[index] = attr[k];
        			++index;
        		}
        	}
        	// Find size of partitioned data sets
        	int maxZeros = 0;
        	int maxOnes = 0;
        	for (int k = 0; k < numPoints; ++k){
        		if (data[maxIndex][k] == 0){
        			++maxZeros;
        		}
        		else if (data[maxIndex][k] ==1){
        			++maxOnes;
        		}
        	}
        	
        	// Find data sets we'll pass if and when we choose to 
        	// call the function recursively for the left and right branches
        	int[][] zerosData = new int[numAttr][maxZeros];
        	int[][] onesData = new int[numAttr][maxOnes];

        	index = 0;
        	int zerosIndex1 = 0;
        	int zerosIndex2 = 0;
        	int onesIndex1 = 0;
        	int onesIndex2 = 0;
        	for (int k = 0; k < numAttr+1; ++k){
        		if (k == maxIndex){
        			continue;
        		}
        		else{
        			for (int j = 0; j < numPoints; ++j){
        				if (data[maxIndex][j] == 0){
        					zerosData[zerosIndex1][zerosIndex2] = data[k][j];
        					++zerosIndex2;
        				}
        				else if (data[maxIndex][j] == 1){
        					onesData[onesIndex1][onesIndex2] = data[k][j];
        					++onesIndex2;
        				}
        				
        			}
        			++zerosIndex1;
        			zerosIndex2 = 0;
        			++onesIndex1;
        			onesIndex2 = 0;
        		}
        	}
        	
        	// Before we make that recursive call, we need to perform
        	// a chi-square test to make sure we're not overfitting
        	double chiSquare = 0;
        	
        	double actualZeros = 0;
        	double actualOnes = 0;
        	
        	for (int k = 0; k < maxOnes; ++k){
        		if (onesData[numAttr-1][k] == 0){
        			actualZeros+=1;
        		}
        		else if (onesData[numAttr-1][k] ==1){
        			actualOnes+=1;
        		}
        	}
        	double ratio = (actualZeros+actualOnes)/(baseLineZeros+baseLineOnes);

        	double expectedOnes = baseLineOnes * ratio;
        	double expectedZeros = baseLineZeros * ratio;

        	//System.out.println("Baseline 1's: " + baseLineOnes + " Baseline 0's: " + baseLineZeros);
        	//System.out.println("Expected 1's: " + expectedOnes + " Expected 0's: " + expectedZeros);
        	//System.out.println("Actual 1's: " + actualOnes + " Actual 0's: " + actualZeros);
        	
        	chiSquare += (Math.pow((actualOnes-expectedOnes), 2)/expectedOnes);
        	chiSquare += (Math.pow((actualZeros-expectedZeros), 2)/expectedZeros);
        	
        	actualZeros = 0;
        	actualOnes = 0;
        	
        	for (int k = 0; k < maxZeros; ++k){
        		if (zerosData[numAttr-1][k] == 0){
        			actualZeros+=1;
        		}
        		else if (zerosData[numAttr-1][k] ==1){
        			actualOnes+=1;
        		}
        	}
        	
        	ratio = (actualZeros+actualOnes)/(baseLineZeros+baseLineOnes);
        	expectedOnes = baseLineOnes * ratio;
        	expectedZeros = baseLineZeros * ratio;
        	
        	chiSquare += (Math.pow((actualOnes-expectedOnes), 2)/expectedOnes);
        	chiSquare += (Math.pow((actualZeros-expectedOnes), 2)/expectedZeros);
        	
        	// If we determine that splitting further is likely to be valuable,
        	// call function recursively for each branch
        	if (chiSquare > alpha){
        		root.setLeftBranch(RunID3(zerosData,newAttr, baseLineZeros, baseLineOnes, alpha));
        		root.getLeftBranch().setParent(root);
        		root.setRightBranch(RunID3(onesData,newAttr, baseLineZeros, baseLineOnes, alpha));
        		root.getRightBranch().setParent(root);
        	}
        	// Otherwise, stop here and make this a leaf
        	else {
        		root.setAttribute("leaf");
        		if (zeros > ones){
        			root.setLabel(0);
        			return root;
        		}
        		else {
        			root.setLabel(1);
        			return root;
        		}
        	}
        }
        return root;
	}
	
	// Helper function for calculating entropy
	static double calcEntropy(int numZeros, int numOnes, int numEntries){
		double entropy = 0;		
		
		if ((numZeros == numEntries) || (numOnes == numEntries)){
			entropy = 0;
		}
		else {
			entropy -= (((double) numZeros)/numEntries)*(Math.log10(((double) numZeros)/numEntries)/Math.log10(2));
			entropy -= (((double) numOnes)/numEntries)*(Math.log10(((double) numOnes)/numEntries)/Math.log10(2));
		}
		return entropy;
	}
	
	// Helper function for calculating information gain
	static double calcGain(int[] data, int[] classifications){
		double gain = 0;
		
		int baseLineZeros = 0;
		int baseLineOnes = 0;
		int numEntries = classifications.length;
		
		for (int i = 0; i < numEntries; ++i){
			if (classifications[i] == 1){
				++baseLineOnes;
			}
			else if (classifications[i] == 0){
				++baseLineZeros;
			}
		}
		
		double baseLineEntropy = calcEntropy(baseLineZeros, baseLineOnes, numEntries);
		gain += baseLineEntropy;
				
		int attrZeros = 0;
		int attrOnes = 0;
		int zerosZeros = 0;
		int zerosOnes = 0;
		int onesZeros = 0;
		int onesOnes = 0;
		
		for (int i = 0; i < data.length; ++i){
			if (data[i] == 1){
				++attrOnes;
				if (classifications[i] == 1){
					++onesOnes;
				}
				else if (classifications[i] == 0){
					++onesZeros;
				}
			}
			else if (data[i] == 0){
				++attrZeros;
				if (classifications[i] == 1){
					++zerosOnes;
				}
				else if (classifications[i] == 0){
					++zerosZeros;
				}
			}
		}
		
		double zerosTerm = (((double) attrZeros)/numEntries)*calcEntropy(zerosZeros,zerosOnes,attrZeros);
		double onesTerm = (((double) attrOnes)/numEntries)*calcEntropy(onesZeros,onesOnes,attrOnes);
		
		gain -= zerosTerm;
		gain -= onesTerm;
		
		return gain;
	}
	
	// Function for writing tree to file
	static void saveModel(int iterNum, BufferedWriter writer, ID3TreeNode tree) throws IOException{
		++iterNum;
		String writeLine = "";
		if (tree.getAttribute()=="leaf"){
			if (iterNum == 1){
				writeLine += ": ";
				writeLine += tree.getLabel();
				writer.write(writeLine);
			}
			else {
				writeLine += tree.getLabel();
				writer.write(writeLine);
			}
		}
		
		else {
			if (iterNum > 1){
        		writer.newLine();
        	} 
        	for (int k = 0; k < iterNum-1; ++k){
        		writeLine += "| ";
        	} 
        	writeLine += tree.getAttribute()+ " = 0: ";
    		writer.write(writeLine); 
    		saveModel(iterNum, writer, tree.getLeftBranch());
    		writeLine = "";
    		writer.newLine();
        	for (int k = 0; k < iterNum-1; ++k){
        		writeLine += "| ";
        	} 
        	writeLine += tree.getAttribute()+ " = 1: ";
    		writer.write(writeLine); 
    		saveModel(iterNum, writer, tree.getRightBranch());
		}
	}
	
	// Function for testing tree to determine accuracy based on provided
	// test set
	static double testID3(ID3TreeNode tree, BufferedReader reader) throws IOException{
		
		ArrayList<String[]> rows = new ArrayList<String[]>();
        String nextLine;
        while ((nextLine = reader.readLine()) != null){
        	String[] row = nextLine.split(",");
        	rows.add(row);
        }
        
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
        
        double numCorrect = 0;    
        
        if ((tree.getLeftBranch() == null) && (tree.getRightBranch() == null)){
        	int onlyValue = tree.getLabel();
        	for (int i = 0; i < numExamples; ++i){
        		if (valueColumns[numAttr][i] == onlyValue){
        			++numCorrect;
        		}
        	}
        }
        
        else {
        	numCorrect = treeTrace(valueColumns, attributes, tree);
        }
		
        double percCorrect = numCorrect/numExamples;
		return (percCorrect);
	}
	
	// Recursive function used in the above for traversing branches
	static double treeTrace(int[][] data, String[] attr, ID3TreeNode subtree){
		double val = 0;
		int indexOfAttr = -1;
		int numRows = data[0].length;
		int numColumns = data.length-1;
		for (int i = 0; i < attr.length; ++i){
			if (subtree.getAttribute().compareTo(attr[i]) == 0){
				indexOfAttr = i;
			}
		}
		
		if ((subtree.getAttribute().compareTo("leaf") == 0) && (indexOfAttr == -1)){
			for (int i = 0; i < numRows; ++i){
				if (data[numColumns][i] == subtree.getLabel()){
					data[numColumns][i] = -1;
					++val;
				}
			}
		}
		
		else {
			int zeros = 0;
			int ones = 0;
			for (int i = 0; i < numRows; ++i){
				if (data[indexOfAttr][i]==1){
					++ones;
				}
				else if (data[indexOfAttr][i]==0){
					++zeros;
				}
			}
			int[][] leftData = new int[numColumns+1][zeros];
			int[][] rightData = new int[numColumns+1][ones];
			int onesIndex = 0;
			int zerosIndex = 0;
			for (int i = 0; i < numColumns+1; ++i){
				for (int j = 0; j < numRows; ++j){
					if (data[indexOfAttr][j]==1){
						rightData[i][onesIndex] = data[i][j];
						++onesIndex;
					}
					else if (data[indexOfAttr][j]==0){
						leftData[i][zerosIndex] = data[i][j];
						++zerosIndex;
					}
				}
				zerosIndex = 0;
				onesIndex = 0;
			}
			double leftValue = treeTrace(leftData, attr, subtree.getLeftBranch());
			val += leftValue;	
			double rightValue = treeTrace(rightData, attr, subtree.getRightBranch());
			val += rightValue;	
		}
		
		return val;
	}
	
}

// Basic binary tree node used to implement our decision tree
class ID3TreeNode {
	private int label;
	private String attribute;
	private ID3TreeNode leftBranch;
	private ID3TreeNode rightBranch;
	private ID3TreeNode parent;
	
	public ID3TreeNode(){
		label = 2;
		attribute = "";
		leftBranch = null;
		rightBranch = null;
		parent = null;
	}
	
	public void setLabel(int val){
		label = val;
	}
	
	public int getLabel(){
		return label;
	}
	
	public void setAttribute(String attr){
		attribute = attr;
	}
	
	public String getAttribute(){
		return attribute;
	}
	
	public void setLeftBranch(ID3TreeNode lb){
		leftBranch = lb;
	}
	
	public ID3TreeNode getLeftBranch(){
		return leftBranch;
	}
	
	public void setRightBranch(ID3TreeNode rb){
		rightBranch = rb;
	}
	
	public ID3TreeNode getRightBranch(){
		return rightBranch;
	}
	
	public void setParent(ID3TreeNode p){
		parent = p;
	}
	
	public ID3TreeNode getParent(){
		return parent;
	}
}

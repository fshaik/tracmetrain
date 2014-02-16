// given raw file, generate all files needed for training
// the actual training will require calling of methods in Training.java


import java.io.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;


public class GenTraining {
	String rawDataFileName; // this file contains all raw RSSI readings for all sample locations
	String dataPath; // directory to store files
    
	int maxX; // max x-coordinate in the resolution of the area map
	int maxY; // max y-coordinate in the resolution of the area map
	int numAnchors; // number of anchors; e.g., number of reference nodes, access points
	
	ArrayList<ArrayList<String>> dataList;
	
	GenTraining (String rawDataFileName1) {
		rawDataFileName = rawDataFileName1;
		dataPath = rawDataFileName + "_dir/";
		new File(dataPath).mkdir();
		
		RawData raw_data = new RawData(rawDataFileName);
		maxX = raw_data.maxX;
		maxY = raw_data.maxY;
		numAnchors = raw_data.numAnchors;
		dataList = raw_data.dataList;
		
		// save parameters in a parameter.txt file
		try {
			DataOutputStream outputT = new DataOutputStream(new FileOutputStream(dataPath + "parameters.txt"));
			outputT.writeBytes(maxX + "\n" + maxY + "\n" + numAnchors + "\n");
			outputT.close();
		}
		catch(Exception e) {
			System.out.println("Failed GenTraining()");
			System.exit(-1);
		} 
	}
	
	public static void genSubFiles(String inputFile, double p) {
		// generate a sub-file correponding to a p portion of the given file, another sub-file being the remainder portion
		try {
			
			DataInputStream in = new DataInputStream(new FileInputStream(inputFile));
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			
			DataOutputStream outputT1 = new DataOutputStream(new FileOutputStream(inputFile + "_sub_"+p+".1.txt"));
			DataOutputStream outputT2 = new DataOutputStream(new FileOutputStream(inputFile + "_sub_"+p+".2.txt"));
			
			String strLine;
			Random generator = new Random();
			while ((strLine = br.readLine()) != null) {
				double r = generator.nextDouble();
				if (r <= p)	outputT1.writeBytes(strLine+"\n");
				else outputT2.writeBytes(strLine+"\n");
			}
			in.close();
			outputT1.close();
			outputT2.close();
		} catch (Exception e) {
			System.out.println("Failed genSubFiles()");
			System.exit(-1);
		}
	}
    public  void genTrainingAndTestingFiles(double p){
    	// we will create the following files:
    	// - 1 test file: a portion (represented by p) of the total collection will be used for TESTING
    	// 		-- at each location, a portion p of its samples will be for TESTING
    	// - 5 training files: among non-test samples, a portion {20%, 40%, 60%, 80%, 100%} = each training file
    	// in our expriment, we use p=0.5
    	// therefore, we can study the effect of changing the training size, using the same test file to analyze accuracy
    	
    	ArrayList<ArrayList<String>> training;
		ArrayList<ArrayList<String>> testing;
        
        
		String fTesting = dataPath + "test_p"+p+".txt";
		String fTraining = dataPath + "train_p"+p+".txt";
		
    	
		try {
			FileOutputStream fOutStreamT = new FileOutputStream(fTesting);
			DataOutputStream outputT = new DataOutputStream(fOutStreamT);
            
			FileOutputStream fOutStreamTr = new FileOutputStream(fTraining);
			DataOutputStream outputTr = new DataOutputStream(fOutStreamTr);
            
			
			training=new ArrayList<ArrayList<String>>();
			testing=new ArrayList<ArrayList<String>>();
            
            
			// initialization
    		for (int i=0;i<maxX;i++)
    			for (int j=0;j<maxY;j++) {
    				ArrayList<String> arrStr1=new ArrayList<String>();
    				ArrayList<String> arrStr2=new ArrayList<String>();
    				training.add(arrStr1);
    				testing.add(arrStr2);
    			}
            
    		// insert samples into training and testing
    		for (int i=0;i<maxX;i++){
    			for (int j=0;j<maxY;j++){
    				int index=i*maxY+j;
    				int lSize=dataList.get(index).size();
    				// at each sample location, choose p (%) of the corresponding fingerprints for testing
    				// the rest for training
    				Set<Integer> s = Misc.getRandomSet(p, lSize);
    				String str;
    				for (int k=0;k<lSize;k++){
    					str = dataList.get(index).get(k);
    					if (s.contains(k)) {
    						testing.get(index).add(str);
    						outputT.writeBytes(str+"\n");
    					}
    					else {
    						training.get(index).add(str);
    						outputTr.writeBytes(str+"\n");
    					}
    				} //end for k
    			}	//end for j
    		}	//end for i
            
			outputTr.close();
			outputT.close();
    	} catch (Exception e) {
			System.out.println("Failed genTrainingAndTestingFiles()");
			System.exit(-1);
		}
    }
    
    public  void genTrainingForAllClasses_GridSVM(String fTraining, int gridX, int gridY) {
    	// in the grid SVM approach, each class is a cell in the grid of gridX columns and gridY rows; hence, gridX * gridY classes
    	// we will use multi-class SVM to train this file
    	try {
			FileInputStream fstream = new FileInputStream(dataPath + fTraining);
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;
			
 			new File(dataPath + fTraining + "_dir").mkdir();
 			String newDir = dataPath + fTraining + "_dir/grid_classes";
			new File(newDir).mkdir();
			String fClass = newDir + "/grid_X" + gridX + "_Y" + gridY + ".txt";
			DataOutputStream output = new DataOutputStream(new FileOutputStream(fClass));
			
            
            int i;
            double x, y;
            int label; // label of each sample: 1 of gridX*gridY labels
            
			while ((strLine = br.readLine()) != null)   {
				String loc = strLine.split("#")[0];
				x = new Double(loc.split(",")[0]);
				y = new Double(loc.split(",")[1]);
				
				int cellX, cellY;
				cellX =  (int) (x * gridX / maxX);
				cellY =  (int) (y * gridY / maxY);
				label  = cellX * gridY + cellY;
				
				output.writeBytes(label + " ");
				output.writeBytes(strLine.split("#")[1]+"\n");
			}
            in.close();
			output.close();
		} catch (Exception e) {
			System.out.println("Error in genTrainingForAllClasses_GridApproach()");
			System.exit(-1);
		}
    }
    
    public  void genTrainingForAllClasses_StripeSVM(String dimension, String fTraining, int numClasses) {
    	// in the stripe SVM approach, each class is a stripe (vertical stripe for x dimension or horizontal stripe for y dimension; hence, numClasses classes
    	// we will use multi-class SVM to train this file
    	// index for stripe starts at 0
    	try {
			FileInputStream fstream = new FileInputStream(dataPath + fTraining);
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;
			
 			new File(dataPath + fTraining + "_dir").mkdir();
 			String newDir = dataPath + fTraining + "_dir/stripe_classes/";
 			new File(newDir).mkdir();
			
 			String fClass = newDir + "/stripe_" + dimension + numClasses + ".txt";
			DataOutputStream output = new DataOutputStream(new FileOutputStream(fClass));
			
            
            int i;
            double x, y;
            int label; // label of each sample: 1 of numClasses labels
            
			while ((strLine = br.readLine()) != null)   {
				String loc = strLine.split("#")[0];
				x = new Double(loc.split(",")[0]);
				y = new Double(loc.split(",")[1]);
				
				if (dimension.equals("X")) label =  (int) (x * numClasses / maxX);
				else label =  (int) (y * numClasses / maxY);
				
				output.writeBytes(label + " ");
				output.writeBytes(strLine.split("#")[1]+"\n");
			}
            in.close();
			output.close();
			
			
		} catch (Exception e) {
			System.out.println("Error in genTrainingForAllClasses_StripeApproach()");
			System.exit(-1);
		}
    }
        
    public  void genTrainingForAllClasses(String dimension, String fTraining, int numClasses) {
    	// dimension = "X" or "Y" representing the X or Y dimension we are generating classes for
    	// numClasses = number of classes
    	// fTraining = file of entire sample collection
		try {
			
			FileInputStream fstream = new FileInputStream(dataPath + fTraining);
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;
			
            
			FileOutputStream[] fOutStreamC = new FileOutputStream[numClasses];
			DataOutputStream[] outputC = new DataOutputStream[numClasses];
			
			new File(dataPath + fTraining + "_dir").mkdir();
			for (int i=0;i<numClasses;i++) {
				String newDir = dataPath + fTraining + "_dir/" + dimension + numClasses;
				new File(newDir).mkdir();
				String fClass = newDir + "/" + (i+1) + ".txt";
				fOutStreamC[i] = new FileOutputStream(fClass);
				outputC[i] = new DataOutputStream(fOutStreamC[i]);
			}
            
            double coordinate;
            int i;
			while ((strLine = br.readLine()) != null)   {
				String loc = strLine.split("#")[0];
				if (dimension.equals("X")) {
					coordinate = new Double(loc.split(",")[0]);
					for (i=0;i<numClasses;i++) {
						if (coordinate * (double) (numClasses+1) <= (double) (i+1)*maxX) {
							// this fingerprint is in class X[i]
							outputC[i].writeBytes("+1 ");
						}
						else {
							outputC[i].writeBytes("-1 ");
						}
						outputC[i].writeBytes(strLine.split("#")[1]+"\n");
					}
					continue;
				}
				if (dimension.equals("Y")) {
					coordinate = new Double(loc.split(",")[1]);
					for (i=0;i<numClasses;i++) {
						if (coordinate * (double) (numClasses+1) <= (double) (i+1)*maxY) {
							// this fingerprint is in class Y[i]
							outputC[i].writeBytes("+1 ");
						}
						else {
							outputC[i].writeBytes("-1 ");
						}
						outputC[i].writeBytes(strLine.split("#")[1]+"\n");
					}
				}
			}
            
			for (i=0;i<numClasses;i++) outputC[i].close();
		} catch (Exception e) {
			System.out.println("Error in genTrainingForAllClasses()");
			System.exit(-1);
		}
    }    
    /*
    public  void genTrainingForAllClasses_Spectral(String fTraining, int numClasses) {
    	// used for the case where classes are formed based on hiearchical spectral bisectioning of the area 
    	// numClasses = number of classes
    	// fTraining = file of entire sample collection
		try {
			
			FileInputStream fstream = new FileInputStream(dataPath + fTraining);
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;
			
            
			FileOutputStream[] fOutStreamC = new FileOutputStream[numClasses];
			DataOutputStream[] outputC = new DataOutputStream[numClasses];
			
			new File(dataPath + fTraining + "_dir").mkdir();
			for (int i=0;i<numClasses;i++) {
				String newDir = dataPath + fTraining + "_dir/spectral_classes/";
				new File(newDir).mkdir();
				
				newDir = dataPath + fTraining + "_dir/spectral_classes/" + numClasses;
				new File(newDir).mkdir();
				
				String fClass = newDir + "/" + (i+1) + ".txt";
				fOutStreamC[i] = new FileOutputStream(fClass);
				outputC[i] = new DataOutputStream(fOutStreamC[i]);
			}
            
            double coordinate_x, coordinate_y;
            int i;
			while ((strLine = br.readLine()) != null)   {
				String loc = strLine.split("#")[0];
				coordinate_x = new Double(loc.split(",")[0]);
				coordinate_y = new Double(loc.split(",")[1]);
				
				for (i=0;i<numClasses;i++) {
					if (this location is in class i) {
						// this fingerprint is in class i
						outputC[i].writeBytes("+1 ");
					}
					else {
						outputC[i].writeBytes("-1 ");
					}
					outputC[i].writeBytes(strLine.split("#")[1]+"\n");
				}
			}
			for (i=0;i<numClasses;i++) outputC[i].close();
		} catch (Exception e) {
			System.out.println("Error in genTrainingForAllClasses_Spectral()");
			System.exit(-1);
		}
    }    
    */
}


// given train file, provide methods for prediction

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;
import java.util.Queue;

public	class Testing {
	private String rawDataFile; // name of raw data file, given by constructor method; e.g., rawDataFile = "brunato_data.txt";
	private String trainFile; // name of train file, given by constructor method; e.g., trainFile = "train_p0.5.txt"
	
	int maxX; // max x-coordinate in the resolution of the area map
	int maxY; // max y-coordinate in the resolution of the area map
	int numAnchors; // number of anchors; e.g., number of reference nodes, access points
	
	// attributes below are for SH-SVM
	private int numClassesX; // number of classes for X dimension
	private int numClassesY; // number of classes for Y dimension
	TrainingModel [] modelX, modelY;
	
	Testing(String rawDataFile1, String trainFile1) {
		rawDataFile = rawDataFile1;
		trainFile = trainFile1;

		numClassesX = -1;
		numClassesY = -1;
		
		try {
			// read parameters maxX, maxY, numAnchors
			FileInputStream fstream = new FileInputStream(rawDataFile +"_dir/parameters.txt");
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			maxX = Integer.parseInt(br.readLine());
			maxY = Integer.parseInt(br.readLine());
			numAnchors = Integer.parseInt(br.readLine());
			in.close();
		}
		catch(Exception e) {
			System.out.println("Failed Testing()");
			System.exit(-1);
		} 
	}
	
	public void setNumClasses (int numClassesX1, int numClassesY1) {
		numClassesX = numClassesX1;
		numClassesY = numClassesY1;	
		
		modelX = new TrainingModel[numClassesX];
		modelY = new TrainingModel[numClassesY];
		
		for (int i = 0; i < numClassesX; i++) {
			String str = rawDataFile +"_dir/" + trainFile + "_dir/X" + numClassesX + "/" + (i+1) + ".txt";
			//System.out.println("Load training model " + str);
			modelX[i] = new TrainingModel(str, numAnchors);
		}
		for (int i = 0; i < numClassesY; i++) {
			String str = rawDataFile +"_dir/" + trainFile + "_dir/Y" + numClassesY + "/" + (i+1) + ".txt";
			//System.out.println("Load training model " + str);
			modelY[i] = new TrainingModel(str, numAnchors);
		}
	}
	
	
	private int getClassID(String dimension, double[] b) {
		// given a reading b, return the smallest class containing b
		
		boolean label;
    	label= (dimension.equals("X"))? modelX[0].contains(b) : modelY[0].contains(b);
    	if (label) return 1;
    	
        int lo = 0;
        int hi = (dimension.equals("X"))? (numClassesX-1) : (numClassesY-1);	
        while (true) {
            if (hi - lo == 1) return hi+1;
        	int mid = (lo + hi) / 2;
        	label= (dimension.equals("X"))? modelX[mid].contains(b) : modelY[mid].contains(b);
        	if (label) hi = mid;
            else lo = mid;
        }
     }
	
	
	private int getClassID_Enhanced(String dimension, double[] b) {
		// given a reading b, return the smallest class containing b
		// each membership query asks 3 classes instead of 1: if at least 2 give consistent answers, go with those 2
		
		boolean label, label_next, label_prev;
    	label= (dimension.equals("X"))? modelX[0].contains(b) : modelY[0].contains(b);
    	if (label) return 1;
    	
        int lo = 0;
        int hi = (dimension.equals("X"))? (numClassesX-1) : (numClassesY-1);	
        while (true) {
            if (hi - lo == 1) return hi+1;
        	int mid = (lo + hi) / 2;
        	
        	label = (dimension.equals("X"))? modelX[mid].contains(b) : modelY[mid].contains(b);
        	if (label == true) {
        		// predicted to be left of mid
        		// double check if it is also predicted to be left of (mid+1)
        		label_next = (dimension.equals("X"))? modelX[mid+1].contains(b) : modelY[mid+1].contains(b);
            	if (label_next == true) {
            		hi = mid;
            	}
            	else {
            		// do not agree
            		// triple check with (mid-1)
            		label_prev = (dimension.equals("X"))? modelX[mid-1].contains(b) : modelY[mid-1].contains(b);
            		if (label_prev == true) hi = mid;
            		else lo = mid;
            	}
        	}
        	else {
        		// predicted to be right of mid
        		// double check if it is also predicted to be right of (mid-1)
        		label_prev = (dimension.equals("X"))? modelX[mid-1].contains(b) : modelY[mid-1].contains(b);
            	if (label_prev == false) {
            		lo = mid;
            	}
            	else {
            		// do not agree
            		// triple check with (mid+1)
            		label_next = (dimension.equals("X"))? modelX[mid+1].contains(b) : modelY[mid+1].contains(b);
            		if (label_next == false) lo = mid;
            		else hi = mid;
            	}
        	}
        }
     }
	
	public double[] getEstLocation(double[] newSample) {
		// given a new reading sample, return estimated location
		double[] estLocation = new double[2];
		estLocation[0] = ((double) getClassID("X", newSample) - 0.5) * (double) maxX / (double) (numClassesX+1);
		estLocation[1] = ((double) getClassID("Y", newSample) - 0.5) * (double) maxY / (double) (numClassesY+1);
		return estLocation;
	}
	
	public double[] getEstLocation_Enhanced(double[] newSample) {
		// given a new reading sample, return estimated location
		double[] estLocation = new double[2];
		estLocation[0] = ((double) getClassID_Enhanced("X", newSample) - 0.5) * (double) maxX / (double) (numClassesX+1);
		estLocation[1] = ((double) getClassID_Enhanced("Y", newSample) - 0.5) * (double) maxY / (double) (numClassesY+1);
		return estLocation;
	}
	
	public void predict(String testFile) {
		// for each testing sample, predict its location
		// save into a .predict file
		
		String predictDir = rawDataFile +"_dir/" + trainFile + "_dir/predict/";
		new File(predictDir).mkdir();
		
		
		String predictFile = predictDir + testFile + "_X" + numClassesX + "_Y" + numClassesY + ".predict";
		//System.out.println("Our Predict: " + predictFile);
		
		double avgErr = 0;
		double maxErr = 0.0;
		
		try {
			FileInputStream fstream = new FileInputStream(rawDataFile +"_dir/" + testFile);
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			
			DataOutputStream output = new DataOutputStream(new FileOutputStream(predictFile));
			
			
			double[] exactLocation = new double[2];
			
			String line;
			int numTestSamples = 0;
			while ((line = br.readLine()) != null)   {
				//System.out.println(line);
				numTestSamples++;
				double [] newSample = new double[numAnchors];
				
				exactLocation[0] = new Double(line.split("#")[0].split(",")[0]);
				exactLocation[1] = new Double(line.split("#")[0].split(",")[1]);
				
				String[] reading = line.split("#")[1].trim().split("\\s+");
				
				for (int j = 0; j < reading.length; j++) {
					newSample[Integer.parseInt(reading[j].split(":")[0])-1] = new Double(reading[j].split(":")[1]);
				}
				
				
				double[] estLocation = getEstLocation(newSample);
				
				double locationErr = Misc.euclideanDist(exactLocation,  estLocation);
				avgErr += locationErr;
				if (maxErr < locationErr) maxErr = locationErr;
				
				output.writeBytes(line + "#" +  estLocation[0] + "," +  estLocation[1] + "#" + locationErr + "\n");
			}
			in.close();
			output.close();
			
			avgErr = avgErr / (double) numTestSamples;
			System.out.println("avgErr = " + avgErr + ", maxErr = " + maxErr);
			
		} catch(Exception e) {
			System.out.println("Failed predict()");
			System.exit(-1);
		}	
	}
	
	public void predict_Enhanced(String testFile) {
		// for each testing sample, predict its location
		// save into a .predict file
		
		String predictDir = rawDataFile +"_dir/" + trainFile + "_dir/predict/";
		new File(predictDir).mkdir();
		
		
		String predictFile = predictDir + testFile + "_X" + numClassesX + "_Y" + numClassesY + ".predict.enhanced";
		//System.out.println("Our Predict: " + predictFile);
		
		double avgErr = 0;
		double maxErr = 0.0;
		
		try {
			FileInputStream fstream = new FileInputStream(rawDataFile +"_dir/" + testFile);
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			
			DataOutputStream output = new DataOutputStream(new FileOutputStream(predictFile));
			
			
			double[] exactLocation = new double[2];
			
			String line;
			int numTestSamples = 0;
			while ((line = br.readLine()) != null)   {
				//System.out.println(line);
				numTestSamples++;
				double [] newSample = new double[numAnchors];
				
				exactLocation[0] = new Double(line.split("#")[0].split(",")[0]);
				exactLocation[1] = new Double(line.split("#")[0].split(",")[1]);
				
				String[] reading = line.split("#")[1].trim().split("\\s+");
				
				for (int j = 0; j < reading.length; j++) {
					newSample[Integer.parseInt(reading[j].split(":")[0])-1] = new Double(reading[j].split(":")[1]);
				}
				
				
				double[] estLocation = getEstLocation_Enhanced(newSample);
				
				double locationErr = Misc.euclideanDist(exactLocation,  estLocation);
				avgErr += locationErr;
				if (maxErr < locationErr) maxErr = locationErr;
				
				output.writeBytes(line + "#" +  estLocation[0] + "," +  estLocation[1] + "#" + locationErr + "\n");
			}
			in.close();
			output.close();
			
			avgErr = avgErr / (double) numTestSamples;
			System.out.println("avgErr = " + avgErr + ", maxErr = " + maxErr);
			
		} catch(Exception e) {
			System.out.println("Failed predict_Enhanced()");
			System.exit(-1);
		}	
	}
	
	
	public void predict_GridSVM(String testFile, int gridX, int gridY) {
		// for each testing sample, predict its location using multi-class grid SVM
			
		
		String testFile1 = rawDataFile +"_dir/" + testFile + "_dir/grid_classes/grid_X" + gridX + "_Y" + gridY + ".txt";
		String temp_predict = testFile + ".predict"; // this temp file will be deleted after the function is finished
		String predictFile = rawDataFile +"_dir/" + trainFile + "_dir/predict/" + testFile + "_grid_X" + gridX + "_Y" + gridY + ".predict";
				
		TrainingModel model = new TrainingModel(rawDataFile +"_dir/" + trainFile + "_dir/grid_classes/grid_X" + gridX + "_Y" + gridY + ".txt", numAnchors);
		model.predictFile(testFile1, temp_predict);
		
		// now compute location
		double avgErr = 0;
		double maxErr = 0.0;
		double[] exactLocation = new double[2];
		int numTestSamples = 0;

		try {
			DataInputStream in = new DataInputStream(new FileInputStream(temp_predict));
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			
			DataInputStream in1 = new DataInputStream(new FileInputStream(rawDataFile +"_dir/" + testFile));
			BufferedReader br1 = new BufferedReader(new InputStreamReader(in1));
			
    	    DataOutputStream output = new DataOutputStream(new FileOutputStream(predictFile));
    	    
    	    String strLine;
			while ((strLine = br1.readLine()) != null)   {
    	    	numTestSamples++;
    	    	
    	    	exactLocation[0] = new Double(strLine.split("#")[0].split(",")[0]);
    	    	exactLocation[1] = new Double(strLine.split("#")[0].split(",")[1]);
    	    	
    	    	
    	    	// read class label
    	    	String line = br.readLine();
    	    	int label = Integer.parseInt(line.split(" ")[0]);
    	    	int cellX = label / gridY + 1;
    	    	int cellY = label % gridY + 1;
    	    	double[] estLocation = new double[2];
    	    	estLocation[0] = ((double) cellX-0.5) * (double) maxX / (double) gridX;
    	    	estLocation[1] = ((double) cellY-0.5) * (double) maxY / (double) gridY;
    	    	
    	    	
    	    	double locationErr = Misc.euclideanDist(exactLocation, estLocation);
				avgErr += locationErr;
				if (maxErr < locationErr) maxErr = locationErr;
					
				output.writeBytes(strLine + "#" + (int) estLocation[0] + "," + (int) estLocation[1] + "#" + locationErr + "\n");
			}
			new File(temp_predict).delete();
			in.close();
			in1.close();
			output.close();
			
			avgErr = avgErr / (double) numTestSamples;
			System.out.println("avgErr = " + avgErr + ", maxErr = " + maxErr);
			
		}
		catch (Exception e) {
			System.out.println("Failed predict_Grid()");
			System.exit(-1);
		}
	}
	
	public void predict_StripeSVM(String testFile, int numClassesX, int numClassesY) {
		// for each testing sample, predict its location using multi-class stripe SVM
		
		String predictFile = rawDataFile +"_dir/" + trainFile + "_dir/predict/" + testFile + "_stripe_X" + numClassesX + "_Y" + numClassesY + ".predict";
				
		String temp_predict_X = testFile + ".predict.X"; // this temp file will be deleted after the function is finished
		String temp_predict_Y = testFile + ".predict.Y"; // this temp file will be deleted after the function is finished
		
		
		TrainingModel model;
		
		model = new TrainingModel(rawDataFile +"_dir/" + trainFile + "_dir/stripe_classes/stripe_X" + numClassesX + ".txt", numAnchors);
		model.predictFile(rawDataFile +"_dir/" + testFile + "_dir/stripe_classes/stripe_X" + numClassesX + ".txt", temp_predict_X);
		
		model = new TrainingModel(rawDataFile +"_dir/" + trainFile + "_dir/stripe_classes/stripe_Y" + numClassesY + ".txt", numAnchors);
		model.predictFile(rawDataFile +"_dir/" + testFile + "_dir/stripe_classes/stripe_Y" + numClassesY + ".txt", temp_predict_Y);
		
		
		// now compute location
		double avgErr = 0;
		double maxErr = 0.0;
		double[] exactLocation = new double[2];
		int numTestSamples = 0;

		try {
			DataInputStream in_X = new DataInputStream(new FileInputStream(temp_predict_X));
			BufferedReader br_X = new BufferedReader(new InputStreamReader(in_X));
			
			DataInputStream in_Y = new DataInputStream(new FileInputStream(temp_predict_Y));
			BufferedReader br_Y = new BufferedReader(new InputStreamReader(in_Y));
			
			DataInputStream in1 = new DataInputStream(new FileInputStream(rawDataFile +"_dir/" + testFile));
			BufferedReader br1 = new BufferedReader(new InputStreamReader(in1));
			
    	    DataOutputStream output = new DataOutputStream(new FileOutputStream(predictFile));
    	    
    	    String strLine, strLine_X, strLine_Y;
			while ((strLine = br1.readLine()) != null)   {
				//System.out.println(strLine);
    	    	numTestSamples++;
    	    	exactLocation[0] = new Double(strLine.split("#")[0].split(",")[0]);
    	    	exactLocation[1] = new Double(strLine.split("#")[0].split(",")[1]);
    	    	
    	    	// read X class label
    	       	double labelX = new Double(br_X.readLine().split(" ")[0]);
    	       	double labelY = new Double(br_Y.readLine().split(" ")[0]);
    	    	double[] estLocation = new double[2];
    	    	estLocation[0] = (labelX+0.5) * (double) maxX / (double) numClassesX;
    	    	estLocation[1] = (labelY+0.5) * (double) maxY / (double) numClassesY;
    	    	
    	    	
    	    	double locationErr = Misc.euclideanDist(exactLocation, estLocation);
				avgErr += locationErr;
				if (maxErr < locationErr) maxErr = locationErr;
					
				output.writeBytes(strLine + "#" + (int) estLocation[0] + "," + (int) estLocation[1] + "#" + locationErr + "\n");
			}
			new File(temp_predict_X).delete();
			new File(temp_predict_Y).delete();
			
			in1.close();
			in_X.close();
			in_Y.close();
			output.close();
			
			avgErr = avgErr / (double) numTestSamples;
			System.out.println("avgErr = " + avgErr + ", maxErr = " + maxErr);
			
		}
		catch (Exception e) {
			System.out.println("Failed predict_StripeSVM()");
			System.exit(-1);
		}
	}
}
	

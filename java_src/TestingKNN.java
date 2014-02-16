// given train file, give a prediction using KNN method

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;
import java.util.Queue;


	
public	class TestingKNN {
	private String rawDataFile;
	private String trainFile; // name of train file, given by constructor method; e.g., trainFile = "train_p0.5.txt"
	int numAnchors; // number of anchors; e.g., number of reference nodes, access points
	
	ArrayList<String> trainList;
	TrainingModel [] modelX, modelY;
	
	TestingKNN(String rawDataFile1, String trainFile1) {
		rawDataFile = rawDataFile1;
		trainFile = trainFile1;
		numAnchors = -1;
		
		try {
			// load training samples into trainList (used for kNN prediction)
			FileInputStream fstream = new FileInputStream(rawDataFile +"_dir/" + trainFile);
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
		
			trainList = new ArrayList<String>();
			String strLine;
			while ((strLine = br.readLine()) != null)   {
				if (numAnchors == -1) {
					numAnchors = strLine.split("#")[1].split(" ").length;
				}
				trainList.add(strLine);
			}
			in.close();
			
			// create directories for resultant files
			new File(rawDataFile +"_dir/" + trainFile + "_dir/").mkdir();
			new File(rawDataFile +"_dir/" + trainFile + "_dir/predict/").mkdir();
		}
		catch(Exception e) {
			System.out.println("Failed loading training samples in Testing() constructor");
			System.exit(-1);
		}
	}

	
	
	
	private int getNearest(ArrayList<String> list, double[] newSample, String typeOfDistance) {
		// return index of nearest neighbor 
		
		double minError = -1;
		int nearestIndex = 0;
		
		for (int i = 0; i < list.size(); i++) {
			// get value vector
			double[] sample = new double[numAnchors];
						
			String[] reading = list.get(i).split("#")[1].trim().split("\\s+");
			
			for (int j = 0; j < numAnchors; j++) {
				sample[j] = new Double(reading[j].split(":")[1]);
			}
			
			double err;
			if (typeOfDistance.equals("Euclidean")) err = Misc.euclideanDist(sample, newSample);
			else err = Misc.manhattanDist(sample, newSample);
				
			if (minError == -1 || minError > err) {
				minError = err;
				nearestIndex = i;
			}
		}
		return nearestIndex;
	}
	
	public double[] getEstLocation(int K, double[] newSample, String typeOfDistance) {
		// return centroid location of k nearest neighbors
		double[] bestLocation = new double[2]; 
		bestLocation[0] = 0;
		bestLocation[1] = 0;
		
		ArrayList<String> workList = new ArrayList<String>(trainList);
		
		for (int i = 0; i < K; i++) {
			int nearestIndex = getNearest(workList, newSample, typeOfDistance);
			
			String line = workList.get(nearestIndex);
			bestLocation[0] += new Double(line.split("#")[0].split(",")[0]);
			bestLocation[1] += new Double(line.split("#")[0].split(",")[1]);
			
			workList.remove(nearestIndex);
		}
		bestLocation[0] = bestLocation[0] / (double) K;
		bestLocation[1] = bestLocation[1] / (double) K;
		
		return bestLocation;
	}
	
	public double[] predict(int K, String testFile, String typeOfDistance) {
		// for each testing sample, predict its location using nearest neighbor (1-NN)
		// save into a .predict.knn file
		
		String predictDir = rawDataFile +"_dir/" + trainFile + "_dir/predict/";
		String predictFile = predictDir + testFile + ".predict." + K + "nn." + typeOfDistance;
		//System.out.println("KNN Predict: " + predictFile);
		
		double avgErr = 0;
		double maxErr = 0.0;
		
		try {
			FileInputStream fstream = new FileInputStream(rawDataFile +"_dir/" + testFile);
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			
			DataOutputStream output = new DataOutputStream(new FileOutputStream(predictFile));
			
			double[] exactLocation = new double[2];
			double[] estLocation;

			

			String line;
			int numTestSamples = 0;
			while ((line = br.readLine()) != null)   {
				numTestSamples++;
				double [] testSample = new double[numAnchors];
				
				exactLocation[0] = new Double(line.split("#")[0].split(",")[0]);
				exactLocation[1] = new Double(line.split("#")[0].split(",")[1]);
				
				String[] reading = line.split("#")[1].trim().split("\\s+");
				
				for (int j = 0; j < numAnchors; j++) {
					testSample[j] = new Double(reading[j].split(":")[1]);
				}
				
				estLocation = getEstLocation(K, testSample, typeOfDistance);
				
				double locationErr = Misc.euclideanDist(exactLocation, estLocation);
				avgErr += locationErr;
				if (maxErr < locationErr) maxErr = locationErr;
						
				output.writeBytes(line + "#" +  estLocation[0] + "," +  estLocation[1] + "#" + locationErr + "\n");
			}
			in.close();
			output.close();
			
			avgErr = avgErr / (double) numTestSamples;
			//System.out.println("avgErr = " + avgErr + ", maxErr = " + maxErr);
			
			
		} catch(Exception e) {
			System.out.println("Failed predictKNN()");
			System.exit(-1);
		}
		
		double[] ret_array = new double[2];
		ret_array[0] = avgErr;
		ret_array[1] = maxErr;
		
		return ret_array;
		
	}	
	
	
}
	

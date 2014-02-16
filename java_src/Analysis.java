/* error analyis

*/ 



import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;
import java.util.Queue;

public	class Analysis {
	private double[] train_eX;
	private double[] train_eY;
	
	private double[] test_eX;
	private double[] test_eY;
	
	
	private double maxX;
	private double maxY;
	
	Analysis (String rawDataFile, String trainFile, String testFile) {
		try {
			// read parameters maxX, maxY, numAnchors
			DataInputStream in = new DataInputStream(new FileInputStream(rawDataFile +"_dir/parameters.txt"));
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			maxX = new Double(br.readLine());
			maxY = new Double(br.readLine());
			in.close();
			
			summarySVM_Train("X", rawDataFile+"_dir/"+trainFile+"_dir/");
			summarySVM_Train("Y", rawDataFile+"_dir/"+trainFile+"_dir/");
			summarySVM_Test(rawDataFile+"_dir/"+trainFile, testFile);
			summaryPredictAll(rawDataFile+"_dir/"+trainFile+"_dir/", testFile);
			summaryPredictAll_Enhanced(rawDataFile+"_dir/"+trainFile+"_dir/", testFile);
			
			
	//		summaryPredictAll_GridSVM(rawDataFile+"_dir/"+trainFile+"_dir/", testFile);
	//		summaryPredictAll_StripeSVM(rawDataFile+"_dir/"+trainFile+"_dir/", testFile);
	//		summaryPredictAll_KNN(rawDataFile+"_dir/"+trainFile+"_dir/", testFile);
			
		}
		catch(Exception e) {
			System.out.println("Failed Analysis()");
			System.exit(-1);
		}
	}
	
	private void summarySVM_Train(String dimension, String trainFileDir) {
		try{
			DataOutputStream output = new DataOutputStream(new FileOutputStream(trainFileDir + "summary_SVM_train."+dimension+ ".txt"));
		
			// write to the output file each line (for each case of m)
			// m ; minSVMErr ;	avgSVMErr ; E
			
			train_eX = new double[101];
			train_eY = new double[101];
			
			
			for (int numClasses = 10; numClasses <= 100; numClasses += 10) {
				DataInputStream in = new DataInputStream(new FileInputStream(trainFileDir + dimension + numClasses + "/summary.csv"));
				BufferedReader br = new BufferedReader(new InputStreamReader(in));
			
				
				String line;
			
				// skip first 3 lines
				line = br.readLine();
				line = br.readLine();
				line = br.readLine();
			
				// get SVM error for each class
				double minAccu = -1;
				double avgAccu = 0;
				double accu;
			
				for (int i = 0; i < numClasses; i++) {
					line = br.readLine();
					
					
					accu = new Double(line.split(";")[2]);
					if (minAccu == -1) minAccu = accu;
					else {
						if (minAccu > accu) minAccu = accu;
					}
					avgAccu += accu;
				}
				avgAccu = avgAccu / (double) numClasses;
			
				double eps = (100 - minAccu)/100;
	    	    double m = Math.log(1+numClasses) / Math.log(2);  	    
	    	    double expectedLocErr = Misc.errBound(m, eps); 
	    	    if (dimension.equals("X")) {
	    	    	expectedLocErr = expectedLocErr * maxX;
	    	    	train_eX[numClasses] = expectedLocErr;
	    	    }
				if (dimension.equals("Y")) {
					expectedLocErr = expectedLocErr * maxY;
					train_eY[numClasses] = expectedLocErr;
				}
	    	    
	    	    output.writeBytes(numClasses + ";" + minAccu + ";" + avgAccu + ";" + expectedLocErr + "\n");	
				
	    	    
				in.close();
				
			}
		
			
			output.close();
		}
		catch(Exception e) {
			System.out.println("Failed summarySVM_Train()");
			System.exit(-1);
		}
	}
	
	private  void summarySVM_Test(String trainCollectionFile, String testFile) {
		String trainDir = trainCollectionFile + "_dir/";
        int numClassess; 
    	try {
    		
    	    DataOutputStream outX = new DataOutputStream(new FileOutputStream(trainDir + "summary.SVM_test.X.txt"));
    	    DataOutputStream outY = new DataOutputStream(new FileOutputStream(trainDir + "summary.SVM_test.Y.txt"));

    	    
    	    test_eX = new double[101];
    		test_eY = new double[101];;
			
            for (int numClasses = 10; numClasses <= 100; numClasses += 10) {
            	DataInputStream in = new DataInputStream(new FileInputStream(trainDir + "predict/" + testFile + "_X" + numClasses + "_Y"+numClasses + ".predict"));
    			BufferedReader br = new BufferedReader(new InputStreamReader(in));
            
    			System.out.println(trainDir + "predict/" + testFile + "_X" + numClasses + "_Y"+numClasses + ".predict");
    			
    			String line;
    			int numPoints = 0;
    			
    			Double [] countX = new Double[numClasses];
    			Double [] countY = new Double[numClasses];
    			
    			for (int i = 0; i < numClasses; i++) {
					countX[i] = new Double(0.0);
					countY[i] = new Double(0.0);
				}
    			
    			while ((line = br.readLine())!=null) {
    				// e.g., line = "2.43892672440343,19.5114137952274#1:-50 2:-73 3:-63 4:-57 5:-95 #2,19#0.3702457639991829"
    				numPoints++;
    				double trueX, trueY, estX, estY;
    				trueX = new Double(line.split("#")[0].split(",")[0]);
    				trueY = new Double(line.split("#")[0].split(",")[1]);
    				estX = new Double(line.split("#")[2].split(",")[0]);
    				estY = new Double(line.split("#")[2].split(",")[1]);
    				
    					
    				for (int i = 0; i < numClasses; i++) {
    					if ((trueX * (double) (numClasses+1) <= (double) (i+1)*maxX && estX * (double) (numClasses+1) <= (double) (i+1)*maxX) || (trueX * (double) (numClasses+1) > (double) (i+1)*maxX && estX * (double) (numClasses+1) > (double) (i+1)*maxX)) {
    						countX[i] = new Double(countX[i].doubleValue()+1.0);
    					}
    					if ((trueY * (double) (numClasses+1) <= (double) (i+1)*maxY && estY * (double) (numClasses+1) <= (double) (i+1)*maxY) || (trueY * (double) (numClasses+1) > (double) (i+1)*maxY && estY * (double) (numClasses+1) > (double) (i+1)*maxY)) {
    						countY[i] = new Double(countY[i].doubleValue()+1.0);
    					}
    				}
    			}
    			in.close();
    			
    			for (int i=0; i < numClasses; i++) {
    				countX[i] = new Double(countX[i].doubleValue()/(double) numPoints);
    				countY[i] = new Double(countY[i].doubleValue()/(double) numPoints);			
    			}
    			
    			// compute worst empirical error
    			double max_errX = 0;
    			double max_errY = 0;
    			double avg_errX = 0;
    			double avg_errY = 0;
    			for (int i=0; i < numClasses; i++) {
    				avg_errX = avg_errX + countX[i].doubleValue()/(double) numClasses;
    				avg_errY = avg_errY + countX[i].doubleValue()/(double) numClasses;
    				if (max_errX < 1- countX[i].doubleValue()) max_errX = 1 - countX[i].doubleValue();
    				if (max_errY < 1- countY[i].doubleValue()) max_errY = 1 - countY[i].doubleValue();
    			}
    			
    			
    			test_eX[numClasses] = (double) maxX * Misc.errBound(Math.log(1+numClasses) / Math.log(2), max_errX);
    			test_eY[numClasses] = (double) maxY * Misc.errBound(Math.log(1+numClasses) / Math.log(2), max_errY);
	    	    outX.writeBytes(numClasses + ";" + (1-max_errX) + ";" + avg_errX + ";" + test_eX[numClasses] + "\n");	
	    	    outY.writeBytes(numClasses + ";" + (1-max_errY) + ";" + avg_errY + ";" + test_eY[numClasses] + "\n");
	    	}
    	    outX.close();
    	    outY.close();
    	}
    	catch (Exception e) {
    		System.out.println("Error in summarySVM_test()");
    		System.exit(-1);
    	}
	}
	
	private String summaryPredict(String trainFileDir, String predictFile) {
		double maxErr = -1;
		double avgErr = 0;
		double err;
		int count = 0;
		try{
			// return a line
			// mx ; my ; avgErr ;	maxErr 
					DataInputStream in = new DataInputStream(new FileInputStream(trainFileDir + "predict/" + predictFile));
					BufferedReader br = new BufferedReader(new InputStreamReader(in));
					String line;
			
					// get SVM error for each class
					
					while ((line = br.readLine())!=null) {
							err = new Double(line.split("#")[3]);
							if (maxErr < err) maxErr = err;
							avgErr += err;
							count++;
					}
					avgErr = avgErr / (double) count;
					in.close();
		}
		catch(Exception e) {
			System.out.println("Failed summaryPredict()");
			System.exit(-1);
		}
		return (avgErr + ";" + maxErr);	
		
	}
	
	private void summaryPredictAll(String trainFileDir, String testFile) {
		try{
			DataOutputStream output = new DataOutputStream(new FileOutputStream(trainFileDir + "summary.predict.csv" ));
		
			// write to the output file each line (for each case of m)
			// mx ; my ; avgErr ;	maxErr ; E
			System.out.println(trainFileDir + "summary.predict.csv");
			
			for (int mX = 10; mX <= 100; mX += 10) {
				for (int mY = 10; mY <= 100; mY += 10) {
					String predictFile = testFile+"_X"+mX+"_Y"+mY+".predict";
					String str = summaryPredict(trainFileDir, predictFile);
					output.writeBytes(mX + ";" + mY + ";" + Math.sqrt(train_eX[mX]*train_eX[mX]+train_eY[mY]*train_eY[mY]) + ";" + 
										Math.sqrt(test_eX[mX]*test_eX[mX]+test_eY[mY]*test_eY[mY]) + ";" + str +  "\n");	
					//System.out.println(predictFile);
				}
			}
			output.close();
		}
		catch(Exception e) {
			System.out.println("Failed summaryPredictAll()");
			System.exit(-1);
		}
	}
	
	private void summaryPredictAll_Enhanced(String trainFileDir, String testFile) {
		try{
			DataOutputStream output = new DataOutputStream(new FileOutputStream(trainFileDir + "summary_Enhanced.predict.csv" ));
		
			// write to the output file each line (for each case of m)
			// mx ; my ; avgErr ;	maxErr ; E
			System.out.println(trainFileDir + "summary_Enhanced.predict.csv");
			
			for (int mX = 10; mX <= 100; mX += 10) {
				for (int mY = 10; mY <= 100; mY += 10) {
					String predictFile = testFile+"_X"+mX+"_Y"+mY+".predict.enhanced";
					String str = summaryPredict(trainFileDir, predictFile);
					output.writeBytes(mX + ";" + mY + ";" + Math.sqrt(train_eX[mX]*train_eX[mX]+train_eY[mY]*train_eY[mY]) + ";" + 
										Math.sqrt(test_eX[mX]*test_eX[mX]+test_eY[mY]*test_eY[mY]) + ";" + str +  "\n");	
					//System.out.println(predictFile);
				}
			}
			output.close();
		}
		catch(Exception e) {
			System.out.println("Failed summaryPredictAll_Enhanced()");
			System.exit(-1);
		}
	}
	
	private void summaryPredictAll_KNN(String trainFileDir, String testFile) {
		try{
			DataOutputStream output = new DataOutputStream(new FileOutputStream(trainFileDir + "summary_KNN.predict.csv" ));
		
			// write to the output file each line (for each case of kNN)
			// mx ; my ; avgErr ;	maxErr ; E
			
			String str = summaryPredict(trainFileDir, testFile+".predict.1nn.Euclidean");
			output.writeBytes("1NN(Euclidean)" + str +  "\n");	
			
			
			 str = summaryPredict(trainFileDir, testFile+".predict.3nn.Euclidean");
			output.writeBytes("3NN(Euclidean)" + str +  "\n");	
			
			 str = summaryPredict(trainFileDir, testFile+".predict.5nn.Euclidean");
			output.writeBytes("5NN(Euclidean)" + str +  "\n");	
			
			 str = summaryPredict(trainFileDir, testFile+".predict.1nn.Manhattan");
			output.writeBytes("1NN(Manhattan)" + str +  "\n");	
			
			 str = summaryPredict(trainFileDir, testFile+".predict.3nn.Manhattan");
			output.writeBytes("3NN(Manhattan)" + str +  "\n");	
			
			 str = summaryPredict(trainFileDir, testFile+".predict.5nn.Manhattan");
			output.writeBytes("5NN(Manhattan)" + str +  "\n");	
			
			output.close();
		}
		catch(Exception e) {
			System.out.println("Failed summaryPredictAll_KNN()");
			System.exit(-1);
		}
	}
	
	private void summaryPredictAll_GridSVM(String trainFileDir, String testFile) {
		try{
			DataOutputStream output = new DataOutputStream(new FileOutputStream(trainFileDir + "summary_grid.predict.csv" ));
		
			// write to the output file each line (for each case of m)
			// mx ; my ; avgErr ;	maxErr ; E
		
			
			for (int mX = 2; mX <= 10; mX += 1) {
					String predictFile = testFile+"_grid_X"+mX+"_Y"+mX+".predict";
					String str = summaryPredict(trainFileDir, predictFile);
					output.writeBytes(mX + ";" + mX  + ";" + str +  "\n");	
			}
			
			for (int mX = 20; mX <= 100; mX += 10) {
				String predictFile = testFile+"_grid_X"+mX+"_Y"+mX+".predict";
				//System.out.println(predictFile);
				String str = summaryPredict(trainFileDir, predictFile);
				output.writeBytes(mX + ";" + mX  + ";" + str +  "\n");	
			}
			output.close();
		}
		catch(Exception e) {
			System.out.println("Failed summaryPredictAll_GridSVM()");
			System.exit(-1);
		}
	}
	
	private void summaryPredictAll_StripeSVM(String trainFileDir, String testFile) {
		try{
			DataOutputStream output = new DataOutputStream(new FileOutputStream(trainFileDir + "summary_stripe.predict.csv" ));
		
			// write to the output file each line (for each case of m)
			// mx ; my ; avgErr ;	maxErr ; E
		
			
			for (int mX = 10; mX <= 100; mX += 10) {
				for (int mY = 10; mY <= 100; mY += 10) {
					String predictFile = testFile+"_stripe_X"+mX+"_Y"+mY+".predict";
					String str = summaryPredict(trainFileDir, predictFile);
					output.writeBytes(mX + ";" + mY + ";" + str +  "\n");	
				}
			}
			output.close();
		}
		catch(Exception e) {
			System.out.println("Failed summaryPredictAll_StripeSVM()");
			System.exit(-1);
		}
	}
	
	public static void summarySVM(String dimension, String trainCollectionFile, int numClasses, int numAnchors) {
    	// create an excel file summarizing parameters such as num of Support Vectors, training accuracy, etc.
    	
    	String trainDir = trainCollectionFile + "_dir/" + dimension + numClasses +"/";
        String summaryFile = trainDir + "summary.csv";
        //System.out.println("Creating summary file: " + summaryFile);
        
        String line;
        String [] lineArr = new String[numClasses];
        
        double minAccuracy = 100;
    	try {
    		
    	    FileOutputStream fOutStreamT = new FileOutputStream(summaryFile);
    	    DataOutputStream outputT = new DataOutputStream(fOutStreamT);
    	    
    	    for (int i = 1; i <= numClasses; i++) {
    	    	TrainingModel model = new TrainingModel(trainDir + i +".txt", numAnchors);
    	    	lineArr[i-1] = i + ";" + model.total_sv + ";" + model.training_accuracy;
    	    	if (minAccuracy > model.training_accuracy) minAccuracy = model.training_accuracy;
    	    }
    	    // compute Location Error bound (formula E in the eTrack paper)
    	    double eps = (100 - minAccuracy)/100;
    	    double m = Math.log(1+numClasses) / Math.log(2);  	    
    	    double E = Misc.errBound(m, eps);
    	    line = "location error E = " + E; 
    	    outputT.writeBytes(line + "\n");
    	    line = "num of classes = " + numClasses;
    	    outputT.writeBytes(line + "\n");
    	    line = "ClassID; #Samples; #SV; TrainAccuracy";
    	    outputT.writeBytes(line + "\n");
    	    for (int i = 0; i < numClasses; i++) {
    	    	outputT.writeBytes(lineArr[i] + "\n");
    	    }
    	    outputT.close();
    	}
    	catch (Exception e) {
    		System.out.println("Error in genTrainingForAllClasses()");
    		System.exit(-1);
    	}
    }
	
}


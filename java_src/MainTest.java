
import java.io.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;


public class MainTest {
	static int numAnchors;
	static String rawFileName;
	static double pTest, pTrain;
	static String trainFile;
	static String testFile;
	
	private  static void genTraining() {
		GenTraining gt = new GenTraining(rawFileName);
		gt.genTrainingAndTestingFiles(pTest);
		GenTraining.genSubFiles(rawFileName + "_dir/train_p" + pTest + ".txt", pTrain);
			
		// generate training files
		for (int numClassesX = 10; numClassesX <= 100; numClassesX += 10) {
			System.out.println("Generating train and test files for numClassesX = " + numClassesX);
			gt.genTrainingForAllClasses("X", trainFile, numClassesX);
			//gt.genTrainingForAllClasses("X", testFile, numClassesX);	
		}
		for (int numClassesY = 10; numClassesY <= 100; numClassesY += 10) {
			System.out.println("Generating train and test files for numClassesY = " + numClassesY);
			gt.genTrainingForAllClasses("Y", trainFile, numClassesY);
		    //gt.genTrainingForAllClasses("Y", testFile, numClassesY);
		}
	}
	
	
	private  static void runTraining() {
		GenTraining gt = new GenTraining(rawFileName);
		
		for (int numClassesX = 10; numClassesX <= 100; numClassesX += 10) {
			System.out.println("Training for numClassesX = " + numClassesX);
			Training.runSVMTrainingAllClasses("X", rawFileName + "_dir/" + trainFile, numClassesX);
			Analysis.summarySVM("X", rawFileName + "_dir/" + trainFile, numClassesX, numAnchors);
			
		}
		for (int numClassesY = 10; numClassesY <= 100; numClassesY += 10) {
			System.out.println("Training for numClassesY = " + numClassesY);
		    Training.runSVMTrainingAllClasses("Y", rawFileName + "_dir/" + trainFile, numClassesY);  
		    Analysis.summarySVM("Y", rawFileName + "_dir/" + trainFile, numClassesY, numAnchors);
		}
		
		Misc.computeBestNumClasses("X", rawFileName + "_dir/" + trainFile);
		Misc.computeBestNumClasses("Y", rawFileName + "_dir/" + trainFile);		
	}
	
	private  static void test() {
		Testing test = new Testing(rawFileName, trainFile);
		
		try {
			FileOutputStream fOutStreamT = new FileOutputStream(rawFileName + "_dir/" + trainFile + "_dir/" + testFile + ".time");
			DataOutputStream outputT = new DataOutputStream(fOutStreamT);
		
			for (int i = 10; i <= 100; i+=10) {
				for (int j = 10; j <=100; j+=10) {
					test.setNumClasses(i, j);
				
					// record the time too.
					long time1= System.nanoTime();
					test.predict(testFile);
					long time2 = System.nanoTime();
					long timeSpent = time2-time1;
				
					outputT.writeBytes(i + " " + j + " " + timeSpent + "\n");
				}
			}
			outputT.close();
		}
		catch (Exception e) {
	    		System.out.println("Error in test()");
	    		System.exit(-1);
		}
	}
	private  static void test_Enhanced() {
		Testing test = new Testing(rawFileName, trainFile);
		
		try {
			FileOutputStream fOutStreamT = new FileOutputStream(rawFileName + "_dir/" + trainFile + "_dir/" + testFile + ".time.enhanced");
			DataOutputStream outputT = new DataOutputStream(fOutStreamT);
		
			for (int i = 10; i <= 100; i+=10) {
				for (int j = 10; j <=100; j+=10) {
					test.setNumClasses(i, j);
				
					// record the time too.
					long time1= System.nanoTime();
					test.predict_Enhanced(testFile);
					long time2 = System.nanoTime();
					long timeSpent = time2-time1;
				
					outputT.writeBytes(i + " " + j + " " + timeSpent + "\n");
				}
			}
			outputT.close();
		}
		catch (Exception e) {
	    		System.out.println("Error in test_Enhanced()");
	    		System.exit(-1);
		}
	}
	
	/**
	private static void train_Spectral() {
		Spectral train = new Spectral(rawFileName, trainFile);
		train.training();
		train.loadTrainingModels();
		train.summarySVM();
	}
	
	private static void test_Spectral() {
		long time1, time2, timeSpent;
		Spectral test = new Spectral(rawFileName, trainFile);
		test.loadTrainingModels();
		
		//test.testAll(testFile);
		
		
		try {
			FileOutputStream fOutStreamT = new FileOutputStream(rawFileName + "_dir/" + trainFile + "_dir/" + testFile + ".time.spectral");
			DataOutputStream outputT = new DataOutputStream(fOutStreamT);
		
			for (int depth = 2; depth <=2; depth +=2) {
				time1= System.nanoTime();
				test.predict(testFile, depth);
				time2 = System.nanoTime();
				timeSpent = time2-time1;			
				outputT.writeBytes("depth=" + depth + " " + timeSpent + "\n");
			}
			outputT.close();
		} catch (Exception e) {
			System.out.println("Error in test_Spectral()");
			System.exit(-1);
		}
		
		
	}
	**/
	
	private static void test_KNN() {
		TestingKNN test = new TestingKNN(rawFileName, trainFile);
		long time1, time2, timeSpent;
		try {
			FileOutputStream fOutStreamT = new FileOutputStream(rawFileName + "_dir/" + trainFile + "_dir/" + testFile + ".time.kNN");
			DataOutputStream outputT = new DataOutputStream(fOutStreamT);
		
			
			time1= System.nanoTime();
			test.predict(1, testFile, "Euclidean");
			time2 = System.nanoTime();
			timeSpent = time2-time1;
			outputT.writeBytes("1NN(Euclidean) " + timeSpent + "\n");
			
			time1= System.nanoTime();
			test.predict(3, testFile, "Euclidean");
			time2 = System.nanoTime();
			timeSpent = time2-time1;
			outputT.writeBytes("3NN(Euclidean) " + timeSpent + "\n");
			
			time1= System.nanoTime();
			test.predict(5, testFile, "Euclidean");
			time2 = System.nanoTime();
			timeSpent = time2-time1;
			outputT.writeBytes("5NN(Euclidean) " + timeSpent + "\n");
			
			
			time1= System.nanoTime();
			test.predict(1, testFile, "Manhattan");
			time2 = System.nanoTime();
			timeSpent = time2-time1;
			outputT.writeBytes("1NN(Manhattan) " + timeSpent + "\n");
			
			time1= System.nanoTime();
			test.predict(3, testFile, "Manhattan");
			time2 = System.nanoTime();
			timeSpent = time2-time1;
			outputT.writeBytes("3NN(Manhattan) " + timeSpent + "\n");
			
			time1= System.nanoTime();
			test.predict(5, testFile, "Manhattan");
			time2 = System.nanoTime();
			timeSpent = time2-time1;
			outputT.writeBytes("5NN(Manhattan) " + timeSpent + "\n");
			
			outputT.close();
			
		}
		catch (Exception e) {
	    		System.out.println("Error in test_KNN()");
	    		System.exit(-1);
		}
	}
	
	private static void train_GridSVM() {
		GenTraining gt = new GenTraining(rawFileName);
		
		for (int i = 2; i <= 9; i+=1) {
			gt.genTrainingForAllClasses_GridSVM(trainFile, i, i);
			gt.genTrainingForAllClasses_GridSVM(testFile, i, i);
			Training.runSVMTraining(rawFileName + "_dir/" + trainFile + "_dir/grid_classes/", "grid_X" + i + "_Y" + i + ".txt");
		}
		
		for (int i = 10; i <= 100; i+=10) {
				gt.genTrainingForAllClasses_GridSVM(trainFile, i, i);
				gt.genTrainingForAllClasses_GridSVM(testFile, i, i);
				Training.runSVMTraining(rawFileName + "_dir/" + trainFile + "_dir/grid_classes/", "grid_X" + i + "_Y" + i + ".txt");
			}
			
	}
	
	private static void test_GridSVM() {
		long time1, time2, timeSpent;
		Testing gt = new Testing(rawFileName, trainFile);
		
		try{
			
		
			FileOutputStream fOutStreamT = new FileOutputStream(rawFileName + "_dir/" + trainFile + "_dir/" + testFile + ".time.grid");
			DataOutputStream outputT = new DataOutputStream(fOutStreamT);
	
			for (int i = 2; i <= 9; i+=1) {
				time1= System.nanoTime();
				gt.predict_GridSVM(testFile, i, i);
				time2 = System.nanoTime();
				timeSpent = time2-time1;
				outputT.writeBytes(i + " " + i + " " + timeSpent + "\n");
			}
			
			for (int i = 10; i <= 100; i+=10) {
				time1= System.nanoTime();
				gt.predict_GridSVM(testFile, i, i);
				time2 = System.nanoTime();
				timeSpent = time2-time1;
				outputT.writeBytes(i + " " + i + " " + timeSpent + "\n");
			}
			outputT.close();
		}
		catch (Exception e) {
    		System.out.println("Error in test_GridSVM()");
    		System.exit(-1);
		}
	}
	
	
	private static void train_StripeSVM() {
		GenTraining gt = new GenTraining(rawFileName);
		
		for (int i = 10; i <= 100; i+=10) {
				gt.genTrainingForAllClasses_StripeSVM("X", trainFile, i);
				gt.genTrainingForAllClasses_StripeSVM("Y", trainFile, i);
				
				gt.genTrainingForAllClasses_StripeSVM("X", testFile, i);
				gt.genTrainingForAllClasses_StripeSVM("Y", testFile, i);
				
				Training.runSVMTraining(rawFileName + "_dir/" + trainFile + "_dir/stripe_classes/", "stripe_X" + i + ".txt");
				Training.runSVMTraining(rawFileName + "_dir/" + trainFile + "_dir/stripe_classes/", "stripe_Y" + i + ".txt");
			}
	}
	
	private static void test_StripeSVM() {
		long time1, time2, timeSpent;
		Testing gt = new Testing(rawFileName, trainFile);
		try {
			FileOutputStream fOutStreamT = new FileOutputStream(rawFileName + "_dir/" + trainFile + "_dir/" + testFile + ".time.stripe");
			DataOutputStream outputT = new DataOutputStream(fOutStreamT);
	
		
			for (int i = 10; i <= 100; i+=10) {
				for (int j = 10; j <= 100; j+=10) {
					time1= System.nanoTime();
					gt.predict_StripeSVM(testFile, i, j);
					time2 = System.nanoTime();
					timeSpent = time2-time1;
					outputT.writeBytes(i + " " + j + " " + timeSpent + "\n");
				}
			}
			outputT.close();
		}
		catch (Exception e) {
    		System.out.println("Error in test_StripeSVM()");
    		System.exit(-1);
		}
	}
	
	private static void testTrento() {
		rawFileName = "trento_data.txt";
		numAnchors  = 6;
		pTest = 0.5;
		pTrain = 1;
		trainFile = "train_p" + pTest + ".txt_sub_" + pTrain + ".1.txt";
		testFile = "test_p" + pTest + ".txt";
	}
	
	private static void testColorado() {
		rawFileName = "colorado_omni_16dbm_dev1.txt";
		numAnchors  = 5;
		pTest = 0.5; //0.95;
		pTrain = 0.02;
		trainFile = "train_p" + pTest + ".txt_sub_" + pTrain + ".1.txt"; //train_p0.5.txt_sub_0.02.1
		testFile = "test_p" + pTest + ".txt";
	}
	
	
	private static void testColorado_SemiSupervised() {
		rawFileName = "colorado_semi_supervised.txt"; //"colorado_omni_16dbm_dev1.txt";
		numAnchors  = 5;
		pTest = 0.5; //0.95;
		pTrain = 0.0050; //0.01; //0.02;
		trainFile = "train_p" + pTest + ".txt_sub_" + pTrain + ".1_plusFraction_0.2.txt"; //train_p0.5.txt_sub_0.0050.1_plusFraction_0.2
		testFile = "test_p" + pTest + ".txt";
	}
	
	private static void testCalPoly() {
		rawFileName = "TEST.txt"; //"DL222NWSMW.txt"; //"DL221NWSMW.txt"; // "DL10x10WinSMW.txt"; // "DexLawnNthUbSMW.txt";
		numAnchors  = 22;
		pTest = 0;
		pTrain = 1;
		trainFile = "train_p" + pTest + ".txt_sub_" + pTrain + ".1.txt";
		testFile = "test_p" + pTest + ".txt";
	}
	
	
	
	public static void main(String[] args) {
		testCalPoly();
		
        
        // only one of the 3 calls below is run at a time
        
		 genTraining(); // generate training file. You need to run this first
		
		//runTraining(); // run training file. Do this after you do genTraining().
		
		    //test(); // test with the test file; can only be run after training
		
		new Analysis(rawFileName, trainFile, testFile);
		
	}
}


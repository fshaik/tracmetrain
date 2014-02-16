
import java.io.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;


public class MainTestCalPoly {
	static int numAnchors;
	static String rawFileName;
	static double p;
	static String trainFile;
	
	
	private static void testCalPoly() {
		rawFileName = "DexLawnNthUbSMW.txt";
		numAnchors  = 10;
		trainFile = "train_p0.0.txt_sub_1.0.1.txt";
	}
	
	
	public static void main(String[] args) {
		testCalPoly();
		Testing test = new Testing(rawFileName, trainFile);
		
		double[]  newSample;
		/* your program will be like
		for each reading newSample {
			double[] estLoc = new double[2];
			estLoc = test.getEstLocation(newSample);
		}
		*/
	}
}


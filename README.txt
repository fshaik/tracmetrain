Tracme:
     Tracme sampling program can be found on my github: https://github.com/kwafarkye/TracMe. 
	 I may need to give them permission to contribute (just have them email me or something and I can do that) or they can just fork a new repository.

Localize:
 Localize program can be found on my github: https://github.com/kwafarkye/Localize. Same thing goes with permisison/access.

Python Script (AdjustRSSI.py):
      This script was intended for eliminating outliers in the sample set. For each point sampled, we took each access point's RSSI values. 
	  For each set of values, if the majority of the values were equal to 0, then the values not equal to zero were assigned the value 0. 
	  If the majority of the values were not zero, then the values equal to zero were set to equal the greatest value in the set.
USAGE: AdjustRSSIs.py [SampleFileName] [NewSampleFileName]

How To Produce Models:
     To produce models you will need:
          Python installed on your comp.
          Gnu-plot installed on your comp.
          libsvm package (can be found here: http://www.csie.ntu.edu.tw/~cjlin/libsvm/)**

Here's a good video I watched on using libsvm: http://www.youtube.com/watch?v=gePWtNAQcK8

     Open MainTest.java (can be found in the "java_src" zip) and in the testCalPoly() method, update the information to match your sample output information. 
	 Set "rawFileName" equal to the name of the output file you would like to train and set "numAnchors" equal to the number of access points received from the scan.

	Now, go to the "main" method and uncomment the "genTraining();" method. 
	This will be the first method to run for training. Save and compile MainTest.java (leaving the other two methods, "runTraining" and "test" ,commented out) 
	along with the other java files in "java_src" directory. Once compiled, place the class files into the "tools" folder in the libsvm-3.17** directory. 
	Open a command prompt and go to the libsvm/tools directory and run "java MainTest". This command may take some time. 

	Once the first run of MainTest is done, open up "MainTest.java", comment out the "genTraining();" method, uncomment the "runTraining()" method, save and compile the file. 
	Move the file over to the libsvm/tools directory and run MainTest again.

NOTE: THIS RUN TAKES THE LONGEST. This run may take more than a day to complete depending on the size of your sample output data.

	Once the second run is finished, repeat the steps with "test()" uncommented and "runTraining()" commented.

	Once all three methods have been run the training process is complete. You can now use your models for localization

	** I may have made some adjustments to the libsvm files to get them to work with our sample sets. Try and install and use libsvm normally,
		but I provided the libsvm files I used just in case the originala don't work.

Cluster Program:
     The cluster program will filter the original sample output file and only outputs data for the access points chosen from clustering. 
	 I used weka-clustering program: http://www.cs.waikato.ac.nz/ml/weka/ to make clusters. Then I used Cluster.java to parse and filter the output file.
     

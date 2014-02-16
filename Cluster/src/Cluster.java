import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Scanner;


public class Cluster {

	BufferedWriter bw; // Output buffered writer
	BufferedWriter apbw; // Access point file buffered writer
	File f; // Output file
	File inf; // Input file
	File apf; // AP file
	File apfile; // New Access point file
	FileWriter fw; // Output file writer
	FileWriter apfw; // Access point file writer
	String name = "cc1_76_cluster40"; // Name of new output file
	String apFileName = "apcc1_76_cluster40"; // Name of new access point file
	int numAPs = 138;
	int numNewAPs = 40;
	int totalSamples = 760;
	ArrayList <AccessPoint> aps = new ArrayList <AccessPoint>();
	ArrayList <String> points = new ArrayList <String>();
	ArrayList <AccessPoint> newAPs = new ArrayList <AccessPoint>();
	
	/**
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		Cluster clusterProg = new Cluster();
	}
	
	public Cluster() {
		initArrayList();
		if ( createOutputFile(name) ) {
			//createHeader();
			loadSampleSet();
			createAPFile(apFileName);
			loadNewAPs();
			loadTable();
			writeAdjSetToFile();
		}
		try {
			bw.close();
			apbw.close();
		} catch (Exception e)
		{
			e.printStackTrace();
			return;
		}
	}
	
	public void initArrayList()
	{
		for (int i = 0; i < numAPs; i++)
		{
			AccessPoint ap = new AccessPoint(totalSamples);
			aps.add(ap);
		}
	}

	public void loadSampleSet()
	{
		String data = load();
		Scanner ssScan = new Scanner(data);
		int indx = 0;
		//writeToFile("@data\n");
		while (ssScan.hasNext())
		{
			String line = ssScan.next();
			if (line.contains("#"))
			{
				points.add(line);
				continue;
			}
	
			String[] relation = line.split(";");
			for (int k = 0; k < relation.length; k++)
			{
				String[] vals = relation[k].split(":");
				aps.get(k).setRSSI(indx, Integer.parseInt(vals[1]));
				/*if (k == numAPs -1)
					writeToFile(vals[1]+"\n");
				else
					writeToFile(vals[1]+",");
				*/
				//System.out.print("rssi val: " + vals[1] + ",");
			}
			
			indx++; // Increment the index
		}
		
		ssScan.close();
		System.out.println("Points size: " + points.size());
	}
	
	public void writeAdjSetToFile()
	{
		int headerNum = 0;
		int cnt = 0;
		int index = 1;
		
		writeToOutFile(points.get(headerNum++) + "\n");
		for (int i = 0; i < totalSamples; i++)
		{
			if (cnt == 10)
			{
				writeToOutFile(points.get(headerNum++) + "\n");
				cnt = 0;
			}
			
			while ( index <= numNewAPs)
			{
				for (int j = 0; j < newAPs.size(); j++)
				{
					if (newAPs.get(j).getNewID() == index)
					{
						if (newAPs.get(j).getNewID() == numNewAPs)
							writeToOutFile(newAPs.get(j).getNewID() + ":" + aps.get(newAPs.get(j).getID()-1).getRSSI(i) + "\n");
						else
							writeToOutFile(newAPs.get(j).getNewID() + ":" + aps.get(newAPs.get(j).getID()-1).getRSSI(i) + ";");
						
						index++;
						break;
					}
				}
			}
			
			/*writeToFile("1:" + aps.get(27).getRSSI(i)+";");
			writeToFile("2:" + aps.get(12).getRSSI(i)+";");
			writeToFile("3:" + aps.get(18).getRSSI(i)+";");
			writeToFile("4:" + aps.get(102).getRSSI(i)+";");
			writeToFile("5:" + aps.get(88).getRSSI(i)+";");
			writeToFile("6:" + aps.get(121).getRSSI(i)+";");
			writeToFile("7:" + aps.get(74).getRSSI(i)+";");
			writeToFile("8:" + aps.get(20).getRSSI(i)+";");
			writeToFile("9:" + aps.get(68).getRSSI(i)+";");
			writeToFile("10:" + aps.get(46).getRSSI(i)+";");
			writeToFile("11:" + aps.get(106).getRSSI(i)+";");
			writeToFile("12:" + aps.get(31).getRSSI(i)+";");
			writeToFile("13:" + aps.get(11).getRSSI(i)+";");
			writeToFile("14:" + aps.get(5).getRSSI(i)+";");
			writeToFile("15:" + aps.get(52).getRSSI(i)+";");
			writeToFile("16:" + aps.get(113).getRSSI(i)+";");
			writeToFile("17:" + aps.get(84).getRSSI(i)+";");
			writeToFile("18:" + aps.get(59).getRSSI(i)+";");
			writeToFile("19:" + aps.get(109).getRSSI(i)+";");
			writeToFile("20:" + aps.get(16).getRSSI(i)+"\n"); */
			cnt++;
			index = 1; // Reset our id index
		}
	}
	
	public void writeArrayListToFile()
	{
		int val;
		writeToFile("@data\n");
		for (int i = 0; i < aps.size(); i++)
		{
			for (int k = 0; k < totalSamples; k++)
			{
				val = aps.get(i).getRSSI(k);
				if (k == totalSamples -1)
					writeToFile(val+"\n");
				else
					writeToFile(val+",");
			}
		}
	}
	
	public void writeToFile(String result)
	{
		try {
			apbw.write(result);
		} catch (Exception e)
		{
			e.printStackTrace();
			return;
		}
	}
	
	public void writeToOutFile(String result)
	{
		try {
			bw.write(result);
		} catch (Exception e)
		{
			e.printStackTrace();
			return;
		}
	}
	
	public boolean createHeader()
	{
		try {
			bw.write("@relation sample_cc\n\n");
			for (int i = 0; i < totalSamples; i++)
			{
				bw.write("@ATTRIBUTE sample" + (i+1) + " NUMERIC\n");
			}
			
			bw.write("\n");
			//bw.close();
			return true;
		} catch (Exception e)
		{
			e.printStackTrace();
			return false;
		}
	}
	
	public String load()
	{
	    try
	    {
	      inf = new File("C:\\Users\\Kwaku\\cc1_76_points.txt");
	      FileInputStream fis = new FileInputStream(inf);
	      BufferedReader reader = new BufferedReader(new InputStreamReader(fis));
	      String line = null, input="";
	      while ((line = reader.readLine()) != null)
	          input += line + "\n";
	      reader.close();
	      fis.close();
	      System.out.println("File successfully loaded.");
	      return input;
	    }
	    catch (Exception ex)
	    {
	      System.out.println("Error loading file: " + ex.getLocalizedMessage());
	      return "";
	    }
	} 
	
	public void loadNewAPs()
	{
		try {
			// Load the access points we want for the clustered file
			File newAPf = new File("C:\\Users\\Kwaku\\ap40.txt");
			FileInputStream fis = new FileInputStream(newAPf);
			BufferedReader reader = new BufferedReader(new InputStreamReader(fis));
		      String line = null;//, input="";
		      while ((line = reader.readLine()) != null)
		      {
		    	  System.out.println("line: " + line+"\n");
		    	  AccessPoint ap = new AccessPoint();
		    	  ap.setID(Integer.parseInt(line));
		    	  newAPs.add(ap);
		    	  System.out.println(ap.getID());
		      } 
		    	  //newAPs.add(Integer.parseInt(line)); // Add the new access points to the array list
		      reader.close();
		      fis.close();
		      
		      // Set the number of new APs
		      numNewAPs = newAPs.size();
		      
		      System.out.println("Access points successfully loaded.");
		} catch (Exception ex)
		{
			System.out.println("Error loading new access points");
			ex.printStackTrace();
			return;
		}
	}
	
	public void loadTable()
	{
		String input = "";
		int id;
		int newID = 1;
	    try
	    {
	      apf = new File("C:\\Users\\Kwaku\\apcc1_76_nexus.txt");
	      FileInputStream fis = new FileInputStream(apf);
	      BufferedReader reader = new BufferedReader(new InputStreamReader(fis));
	      String line = null;//, input="";
	      while ((line = reader.readLine()) != null)
	          input += line + "\n";
	      reader.close();
	      fis.close();
	      System.out.println("File successfully loaded.");
	    }
	    catch (Exception ex)
	    {
	      System.out.println("Error loading file: " + ex.getLocalizedMessage());
	      return;
	    }
	    
	    String[] r = input.split("\n");
	    
	    for (int k = 0; k < r.length; k++)
	    {
	    	String[] results = r[k].split(" ");
	    	id = Integer.parseInt(results[0]);
		    
	    		// Perform a lookup on the new access points list
	    		for (int j = 0; j < newAPs.size(); j++)
	    		{
	    			if (id == newAPs.get(j).getID())
	    			{
	    				//Set the new id
	    				writeToFile(newID + " " + results[1] + "\n");
	    				newAPs.get(j).setNewID(newID);
	    				newID++;
	    				break;
	    			}
	    		}
	    }
	    /*
	 	// Create a scanner on the input stream so we can parse the file data.
	    Scanner tabScan = new Scanner( input );
	    
	    // Clear the previously allocated AP table.
	    aps.clear();
	      
	    // Loop through each line in the file which contains AP mapping data.
	    while( tabScan.hasNext() )
	    {
	       // Create a new access point that we will be adding to our table.
	       AccessPoint newAp = new AccessPoint();
	       String line = tabScan.next();
	       String[] results = line.split(":");
	       writeToFile(tabScan.next());
	       id = 1;
	       //continue;//results[0]);
	       //id = tabScan.nextInt();
	       // Read the id number used for mapping APs.
	       //newAp.setID( Integer.parseInt(results[0]) );
	       
	       
	       // Read the unique BSSID of the AP that will be used to map to an id value.
	       //newAp.setBSSID( tabScan.next() );

	       // Add the AP to the table.
	       aps.add( newAp );
	    }
	    */
	}
	
	public boolean createOutputFile(String name)
	{
		try {
			f = new File("C:\\users\\Kwaku\\" + name + ".txt");
			
			f.createNewFile();
			
			fw = new FileWriter(f.getAbsolutePath());
			bw = new BufferedWriter(fw);
			
			return true;
			
		} catch (Exception e)
		{
			e.printStackTrace();
			return false;
		}
	}
	
	public void createAPFile(String newapname)
	{
		try {
			apfile = new File("C:\\users\\Kwaku\\" + newapname + ".txt");
			
			apfile.createNewFile();
			
			apfw = new FileWriter(apfile.getAbsolutePath());
			apbw = new BufferedWriter(apfw);
			
			
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
}

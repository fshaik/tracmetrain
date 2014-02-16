import sys
import re
import shlex
import collections

"""
 AdjustRSSIs
 Author: Kwaku Farkye
 This script takes a sample set (for sample output file format, 
 see https://sites.google.com/sites/tracmeboston) and adjusts
 the values based on parameters specified in the script.
 The script also outputs analysis on each access point and coordinate
 NOTE: This program may not work for either python3 or python2.7
 (I FORGET WHICH ONE IT DOES/DOESNT WORK FOR) 
 """
 
 """ USAGE: AdjustRSSIs.py [SampleFileName] [NewSampleFileName] """

""" Class representing a coordinate/point.
 Each coordinate has a list of samples for each scan and
 as well as a list of samples for each access point
 """
class coordinateSet:
	""" Class storing a coordinate and its sample set """
	#def __init__(self, coord, list):
	#	self.coord = coord
	#	self.setList = list
	coord = ''
	setList = []
	zippedRows = []
	adjustedRows = []
	finalList = []
	origMeans = [] #Mean for each access point (original values)
	origStdDev = [] #Standard deviation for each access point (original values)
	adjMeans = [] #Mean for each access point (adjusted values)
	adjStdDev = [] #Standard deviation for each access point (adjusted values)
	def printCoord(self):
		print(self.coord + '\n')
	
	def printSet(self):
		print(self.setList)
		
	def printFinal(self):
		print(self.finalList)
	
	def printAdjusted(self):
		print(self.adjustedRows)
	
	def printZipped(self):
		print(self.zippedRows)
		
	def addColumn(self, col):
		self.adjustedRows.append(col)
		
""" Object for each access point / rssi pair """
class sigReading:
	def __init__(self, ap, rssi):
		self.ap = ap
		self.rssi = rssi
		
	ap = 0;
	rssi = 0;

""" Arguments passed in """
class argFiles:
	def __init__(self, inFile, outFile):
		self.inFile = inFile
		self.outFile = outFile
	
	
def main(argv):
	set = []
	cSet = coordinateSet()
	files = argFiles(argv[1],argv[2])
	rFile = open(str(files.inFile), 'r')
	wFile = open(str(files.outFile), 'w')
	dFileName = str(files.outFile).split(".txt")
	dFile = open(str(dFileName[0]) + '_Analysis.txt', 'w')
	
	xgrid = rFile.readline()
	ygrid = rFile.readline()
	numAps = rFile.readline()
	
	wFile.write(xgrid + ygrid + numAps)
	i = 0
	
	coordSetList = [] #List of coordinateSet Objects
	line = rFile.readline()
		
		#Beginning of new coordinate
	if line[0] == '#': 
		cSet = coordinateSet()
		cSet.coord = line
		print(line + '\n')
			
	while True:
		readList = []
		line = rFile.readline()
		
		### Loop through the coordinate's sample set ###
		while (line[0] != '#'):
			readList = []
			## Regular Expression for access point/rssi pair
			exps = re.findall('[-+]?\d+:[-+]?\d+;', line)
			i = 0
			for x in exps:
				data = exps[i]
				rssi = int(data[data.find(":")+1:data.find(";")])
				readList.append(rssi)
				i = i+1
		
			set.append(readList)
			line = rFile.readline()
			if not line:
				cSet.setList = set
				cSet.zippedRows = [[row[i] for row in cSet.setList] for i in range(len(cSet.setList[0]))]
				coordSetList.append(cSet)
				cSet.printCoord()
				cSet.printSet()
				print('Not a line')
				break
		
		if not line:
			break
			
		#Finished with set, now add to instance
		cSet.setList = set
		
		#### PRINT TO TERMINAL ####
		cSet.printCoord()
		cSet.printSet()	
		
		if line[0] == '#': #Beginning of new coordinate
			coordSetList.append(cSet)
			cSet.zippedRows = [[row[i] for row in cSet.setList] for i in range(len(cSet.setList[0]))]
			set = []
			cSet = coordinateSet()
			cSet.coord = line
			print(line + '\n')
			
	
	### Now get the mean and standard deviation of each access point
	
	
	
	for x in coordSetList:
		x.printCoord()
		print(x.zippedRows)
		dFile.write(x.coord + '\n')
		
		inc = 0
		for y in x.zippedRows:
			x.origMeans.append(0)
			x.origStdDev.append(0)
			x.origMeans[inc], x.origStdDev[inc] = meanstdv(y)
			inc += 1
		
		print('\n')
		i = 0
		colSet = []
		for col in x.zippedRows:
			i += 1
			print(col)
			adjustedToZero = 0
			adjustedToMax = 0
			if col.count(0) <= (len(col)/2): #If a column has less than half of its vals as zeros
				#Replace zero with max value TODO: Take most common value and replace zero
				for n,o in enumerate(col):
					if o == 0:
						col[n] = max(col) #TODO: Replace max(col) with function that takes avg val of col
						adjustedToMax += 1
				#[max(col) if c == 0 else c for c in col]
				print('Change zeros to highest value')
				#x.adjustedRows.append(col)
			elif col.count(0) > ((1/2) * len(col)): #If a column has more than half of its vals as zeros
				for n,o in enumerate(col):
					if o != 0:
						col[n] = 0
						adjustedToZero += 1
				#[0 if c != 0 else c for c in col]
				#x.adjustedRows.append(col)
				print('Change other value to zero')
			
			## Now that zippedRow has adjusted values, get the adjustedMean and stddev
			x.adjMeans.append(0)
			x.adjStdDev.append(0)
			x.adjMeans[i-1], x.adjStdDev[i-1] = meanstdv(col)
			dFile.write('For Access Point: ' + str(i) + '\n')
			dFile.write('Values Adjusted to Zero: ' + str(adjustedToZero) + '\n')
			dFile.write('Values Adjusted to Max: ' + str(adjustedToMax) + '\n')
			dFile.write('Original Mean: ' + str(x.origMeans[i-1]) + '\n')
			dFile.write('Adjusted Mean: ' + str(x.adjMeans[i-1]) + '\n')
			dFile.write('Original StdDev: ' + str(x.origStdDev[i-1]) + '\n')
			dFile.write('Adjusted StdDev: ' + str(x.adjStdDev[i-1]) + '\n\n')
			
			colSet.append(col)
			print(col)
			print('hello')
			
			
		
		print('\n')
		x.adjustedRows = colSet
		
	print('Checking if first element has changed')
	for z in coordSetList:
		z.printCoord()
		print('\n')
		z.finalList = [[row[i] for row in z.adjustedRows] for i in range(len(z.adjustedRows[0]))]
		#z.printAdjusted()
		print('\n')
		#z.printFinal()
		print('\n')
		
	writeToFile(coordSetList, wFile)

#Writes the data to the files
def writeToFile(coordSetList, wFile):
	for a in coordSetList: #Get each coordinateSet object
		wFile.write(a.coord) #Write the coordinate
		for it in a.finalList: #Write the adjusted rssi's to file
			j=1
			while j <= len(it):
				wFile.write(str(j) + ':' + str(it[j-1]) + ';')
				j = j+1
			wFile.write('\n')
		
"""
FROM: William Park, Simple Recipes in Python
	http://www.physics.rutgers.edu/~masud/computing/WPark_recipes_in_python.html
Calculate mean and standard deviation of data x[]:
    mean = {\sum_i x_i \over n}
    std = sqrt(\sum_i (x_i - mean)^2 \over n-1)
"""
def meanstdv(x):
    from math import sqrt
    n, mean, std = len(x), 0, 0
    for a in x:
	mean = mean + a
    mean = mean / float(n)
    for a in x:
	std = std + (a - mean)**2
    std = sqrt(std / float(n-1))
    return mean, std
		
""" Main """
if __name__ == '__main__':
	main(sys.argv)
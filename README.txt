This program is used to extract and decipher GPS NMEA data
recorded using a GARMIN GPS Logger. This logger is used
for the Infrasound Experiment that involves use of M-Audio 
Recorder. 

The following are the files contained in this folder,
A) The JAVA source files used are
	1. extract.java ( main class file)
	2. extract_GPS_Data.java (class file)
	3. extractGPS.java (class file)
	4. oneChannelWAV.java (class file)
	5. Queue.java  (class file)
	6. twoChannelWAV.java (class file)
	7. WavInfo.java (class file)
	8. WavToTxt.java (class file)
	
B) ManifestGPSExtraction ( Used to generate JAR file)
C) GenerateGPSExtractionJar.bat (Executed from command line to generate JAR file)
	NOTE : To generate GPSExtraction.jar file execute "GenerateGPSExtractionJar.bat"
		   from the COMMAND PROMPT. This generates the JAR file for the Project in the 
           same folder.  		   
D) t.jpg ( Figure displayed in the GUI tool )
E) extract_GPS_Data.form is required when the above JAVA source files
   are used as source files for a NETBEANS project. Just copy
   all the JAVA source files along with the form file into the "src"
   directory of NETBEANS project folder.

   Note : extract_GPS_Data.form is not required when generating JAR from 
          command line. 

 To use the tool, double click on the Executable JAR file GPSExtraction.jar.

 
 Author : Mir M Ali


	
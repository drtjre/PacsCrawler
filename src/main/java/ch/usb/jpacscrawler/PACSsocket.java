package ch.usb.jpacscrawler;
/** PACSsocket.java
*   Encapsulates connection to PACS.
*   Perform queries and PULLS on PACS
*   current version based on dcmtk shell calls.
*  
*   @author thomas.re@usb.ch // +41779265636 - wechat wxid_8gx3f9phpivp22 GIOVANNI
*   @date Nov2016
*/

import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.io.OutputStreamWriter;
import java.io.FileOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.IOException;
import java.io.BufferedWriter;
import java.util.List;
import java.util.ArrayList;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;

public class PACSsocket {
         private static boolean DEBUG=true;
         private static boolean FREEZE=true;
         private boolean SHOW_ONLY= false; // do not actually perform query 
	 // Set for IMAGE level so coords of 1st image of SERIES are available for deep crawl.
	 private static final String[] FINDSCU_BASE= {"findscu","-to","6000","-v","-S"
	                                               ,"-k","StudyInstanceUID","-k","SeriesInstanceUID"
				                       ,"-k","SOPInstanceUID"};
	 private static final String[] FINDSCU_APPEND= {"-k","0008,0052=IMAGE","-k","InstanceNumber=1"};
	 private static final String[] MOVESCU_BASE= {"movescu","-v","-S","-k","0008,0052=IMAGE","+P","11112"};

	 private static final String DCM_IN= "/Applications/dcmtk3.6/dcm.in";
	 private static final String[] DCMDUMP_BASE= {"dcmdump"};
         String _aec;
         String _aet;
	 boolean _verbose;

	 public void setAEC(String aec) {_aec=aec;}
	 public void setAET(String aet) {_aet=aet;}

	 public PACSsocket(String aec, String aet) {
	   setAEC(aec);
	   setAET(aet);
	 }

         /** PULL single image for header analysis
         * mkdir /Volumes/RETJ4T/bonex/CT0000010;
	 *  
	 *  movescu  -v -S -k 0008,0052=IMAGE -aet YETI  -aec OSIRIX 127.0.0.1 11112 +P 11113 
	 *   -k StudyInstanceUID=1.2.276.0.18.14.200.2.0.0.20100407.45401.28 
	 *   -k SeriesInstanceUID=1.3.12.2.1107.5.1.4.49001.30000010040704122365600000000 
	 *   -k SOPInstanceUID=1.3.12.2.1107.5.1.4.49001.30000010040704122365600000250 
	 *   --output-directory /tmp/m1/ +P 11113 /Applications/dcmtk3.6/dcm.in

	 *  movescu -v -S -aet YETI -aec AE_ARCH2_4PR 10.5.66.74 104 +P 11112 -k 0008,0052="IMAGE"
	 *   --output-directory /tmp/m3 -k StudyInstanceUID=xxx -k SeriesInstanceUID=xxx -k SOPInstanceUID=xxx
	 *   /Applications/dcmtk/dcm.in
	 *
	 *  @args StudyInstanceUID uid for study
	 *  @args SeriesInstanceUID uid for series
	 *  @args SOPInstanceUID uid for image (object)
	 *   returns File dicom image data retrieved from PACS
         */
         public File pull(String StudyInstanceUID, String SeriesInstanceUID, String SOPInstanceUID) {
	     File pulledImageFile=null;
	   // create move to temp directory
	     Path tmpDir= FileUtil.createTempDirectory();
	     // Pull image to tempDir
	     ArrayList<String> argList= new ArrayList<String>();
	     for (String arg : MOVESCU_BASE) argList.add(arg);
	     argList.add("-aec");for (String arg : _aec.split(" ")) argList.add(arg);
	     argList.add("-aet");for (String arg : _aet.split(" ")) argList.add(arg);
	     argList.add("-k");argList.add("StudyInstanceUID="+StudyInstanceUID);
	     argList.add("-k");argList.add("SeriesInstanceUID="+SeriesInstanceUID);
	     argList.add("-k");argList.add("SOPInstanceUID="+SOPInstanceUID);
	     argList.add("--output-directory");argList.add(tmpDir.toString());
	     argList.add(DCM_IN);

	     File pulledFile= JavaSystemCaller.call(argList);

	     // get first file in directory if there is one:
	     File[] dcms= tmpDir.toFile().listFiles();
	     if (dcms.length>0) pulledImageFile= dcms[0];

	     return pulledImageFile;
	 }

	 /** dump contents - used for testing */
         public String toString() {
	   StringBuffer buf= new StringBuffer("PACSsocket::");
           buf.append("\n  _aec="+String.valueOf(_aec));
           buf.append("\n  _aet="+String.valueOf(_aet));
           buf.append("\n  DEBUG="+String.valueOf(DEBUG));
	   return buf.toString();
	 }

        /* example findscu:
        * findscu -v -S -k 0008,0052=SERIES  -aet YETI -aec AE_ARCH2_4PR 10.5.66
        *  -k PatientName -k PatientBirthDate -k PatientID -k PatientSex -k Stud
        *  -k BodyPartExamined -k StudyDescription -k SeriesDescription -k Acces
        *  -k SeriesNumber -k InstanceNumber -k ReferringPhysicianName -k Instan
        *  -k StudyInstanceUID -k SeriesInstanceUID
        *  -k StudyDate=20130101 -k StudyTime=000000-005959
        *  -k SeriesDate=20130101 -k SeriesTime=000000-005959
        *  -k Modality=CT
        */

	/** Test only */
        public static void main(String[] args) {
	   try {
	     DcmHeader out;  // use for single header result
	     DcmHeaderList outs; // use for multiple header lists

	     // Setup PACS connection
	     String aec= args[0]; 
	     String aet= args[1]; 
	     PACSsocket pacs= new PACSsocket(aec,aet);

	     Log.DEBUG("\n=== Query on Data/Modality ===");
	     String StudyDate= args[2];
	     String Modality= args[3];
	     outs= pacs.query(StudyDate, Modality);
	     Log.DEBUG("PACSsocket.query =======",outs);

	     Log.DEBUG("\n=== PULL Single Image and Get Header ===");
	     String StudyInstanceUID= "1.2.276.0.18.14.200.2.0.0.20100407.45401.28";
	     String SeriesInstanceUID= "1.3.12.2.1107.5.1.4.49001.30000010040704122365600000000";
	     String SOPInstanceUID= "1.3.12.2.1107.5.1.4.49001.30000010040704122365600000250";
	     outs= pacs.deepQuery(StudyInstanceUID, SeriesInstanceUID, SOPInstanceUID);
	     Log.DEBUG("PACSsocket.deepQuery =======",outs);

	     Log.DEBUG("\n=== deepQuery for DcmHeader single ===");
	     DcmHeader dcm= new DcmHeader();
             dcm.put("0020,000D",StudyInstanceUID);
             dcm.put("0020,000E",SeriesInstanceUID);
             dcm.put("0008,0018",SOPInstanceUID);
	     outs= pacs.deepQuery(dcm);
	     Log.DEBUG("pacs.deepQuery",outs);

	     Log.DEBUG("\n=== deepQuery for DcmHeaderList ===");
	     DcmHeaderList inList= new DcmHeaderList();
	     inList.add(dcm);
	     inList.add(dcm);
	     inList.add(dcm); // use same image but 3 times
	     outs= pacs.deepQuery(inList);
	     Log.DEBUG("pacs.deepQuery",outs);
	     Log.DEBUG("outs.length",outs.size());

	     Log.DEBUG("\n=== DeepQuery on Date/Modality  ===");
	     outs= pacs.deepQuery(StudyDate, Modality);
	     Log.DEBUG("pacs.deepQuery",outs);
	     System.out.println(DcmHeader.getDescKeysCsv());
	     System.out.println(outs);


	   }
	   catch (Exception e) {
	     System.out.println("TESTING usage: PACSsocket aec aet StudyDate Modality");
	     System.out.println(e);
	     e.printStackTrace();
	   }
	}

	/** given a list of DcmHeaders indicating a list of images in PACS
	*   pull each image, retrieve header and parse, 
	*   return list of headers found.
	*   // TODO Cleanup temp files and directories
	*/
	public DcmHeaderList deepQuery(DcmHeaderList inList) {
	  DcmHeaderList outList= new DcmHeaderList();

          for (DcmHeader dcm : inList) {
	    DcmHeaderList singleList= deepQuery(dcm);
	    outList.add(singleList);
	  }

	  return outList;
	}


	/** given a single DcmHeader which specifies a single dicom image
	*   retrieve image temporarily and parse header.
	*   (DcmHeaderList) - returns list with one entry - the DcmHeader for requested image
	*   returns empty list no match found, null if connection error.
	*   @arg DcmHeader must specify StudyInstanceUID, SeriesInstanceUID, SOPInstanceUID
	*/
	public DcmHeaderList deepQuery(DcmHeader dcmIn) {
	  DcmHeaderList outList= null;

          String StudyInstanceUID= dcmIn.get("0020,000D");
          String SeriesInstanceUID= dcmIn.get("0020,000E");
          String SOPInstanceUID= dcmIn.get("0008,0018");
	  if (StudyInstanceUID!=null && StudyInstanceUID.length()>0) 
	    if (SeriesInstanceUID!=null && SeriesInstanceUID.length()>0) 
	      if (SOPInstanceUID!=null && SOPInstanceUID.length()>0) 
	        outList= deepQuery(StudyInstanceUID, SeriesInstanceUID, SOPInstanceUID);

	  return outList;
	}

	/** given UID coordinates, pull image and parse header returning list of dicom tags 
	*   (DcmHeaderList) - returns list with one entry - the DcmHeader for requested image
	*   returns empty list no match found, null if connection error.
	*/
	public DcmHeaderList deepQuery(String StudyInstanceUID, String SeriesInstanceUID, String SOPInstanceUID) {
	     File dicom= pull(StudyInstanceUID,SeriesInstanceUID,SOPInstanceUID);
	     DcmHeaderList dl= (new DcmDump()).getDcmHeader(dicom);
	     FileUtil.deleteFileAndParentDirIfEmpty(dicom);
	     return dl;
	}

	/** perform a single query given 
	*  @arg selectTagList list of select tags 
	*  @arg whereTagList list of where tags 
	*  @arg date StudyDate in where clause
	*  @arg modality Modality in where clause
        *
	* example:
        * findscu -v -S -k 0008,0052=SERIES  -aet YETI -aec AE_ARCH2_4PR 10.5.66.74 104 
        *  -k PatientName -k PatientBirthDate -k PatientID -k PatientSex -k StudyDate -k Modality 
        *  -k BodyPartExamined -k StudyDescription -k SeriesDescription -k AccessionNumber -k StudyID 
        *  -k SeriesNumber -k InstanceNumber -k ReferringPhysicianName -k InstanceAvailability -k InstitutionName 
        *  -k StudyInstanceUID -k SeriesInstanceUID 
	*  -k StudyDate=20130101 -k StudyTime=000000-005959 
	*  -k SeriesDate=20130101 -k SeriesTime=000000-005959 
	*  -k Modality=CT
	*
	*  returns results as a list of DcmHeaders
	*/
	public DcmHeaderList query(List<String> selectTagList, List<String> whereTagList) {
	     // create query
	     int recordCount=0;
  	     DcmHeaderList dhlResults=null;

	     ArrayList<String> argList= new ArrayList<String>();
	     for (String arg : FINDSCU_BASE) argList.add(arg);
	     argList.add("-aec");for (String arg : _aec.split(" ")) argList.add(arg);
	     argList.add("-aet");for (String arg : _aet.split(" ")) argList.add(arg);
	     for (String s : selectTagList) if (s!=null && s.length()>0) {argList.add("-k");argList.add(s);}
	     for (String s : whereTagList) if (s!=null && s.length()>0) {argList.add("-k");argList.add(s);}
	     for (String arg : FINDSCU_APPEND) argList.add(arg);

	     if (SHOW_ONLY || DEBUG) {for (String s : argList) System.out.print(s+" ");System.out.println();}

	     if (!SHOW_ONLY) {
	       File queryOutput= JavaSystemCaller.call(argList);
	       //Log.DEBUG("jPCQuery.performSingleQuery queryOutput=",queryOutput);
               if (queryOutput!=null) {
  	         DcmtkParser dparser= new DcmtkParser(queryOutput);
  	         dhlResults= dparser.getDcmHeaders();
		 recordCount+= dhlResults.size();
  	         queryOutput.delete();
               }
	     }
	     return dhlResults;
	}

	/** query specific date/modality combo for ALL dicom fields in image header.
	*  Actually retrieves sample image for each series.
	*  
	*  @arg date StudyDate in where clause
	*  @arg modality Modality in where clause
	*/
	public DcmHeaderList deepQuery(String date, String modality) {
	  Log.DEBUG("deepQuery");
	  DcmHeaderList shallowResults= query(date, modality);
	  Log.DEBUG("shallowResults",shallowResults);
	  return deepQuery(shallowResults);
	}




	/** query specific date/modality combo with default SELECT fields
	*  @arg date StudyDate in where clause
	*  @arg modality Modality in where clause
	*/
	public DcmHeaderList query(String date, String modality) {
	   // Prepare default select tags
	   String[] selectTags= DcmHeader.getBasicDescTags();
	   ArrayList<String> selectTagList= new ArrayList<String>();
	   for (String tag : selectTags) selectTagList.add(tag);
	   // Prepare Where clause
	   ArrayList<String> whereTagList= new ArrayList<String>();
	   whereTagList.add("StudyDate="+date);
	   whereTagList.add("Modality="+modality);
	   // perform query
	   return query(selectTagList, whereTagList);
	}

}

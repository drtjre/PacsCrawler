package jPacsCrawler;

/** jPC_Query
*   ImageJ plugin wrapper for jPCQuery
*   Provides basic GUI for launching jPCQuery 
*   and permits visual feedback of results in a ResultsTable.
*
*   @author TJRE
*   @date   Aug2016
*/

import ij.*;
import ij.process.*;
import ij.gui.*;
import java.awt.*;
import ij.plugin.PlugIn;
import ij.plugin.filter.Analyzer;
import ij.measure.ResultsTable;
import java.io.*;
import java.util.List;
import java.util.ArrayList;

public class jPC_Query implements PlugIn {
         private static boolean DEBUG=false;
         private boolean SHOW_ONLY= false; // do not actually perform query 
         private static final String QUOTE="\"";
	 private static final String[] FINDSCU_BASE= {"findscu","-to","6000","-v","-S","-k","0008,0052=SERIES"};
	 String _aec;
	 String _aet;
	 boolean _deepCrawl;
	 boolean _xjsonDB;
	 String _jsonDBURL;
	 boolean _outputCSV;
	 String  _outputCSVFileName;
	 boolean _headless;
	 String _BodyPartExamined;
	 String _StudyDescription;
	 String _SeriesDescription;
	 String _InstitutionName;
	 String _ReferringPhysicianName;


	 String _startDate, _endDate;
	 String[] _modalityList;
	 String[] _selectTagList;

	public void run(String arg) {
	   if (dialog()) {
             performQueriesAccrossDateRange();
           }
        }

	/** perform a single query given 
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
	*/
	private void performSingleQuery(String date, String modality) {
	     // create query
	     ArrayList<String> argList= new ArrayList<String>();
	     for (String arg : FINDSCU_BASE) argList.add(arg);
	     argList.add("-aec");for (String arg : _aec.split(" ")) argList.add(arg);
	     argList.add("-aet");for (String arg : _aet.split(" ")) argList.add(arg);
	     for (String s : _selectTagList) if (s!=null && s.length()>0) {argList.add("-k");argList.add(s);}
	     if (_BodyPartExamined!=null && _BodyPartExamined.length()>0) 
               {argList.add("-k");argList.add("BodyPartExamined="+_BodyPartExamined);}
	     if (_StudyDescription!=null && _StudyDescription.length()>0) 
               {argList.add("-k");argList.add("StudyDescription="+_StudyDescription);}
	     if (_SeriesDescription!=null && _SeriesDescription.length()>0) 
               {argList.add("-k");argList.add("SeriesDescription="+_SeriesDescription);}
	     if (_InstitutionName!=null && _InstitutionName.length()>0) 
               {argList.add("-k");argList.add("InstitutionName="+_InstitutionName);}
	     if (_ReferringPhysicianName!=null && _ReferringPhysicianName.length()>0) 
               {argList.add("-k");argList.add("ReferringPhysicianName="+_ReferringPhysicianName);}

	     argList.add("-k");argList.add("StudyDate="+date);
	     argList.add("-k");argList.add("SeriesDate="+date);
	     argList.add("-k");argList.add("Modality="+modality);

	     if (SHOW_ONLY || DEBUG) {for (String s : argList) System.out.print(s+" ");System.out.println();}

	     if (!SHOW_ONLY) {
	       File queryOutput= JavaSystemCaller.call(argList);
               if (queryOutput!=null) {
  	         DcmtkParser dparser= new DcmtkParser(queryOutput);
  	         DcmHeaderList dhList= dparser.getDcmHeaders();
  	         if (DEBUG) dhList.toCSV();
  	         queryOutput.delete();
  	         if (!_headless) showResults(dhList);
		 if (_xjsonDB) sendResults(dhList);
		 if (_outputCSV) sendCSV(dhList);
               }
	     }
	}



	private void performQueriesAccrossDateRange() {
             // iterate over all chosen modalities
             int startYear= Integer.parseInt(_startDate.substring(0,4));
             int startMonth= Integer.parseInt(_startDate.substring(4,6));
             int startDay= Integer.parseInt(_startDate.substring(6,8));
             int endYear= Integer.parseInt(_endDate.substring(0,4));
             int endMonth= Integer.parseInt(_endDate.substring(4,6));
             int endDay= Integer.parseInt(_endDate.substring(6,8));
             int startMonthTmp, endMonthTmp, startDayTmp, endDayTmp;
             for (String modality : _modalityList) if (modality!=null) {
               for (int year= startYear;year<=endYear;year++) {
                 startMonthTmp=  (year==startYear?startMonth:1);
                 endMonthTmp=  (year==endYear?endMonth:12);
                 for (int month=startMonthTmp;month<=endMonthTmp;month++) {
                   startDayTmp= ((year==startYear)&&(month==startMonth)?startDay:1);
                   endDayTmp= ((year==endYear)&&(month==endMonth)?endDay:31);
                   for (int day=startDayTmp;day<=endDayTmp;day++) {
                     String date= String.format("%04d",year)
                       +String.format("%02d",month)
                       +String.format("%02d",day);
		     performSingleQuery(date,modality);
                   }
                 }
               }
             }
	}
	     


	/** Displays dialog with Query options 
	*   updates corresponding data members
	*   returns if choice was made
	 */
	private boolean dialog() {
	  final String[] MACHBOXES= {"Exact Match (else keywords)", "Match Case"};
	  final boolean[] MACHBOXES_DEF= {true,true};
	  final String EMPTY="";
	  GenericDialog gd = new GenericDialog("PACSminer QUERY");
	  gd.addMessage("Connection Info");
	  gd.addStringField("AEC","AE_ARCH2_4PR 10.5.66.74 104",64);
	  gd.addStringField("AET","YETI",64);
	  gd.addMessage("Query Info");
	  gd.addStringField("Body_Part_Examined",EMPTY,64);
	  gd.addStringField("Study_Description_",EMPTY,64);
	  gd.addStringField("Series_Description_",EMPTY,64);
	  gd.addStringField("Institution_Name_",EMPTY,64);
	  gd.addStringField("Referring_Physician_Name_",EMPTY,64);
	  gd.addMessage("StudyDate Range");
	  gd.addNumericField("after :",20100319.,0,8,"(YYYYMMDD)");
	  gd.addNumericField("before:",20100319.,0,8,"(YYYYMMDD)");
	  gd.addMessage("Modality");
	  final String[] MODALITIES= {"CX","CT","MR","PT","NM"};
	  final boolean[] MODALITIES_BOOL= new boolean[MODALITIES.length]; 
	  for (int i=0;i<MODALITIES_BOOL.length;i++) MODALITIES_BOOL[i]= true;
	  gd.addCheckboxGroup(1, MODALITIES.length, MODALITIES, MODALITIES_BOOL);
          gd.addMessage("Include in Response:");

	  final String[] DICOMTAGS= 
	   {
             "StudyDate",
             "StudyTime",
             "AccessionNumber",
             "InstanceAvailability",
             "Modality",
             "ReferringPhysicianName",
             "StudyDescription",
             "SeriesDescription",
             "PatientName",
             "PatientID",
             "PatientBirthDate",
             "PatientSex",
             "BodyPartExamined",
             "StudyID",
             "SeriesNumber",
             "InstanceNumber",
             "Rows",
             "Columns",
             "InstitutionName",
             "StudyInstanceUID",
             "SeriesInstanceUID"
	   };
	  final boolean[] DICOMTAGS_DEFS= new boolean[DICOMTAGS.length]; 
	  for (int i=0;i<DICOMTAGS_DEFS.length;i++) DICOMTAGS_DEFS[i]= true;
	  gd.addCheckboxGroup((int)Math.ceil(DICOMTAGS.length/4.),4, DICOMTAGS, DICOMTAGS_DEFS);
          gd.addMessage("Alternatively:");
	  gd.addCheckbox("DEEP CRAWL ALL DICOM TAGS (Takes Time)", false);
          gd.addMessage("DBASE Connection:");
	  gd.addCheckbox("Send Results to DB as JSBON", false);
	  gd.addStringField("JSON DBase URL","http://localhost:8983/solr/demo/update",64);
	  gd.addCheckbox("Save Results to CSV File", false);
	  gd.addStringField("CSV File Location","/tmp/pacscrawler.csv",64);
	  gd.addCheckbox("Hide Results (go headless)", false);
	  gd.showDialog();
	  if (gd.wasCanceled()) return false;
	  
	  // Get responses
	  _aec= gd.getNextString().trim();
	  _aet= gd.getNextString().trim();
	  _BodyPartExamined= gd.getNextString().trim();
	  _StudyDescription= gd.getNextString().trim();
	  _SeriesDescription= gd.getNextString().trim();
	  _InstitutionName= gd.getNextString().trim();
	  _ReferringPhysicianName= gd.getNextString().trim();
	  int startDate= (int)gd.getNextNumber();
	  int endDate= (int)gd.getNextNumber();
	  _startDate= String.format("%08d",startDate);
          _endDate= String.format("%08d",endDate);

	  for (int i=0;i<MODALITIES_BOOL.length;i++)  if (!gd.getNextBoolean()) MODALITIES[i]=null; // eliminated undesired Modalities
	  _modalityList= MODALITIES;
	  for (int i=0;i<DICOMTAGS_DEFS.length;i++)  if (!gd.getNextBoolean()) DICOMTAGS[i]=null;
	  _selectTagList= DICOMTAGS;
	  
	  _deepCrawl=   gd.getNextBoolean();

	  _xjsonDB=   gd.getNextBoolean();
	  _jsonDBURL= gd.getNextString().trim();
	  _outputCSV=   gd.getNextBoolean();
	  _outputCSVFileName= gd.getNextString().trim();

	  _headless=   gd.getNextBoolean();

	  return true;
	}


    /** Add to CSV file */

  private void sendCSV(DcmHeaderList dcmHeaderList) {
    try {
	  BufferedWriter writer = new BufferedWriter(new FileWriter(_outputCSVFileName,true)) ;
	  writer.write(dcmHeaderList.toCSV()) ;
	  writer.close();
    }
    catch (IOException e) {
      // TODO 
    }

  }

    /**
     * Add PD and LAA values to Results Table
     */
  private void showResults(DcmHeaderList dcmHeaderList) {
    ResultsTable rt= ResultsTable.getResultsTable();
    if (rt==null) {
      rt= new ResultsTable();
      Analyzer.setResultsTable(rt);
    }
   
    for (DcmHeader dcmHeader : dcmHeaderList) {
      rt.incrementCounter();
      String[] keys= dcmHeader.getKeys();
      for (String key : keys) {
        String val= dcmHeader.getValue(key);
        rt.addValue(key,val);
        if (DEBUG) System.out.print(key+"="+val+";");
      }
      if (DEBUG) System.out.println();
    }
    rt.show("Results");
  }

  /** send set of dicom headers to a database */
  private void sendResults(DcmHeaderList dhlist) {
    jPCdbc dbc= new jPCdbc(_jsonDBURL);
    dbc.post(dhlist);
  }

  private static void DEBUG(String msg, Object obj) {
    if (DEBUG) System.out.println(msg+"="+obj.toString());
  }
}

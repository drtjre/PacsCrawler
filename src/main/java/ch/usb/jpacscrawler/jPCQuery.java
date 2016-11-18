package ch.usb.jpacscrawler;

/** jPCQuery 
*   PACScrawler Query Object Core
*   Performs Query on PACS based on parameter list set during CTOR or setParams();
*
*   Parses response from PACS and sends results to a DB via http POST/JSON
*   and/or to a file in CSV format.
*
*   @author TJRE
*   @date   Aug2016
*/

/* @TODO: set option for TODAY so can run as cron job 
 *        call from dialog
 *        clean up code
 *        write user cases
 */

import java.io.File;
import java.util.List;
import java.util.ArrayList;

public class jPCQuery {
         private static boolean DEBUG=false;
         private boolean SHOW_ONLY= false; // do not actually perform query 

	 private static final String[] FINDSCU_BASE= {"findscu","-to","6000","-v","-S","-k","0008,0052=SERIES"};
         String _aec;
         String _aet;
         boolean _DEEP;
         boolean _xjsonDB;
         String _jsonDBURL;
         boolean _outputCSV;
         String  _outputCSVFileName;
	 boolean _verbose;
         String _startDate, _endDate;
         boolean _daily=false; 
	 int     _dailyTime= 2;
         List<String> _modalityList;
         List<String> _selectTagList; // "select" clause field names
         List<String> _whereTagList; // "where" clause expressions

	 DcmHeaderListOperator _plugin;  // generic plugin to perform on each header found

	 /** Set a plugin to call on each header item found */
	 public void setPlugin(DcmHeaderListOperator plugin) {
	   _plugin= plugin;
	 }


	 /** dump contents - used for testing */
         public String toString() {
	   StringBuffer buf= new StringBuffer("jPCQuer::");
           buf.append("\n  _aec="+String.valueOf(_aec));
           buf.append("\n  _aet="+String.valueOf(_aet));
           buf.append("\n  _DEEP="+String.valueOf(_DEEP));
           buf.append("\n  _xjsonDB="+String.valueOf(_xjsonDB));
           buf.append("\n  _jsonDBURL="+String.valueOf(_jsonDBURL));
           buf.append("\n  _outputCSV="+String.valueOf(_outputCSV));
           buf.append("\n  _outputCSVFileName="+String.valueOf(_outputCSVFileName));
           buf.append("\n  _verbose="+String.valueOf(_verbose));
           buf.append("\n  DEBUG="+String.valueOf(DEBUG));
           buf.append("\n  _endDate="+String.valueOf(_endDate));
           buf.append("\n  _daily="+String.valueOf(_daily));
           buf.append("\n  _dailyTime="+String.valueOf(_dailyTime));
           buf.append("\n  _daily="+String.valueOf(_daily));
	   buf.append("\n  _modalityList: "); for (String s : _modalityList) buf.append(s+" ");
	   buf.append("\n  _selectTagList: "); for (String s : _selectTagList) buf.append(s+" ");
	   buf.append("\n  _whereTagList: "); for (String s : _whereTagList) buf.append(s+" ");
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

	/** Launch a jPCQuery object with command line parameters */
        public static void main(String[] args) {
	   try {
	      jPCQuery jpcq= new jPCQuery(args);
	      Log.DEBUG("jPCQuery ver 1.0");
	      jpcq.run(null);
	   }
	   catch (Exception e) {
	     Log.error("jPCQuery.main",e);
	     showHelp();
	   }
	}

	private static void showHelp() {
	     System.out.println("usage: jPCQuery options {-k DicomTag1 -k DicomTag2 -k DicomTag3 ...}"
		   + "\n\t-aet AET"
		   + "\n\t-aec AEC IP PORT"
		   + "\n\t-start YYYYMMDD|today|today+-relativeDays \t# start date for search range "
		   + "\n\t-end YYYYMMDD|today|today+-relativeDays\t#  end date for search range"
		   + "\n\t-xDB http://DATABASE_URL:port\t# database to send results via http post/json  "
		   + "\n\t-csv PATH\t# store results as CSV file - can be used together with -xDB  "
		   + "\n\t-mod MODALITY\t# modality to use for search may be multiple (ie -mod CT -mod MR -mod CR)  "
		   + "\n\t-sel DicomTag\t# select clause dicom tag to retrieve - may be multiple (ie -sel StudyID -sel StudyDescription ...  "
		   + "\n\t-w   DicomTag=val\t# where clause dicom tag on which to filter (ie -w StudyDescription=*Thorax*)"
		   + "\n\t                    if using spaces in where clause, add double quotes around entire arg: -w \"StudyDescription=Thorax CT\""
		   + "\n\t-d   \t# perform deep crawl - will take a long time."
		   + "\n\t-daily hour24 # repeat daily at hour specified on a 24h format"
		   + "\n\t-SHOW_ONLY   \t# output intended call to PACS but do not perform"
		   + "\n\t--help \t# show help"
		   + "\n"
		   );
	    System.out.println("Example: \n\t"
	           + "\n  jPCQuery -aet YETI -aec AE_ARCH2_4PR 10.5.66 4100 "
		   + "-sel PatientName -sel PatientBirthDate -sel PatientID -sel PatientSex "
		   + "-sel StudyID -sel StudyDate -sel Modality -sel AccessionNumber "
		   + "-sel BodyPartExamined -sel StudyDescription -sel SeriesDescription "
		   + "-sel SeriesNumber -sel InstanceNumber -sel ReferringPhysicianName "
		   + "-sel InstitutionName "
		   + "-sel StudyInstanceUID -sel SeriesInstanceUID "
		   + "-w \"InstitutionName=University*\" "
		   + "-start 20100101 -end 20161001 -mod CT -mod MR -mod PET "
		   + "-csv /tmp/query_results.csv -xDB http://10.2.2.1:8989/update/pacsdb1/ "
		   + "\n"
		   );
	}

	 /** CTOR establishes details of findscu parameters necessary for 
	 *   connecting to PACS, dbase, and output csv file.
	 *
	 *   @args args - see usage below for arg list
	 */
	 public jPCQuery(String[] args) throws Exception {
	   if (args.length<2) throw new ArrayIndexOutOfBoundsException(""); 
	     _modalityList= new ArrayList<String>();
	     _selectTagList= new ArrayList<String>();
	     _whereTagList= new ArrayList<String>();
	     for (int i=0;i<args.length;i++) {
	       if (args[i].startsWith("-")) {
	         switch (args[i]) {
		   case "-aet":  _aet= args[++i]; break;
		   case "-aec":  _aec= args[++i]+" "+args[++i]+" "+args[++i];break;
		   case "-start" :   _startDate= args[++i];break;
		   case "-end" :   _endDate= args[++i];break;
		   case "-xDB" :   _xjsonDB=true; _jsonDBURL=args[++i];break;
		   case "-csv" :   _outputCSV=true; _outputCSVFileName=args[++i];break;
		   case "-mod" :   _modalityList.add(args[++i]);break;
		   case "-sel" : _selectTagList.add(args[++i]);break;
		   case "-w" :  _whereTagList.add(args[++i]);break;
		   case "-DEEP" :  _DEEP= true;break;
		   case "-v" :  _verbose= true; break;
		   case "-DEBUG" :  DEBUG= true; break;
		   case "-daily" :  _daily=true; _dailyTime= Integer.parseInt(args[++i]);break;
		   case "-SHOW_ONLY" :  SHOW_ONLY= true; break;
		   case "--help" : showHelp();break;
		   default :
		 }
	       }
	     }
	 }

	/** wrapper to run(String) */
	public void run() {run(null);}

	/** Central engine of this class - perform queries based on previously set arguments */
	public void run(String arg) {
	  String lastDate="00000000";
	   if (!_daily) {
	       Log.out("jPCQuery Starting Query Crawl");
               int recordCount= performQueriesAccrossDateRange();
	       Log.out("jPCQuery Done. RecordsFound=",recordCount);
	   }
	   else {
	     Log.DEBUG("jPCQuery.run:doing daily at time:",_dailyTime);
	     while (true) {
	       if (!DateUtil.getTodaysDate().equals(lastDate) && DateUtil.getCurrentHour()==_dailyTime) {
	         Log.out("jPCQuery: starting daily update...", DateUtil.getDate());
                 int recordCount= performQueriesAccrossDateRange();
	         Log.out("jPCQuery Done. RecordsFound=",recordCount);
		 Log.out("jPCQuery: next update scheduled for: "+DateUtil.getDate("today+1")+"@"+_dailyTime);
		 lastDate= DateUtil.getTodaysDate();
	       }
	       try {
	           Thread.sleep(60000); // pole for repeat hour every few minutes
	       }
	       catch(Exception e) {
	           Log.error("jPCQuery.run",e);
	       }
	     }
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
	*
	*  returns number or records found
	*/
	protected int performSingleQuery(String date, String modality) {             
	     int recordCount=0;
             DcmHeaderList qresults; 

             // Setup PACS connection
             PACSsocket pacs= new PACSsocket(_aec,_aet);

	     // create query
	     if (!_DEEP) 
	       qresults= pacs.query(date,modality);
	     else 
	       qresults= pacs.deepQuery(date,modality);

             if (qresults!=null) {
		 recordCount+= qresults.size();
		 if (_outputCSV) qresults.saveCSV(new File(_outputCSVFileName));
		 if (_xjsonDB) sendResults(qresults);
  	         if (_plugin!=null) _plugin.run(qresults);
             }
	     return recordCount;
	}


	/** Performs Query across date range by dividing up into daily and modality-based
	*   short queries.  Motivation: most PAC's have a limit of query responses 
	*   and by dividing into day based queries, one can stay below this limit.
	*
	*   returns total number of records found.
	*/
	private int performQueriesAccrossDateRange() {
             // iterate over all chosen modalities
	     // important calculate DateRange at every query as query may be repeated over many days
	     int recordCount=0;
	     String startDate=DateUtil.getDate(_startDate);  
	     String endDate= DateUtil.getDate(_endDate);
             int startYear= Integer.parseInt(startDate.substring(0,4));
             int startMonth= Integer.parseInt(startDate.substring(4,6));
             int startDay= Integer.parseInt(startDate.substring(6,8));
             int endYear= Integer.parseInt(endDate.substring(0,4));
             int endMonth= Integer.parseInt(endDate.substring(4,6));
             int endDay= Integer.parseInt(endDate.substring(6,8));
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
		     recordCount+= performSingleQuery(date,modality);
                   }
                 }
               }
             }
	     return recordCount;
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

package jPacsCrawler;

/** DcmHeader 
*   Simple Hashtable based data structure
*   for sorting, storing, outputing as csv format
*   dicom header data.
*
*   author thomas.re@usb.ch
*   date July2016
*/

import java.util.Hashtable;
import java.util.List;
import java.io.UnsupportedEncodingException;

public class DcmHeader {

  /** test routine */
  public static void main(String[] args) {
    DcmHeader dh= new DcmHeader();
    for (int i=0;i<args.length;i++)
      dh.put(args[i++], args[i]);
    System.out.println(dh.getValuesCsvQuoted());
    System.out.println(dh.toJSON());

  }

  public String toCSV() {return getValuesCsvQuoted();}

  private static final String ID_TAG="0000,0000"; 
  private static final String ID_DESC="id"; 
  private static final String ID_MATCH_TAG="0020,000E"; // field which id duplicates

  private static final String[][] KEYS=
    {
    {"0010,0010","PatientName"},
    {"0010,0030","PatientBirthDate"},
    {"0010,0020","PatientID"},
    {"0010,0040","PatientSex"},
    {"0008,0020","StudyDate"},
    {"0008,0030","StudyTime"},
    {"0008,0060","Modality"},
    {"0018,0015","BodyPartExamined"},
    {"0008,1030","StudyDescription"},
    {"0008,103E","SeriesDescription"},
    {"0008,0050","AccessionNumber"},
    {"0020,0010","StudyID"},
    {"0020,0011","SeriesNumber"},
    {"0020,0013","InstanceNumber"},
    {"0008,0090","ReferringPhysicianName"},
    {"0008,0056","InstanceAvailability"},
    {"0018,1030","ProtocolName"},
    {"0008,0080","InstitutionName"},
    {"0020,000D","StudyInstanceUID"},
    {"0020,000E","SeriesInstanceUID"},
    {ID_TAG,ID_DESC}   // added field for dbase use
    };
    /* TODO
    {"0018,0050","SliceThickness"},
    {"0018,0060","kVp"},
    {"0018,1151","X-rayTubeCurrent"},
    {"0018,1152","Exposure"},
    {"0018,1200","DateofLastCalibration"},
    {"0018,1210","ConvolutionKernel"}
    */


  private static final String REQUIRED_TAG="0020,000D";
  private static final String PACS_UTIL_TAG="XferredBy";
  private static final String PACS_UTIL="PACScrawler20160923";

  private static final String EMPTY="";
  private static final String CSVDLT=";";
  private static final String JSON_PAIR_DLT=":";
  private static final String JSON_FLD_DLT=", ";
  private static final String LF="\\n";
  private static final String QT="\"";
  
  private static final char QUOTE='\"';
  private static final char HASH='#';


  private Hashtable _data;
  /** initiate data as empty */
  public DcmHeader() { 
    _data= new Hashtable();
    for (int i=0;i<KEYS.length;i++) _data.put(KEYS[i][0],EMPTY);
  }

  /** Add key value pair, but only if key is recognized. */
  public void put(String key, String val) {
    val= val.trim().replace(QUOTE,HASH);
    String keyUC= key.toUpperCase();
    if (_data.containsKey(keyUC)) {
      _data.put(keyUC,val);
      if (keyUC.equals(ID_MATCH_TAG)) _data.put(ID_TAG,val);
    }
  }

  public boolean isComplete() {
    String requiredVal= (String)_data.get(REQUIRED_TAG);
    return (requiredVal!=null && requiredVal.length()>0);
  }

  public String get(String key) {
    return (String)_data.get(key.toUpperCase());
  }

  public String getValue(String key) {return get(key);}

  /** returns semi-colon delimited list of dicom values */
  public String toString() {
    return getValuesCsv();
  }


  /** keys in header */
  public static String[] getKeys() {
    String[] result= new String[KEYS.length];
    for (int i=0;i<KEYS.length;i++) 
      result[i]= (KEYS[i][0]);
    return result;
  }

  /** keys in header */
  public static String[] getDescKeys() {
    String[] result= new String[KEYS.length];
    for (int i=0;i<KEYS.length;i++) 
      result[i]= KEYS[i][1];
    return result;
  }

  /** Values delimited as JSON */
  public String toJSON() {
    StringBuffer buffy= new StringBuffer("{");
    for (String[] keypair : KEYS) { 
      buffy.append(QT+keypair[1]+QT+JSON_PAIR_DLT+QT+getValue(keypair[0])+QT+JSON_FLD_DLT);
    }
    buffy.append(QT+PACS_UTIL_TAG+QT+JSON_PAIR_DLT+QT+PACS_UTIL+QT);
    buffy.append("}");
    return buffy.toString();
  }


  /** Values delimited as CSV */
  public String getValuesCsv() {
    StringBuffer buffy= new StringBuffer();
    for (String[] keypair : KEYS)
      buffy.append(String.valueOf(_data.get(keypair[0]))+CSVDLT);
    return buffy.toString();
  }

  /** Values delimited as CSV */
  public String getValuesCsvQuoted() {
    StringBuffer buffy= new StringBuffer();
    for (String[] keypair : KEYS)
      buffy.append(QT+String.valueOf(_data.get(keypair[0]))+QT+CSVDLT);
    return buffy.toString();
  }

  /** returns csv delimited list of dicom keys */
  public String getKeysCsv() {
    StringBuffer buffy= new StringBuffer();
    for (int i=0;i<KEYS.length;i++) 
      buffy.append(KEYS[i][0]+CSVDLT);
    return buffy.toString();
  }

  /** returns semi-colon delimited list of dicom keys */
  public static String getDescKeysCsv() {
    StringBuffer buffy= new StringBuffer();
    for (int i=0;i<KEYS.length;i++) 
      buffy.append(KEYS[i][1]+CSVDLT);
    return buffy.toString();
  }
}

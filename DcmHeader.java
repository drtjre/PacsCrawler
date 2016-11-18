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

  /** returns set of basic Descriptive tags */
  public static String[] getBasicDescTags() {
    return BASIC_DESC_TAGS;
  }
  private static final String[] BASIC_DESC_TAGS=
   {
     "PatientName",
     "PatientBirthDate",
     "PatientID",
     "PatientSex",
     "StudyDate",
     "StudyTime",
     "Modality",
     "BodyPartExamined",
     "StudyDescription",
     "SeriesDescription",
     "AccessionNumber",
     "StudyID",
     "SeriesNumber",
     "InstanceNumber",
     "ReferringPhysicianName",
     "InstanceAvailability",
     "ProtocolName",
     "InstitutionName",
     "StudyInstanceUID",
     "SeriesInstanceUID",
     "SOPInstanceUID"
   };

  private static final String[][] KEYS=
    {
    // Basics
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

    // Required for Image Retrieval
    {"0020,000D","StudyInstanceUID"},
    {"0020,000E","SeriesInstanceUID"},
    {"0008,0018","SOPInstanceUID"},

    // Deep-Crawl Additions
    {"0002,0003","MediaStorageSOPInstanceUID"},
    {"0002,0012","ImplementationClassUID"},
    {"0002,0013","ImplementationVersionName"},
    {"0002,0016","SourceApplicationEntityTitle"},
    {"0008,0005","SpecificCharacterSet"},
    {"0008,0008","ImageType"},
    {"0008,0012","InstanceCreationDate"},
    {"0008,0013","InstanceCreationTime"},
    {"0008,0021","SeriesDate"},
    {"0008,0022","AcquisitionDate"},
    {"0008,0023","ContentDate"},
    {"0008,0031","SeriesTime"},
    {"0008,0032","AcquisitionTime"},
    {"0008,0033","ContentTime"},
    {"0008,0070","Manufacturer"},
    {"0008,0081","InstitutionAddress"},
    {"0008,1010","StationName"},
    {"0008,1048","PhysiciansOfRecord"},
    {"0008,1050","PerformingPhysicianName"},
    {"0008,1090","ManufacturerModelName"},
    {"0009,0010","PrivateCreator"},
    {"0010,0021","IssuerOfPatientID"},
    {"0010,1010","PatientAge"},
    {"0010,1030","PatientWeight"},
    {"0010,1040","PatientAddress"},
    {"0018,0020","ScanningSequence"},
    {"0018,0021","SequenceVariant"},
    {"0018,0022","ScanOptions"},
    {"0018,0023","MRAcquisitionType"},
    {"0018,0024","SequenceName"},
    {"0018,0025","AngioFlag"},
    {"0018,0050","SliceThickness"},
    {"0018,0080","RepetitionTime"},
    {"0018,0081","EchoTime"},
    {"0018,0083","NumberOfAverages"},
    {"0018,0084","ImagingFrequency"},
    {"0018,0085","ImagedNucleus"},
    {"0018,0086","EchoNumbers"},
    {"0018,0087","MagneticFieldStrength"},
    {"0018,0089","NumberOfPhaseEncodingSteps"},
    {"0018,0091","EchoTrainLength"},
    {"0018,0093","PercentSampling"},
    {"0018,0094","PercentPhaseFieldOfView"},
    {"0018,0095","PixelBandwidth"},
    {"0018,1000","DeviceSerialNumber"},
    {"0018,1020","SoftwareVersions"},
    {"0018,1251","TransmitCoilName"},
    {"0018,1312","InPlanePhaseEncodingDirection"},
    {"0018,1314","FlipAngle"},
    {"0018,1315","VariableFlipAngleFlag"},
    {"0018,1316","SAR"},
    {"0018,1318","dBdt"},
    {"0018,5100","PatientPosition"},
    {"0019,0010","PrivateCreator"},
    {"0020,000d","StudyInstanceUID"},
    {"0020,000e","SeriesInstanceUID"},
    {"0020,0012","AcquisitionNumber"},
    {"0020,0032","ImagePositionPatient"},
    {"0020,0037","ImageOrientationPatient"},
    {"0020,0052","FrameOfReferenceUID"},
    {"0020,1041","SliceLocation"},
    {"0028,0004","PhotometricInterpretation"},
    {"0028,0030","PixelSpacing"},
    {"0028,1050","WindowCenter"},
    {"0028,1051","WindowWidth"},
    {"0028,1055","WindowCenterWidthExplanation"},
    {"0029,0010","PrivateCreator"},
    {"0029,0011","PrivateCreator"},
    {"0032,1032","RequestingPhysician"},
    {"0040,0244","PerformedProcedureStepStartDate"},
    {"0040,0245","PerformedProcedureStepStartTime"},
    {"0040,0253","PerformedProcedureStepID"},
    {"0040,0254","PerformedProcedureStepDescription"},
    {"0051,0010","PrivateCreator"},
    {"0903,0010","PrivateCreator"},
    {"0905,0010","PrivateCreator"},
    {"7fd1,0010","PrivateCreator"},


    // Special Last field for DBASE USE
    {ID_TAG,ID_DESC}   
    };


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
    //Log.DEBUG("dcmHeader.put-key=:",key);
    //Log.DEBUG("dcmHeader.put-val=:",val);
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
    StringBuffer buffy= new StringBuffer();
    for (int i=0;i<KEYS.length;i++) {
      String key= (KEYS[i][0]);
      String keyDesc= (KEYS[i][1]);
      buffy.append(keyDesc+"="+getValue(key)+";");
    }
    return buffy.toString();
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

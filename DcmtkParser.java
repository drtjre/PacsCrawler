package jPacsCrawler;


/** 
 *  Parse output from DCMTK findscu command (with -v option).
 *  identifies Dicom Header for each data set in reply 
 *  and outputs to stdout .csv formated datasets.
 *
 */
import java.io.*;
import java.util.List;
import java.util.ArrayList;

public class DcmtkParser {
  private static final String DATA_TAG="W: # Dicom-Data-Set";
  private static final int MIN_DATA_LINE_LENGTH= 20;
  private static final String DL= ";";  // delimiter
  private static final String LF= "\\n";  // delimiter

  private DcmHeaderList _dhList;
  private String _outputCSVFileName;

  public static String getVer() {return "ver 8859-1";}

  public DcmtkParser(String infileName) {
    this(new File(infileName));
  } 

  public DcmtkParser(String infileName, String outfileName) {
    this(new File(infileName));
    _outputCSVFileName= outfileName;
  } 

  /** parse file and store records internally until requested 
  *   @arg infileName full path of file to parse
  */
  public DcmtkParser(File infile) {
    _dhList= new DcmHeaderList();
    try {
      BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(infile), "ISO-8859-1"));
      String line;
      DcmHeader dcmHeader= new DcmHeader();

      while ((line = br.readLine()) != null) {
        // process the line.
        if (line.startsWith(DATA_TAG)) {
          // new record
          dcmHeader= new DcmHeader(); 
          while ((line = br.readLine())!= null && line.length()>MIN_DATA_LINE_LENGTH) {
            dcmHeader.put(getTag(line),getValue(line));
          }
          if (dcmHeader.isComplete()) {
	    _dhList.add(dcmHeader);
          }
        }
      }
    }
    catch (IOException e)  {
      Log.error("DcmtkParser.ctor",e);
    }
  }


  /** retrieve list of DcmHeaders currently stored in object.
  *   if object was created with an input file, then it will 
  *   have one DcmHeader per record parsed from this file.
  */
  public DcmHeaderList getDcmHeaders()  {
    return _dhList;
  }

  /** Convert contents of infile to CSV string 
  */
  public String toCSV() {
    StringBuffer outBuff= new StringBuffer();
    outBuff.append(_dhList.toCSV());
    return outBuff.toString();
  }

  /** send current contents of last parse to a file in CSV format */
  public void saveCSV(String csvFileName) {
    try {
      File csvFile= new File(csvFileName);
      if (!csvFile.exists()) {
          // Start with Header on First Access
          OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(csvFileName, true), "UTF-8") ;
          writer.write(DcmHeader.getDescKeysCsv());
          writer.write("\n");
          writer.close();
      }
      OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(csvFileName, true), "UTF-8") ;
      writer.write(this.toCSV()) ;
      writer.close();
    }
    catch (IOException e) {
      Log.error("DcmtkParser.saveCSV",e);
    }
  }


  /** Test only */
  public static void main(String[] args) {
    try {
      DcmtkParser dp= new DcmtkParser(args[0]);
      System.out.println(dp.toCSV());
      dp.saveCSV(args[1]);
    }
    catch (NullPointerException e)  {
      e.printStackTrace(System.out);
    }
    catch (Exception e)  {
      System.out.println("usage: DcmtkParser  infilename outfilename");
      System.out.println("  where filename contains 2> output from dcmtk/findscu.");
      System.out.println("  output is semi-colon delimited quoted fields");
      System.out.println();
      Log.error("DcmtkParser.main",e);
    }
  }

  /** extract numeric DICOM TAG from a line of dcmtk output
   *  
   *  @arg dcmLine line of stdout form dcmtk
   */
  private static String getTag(String dcmLine) {
    String val= "";
    int openBracketIndex= dcmLine.indexOf("(");
    int closeBracketIndex= dcmLine.indexOf(")");
    if (openBracketIndex>=0 && closeBracketIndex>=0) {
      val= dcmLine.substring(openBracketIndex+1, closeBracketIndex);
    }
    return val;
  }

  /** extract value of DICOM header from a line of dcmtk output
   *  
   *  @arg dcmLine line of stdout form dcmtk
   */
  private static String getValue(String dcmLine) {
    String val= "";
    int openBracketIndex= dcmLine.indexOf("[");
    int closeBracketIndex= dcmLine.indexOf("]");
    if (openBracketIndex>=0 && closeBracketIndex>=0) {
      val= dcmLine.substring(openBracketIndex+1, closeBracketIndex);
    }
    return val;
  }
}


package ch.usb.pacscrawler;

/** DcmDump
*   wrapper for dcmdump of dcmtk
*   retrieves header of a dicom file and parses
*
*   @author TJRE
*   @date   Nov2016
*/

import java.util.ArrayList;
import java.io.File;

public class DcmDump {

  private static final String DCMDUMP="dcmdump"; // command line for dcmtk tool

  /** Parses out header of current dicom file 
   *  result should contain only one header 
  */
  public DcmHeaderList getDcmHeader(File dcmFile) {
    DcmHeaderList dcmHeaderList= null;
      ArrayList<String> alist= new ArrayList<String>();
      alist.add(DCMDUMP);
      alist.add(dcmFile.getPath().toString());
      File out= JavaSystemCaller.call(alist);
      DcmtkParser dp= new DcmtkParser();
      dp.parse(out);
      dcmHeaderList= dp.getDcmHeaders();

    return dcmHeaderList;
  }


  /** For Testing only */
  public static void main(String[] args) {
    try {
      DcmDump ddp= new DcmDump();
      System.out.println(ddp.getDcmHeader(new File(args[0])).toString());
    }
    catch (Exception e) {
      System.out.println("usage: DcmDump filename");
    }
  }
}

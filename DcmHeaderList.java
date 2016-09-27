package jPacsCrawler;


/** Container class for DcmHeader 
* 
*  @author TJRE
*  @date   Sep2016
*/

import java.util.ArrayList;
import java.util.Iterator;
import java.io.File;

public class DcmHeaderList extends ArrayList<DcmHeader> {

  public String toCSV() {
    StringBuffer buffy= new StringBuffer();
    Iterator<DcmHeader> iter = super.iterator();
    while (iter.hasNext()) {
      DcmHeader dh= iter.next(); 
      buffy.append(dh.toCSV()+"\n");
    }
    return buffy.toString();
  }

  /** Convert contents of List to JSON format.
   *  Each contained Dicom Header Is added in JSON format.
   */
  public String toJSON() {
    StringBuffer buffy= new StringBuffer("[");
    Iterator<DcmHeader> iter = super.iterator();
    while (iter.hasNext()) {
      DcmHeader dh= iter.next(); 
      buffy.append(dh.toJSON()+(iter.hasNext()?", ":""));
    }
    buffy.append("]");
    return buffy.toString();
  }

  /** Testing only */
  public static void main(String[] args) {
    DcmHeaderList dhl= DcmHeaderList.genTestData();
    System.out.println(dhl.toCSV());
    System.out.println(dhl.toJSON());
  }

  public static DcmHeaderList genTestData() {
    DcmHeaderList dhl= new DcmHeaderList();
    DcmHeader dh1= new DcmHeader();
    DcmHeader dh2= new DcmHeader();
    DcmHeader dh3= new DcmHeader();
    dh1.put("0010,0010","JoePesce");
    dh2.put("0010,0010","MariaPesce");
    dh3.put("0010,0010","AugPesce");
    dh1.put("0020,0010","1234");
    dh2.put("0020,0010","5678");
    dh3.put("0020,0010","9823");

    dhl.add(dh1);
    dhl.add(dh2);
    dhl.add(dh3);

    return dhl;

  }
}

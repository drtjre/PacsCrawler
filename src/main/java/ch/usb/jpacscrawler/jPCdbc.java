package usb.jpacscrawler;

/** jPCdbc - jPACScrawler DataBase Connectivity
* 
* @author TJRE
* @date   Sep2016
*/

public class jPCdbc {

  /** create connection to specific URL for http transfer 
  *  
  * @arg dbURL URL to DB for http POST
  */
  public jPCdbc(String dbURL) {
    _curlBase= "curl "+dbURL+" -d ";
  }

  private String _curlBase;

  /** send data to data base
  *   @arg dhlist dicom headers to post to dbase
  */
  public void post(DcmHeaderList dhlist) {
    // this implementation uses a temporary bash file
    // send one record at a time to ensure no overload
    StringBuffer cmdBuff= new StringBuffer();
    for (DcmHeader dh : dhlist) 
      cmdBuff.append(_curlBase+"'["+dh.toJSON()+"]'\n");
    DEBUG("post:",cmdBuff);
    JavaSystemCaller.executeBash(cmdBuff.toString());
  }


  /** test only */
  public static void main(String[] args) {
    try {
      jPCdbc dbc= new jPCdbc(args[0]);
      DcmHeaderList dhl= DcmHeaderList.genTestData();
      System.out.println("Preparing to send : "+dhl);
      dbc.post(dhl);
    }
    catch (Exception e) {
      System.out.println("This main is for testing ONLY!");
      System.out.println("usage: jPCdbc URL");
    }


    
  }

  private static final boolean DEBUG=false;
  private void DEBUG(String msg,Object obj) {
    if (DEBUG) {
      System.out.println("jPCdbc::----------------------\n"+msg+"="+obj.toString());
      System.out.println("-------------------------");
    }
  }
}

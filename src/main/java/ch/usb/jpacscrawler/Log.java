package usb.jpacscrawler;


/** Log
*   Simple Logging Util
*   output to STD or STD_ERROR
*    
*   @author TJRE
*   @date   Aug2016
*/

public class Log {
  private static boolean VERBOSE=true;
  private static boolean DEBUG=true;

  public static void setVerbose(boolean b) {VERBOSE=b;}
  public static void setDebug(boolean b) {DEBUG=b;}

  public static void DEBUG(String msg, Object obj) {
    if (DEBUG) System.out.println(combine("DEBUG",msg,obj));
  }
  public static void DEBUG(String msg) {DEBUG(msg,null);}



  public static void out(String msg, Object obj) {
    if (VERBOSE) System.out.println(combine("Log",msg,obj));
  }
  public static void out(String msg) {out(msg,null);}


  public static void error(String msg, Object obj) {
    System.err.println(combine("ERROR",msg,obj));
  }
  public static void error(String msg) {error(msg,null);}

  private static String combine(String tag, String msg, Object obj) {
    return tag+": "+msg+(obj!=null?"="+obj.toString():".");
  }

  /** Tester */
  public static void main(String[] args) {
    int i= 23;
    Log.DEBUG("Hello World DEBUG", i);
    Log.DEBUG("Hello World DEBUG");
    Log.out("Hellow World out", i);
    Log.out("Hellow World out");
    Log.error("Hellow World error", i);
    Log.error("Hellow World error");
  }


}

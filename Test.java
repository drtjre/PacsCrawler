
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

public class Test {

  /** Add key value pair, but only if key is recognized. */
  public static void main(String[] args) {
    try {
      char QUOTE='\"';
      char HASH='#';
      String val= args[0].replace(QUOTE,HASH);
      System.out.println(val);
    }
    catch (Exception e) {
      System.out.println("ERROR"+e.toString());
    }
  }

}

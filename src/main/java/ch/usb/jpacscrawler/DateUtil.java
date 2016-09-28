package usb.jpacscrawler;

/** Simple Date Utilities to use in jPC 
* 
* @author TJRE
* @date   sep2016
*/

import java.util.Date;
import java.util.Calendar;
import java.text.DateFormat;
import java.text.SimpleDateFormat;


public class DateUtil {

        public static String getTodaysDate() {
          DateFormat dateFormat= new SimpleDateFormat("yyyyMMdd");
          Date date= new Date();
          return String.valueOf(dateFormat.format(date));
        }

        /** returns date of previous day
         *  @arg daysAgo number of days to go back (1=yesterday)
         */
        public static String getRelativeDate(int days) {
              DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
              Calendar cal = Calendar.getInstance();
              cal.add(Calendar.DATE, days);
              return dateFormat.format(cal.getTime());
        }

        public static int getCurrentHour() {
          int HH= 0;
          try {
            DateFormat hourFormat= new SimpleDateFormat("HH");
            Date date= new Date();
            HH=Integer.parseInt(hourFormat.format(date));
          }
          catch (Exception e) {
          }
          return HH;
        }

	/** interpret date from number or simple phrase */
	public static String getDate(String dateString) {
	  String out= dateString; // default
	  if (dateString.equals("today")) {out=getRelativeDate(0);}
	  if (dateString.startsWith("today-") || dateString.startsWith("today+")) {
	    try {
	      int i= Integer.parseInt(dateString.substring(5));
	      out= getRelativeDate(i);
	    }
	    catch(Exception e) {
	      Log.error("Bad Date format",dateString);
	    }
	    
	  }
	  return out;
	}

  public static void main(String[] args) {
   try {
    switch (args[0]) {
      case "-today" : System.out.println(getTodaysDate());
        break;
      case "-relative" : System.out.println(getRelativeDate(Integer.parseInt(args[1]))); break;
      case "-hour" : System.out.println(getCurrentHour());break;
      case "-date" : System.out.println(getDate(args[1]));break;


      default : System.out.println("unrecognized");
    }
    System.out.println("done");
   } catch (Exception e) {}
  }

}


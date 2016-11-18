package jPacsCrawler;

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

        public static String getDate() {
          DateFormat dateFormat= new SimpleDateFormat("yyyyMMdd@HH:mm:ss");
          Date date= new Date();
          return String.valueOf(dateFormat.format(date));
        }

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
      case "-now" : System.out.println(getDate());break;
      case "-isvalid" : System.out.println(isValid(args[1]));break;


      default : System.out.println("unrecognized");
    }
    System.out.println("done");
   } catch (Exception e) {}
  }


  /** checks if date is valid 
  *  @args date in YYYYMMDD format
  */

  public static boolean isValid(String date) {
    return VALID_DATES.contains(date.substring(4));
  }

  private static final String VALID_DATES= "0101.0102.0103.0104.0105.0106.0107.0108.0109.0110.0111.0112.0113.0114.0115.0116.0117.0118.0119.0120.0121.0122.0123.0124.0125.0126.0127.0128.0129.0130.0131.0201.0202.0203.0204.0205.0206.0207.0208.0209.0210.0211.0212.0213.0214.0215.0216.0217.0218.0219.0220.0221.0222.0223.0224.0225.0226.0227.0228.0229.0301.0302.0303.0304.0305.0306.0307.0308.0309.0310.0311.0312.0313.0314.0315.0316.0317.0318.0319.0320.0321.0322.0323.0324.0325.0326.0327.0328.0329.0330.0331.0401.0402.0403.0404.0405.0406.0407.0408.0409.0410.0411.0412.0413.0414.0415.0416.0417.0418.0419.0420.0421.0422.0423.0424.0425.0426.0427.0428.0429.0430.0501.0502.0503.0504.0505.0506.0507.0508.0509.0510.0511.0512.0513.0514.0515.0516.0517.0518.0519.0520.0521.0522.0523.0524.0525.0526.0527.0528.0529.0530.0531.0601.0602.0603.0604.0605.0606.0607.0608.0609.0610.0611.0612.0613.0614.0615.0616.0617.0618.0619.0620.0621.0622.0623.0624.0625.0626.0627.0628.0629.0630.0701.0702.0703.0704.0705.0706.0707.0708.0709.0710.0711.0712.0713.0714.0715.0716.0717.0718.0719.0720.0721.0722.0723.0724.0725.0726.0727.0728.0729.0730.0731.0801.0802.0803.0804.0805.0806.0807.0808.0809.0810.0811.0812.0813.0814.0815.0816.0817.0818.0819.0820.0821.0822.0823.0824.0825.0826.0827.0828.0829.0830.0831.0901.0902.0903.0904.0905.0906.0907.0908.0909.0910.0911.0912.0913.0914.0915.0916.0917.0918.0919.0920.0921.0922.0923.0924.0925.0926.0927.0928.0929.0930.1001.1002.1003.1004.1005.1006.1007.1008.1009.1010.1011.1012.1013.1014.1015.1016.1017.1018.1019.1020.1021.1022.1023.1024.1025.1026.1027.1028.1029.1030.1031.1101.1102.1103.1104.1105.1106.1107.1108.1109.1110.1111.1112.1113.1114.1115.1116.1117.1118.1119.1120.1121.1122.1123.1124.1125.1126.1127.1128.1129.1130.1201.1202.1203.1204.1205.1206.1207.1208.1209.1210.1211.1212.1213.1214.1215.1216.1217.1218.1219.1220.1221.1222.1223.1224.1225.1226.1227.1228.1229.1230.1231.";
}

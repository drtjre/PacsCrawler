package ch.usb.jpacscrawler;

/** Utilities for system calls
* @author TJRE
* @date   Aug2016
*/

import java.util.ArrayList;
import java.util.List;
import java.io.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;


public class JavaSystemCaller {
    private static final boolean DEBUG= false;
    private static final boolean DEBUG_FREEZE= false;

    public static File call(String cmd) {
      Log.DEBUG("JavaSystemCaller.call cmd=",cmd);
      return call(cmd.split(" "));
    }

    public  static File call(List<String> slist) {
      File file= null;
      try {
        ProcessBuilder pb = new ProcessBuilder(slist);
	File output = File.createTempFile("jsyscall",".jsys");
	StringBuffer buf= new StringBuffer();
	output.deleteOnExit();
        pb.redirectErrorStream(true);
        pb.redirectOutput(ProcessBuilder.Redirect.appendTo(output));
        Process p = pb.start();
        int result = p.waitFor();
	if (result>=0) file= output;
      }
      catch (IOException e) {System.out.println(e); }
      catch (InterruptedException e) {System.out.println(e); }
	/*
        assert pb.redirectInput() == ProcessBuilder.Redirect.PIPE;
        assert pb.redirectOutput().file() == output;
        assert p.getInputStream().read() == -1;
	*/
      return file;
    }


    /** perform a bash script 
    *   useful for performing complex command line calls 
    *   without parsing issues caused by direct execution 
    *   of these calls by ProcessBuilder
    *   returns result of call
    *
    *   @arg script bash script to perform
    */
    public  static int executeBash(String script) {
      File bashFile= createTempFile(script);
      bashFile.deleteOnExit();
      File outfile;
      ArrayList<String> alist= new ArrayList<String>();
      int result=0;
      alist.add("/bin/bash");
      alist.add(bashFile.getPath());

      try {
	// execute script file
        ProcessBuilder pb = new ProcessBuilder(alist);
	File output = File.createTempFile("jsyscall",".jsys");
	output.deleteOnExit();
        pb.redirectErrorStream(true);
        pb.redirectOutput(ProcessBuilder.Redirect.appendTo(output));
        Process p = pb.start();
        // result = p.waitFor();
      }
      catch (IOException e) {
        Log.error("JavaSystemCall.call",e); 
	result=-1; 
      }
      
      return result;
    }

    public  static File call(String[] args) {
      File file= null;
      ArrayList<String> alist= new ArrayList<String>();
      for (String arg : args) alist.add(arg);

      try {
        ProcessBuilder pb = new ProcessBuilder(alist);
	File output = File.createTempFile("jsyscall",".jsys");
	output.deleteOnExit();
        pb.redirectErrorStream(true);
        pb.redirectOutput(ProcessBuilder.Redirect.appendTo(output));
        Process p = pb.start();
        int result = p.waitFor();
	if (result>=0) file= output;
      }
      catch (IOException e) {Log.error("JavaSystemCall.call",e); }
      catch (InterruptedException e) {Log.error("JavaSystemCall.call",e); }
      return file;
    }

    /** Create a temp file and store data in it */
    public static File createTempFile(String data) {
      File file=null;
      try {
        file= File.createTempFile("jPC_",".tmp");
	file.deleteOnExit();
	PrintWriter fo = new PrintWriter(new FileOutputStream(file));
	fo.print(data);
	fo.close();
      }
      catch (IOException e) {}
      return file;
    }

    public static void main(String[] args) throws Exception {
             ArrayList<String> alist= new ArrayList<String>();
             alist.add("/Users/pacs/dcmtk/dcmtk-3.6.0-mac-i686-dynamic/bin//findscu");
             alist.add("-to");
             alist.add("6000");
             alist.add("-v"); alist.add("-S");alist.add("-k");alist.add("0008,0052=SERIES");
             alist.add("-aec");
             alist.add("AE_ARCH2_4PR");
             alist.add("10.5.66.74");
	     alist.add("104");
             alist.add("-aet");
             alist.add("YETI");
	     alist.add("-k");
	     alist.add("AccessionNumber");
	     alist.add("-k");
	     alist.add("StudyDescription");
	     alist.add("-k");
	     alist.add("StudyDate=20101013");
	     alist.add("-k");
	     alist.add("StudyDescription=CT Thorax*");
	     // findscu -to 6000 -v -S -k 0008,0052=SERIES -aec AE_ARCH2_4PR 10.5.66.74 104 -aet YETI -k StudyDate -k StudyTime -k AccessionNumber -k InstanceAvailability -k Modality -k ReferringPhysicianName -k StudyDescription -k SeriesDescription -k PatientName -k PatientID -k PatientBirthDate -k PatientSex -k BodyPartExamined -k StudyID -k SeriesNumber -k InstanceNumber -k Rows -k Columns -k InstitutionName -k StudyInstanceUID -k SeriesInstanceUID -k StudyDate=20130513 -k SeriesDate=20130513 -k Modality=MR

            JavaSystemCaller.call(alist); 
    }


    private static void DEBUG(String msg, Object obj) {
      if (DEBUG) 
        System.out.println("JavaSystemCaller::"+msg+obj.toString());
    }
    private static void DEBUG(Object obj) {
      if (DEBUG) 
        System.out.println("JavaSystemCaller::"+obj.toString());
    }
}

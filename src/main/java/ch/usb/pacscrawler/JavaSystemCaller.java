package ch.usb.pacscrawler;

/** Utilities for system calls
* @author TJRE
* @date   Aug2016
*/

import java.util.ArrayList;
import java.util.List;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;

import java.io.IOException; 
import java.nio.charset.StandardCharsets; 
import java.nio.file.Files; 
import java.nio.file.Paths;


public class JavaSystemCaller {
    private static final boolean DEBUG= false;
    private static final boolean DEBUG_FREEZE= false;

    public static File call(String cmd) {
      Log.DEBUG("JavaSystemCaller.call cmd=",cmd);
      return call(cmd.split(" "));
    }

    /** returns contents of a file */
    public static String cat(File file) {
      return sys("cat "+file.getPath());
    }

    /** do system level call and return string of output */
    public static String sys(String cmd) {
      String output="NULL";
      try {
        File file= call(cmd);
        List<String> lines = Files.readAllLines(Paths.get(file.getPath()), StandardCharsets.UTF_8); 
        StringBuilder sb = new StringBuilder(1024); 
        for (String line : lines) { sb.append(line+"\n"); } 
        output = sb.toString();
      }
      catch (IOException e) {output=e.toString();}
      return output;
    }

    public  static File call(List<String> slist) {
      File file= null;
      try {
        Log.DEBUG("JavaSystemCall.call:",slist);
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
      File out;

      Log.DEBUG("cat");
      System.out.println(JavaSystemCaller.cat(new File(args[0])));

      Log.DEBUG("simple Command");
      System.out.println(JavaSystemCaller.sys(args[0]));

           /* 
             ArrayList<String> alist= new ArrayList<String>();
	     for (String s : args) alist.add(s);

            out= JavaSystemCaller.call(alist); 
	    Log.DEBUG(out.getPath().toString());
	    */
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

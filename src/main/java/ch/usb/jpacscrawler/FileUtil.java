package ch.usb.jpacscrawler;
/** FileUtil 
*   Functions to create and delete temporary directory.
*
*  @author TJRE
*  @date   nov2016
*/

import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.IOException;
import java.io.File;

public class FileUtil {

  public static Path createTempDirectory() {
    Path tmpDir= null;
    try {
      tmpDir= Files.createTempDirectory("pacscrawler_tmp_");
    }
      catch (IOException e) {Log.ERROR("FileUtil::createTempDir:"+e);}

    return tmpDir;
  }

  /** delete file and directory if empty. */
  public static void deleteFileAndParentDirIfEmpty(File file) {
    File parent= new File(file.getParent());
    file.delete();
    String[] siblings= parent.list();
    if (siblings.length==0) parent.delete();
  }

  /** delete path and all below it. */
  public static void delete(Path dir) {
            // remove files in temp directory and exit
             String[] entries= dir.toFile().list();
             for (String s: entries) {
               File currentFile= new File(dir.toString()+"/"+s);
               currentFile.delete();
             }
             dir.toFile().delete();
  }

  public static void main(String[] args) {
    try {
      Path tmp;
      switch (args[0]) {
        case "-createdir":  
          tmp= FileUtil.createTempDirectory();
          Log.out(tmp.toString());
	  break;

        case "-deletedir":  
	  tmp= Paths.get(args[1]);
	  FileUtil.delete(tmp);
          Log.out("directory deleted");
	  break;
      }
      
    }
    catch(Exception e) { System.out.println("usage: FileUtil {-createdir|-deletedir}");}


  }


  /** append a text file with contents of data string */
  public void appendFile(String filename, String data) {
    try {
      File outFile= new File(filename);
      OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(outFile, true), "UTF-8") ;
      writer.write(data+"\n");
      writer.close();
    }
    catch (IOException e) {
      Log.error("FileUtil.appendFile",e);
    }
  }



}



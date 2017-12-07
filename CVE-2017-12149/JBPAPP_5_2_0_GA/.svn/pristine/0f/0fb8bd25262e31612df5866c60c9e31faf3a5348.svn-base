package org.jboss.test.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * This utility class calculates check sum of files specified. It is used for
 * Common Criteria certification testify section produced by junit reports.
 * 
 * Called from build.xml as using <java .. > ant task.
 * 
 * @author pskopek@redhat.com
 */
public class CheckSumCalculator {

   class DirSpec {
      public String dirPath;
      public String fileNameSuffix;
   }
   
   
   private LinkedList dirSpecs = new LinkedList();
   private String outputFileName = "MD5SUM";
   private String base = "";
     
   /**
    * @param args
    */
   public static void main(String[] args) {
      CheckSumCalculator csc = new CheckSumCalculator();
      csc.parseArguments(args);
      try {
         csc.calculate();
      }
      catch (IOException e) {
         throw new RuntimeException(e);
      }
   }

   public CheckSumCalculator() {
   }
   
   public void parseArguments(String args[]) {
      
      boolean isDirectory = false;
      boolean isOutput = false;
      boolean isSuffix = false;
      boolean isBase = false;
      
      for (int i = 0; i < args.length; i++) {
         if (args[i].equals("-d")) {
            isDirectory = true;
         }
         else if (args[i].equals("-output")) {
            isOutput = true;
         }
         else if (args[i].equals("-s")) {
            isSuffix = true;
         }
         else if (args[i].equals("-base")) {
            isBase = true;
         }
         else {
            
            if (isDirectory) {
               DirSpec ds = new DirSpec();
               ds.dirPath = args[i].trim();
               ds.fileNameSuffix = ".jar";
               
               dirSpecs.add(ds);
            }
            else if (isSuffix) {
               ((DirSpec)dirSpecs.getLast()).fileNameSuffix = args[i].trim();
            }
            else if (isOutput) {
               outputFileName = args[i].trim();
            }
            else if (isBase) {
               base = args[i].trim();
            }
            
            isDirectory = false;
            isSuffix = false;
            isOutput = false;
            isBase = false;
         }
         
         
      }
   }
   
   public void calculate() throws IOException {
      
      PrintWriter pw = new PrintWriter(outputFileName);
      
      Iterator dsIterator = dirSpecs.iterator();
      while (dsIterator.hasNext()) {
        DirSpec ds = (DirSpec)dsIterator.next(); 
        
        calculateDirectory(ds, pw);
        
      }
      
      pw.close();
      
      
   }

   
   private void calculateDirectory(DirSpec ds, PrintWriter pw) throws IOException {
      
      File dir = new File(ds.dirPath);
      
      File[] content = dir.listFiles();
      for (int i = 0; i < content.length; i++) {
         if (content[i].isDirectory()) {
            DirSpec innerDir = new DirSpec();
            innerDir.dirPath = content[i].getCanonicalPath();
            innerDir.fileNameSuffix = ds.fileNameSuffix;
            calculateDirectory(innerDir, pw);
         }
         else if (content[i].isFile() && content[i].getName().endsWith(ds.fileNameSuffix)) {
            pw.print(calculateFileCheckSum(content[i]));
            pw.print("  ");
            
            String canonicalPath = content[i].getCanonicalPath(); 
            if (canonicalPath.startsWith(base)) {
               canonicalPath = canonicalPath.substring(base.length());
            }
            pw.println(canonicalPath);
            
         }
      }
      
   }
   
   
   private String calculateFileCheckSum(File f) throws IOException {
      
      BufferedInputStream in = new BufferedInputStream(new FileInputStream(f));
      
      byte[] buf = new byte[2048];
      
      MessageDigest md;
      try {
      md = MessageDigest.getInstance("MD5");
      }
      catch (NoSuchAlgorithmException e) {
         throw new IllegalArgumentException(e);
      }
      
      int len;
      while ((len = in.read(buf, 0, buf.length)) > -1) {
         md.update(buf, 0, len);
      }
      in.close();
   
      return CheckSumCalculator.convertToHex(md.digest());
      
      
   }

   private static String convertToHex(byte[] data) {
      StringBuffer buf = new StringBuffer();
      for (int i = 0; i < data.length; i++) {
       int halfbyte = (data[i] >>> 4) & 0x0F;
       int two_halfs = 0;
       do {
          if ((0 <= halfbyte) && (halfbyte <= 9))
                 buf.append((char) ('0' + halfbyte));
             else
                buf.append((char) ('a' + (halfbyte - 10)));
          halfbyte = data[i] & 0x0F;
       } while(two_halfs++ < 1);
      }
      return buf.toString();
  }
}

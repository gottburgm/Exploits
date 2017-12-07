/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.tools.license;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.FileWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.RandomAccessFile;
import java.io.SyncFailedException;
import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.nio.channels.FileChannel;
import java.nio.ByteBuffer;
import java.net.URL;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;

/**
 * A utility which scans all java source files in the cvs tree and validates
 * the license header prior to the package statement for headers that match
 * those declared in varia/src/etc/license-info.xml
 * 
 * @author Scott.Stark@jboss.org
 * @version $Revision: 81038 $
 */
public class ValidateLicenseHeaders
{
   /** Used to strip out diffs due to copyright date ranges */
   static final String COPYRIGHT_REGEX = "copyright\\s(\\(c\\))*\\s*[\\d]+(\\s*,\\s*[\\d]+|\\s*-\\s*[\\d]+)*";

   static final String DEFAULT_HEADER = "/*\n" +
  " * JBoss, Home of Professional Open Source.\n" +
  " * Copyright 2008, Red Hat Middleware LLC, and individual contributors\n" +
  " * as indicated by the @author tags. See the copyright.txt file in the\n" +
  " * distribution for a full listing of individual contributors.\n" +
  " *\n" +
  " * This is free software; you can redistribute it and/or modify it\n" +
  " * under the terms of the GNU Lesser General Public License as\n" +
  " * published by the Free Software Foundation; either version 2.1 of\n" +
  " * the License, or (at your option) any later version.\n" +
  " *\n" +
  " * This software is distributed in the hope that it will be useful,\n" +
  " * but WITHOUT ANY WARRANTY; without even the implied warranty of\n" +
  " * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU\n" +
  " * Lesser General Public License for more details.\n" +
  " *\n" +
  " * You should have received a copy of the GNU Lesser General Public\n" +
  " * License along with this software; if not, write to the Free\n" +
  " * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA\n" +
  " * 02110-1301 USA, or see the FSF site: http://www.fsf.org.\n" +
  " */\n";
   static Logger log = Logger.getLogger("ValidateCopyrightHeaders");
   static boolean addDefaultHeader = false;
   static FileFilter dotJavaFilter = new DotJavaFilter();
   /**
    * The term-headers from the varia/src/etc/license-info.xml
    */ 
   static TreeMap licenseHeaders = new TreeMap();
   /**
    * Java source files with no license header
    */ 
   static ArrayList noheaders = new ArrayList();
   /**
    * Java source files with a header that does not match one from licenseHeaders
    */ 
   static ArrayList invalidheaders = new ArrayList();
   /** Total java source files seen */
   static int totalCount;
   /** Total out of date jboss headers seen */
   static int jbossCount;

   /**
    * ValidateLicenseHeaders jboss-src-root
    * @param args
    */ 
   public static void main(String[] args)
      throws Exception
   {
      if( args.length == 0 || args[0].startsWith("-h") )
      {
         log.info("Usage: ValidateLicenseHeaders [-addheader] jboss-src-root");
         System.exit(1);
      }
      int rootArg = 0;
      if( args.length == 2 )
      {
         if( args[0].startsWith("-add") )
            addDefaultHeader = true;
         else
         {
            log.severe("Uknown argument: "+args[0]);
            log.info("Usage: ValidateLicenseHeaders [-addheader] jboss-src-root");
            System.exit(1);
            
         }
         rootArg = 1;
      }

      File jbossSrcRoot = new File(args[rootArg]);
      if( jbossSrcRoot.exists() == false )
      {
         log.info("Src root does not exist, check "+jbossSrcRoot.getAbsolutePath());
         System.exit(1);
      }

      URL u = Thread.currentThread().getContextClassLoader().getResource("META-INF/services/javax.xml.parsers.DocumentBuilderFactory");
      System.err.println(u);

      // Load the valid copyright statements for the licenses
      File licenseInfo = new File(jbossSrcRoot, "varia/src/etc/license-info.xml");
      if( licenseInfo.exists() == false )
      {
         log.severe("Failed to find the varia/src/etc/license-info.xml under the src root");
         System.exit(1);
      }
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      DocumentBuilder db = factory.newDocumentBuilder();
      Document doc = db.parse(licenseInfo);
      NodeList licenses = doc.getElementsByTagName("license");
      for(int i = 0; i < licenses.getLength(); i ++)
      {
         Element license = (Element) licenses.item(i);
         String key = license.getAttribute("id");
         ArrayList headers = new ArrayList();
         licenseHeaders.put(key, headers);
         NodeList copyrights = license.getElementsByTagName("terms-header");
         for(int j = 0; j < copyrights.getLength(); j ++)
         {
            Element copyright = (Element) copyrights.item(j);
            copyright.normalize();
            String id = copyright.getAttribute("id");
            // The id will be blank if there is no id attribute
            if( id.length() == 0 )
               continue;
            String text = getElementContent(copyright);
            if( text == null )
               continue;
            // Replace all duplicate whitespace and '*' with a single space
            text = text.replaceAll("[\\s*]+", " ");
            if( text.length() == 1)
               continue;

            text = text.toLowerCase().trim();
            // Replace any copyright date0-date1,date2 with copyright ...
            text = text.replaceAll(COPYRIGHT_REGEX, "...");
            LicenseHeader lh = new LicenseHeader(id, text);
            headers.add(lh);
         }
      }
      log.fine(licenseHeaders.toString());

      File[] files = jbossSrcRoot.listFiles(dotJavaFilter);
      log.info("Root files count: "+files.length);
      processSourceFiles(files, 0);

      log.info("Processed "+totalCount);
      log.info("Updated jboss headers: "+jbossCount);
      // Files with no headers details
      log.info("Files with no headers: "+noheaders.size());
      FileWriter fw = new FileWriter("NoHeaders.txt");
      for(Iterator iter = noheaders.iterator(); iter.hasNext();)
      {
         File f = (File) iter.next();
         fw.write(f.getAbsolutePath());
         fw.write('\n');
      }
      fw.close();

      // Files with unknown headers details
      log.info("Files with invalid headers: "+invalidheaders.size());
      fw = new FileWriter("InvalidHeaders.txt");
      for(Iterator iter = invalidheaders.iterator(); iter.hasNext();)
      {
         File f = (File) iter.next();
         fw.write(f.getAbsolutePath());
         fw.write('\n');
      }
      fw.close();

      // License usage summary
      log.info("Creating HeadersSummary.txt");
      fw = new FileWriter("HeadersSummary.txt");
      for(Iterator iter = licenseHeaders.entrySet().iterator(); iter.hasNext();)
      {
         Map.Entry entry = (Map.Entry) iter.next();
         String key = (String) entry.getKey();
         fw.write("+++ License type="+key);
         fw.write('\n');
         List list = (List) entry.getValue();
         Iterator jiter = list.iterator();
         while( jiter.hasNext() )
         {
            LicenseHeader lh = (LicenseHeader) jiter.next();   
            fw.write('\t');
            fw.write(lh.id);
            fw.write(", count=");
            fw.write(""+lh.count);
            fw.write('\n');
         }
      }
      fw.close();
   }

   /**
    * Get all non-comment content from the element.
    * @param element
    * @return the concatenated text/cdata content
    */ 
   public static String getElementContent(Element element)
   {
      if (element == null)
         return null;

      NodeList children = element.getChildNodes();
      StringBuffer result = new StringBuffer();
      for (int i = 0; i < children.getLength(); i++)
      {
         Node child = children.item(i);
         if (child.getNodeType() == Node.TEXT_NODE || 
             child.getNodeType() == Node.CDATA_SECTION_NODE)
         {
            result.append(child.getNodeValue());
         }
         else if( child.getNodeType() == Node.COMMENT_NODE )
         {
            // Ignore comment nodes
         }
         else
         {
            result.append(child.getFirstChild());
         }
      }
      return result.toString().trim();
   }

   /**
    * Validate the headers of all java source files
    * 
    * @param files
    * @param level
    * @throws IOException
    */ 
   static void processSourceFiles(File[] files, int level)
      throws IOException
   {
      for(int i = 0; i < files.length; i ++)
      {
         File f = files[i];
         if( level == 0 )
            log.info("processing "+f);
         if( f.isDirectory() )
         {
            File[] children = f.listFiles(dotJavaFilter);
            processSourceFiles(children, level+1);
         }
         else
         {
            parseHeader(f);
         }
      }
   }

   /**
    * Read the first comment upto the package ...; statement
    * @param javaFile
    */ 
   static void parseHeader(File javaFile)
      throws IOException
   {
      totalCount ++;
      RandomAccessFile raf = new RandomAccessFile(javaFile, "rw");
      String line = raf.readLine();
      StringBuffer tmp = new StringBuffer();
      long endOfHeader = 0;
      boolean packageOrImport = false;
      while( line != null )
      {
         long nextEOH = raf.getFilePointer();
         line = line.trim();
         // Ignore any single line comments
         if( line.startsWith("//") )
         {
            line = raf.readLine();
            continue;
         }

         // If this is a package/import/class/interface statement break
         if( line.startsWith("package") || line.startsWith("import")
            || line.indexOf("class") >= 0 || line.indexOf("interface") >= 0 )
         {
            packageOrImport = true;
            break;
         }

         // Update the current end of header marker
         endOfHeader = nextEOH;

         if( line.startsWith("/**") )
            tmp.append(line.substring(3));
         else if( line.startsWith("/*") )
            tmp.append(line.substring(2));
         else if( line.startsWith("*") )
            tmp.append(line.substring(1));
         else
            tmp.append(line);
         tmp.append(' ');
         line = raf.readLine();
      }
      raf.close();

      if( tmp.length() == 0 || packageOrImport == false )
      {
         addDefaultHeader(javaFile);
         return;
      }

      String text = tmp.toString();
      // Replace all duplicate whitespace with a single space
      text = text.replaceAll("[\\s*]+", " ");
      text = text.toLowerCase().trim();
      // Replace any copyright date0-date1,date2 with copyright ...
      text = text.replaceAll(COPYRIGHT_REGEX, "...");
      if( tmp.length() == 0 )
      {
         addDefaultHeader(javaFile);
         return;
      }
      // Search for a matching header
      boolean matches = false;
      String matchID = null;
      Iterator iter = licenseHeaders.entrySet().iterator();
      escape:
      while( iter.hasNext() )
      {
         Map.Entry entry = (Map.Entry) iter.next();
         String key = (String) entry.getKey();
         List list = (List) entry.getValue();
         Iterator jiter = list.iterator();
         while( jiter.hasNext() )
         {
            LicenseHeader lh = (LicenseHeader) jiter.next();
            if( text.startsWith(lh.text) )
            {
               matches = true;
               matchID = lh.id;
               lh.count ++;
               lh.usage.add(javaFile);
               if( log.isLoggable(Level.FINE) )
                  log.fine(javaFile+" matches copyright key="+key+", id="+lh.id);
               break escape;
            }
         }
      }
      text = null;
      tmp.setLength(0);
      if( matches == false )
         invalidheaders.add(javaFile);
      else if( matchID.startsWith("jboss") && matchID.endsWith("#0") == false )
      {
         // This is a legacy jboss head that needs to be updated to the default
         replaceHeader(javaFile, endOfHeader);
         jbossCount ++;
      }
   }

   /**
    * Replace a legacy jboss header with the current default header
    * @param javaFile - the java source file
    * @param endOfHeader - the offset to the end of the legacy header
    * @throws IOException - thrown on failure to replace the header
    */
   static void replaceHeader(File javaFile, long endOfHeader)
      throws IOException
   {
      if( log.isLoggable(Level.FINE) )
         log.fine("Replacing legacy jboss header in: "+javaFile);
      RandomAccessFile raf = new RandomAccessFile(javaFile, "rw");
      File bakFile = new File(javaFile.getAbsolutePath()+".bak");
      FileOutputStream fos = new FileOutputStream(bakFile);
      fos.write(DEFAULT_HEADER.getBytes());
      FileChannel fc = raf.getChannel();
      long count = raf.length() - endOfHeader;
      fc.transferTo(endOfHeader, count, fos.getChannel());
      fc.close();
      fos.close();
      raf.close();
      if( javaFile.delete() == false )
         log.severe("Failed to delete java file: "+javaFile);
      if( bakFile.renameTo(javaFile) == false )
         throw new SyncFailedException("Failed to replace: "+javaFile);
   }

   /**
    * Add the default jboss lgpl header
    */ 
   static void addDefaultHeader(File javaFile)
      throws IOException
   {
      if( addDefaultHeader )
      {
         FileInputStream fis = new FileInputStream(javaFile);
         FileChannel fc = fis.getChannel();
         int size = (int) fc.size();
         ByteBuffer contents = ByteBuffer.allocate(size);
         fc.read(contents);
         fis.close();
         
         ByteBuffer hdr = ByteBuffer.wrap(DEFAULT_HEADER.getBytes());
         FileOutputStream fos = new FileOutputStream(javaFile);
         fos.write(hdr.array());
         fos.write(contents.array());
         fos.close();
      }

      noheaders.add(javaFile);
   }

   /**
    * A class that encapsulates the license id and valid terms header
    */ 
   static class LicenseHeader
   {
      String id;
      String text;
      int count;
      ArrayList usage = new ArrayList();
      LicenseHeader(String id, String text)
      {
         this.id = id;
         this.text = text;
      }
   }

   /**
    * A filter which accepts files ending in .java (but not _Stub.java), or
    * directories other than gen-src and gen-parsers
    */ 
   static class DotJavaFilter implements FileFilter
   {
      public boolean accept(File pathname)
      {
         boolean accept = false;
         String name = pathname.getName();
         if( pathname.isDirectory() )
         {
            // Ignore the gen-src directories for generated output
            accept = name.equals("gen-src") == false
               && name.equals("gen-parsers") == false;
         }
         else
         {
            accept = name.endsWith("_Stub.java") == false && name.endsWith(".java");
         }
         
         return accept;
      }
   }
}

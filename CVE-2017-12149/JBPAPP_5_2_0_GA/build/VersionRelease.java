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
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.FileWriter;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.TreeSet;

import org.dom4j.Document;
import org.dom4j.DocumentFactory;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

/** A utility which goes through a standard dist build and tags every jar
 * with the current build version using the jar file version manifest
 * headers. The unique jars and their version info and md5 digests are
 * output to the jboss.home/jar-versions.xml.
 * 
 * @author Scott.Stark@jboss.org
 * @version $Revision: 106328 $
 */
public class VersionRelease
{
   static byte[] buffer = new byte[4096];

   /** The jboss dist root directory */
   File jbossHome;
   String specVersion;
   String specVendor;
   String specTitle;
   String implTitle;
   String implURL;
   String implVersion;
   String implVendor;
   String implVendorID;
   MessageDigest md5;
   TreeSet jars = new TreeSet();

   public VersionRelease(String homeDir)
      throws FileNotFoundException, NoSuchAlgorithmException
   {
      jbossHome = new File(homeDir);
      if( jbossHome.exists() == false )
         throw new FileNotFoundException(jbossHome.getAbsolutePath() + " does not exist");
      specTitle = System.getProperty("specification.title");
      specVersion = System.getProperty("specification.version");
      specVendor = System.getProperty("specification.vendor");
      implTitle = System.getProperty("implementation.title");
      implURL = System.getProperty("implementation.url");
      implVersion = System.getProperty("implementation.version");
      implVendor = System.getProperty("implementation.vendor");
      implVendorID = System.getProperty("implementation.vendor.id");
      md5 = MessageDigest.getInstance("MD5");
   }

   public void run()
   {
      processDir(jbossHome);
      try
      {
         System.out.println("VR: Creating jar-versions.xml file...");
         DocumentFactory df = DocumentFactory.getInstance();
         Document doc = df.createDocument();
         Element root = doc.addElement("jar-versions");
         Iterator iter = jars.iterator();
         while( iter.hasNext() )
         {
            JarInfo info = (JarInfo) iter.next();
            System.out.println("VR: Add info for file: "+info.jarName);
            info.writeXML(root);
         }

         File versionsXml = new File(jbossHome, "jar-versions.xml");
         FileWriter versionInfo = new FileWriter(versionsXml);
         OutputFormat outformat = OutputFormat.createPrettyPrint();
         XMLWriter writer = new XMLWriter(versionInfo, outformat);
         writer.setEscapeText(true);
         writer.write(doc);
         writer.flush();
         versionInfo.close();
      }
      catch(IOException e)
      {
         e.printStackTrace();
      }
   }

   void processDir(File dir)
   {
      File[] files = dir.listFiles();
      for(int f = 0; f < files.length; f ++)
      {
         File child = files[f];
         if( child.isDirectory() == true )
            processDir(child);
         else
            processFile(child);
      }
   }
   void processFile(File file)
   {
      // See if this is a jar archive
      try
      {
         JarInfo info = new JarInfo(file, this);
         System.out.println("VR: Checking file: "+file);
         info.write(md5);
         jars.add(info);
      }
      catch(FileNotFoundException e)
      {
         System.out.println("VR: Skipping non-JAR file: "+file);
      }
      catch(Exception e)
      {
         System.out.println("VR: Exception while checking file: "+file);
         e.printStackTrace();
      }
   }

   static class JarInfo implements Comparable
   {
      File file;
      File tmpFile;

      Manifest mf;
      JarFile jarFile;
      JarEntry jarSignature;
      String jarName;
      boolean sealed;
      boolean signed;
      String md5Digest;
      String specVersion;
      String specVendor;
      String specTitle;
      String implTitle;
      String implURL;
      String implVersion;
      String implVendor;
      String implVendorID;

      JarInfo(File file, VersionRelease release)
         throws IOException
      {
         this.file = file;
         this.jarName = file.getName();
         this.tmpFile = new File(file.getAbsolutePath()+".tmp");
         if( file.renameTo(tmpFile) == false )
            throw new IOException("VR: Failed to rename: "+file);

         try
         {
            this.jarFile = new JarFile(tmpFile);
         }
         catch(IOException e)
         {
            tmpFile.renameTo(file);
            throw new FileNotFoundException("VR: Not a JarFile: "+file);
         }

         try
         {
            this.mf = jarFile.getManifest();
            this.jarSignature = jarFile.getJarEntry( "META-INF/JBOSSCOD.SF" );
         }
         catch(Exception e)
         {
            System.out.println("VR: Exception while getting info from file: "+file);
            e.printStackTrace();
            tmpFile.renameTo(file);
         }

         Attributes mfAttrs = mf.getMainAttributes();

         String sealedAttr = mfAttrs.getValue(Attributes.Name.SEALED);
         sealed = Boolean.valueOf(sealedAttr).booleanValue();

         signed = ( jarSignature == null) ? false : true;

         specVersion = mfAttrs.getValue(Attributes.Name.SPECIFICATION_VERSION);
         if( specVersion == null )
         {
            specVersion = release.specVersion;
            mfAttrs.put(Attributes.Name.SPECIFICATION_VERSION, specVersion);
         }
         specVendor = mfAttrs.getValue(Attributes.Name.SPECIFICATION_VENDOR);
         if( specVendor == null )
         {
            specVendor = release.specVendor;
            mfAttrs.put(Attributes.Name.SPECIFICATION_VENDOR, specVendor);
         }
         specTitle = mfAttrs.getValue(Attributes.Name.SPECIFICATION_TITLE);
         if( specTitle == null )
         {
            specTitle = release.specTitle;
            mfAttrs.put(Attributes.Name.SPECIFICATION_TITLE, specTitle);
         }

         implTitle = mfAttrs.getValue(Attributes.Name.IMPLEMENTATION_TITLE);
         if( implTitle == null )
         {
            implTitle = release.implTitle;
            mfAttrs.put(Attributes.Name.IMPLEMENTATION_TITLE, implTitle);
         }
         implVersion = mfAttrs.getValue(Attributes.Name.IMPLEMENTATION_VERSION);
         if( implVersion == null )
         {
            implVersion = release.implVersion;
            mfAttrs.put(Attributes.Name.IMPLEMENTATION_VERSION, implVersion);
         }
         implVendor = mfAttrs.getValue(Attributes.Name.IMPLEMENTATION_VENDOR);
         if( implVendor == null )
         {
            implVendor = release.implVendor;
            mfAttrs.put(Attributes.Name.IMPLEMENTATION_VENDOR, implVendor);
         }
         implVendorID = mfAttrs.getValue(Attributes.Name.IMPLEMENTATION_VENDOR_ID);
         if( implVendorID == null )
         {
            implVendorID = release.implVendorID;
            mfAttrs.put(Attributes.Name.IMPLEMENTATION_VENDOR_ID, implVendorID);
         }
         implURL = mfAttrs.getValue(Attributes.Name.IMPLEMENTATION_URL);
         if( implURL == null )
         {
            implURL = release.implURL;
            mfAttrs.put(Attributes.Name.IMPLEMENTATION_URL, implURL);
         }
      }

      public void write(MessageDigest md5)
         throws IOException
      {
         md5.reset();
         if( ( sealed == true ) || ( signed == true ) )
         {
            tmpFile.renameTo(file);
            System.out.println("VR: Skipping sealed or signed jar: "+file);
         }
         else
         {
            FileOutputStream fos = new FileOutputStream(file);
            JarOutputStream jos = new JarOutputStream(fos, mf);
            Enumeration entries = jarFile.entries();
            while( entries.hasMoreElements() )
            {
               JarEntry entry = (JarEntry) entries.nextElement();
               String name = entry.getName();
               if( name.equals("META-INF/MANIFEST.MF") )
               {
                  continue;
               }

               JarEntry outEntry = new JarEntry(entry.getName());
               outEntry.setTime(entry.getTime());
               if( entry.getComment() != null )
                  outEntry.setComment(entry.getComment());
               jos.putNextEntry(outEntry);
               InputStream is = jarFile.getInputStream(entry);
               int bytes = is.read(buffer);
               while( bytes > 0 )
               {
                  jos.write(buffer, 0, bytes);
                  bytes = is.read(buffer);
               }
               jos.closeEntry();
            }
            jarFile.close();
            jos.close();
            tmpFile.delete();
         }

         // Calculate the md5sum
         FileInputStream fis = new FileInputStream(file);
         int bytes = fis.read(buffer);
         while( bytes > 0 )
         {
            md5.update(buffer, 0, bytes);
            bytes = fis.read(buffer);
         }
         fis.close();
         byte[] digest = md5.digest();
         BigInteger bi = new BigInteger(-1, digest);
         bi = bi.abs();
         md5Digest = bi.toString(16);
         System.out.println(file+", md5: "+md5Digest);
      }

      public int compareTo(Object o)
      {
         JarInfo info = (JarInfo) o;
         return jarName.compareTo(info.jarName);
      }
      public boolean equals(Object o)
      {
         JarInfo info = (JarInfo) o;
         return jarName.equals(info.jarName);
      }
      public int hashCode()
      {
         return jarName.hashCode();
      }
      /* Output an xml string element like:
      <jar name='twiddle.jar' specVersion='3.2.4'
            specVendor='JBoss (http://www.jboss.org/)'
            specTitle='JBoss'
            implVersion='3.2.4RC2 (build: CVSTag=Branch_3_2 date=200404182118)'
            implVendor='JBoss.org'
            implTitle='JBoss [WonderLand]'
            implVendorID='http://www.jboss.org/'
            implURL='http://www.jboss.org/'
            sealed='false'
            md5Digest='ebf8681b4e600cbe7bb2eff68c537c79' />
      */
      public String toString()
      {
         StringBuffer tmp = new StringBuffer("<jar name='");
         tmp.append(jarName);
         tmp.append("' specVersion='");
         tmp.append(specVersion);
         tmp.append("' specVendor='");
         tmp.append(specVendor);
         tmp.append("' specTitle='");
         tmp.append(specTitle);
         tmp.append("' implVersion='");
         tmp.append(implVersion);
         tmp.append("' implVendor='");
         tmp.append(implVendor);
         tmp.append("' implTitle='");
         tmp.append(implTitle);
         tmp.append("' implVendorID='");
         tmp.append(implVendorID);
         tmp.append("' implURL='");
         tmp.append(implURL);
         tmp.append("' sealed='");
         tmp.append(sealed);
         tmp.append("' md5Digest='");
         tmp.append(md5Digest);
         tmp.append("' />");
         return tmp.toString();
      }
      public void writeXML(Element root)
      {
         Element jar = root.addElement("jar");
         jar.addAttribute("name", jarName);
         jar.addAttribute("specVersion", specVersion);
         jar.addAttribute("specVendor", specVendor);
         jar.addAttribute("specTitle", specTitle);
         jar.addAttribute("implVersion", implVersion);
         jar.addAttribute("implVendor", implVendor);
         jar.addAttribute("implTitle", implTitle);
         jar.addAttribute("implVendorID", implVendorID);
         jar.addAttribute("implURL", implURL);
         jar.addAttribute("sealed", ""+sealed);
         jar.addAttribute("md5Digest", md5Digest);
      }
   }

   public static void main(String[] args)
      throws Exception
   {
      VersionRelease vr = new VersionRelease(args[0]);
      vr.run();
   }
}

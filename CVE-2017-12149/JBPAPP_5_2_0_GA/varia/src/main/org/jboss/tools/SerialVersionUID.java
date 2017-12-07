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
package org.jboss.tools;

import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Enumeration;
import java.util.TreeMap;
import java.util.Map;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.jar.JarFile;
import java.util.jar.JarEntry;
import java.net.URLClassLoader;
import java.net.MalformedURLException;
import java.net.URL;

/** A tool/service that computes all the class serialVersionUIDs under the
 * jboss home directory.
 * 
 * @author Scott.Stark@jboss.org
 * @author Dimitris.Andreadis@jboss.org
 * @version $Revision: 84164 $
 */
public class SerialVersionUID
{
   /** A jdk logger so that only this + ClassVersionInfo are needed */
   static Logger log = Logger.getLogger("SerialVersionUID");

   static void buildJarSet(File dir, HashSet jarFiles)
      throws MalformedURLException
   {
      File[] files = dir.listFiles();
      int count = files != null ? files.length : 0;
      System.out.println("Checking dir: "+dir+", count="+count);
      for(int n = 0; n < count; n ++)
      {
         File child = files[n];
         // Ignore the server tmp directory
         if( child.isDirectory() && child.getName().equals("tmp") == false )
            buildJarSet(child, jarFiles);
         else if( child.getName().endsWith(".jar") )
            jarFiles.add(child.toURL());
      }
   }

   /**
    * Build a TreeMap of the class name to ClassVersionInfo
    * @param jar
    * @param classVersionMap TreeMap<String, ClassVersionInfo> for serializable
    *    classes
    * @param cl - the class loader to use
    * @throws IOException thrown if the jar cannot be opened
    */ 
   static void generateJarSerialVersionUIDs(URL jar, TreeMap classVersionMap,
      ClassLoader cl, String pkgPrefix) throws IOException
   {
      String jarName = jar.getFile();
      JarFile jf = new JarFile(jarName);
      Enumeration entries = jf.entries();
      while( entries.hasMoreElements() )
      {
         JarEntry entry = (JarEntry) entries.nextElement();
         String name = entry.getName();
         if( name.endsWith(".class") && name.startsWith(pkgPrefix) )
         {
            name = name.substring(0, name.length() - 6);
            String classname = name.replace('/', '.');
            try
            {
               log.fine("Creating ClassVersionInfo for: "+classname);
               ClassVersionInfo cvi = new ClassVersionInfo(classname, cl);
               log.fine(cvi.toString());
               if( cvi.getSerialVersion() != 0 )
               {
                  ClassVersionInfo prevCVI = (ClassVersionInfo)
                     classVersionMap.put(classname, cvi);
                  if( prevCVI != null )
                  {
                     if( prevCVI.getSerialVersion() != cvi.getSerialVersion() )
                     {
                        log.severe("Found inconsistent classes, "
                           +prevCVI+" != "+cvi+", jar: "+jarName);
                     }
                  }
                  if( cvi.getHasExplicitSerialVersionUID() == false )
                  {
                     log.warning("No explicit serialVersionUID: "+cvi);
                  }
               }
            }
            catch(OutOfMemoryError e)
            {
               log.log(Level.SEVERE, "Check the MaxPermSize", e);
            }
            catch(Throwable e)
            {
               log.log(Level.FINE, "While loading: "+name, e);
            }
         }
      }
      jf.close();
   }

   /**
    * Create a Map<String, ClassVersionInfo> for the jboss dist jars.
    * 
    * @param jbossHome - the jboss dist root directory
    * @return Map<String, ClassVersionInfo>
    * @throws IOException
    */ 
   public static Map generateJBossSerialVersionUIDReport(File jbossHome)
      throws IOException
   {
      // Obtain the jars from the /lib, common/ and /server/all locations
      HashSet jarFiles = new HashSet();
      File lib = new File(jbossHome, "lib");
      buildJarSet(lib, jarFiles);
      File common = new File(jbossHome, "common");
      buildJarSet(common, jarFiles);
      File all = new File(jbossHome, "server/all");
      buildJarSet(all, jarFiles);
      URL[] cp = new URL[jarFiles.size()];
      jarFiles.toArray(cp);
      ClassLoader parent = Thread.currentThread().getContextClassLoader();
      URLClassLoader completeClasspath = new URLClassLoader(cp, parent);

      TreeMap classVersionMap = new TreeMap();
      Iterator jarIter = jarFiles.iterator();
      while( jarIter.hasNext() )
      {
         URL jar = (URL) jarIter.next();
         try
         {
            generateJarSerialVersionUIDs(jar, classVersionMap, completeClasspath, "");
         }
         catch(IOException e)
         {
            log.info("Failed to process jar: "+jar);
         }
      }

      return classVersionMap;
   }

   /**
    * Create a Map<String, ClassVersionInfo> for the jboss dist jars.
    * @param j2eeHome - the j2ee ri dist root directory
    * @return Map<String, ClassVersionInfo>
    * @throws IOException
    */ 
   public static Map generateRISerialVersionUIDReport(File j2eeHome)
      throws IOException
   {
      // Obtain the jars from the /lib
      HashSet jarFiles = new HashSet();
      File lib = new File(j2eeHome, "lib");
      buildJarSet(lib, jarFiles);
      URL[] cp = new URL[jarFiles.size()];
      jarFiles.toArray(cp);
      ClassLoader parent = Thread.currentThread().getContextClassLoader();
      URLClassLoader completeClasspath = new URLClassLoader(cp, parent);

      TreeMap classVersionMap = new TreeMap();
      Iterator jarIter = jarFiles.iterator();
      while( jarIter.hasNext() )
      {
         URL jar = (URL) jarIter.next();
         try
         {
            generateJarSerialVersionUIDs(jar, classVersionMap, completeClasspath, "javax");
         }
         catch(IOException e)
         {
            log.info("Failed to process jar: "+jar);
         }
      }

      return classVersionMap;
   }

   /**
    * Generate a mapping of the serial version UIDs for the serializable classes
    * under the jboss dist directory 
    * @param args - [0] = jboss dist root directory
    * @throws Exception
    */ 
   public static void main(String[] args) throws Exception
   {
      if( args.length != 1 )
      {
         System.err.println("Usage: jboss-home | -rihome ri-home");
         System.exit(1);
      }
      File distHome = new File(args[0]);
      Map classVersionMap = null;
      if( args.length == 2 )
         classVersionMap = generateRISerialVersionUIDReport(distHome);
      else
         classVersionMap = generateJBossSerialVersionUIDReport(distHome);
      // Write the map out the object file
      log.info("Total classes with serialVersionUID != 0: "+classVersionMap.size());
      FileOutputStream fos = new FileOutputStream("serialuid.ser");
      ObjectOutputStream oos = new ObjectOutputStream(fos);
      oos.writeObject(classVersionMap);
      fos.close();
   }
}

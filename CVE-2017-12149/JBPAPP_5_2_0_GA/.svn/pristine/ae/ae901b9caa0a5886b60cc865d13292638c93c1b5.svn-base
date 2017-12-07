/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.test.classloader.leak.clstore;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jboss.logging.Logger;
import org.jboss.profiler.jvmti.ReferenceDataPoint;

public class ClassLoaderStore
{
   private static final Logger log = Logger.getLogger(ClassLoaderStore.class);
   
   private static ClassLoaderStore instance = new ClassLoaderStore();
   
   private final Map<String, WeakReference<ClassLoader>> classloaders = new HashMap<String, WeakReference<ClassLoader>>();

   private final int depth = Integer.parseInt(System.getProperty("jboss.classloader.leak.test.depth", "12"));

   private ClassLoaderStore()
   {
      
   }
   
   public static ClassLoaderStore getInstance()
   {
      return instance;
   }
   
   public void storeClassLoader(String key, ClassLoader loader)
   {
      log.debug("Storing " + loader + " under " + key);
      ClassLoader parent = loader.getParent();
      while (parent != null)
      {
         log.debug("Parent is " + parent);
         parent = parent.getParent();
      }
      WeakReference<ClassLoader> ref = new WeakReference<ClassLoader>(loader);
      classloaders.put(key, ref);
   }
   
   public ClassLoader getClassLoader(String key, boolean forceGC, String reportFile)
   {
      ClassLoader result = null;
      WeakReference<ClassLoader> ref = classloaders.get(key);
      if (ref != null)
      {
         result = (ClassLoader) ref.get();
         if (result != null && forceGC)
         {
            try
            {
               result = null; // Don't hold a ref to it here while analyzing heap
               result = getClassLoader(ref, reportFile);
            }
            catch (Exception e)
            {
               log.error("Caught exception checking for classloader release", e);
            }
         }
      }
      
      return result;
   }
   

   /**
    * If you started your class with -agentlib:jbossAgent in case of leakage (if className still loaded) a file (reportFile) will be created, and a heapSnapshot(./snapshot,mem)
    * 
    * @param weakReferenceOnLoader A weakReference to the created ClassLoader. If there is no references to this classLoader this reference will be cleared
    * @param className The class name supposed to be unloade.
    * @param reportHTMLFile the report file 
    * @throws Exception
    */
   private ClassLoader getClassLoader(WeakReference<ClassLoader> weakReferenceOnLoader, String reportHTMLFile) throws Exception
   {
      LeakAnalyzer leakAnalyzer = null;
      try
      {
         leakAnalyzer = new LeakAnalyzer();
      }
      catch (Throwable t)
      {
         log.debug("Could not instantiate JVMTIInterface:" + t.getLocalizedMessage());
      }
      
      if (leakAnalyzer != null && leakAnalyzer.isActive())
      {
         leakAnalyzer.forceGC();
         
         if (weakReferenceOnLoader.get() == null)
         {
            log.debug("leakAnalyzer.forceGC() released CL ref");
            return null;
         }
         
         fillMemory(weakReferenceOnLoader);
         
         if (weakReferenceOnLoader.get() == null)
         {
            return null;
         }
         
         // Sleep a bit to allow CPU to do work like exchange cluster PING responses
         Thread.sleep(20);
         
         leakAnalyzer.heapSnapshot("snapshot", "mem");
         
         if (weakReferenceOnLoader.get() == null)
         {
            log.debug("leakAnalyzer.heapSnapshot() released CL ref");
            return null;
         }
         
         Thread.sleep(20);
            
         @SuppressWarnings("unchecked")
         HashMap<Long, List<ReferenceDataPoint>> datapoints = leakAnalyzer.createIndexMatrix();
         
         if (weakReferenceOnLoader.get() == null)
         {
            log.debug("leakAnalyzer.createIndexMatrix() released CL ref");
            return null;
         }
         
         Thread.sleep(20);
         
         String report = leakAnalyzer.exploreObjectReferences(datapoints, weakReferenceOnLoader.get(), this.depth, true, false);
         log.info(report);
         if (reportHTMLFile != null)
         {
            File outputfile = new File(reportHTMLFile);
            FileOutputStream outfile = new FileOutputStream(outputfile);
            PrintStream realoutput = new PrintStream(outfile);
            realoutput.println(report);
            realoutput.close();
         }
         
         Thread.sleep(20);

         leakAnalyzer.forceGC();
      }
      else
      {
         log.debug("JVMTI not active; using System.gc()");
         System.gc();
         Thread.sleep(1000);
         
         if (weakReferenceOnLoader.get() != null)
            fillMemory(weakReferenceOnLoader);
         
         if (weakReferenceOnLoader.get() != null)
            fillMemory(weakReferenceOnLoader);
      }
      
      return (ClassLoader) weakReferenceOnLoader.get();
   }
   
   private void fillMemory(WeakReference<ClassLoader> ref)
   {
      Runtime rt = Runtime.getRuntime();
      int[] adds = { 0, 10, 20, 30, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49 };
      for (int i = 0; i < adds.length; i++) 
      {
          int toAdd = adds[i];
          System.gc();
          System.runFinalization();
          
          if (ref.get() == null)
             break;
          
          // create garbage, filling a larger and larger % of
          // free memory on each loop
          byte[][] bytez =  new byte[10000][];
          long avail = rt.freeMemory();
          int create = (int) (avail / 1000 * (950 + toAdd));
          String pct = (95 + (toAdd/10)) + "." + (toAdd - ((toAdd/10) * 10));
          int bucket = create / 10000;
          log.info("Filling " + pct + "% of free memory. Free memory=" + avail + 
                   " Total Memory=" + rt.totalMemory() + " Max Memory=" + rt.maxMemory());
          
          try
          {
             for (int j = 0; j < bytez.length; j++)
             {
                bytez[j] = new byte[bucket];
                if (j % 100 == 0 && ref.get() == null)
                {
                   return;
                }
             }
          }
          catch (Throwable t)
          {
             bytez = null;
             System.gc();
             System.runFinalization();
             log.warn("Caught throwable filling memory: " + t);
             break;
          }       
          finally
          {
             bytez = null;
             // Sleep a bit to allow CPU to do work like exchange cluster PING responses
             try
             {
                Thread.sleep(20);
             }
             catch (InterruptedException ignored)
             {
               log.warn("Interrupted");
               break;
             }
          }
      }
      
      try
      {
         ByteArrayOutputStream byteout = new ByteArrayOutputStream();
         ObjectOutputStream out = new ObjectOutputStream(byteout);
        
         out.writeObject(new Dummy());
         out.close();
         
         ByteArrayInputStream byteInput = new ByteArrayInputStream(byteout.toByteArray());
         ObjectInputStream input = new ObjectInputStream(byteInput);
         input.readObject();
         input.close();
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }
      
      if (ref.get() != null)
      {
         System.gc();
         System.runFinalization();
      }
   }
   
   public void removeClassLoader(String key)
   {
      classloaders.remove(key);
   }
   
   /** Used just to serialize anything and release SoftCache on java Serialization */
   private static class Dummy implements Serializable
   {
        private static final long serialVersionUID = 1L;
   }
}

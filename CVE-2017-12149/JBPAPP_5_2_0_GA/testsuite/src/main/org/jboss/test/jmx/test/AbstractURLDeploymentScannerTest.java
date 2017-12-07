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
package org.jboss.test.jmx.test;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.jboss.mx.util.ObjectNameFactory;
import org.jboss.test.JBossTestCase;
import org.jboss.test.JBossTestSetup;
import org.jboss.util.file.Files;
import org.jboss.util.file.JarUtils;
import org.jboss.util.stream.Streams;

/**
 * Base test for url deployment scanner
 *
 * @author adrian@jboss.org
 * @author dimitris@jboss.org
 * @version $Revision: 92490 $
 */
public abstract class AbstractURLDeploymentScannerTest extends JBossTestCase
{
   protected static ObjectName scanner = ObjectNameFactory.create("jboss.test:type=DeploymentScanner,flavor=URL");
   
   private static URL scanDir;
   
   public AbstractURLDeploymentScannerTest(String name)
   {
      super(name);
   }

   public static Test getTestSuite(Class clazz) throws Exception
   {
      TestSuite suite = new TestSuite();
      suite.addTest(new TestSuite(clazz));
      JBossTestSetup setup = new JBossTestSetup(suite)
      {
         protected void setUp() throws Exception
         {
            super.setUp();
            MBeanServerConnection server = getServer();
            scanDir = getScanURL();
            getLog().debug("Scan Directory=" + scanDir);
            server.invoke(scanner, "addURL", new Object[] { scanDir }, new String[] { URL.class.getName() });
         }
      };
      return getDeploySetup(setup, "jbosstest-urlscanner-service.xml");
   }

   public void startScanner() throws Exception
   {
      getServer().invoke(scanner, "start", null, null);
   }

   public void stopScanner() throws Exception
   {
      getServer().invoke(scanner, "stop", null, null);
   }

   /**
    * The deployment URL of a filename, inside the scan directory
    */
   public URL getTargetURL(String fileName) throws Exception
   {
      return new URL(scanDir, fileName);
   }
   
   /**
    * Delegate to Scanner
    */
   public void suspendDeployment(URL url) throws Exception
   {
      getServer().invoke(
            scanner,
            "suspendDeployment",
            new Object[] { url },
            new String[] { URL.class.getName() });
   }
   
   /**
    * Delegate to Scanner
    */
   public void resumeDeployment(URL url, boolean markUpToDate) throws Exception
   {
      getServer().invoke(
            scanner,
            "resumeDeployment",
            new Object[] { url, new Boolean(markUpToDate) },
            new String[] { URL.class.getName(), boolean.class.getName() });      
   }
   
   public void hotDeploy(String fileName) throws Exception
   {
      hotDeploy(fileName, 2000);
   }

   public void hotDeploy(String fileName, long wait) throws Exception
   {
      URL url = getDeployURL(fileName);
      URL destURL = getTargetURL(fileName);
      copy(url, new File(destURL.getFile()));
      // TODO something better than a sleep
      if (wait > 0)
         Thread.sleep(wait);
   }

   public void hotUndeploy(String fileName) throws Exception
   {
      hotUndeploy(fileName, 2000);
   }

   public void hotUndeploy(String fileName, long wait) throws Exception
   {
      URL destURL = new URL(scanDir, fileName);
      delete(new File(destURL.getFile()));
      // TODO something better than a sleep
      if (wait > 0)
         Thread.sleep(wait);
   }

   protected void copy(URL src, File dest) throws IOException
   {
      log.debug("Copying " + src + " -> " + dest);
      
      // Validate that the dest parent directory structure exists
      File dir = dest.getParentFile();
      if (!dir.exists())
      {
         boolean created = dir.mkdirs();
         if( created == false )
            throw new IOException("mkdirs failed for: "+dir.getAbsolutePath());
      }

      // Remove any existing dest content
      if( dest.exists() == true )
      {
         boolean deleted = Files.delete(dest);
         if( deleted == false )
            throw new IOException("delete of previous content failed for: "+dest.getAbsolutePath());
      }

      if (src.getProtocol().equals("file"))
      {
         File srcFile = new File(src.getFile());
         if (srcFile.isDirectory())
         {
            log.debug("Making zip copy of: " + srcFile);
            // make a jar archive of the directory
            OutputStream out = new BufferedOutputStream(new FileOutputStream(dest));
            JarUtils.jar(out, srcFile.listFiles());
            out.close();
            return;
         }
      }

      InputStream in = new BufferedInputStream(src.openStream());
      OutputStream out = new BufferedOutputStream(new FileOutputStream(dest));
      Streams.copy(in, out);
      out.flush();
      out.close();
      in.close();
   }

   protected void delete(File dest) throws IOException
   {
      log.debug("Deleting " + dest);

      // Remove any existing dest content
      if( dest.exists() == true )
      {
         boolean deleted = Files.delete(dest);
         int tries = 0; int wait=100;
         while (!deleted && tries++ < 10)
         {
            try 
            {
               Thread.sleep(wait<<=1); 
            } 
            catch(Exception e) 
            {
            }

            deleted = Files.delete(dest);
         }
         if (!deleted)
            throw new IOException("delete of previous content failed for: "+dest.getAbsolutePath());
      }
   }

   private static URL getScanURL() throws Exception
   {
      String deployDir = System.getProperty("jbosstest.deploy.dir");
      if (deployDir == null)
         deployDir = "../lib";
      File file = new File(deployDir);
      File scanDir = new File(file, "urlscannertest");
      URL url = scanDir.toURL();
      return url;
   }
}

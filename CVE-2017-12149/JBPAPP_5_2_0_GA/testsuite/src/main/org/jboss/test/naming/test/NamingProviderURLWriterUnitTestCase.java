/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat, Inc. and individual contributors
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

package org.jboss.test.naming.test;

import java.io.File;
import java.io.PrintWriter;
import java.net.URI;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import junit.framework.TestCase;

import org.jboss.naming.NamingProviderURLWriter;
import org.jboss.util.file.Files;

/**
 * Unit tests of {@link NamingProviderURLWriter}
 *
 * @author Brian Stansberry
 * 
 * @version $Revision: 100318 $
 */
public class NamingProviderURLWriterUnitTestCase extends TestCase
{
   private static long count = System.currentTimeMillis();
   private URI tempDir;
   private CountDownLatch tearDownLatch;
   
   /**
    * Create a new NamingProviderURLWriterUnitTestCase.
    * 
    * @param name
    */
   public NamingProviderURLWriterUnitTestCase(String name)
   {
      super(name);
   }

   protected void setUp() throws Exception
   {
      super.setUp();
      
      File tmp = new File(System.getProperty("java.io.tmpdir"));
      File dir = new File(tmp, getClass().getSimpleName());
      tempDir = dir.toURI();
      dir.mkdirs();
      dir.deleteOnExit();
      
      count++;
   }

   protected void tearDown() throws Exception
   {
      try
      {
         super.tearDown();
      }
      finally
      {
         if (tearDownLatch != null)
         {
            tearDownLatch.countDown();
         }
         if (tempDir != null)
         {
            Files.delete(new File(tempDir));
         }
      }
   }
   
   /**
    * JBAS-7674 -- try to lock the file that will be written and check
    * that the NamingProviderURLWriter start/stop methods don't blow up.
    * Note this test is only meaningful on windows.
    * 
    * @throws Exception
    */
   public void testLifecycleWithLockedFile() throws Exception
   {
      String fileName = "jnp-service-" + count + ".url";
      tearDownLatch = new CountDownLatch(1);
      CountDownLatch lockedLatch = new CountDownLatch(1);
      FileLocker locker = new FileLocker(tempDir, fileName, null, tearDownLatch, lockedLatch);
      
      NamingProviderURLWriter testee = new NamingProviderURLWriter();
      testee.setOutputDirURL(tempDir);
      testee.setOutputFileName(fileName);
      testee.setBootstrapAddress("localhost");
      testee.setBootstrapPort(9999);
      
      Thread t = new Thread(locker);
      t.setDaemon(true);
      t.start();
      assertTrue("FileLocker locked file", lockedLatch.await(10, TimeUnit.SECONDS));
      testee.start();
      testee.stop();
   }
   
   /** Hacky attempt to "lock" a file. */
   private static class FileLocker implements Runnable
   {
      private final File file;
      private final String serviceURL;
      private final CountDownLatch tearDownLatch;
      private final CountDownLatch lockedLatch;
      
      private FileLocker(URI tempDir, String fileName, String serviceURL, CountDownLatch tearDownLatch, CountDownLatch lockedLatch)
      {
         this.file = new File(new File(tempDir), fileName);
         this.tearDownLatch = tearDownLatch;
         this.lockedLatch = lockedLatch;
         this.serviceURL = serviceURL == null ? "jnp://localhost:1099" : serviceURL;
      }

      public void run()
      {
         PrintWriter writer = null;
         try
         {
            writer = new PrintWriter(file);
            writer.println(serviceURL);
            writer.flush();
            // Tell driver thread we're ready
            lockedLatch.countDown();
            // Hold lock until test completion
            tearDownLatch.await();
         }
         catch (Exception e)
         {
            e.printStackTrace();
         }
         finally
         {
            if (writer != null)
            {
               writer.close();
            }
         }
         
      }
      
   }

}

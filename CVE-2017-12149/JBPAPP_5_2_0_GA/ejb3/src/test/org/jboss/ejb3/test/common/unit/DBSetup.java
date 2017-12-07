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
package org.jboss.ejb3.test.common.unit;

import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.Statement;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;

import junit.extensions.TestSetup;
import junit.framework.Test;

/** A TestSetup that starts hypersonic before the testcase with a tcp
 * listening port at 1701.
 * 
 * @author Scott.Stark@jboss.org
 * @version $Revison:$
 */
public class DBSetup extends TestSetup
{
   public DBSetup(Test test)
   {
      super(test);
   }

   protected void setUp() throws Exception
   {
         File hypersoniDir = new File("output/hypersonic");
         if (!hypersoniDir.exists())
         {
            hypersoniDir.mkdirs();
         }

         if (!hypersoniDir.isDirectory())
         {
            throw new IOException("Failed to create directory: " + hypersoniDir);
         }
      
         File dbPath = new File(hypersoniDir, "clusteredentity-db");

         // Start DB in new thread, or else it will block us
         DBThread serverThread = new DBThread(dbPath);
         serverThread.start();
         
         int elapsed = 0;
         while (!serverThread.isStarted() && elapsed < 15000)
         {
            try 
            {
               Thread.sleep(100);
               elapsed += 100;
            }
            catch (InterruptedException ie)
            {
               System.out.println("Interrupted while waiting for Hypersonic");
            }
         }
         
         if (!serverThread.isStarted())
            System.out.println("Hypersonic failed to start in a timely fashion");
   }

   protected void tearDown() throws Exception
   {
      Class.forName("org.hsqldb.jdbcDriver");
      String dbURL = "jdbc:hsqldb:hsql://" + System.getProperty("jbosstest.server.host", "localhost") + ":1701";
      Connection conn = DriverManager.getConnection(dbURL, "sa", "");
      Statement statement = conn.createStatement();      
      statement.executeQuery("SHUTDOWN COMPACT");
      
   }

   public static void main(String[] args) throws Exception
   {
      DBSetup setup = new DBSetup(null);
      setup.setUp();
      Thread.sleep(120*1000);
      setup.tearDown();
   }
   
   class DBThread extends Thread
   {
      boolean started;
      File dbPath;
      
      DBThread(File dbPath)
      {
         super("hypersonic");
         this.dbPath = dbPath;
      }
      
      boolean isStarted()
      {
         return started;
      }
      
      public void run()
      {
         try
         {
            // Create startup arguments
            // BES 2007/09/25 We use -silent true to avoid 
            // http://sourceforge.net/tracker/index.php?func=detail&aid=1673747&group_id=23316&atid=378131
            String[] args = {
                  "-database",
                  dbPath.toString(),
                  "-port",
                  String.valueOf(1701),
                  "-silent",
                  "true",
                  "-trace",
                  "false",
                  "-no_system_exit",
                  "true",
             };
            System.out.println("Starting hsqldb");
            // HACK Do this by reflection for now until we determine how 
            // we want to handle this in EJB3
            Class clazz = Thread.currentThread().getContextClassLoader().loadClass("org.hsqldb.Server");
            Method main = clazz.getDeclaredMethod("main", new Class[] { String[].class });
            main.invoke(null, new Object[] { args });
//            org.hsqldb.Server.main(args);
            System.out.println("Done");
         }
         catch (Exception e)
         {
            e.printStackTrace();
         }
         finally
         {
            started = true;
         }
      }
   }
}

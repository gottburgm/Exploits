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
package org.jboss.test.cluster.testutil;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

import org.jboss.test.JBossTestServices;
import org.jboss.test.cluster.web.persistent.PersistentStoreSetupDelegate;
import org.jboss.test.cluster.web.persistent.PersistentStoreTableSetup;

/** A TestSetup that starts hypersonic before the testcase with a tcp
 * listening port at 1701.
 * 
 * @author Brian Stansberry
 * @version $Revison:$
 */
public class DBSetupDelegate implements TestSetupDelegate
{
   public static final String DBADDRESS_PROPERTY = "jbosstest.cluster.node0";
   public static final String DEFAULT_ADDRESS = "localhost";
   public static final int DEFAULT_PORT = 1701;
   
   private final String address;
   private final int port;
   
   public DBSetupDelegate()
   {
      this(DEFAULT_ADDRESS, DEFAULT_PORT);
   }
   
   public DBSetupDelegate(String address, int port)
   {
      if (address == null)
      {
         throw new IllegalArgumentException("Null address");
      }
      this.address = address;
      this.port = port;
   }
   
   public void setTestServices(JBossTestServices services)
   {
      // no-op      
   }

   /* (non-Javadoc)
    * @see org.jboss.test.cluster.testutil.TestSetupDelegate#setUp()
    */
   public void setUp() throws Exception
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
      
         File dbPath = new File(hypersoniDir, "cif-db");

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

   /* (non-Javadoc)
    * @see org.jboss.test.cluster.testutil.TestSetupDelegate#tearDown()
    */
   public void tearDown() throws Exception
   {
      Class.forName("org.hsqldb.jdbcDriver");
      String dbURL = "jdbc:hsqldb:hsql://" + address + ":" + port;
      Connection conn = DriverManager.getConnection(dbURL, "sa", "");
      Statement statement = conn.createStatement();      
      statement.executeQuery("SHUTDOWN COMPACT");      
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
                  String.valueOf(DBSetupDelegate.this.port),
                  "-address",
                  DBSetupDelegate.this.address,
                  "-silent",
                  "true",
                  "-trace",
                  "false",
                  "-no_system_exit",
                  "true",
             };
            System.out.println("Starting hsqldb");
            org.hsqldb.Server.main(args);
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
   
   public static void main(String[] args) {
      try
      {
         new DBSetupDelegate().setUp();
         new PersistentStoreSetupDelegate().setUp();
      }
      catch (Exception e)
      {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }
   }

}

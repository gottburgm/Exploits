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
package test.stress;

import java.util.Hashtable;
import java.util.Random;
import java.util.Properties;
import java.util.List;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Set;

import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ObjectName;

import junit.framework.TestCase;

/**
 * Stresses concurrent use of the MBeanServer for registration, querying.
 * @author Scott.Stark@jboss.org
 * @version $Revision: 81022 $
 */
public class ConcurrencyTestCase
   extends TestCase
{
   // Constants ---------------------------------------------------------------
   static final int N = 100000;
   static final int Nadders = 2;
   static final int NDomains = 1;
   static final List names = Collections.synchronizedList(new LinkedList());

   // Attributes --------------------------------------------------------------

   /**
    * The MBeanServer
    */
   private MBeanServer server;
   private Random rnd;
   private boolean adding;

   // Constructor -------------------------------------------------------------

   /**
    * Construct the test
    */
   public ConcurrencyTestCase(String s)
   {
      super(s);
   }

   // Tests -------------------------------------------------------------------

   public void testAddRemoveQuery() throws Exception
   {
      MBeanAdder[] adders = new MBeanAdder[Nadders];
      Thread[] adderThreads = new Thread[Nadders];
      MBeanRemover[] removers = new MBeanRemover[Nadders];
      Thread[] removerThreads = new Thread[Nadders];
      MBeanFinder finder = new MBeanFinder();
      adding = true;
      for(int n = 0; n < adders.length; n ++)
      {
         int minID = n * N / Nadders;
         int maxID = (n+1) * N / Nadders;
         adders[n] = new MBeanAdder(minID, maxID);
         adderThreads[n] = new Thread(adders[n], "MBeanAdder#"+n);
         adderThreads[n].start();
      }
      Thread t1 = new Thread(finder, "MBeanFinder");
      t1.start();
      for(int n = 0; n < adders.length; n ++)
      {
         removers[n] = new MBeanRemover();
         removerThreads[n] = new Thread(removers[n], "MBeanRemover#"+n);
         removerThreads[n].start();
      }

      for(int n = 0; n < adders.length; n ++)
      {
         adderThreads[n].join();
      }
      adding = false;
      t1.join();
      for(int n = 0; n < adders.length; n ++)
      {
         removerThreads[n].join();
      }

      for(int n = 0; n < adders.length; n ++)
      {
         assertNull("There was no exception in MBeanAdder#"+n, adders[n].getException());
      }
      assertNull("There was no exception in MBeanFinder", finder.getException());
      for(int n = 0; n < adders.length; n ++)
      {
         assertNull("There was no exception in MBeanRemover#"+n, removers[n].getException());
      }
   }

   // Support -----------------------------------------------------------------

   /**
    * Start a new test
    */
   protected void setUp()
   {
      server = MBeanServerFactory.createMBeanServer();
      rnd = new Random();
   }

   /**
    * End the test
    */
   protected void tearDown()
      throws Exception
   {
      MBeanServerFactory.releaseMBeanServer(server);
   }

   /**
    * Sleep for a bit
    */
   private void sleep(long time)
   {
      try
      {
         Thread.sleep(time);
      }
      catch (InterruptedException ignored)
      {
      }
   }

   class MBeanAdder implements Runnable
   {
      private int minID;
      private int maxID;
      private String domain;
      private Hashtable<String, String> nameProps = new Hashtable<String, String>();
      private Throwable ex;

      MBeanAdder(int minID, int maxID)
      {
         this.minID = minID;
         this.maxID = maxID;
      }
      public void run()
      {
         System.out.println("MBeanAdder, min="+minID+", max="+maxID+", starting");
         nameProps.put("type", "simple");
         try
         {
            for(int n = 0; n < N; n ++)
            {
               int id = minID + n % maxID;
               domain = "jboss.test."+ rnd.nextInt(NDomains);
               nameProps.put("id", "#"+id);
               addMBean();
            }
         }
         catch(Throwable t)
         {
            this.ex = t;
            ex.printStackTrace();
         }
         System.out.println("MBeanAdder, min="+minID+", max="+maxID+", ending");
      }
      void addMBean() throws Exception
      {
         ObjectName name = new ObjectName(domain, nameProps);
         if( server.isRegistered(name))
            server.unregisterMBean(name);
         Simple mbean = new Simple(name);
         server.registerMBean(mbean, name);
         names.add(name);
      }
      Throwable getException()
      {
         return ex;
      }
   }

   class MBeanRemover implements Runnable
   {
      private Throwable ex;

      public void run()
      {
         try
         {
            while( adding )
            {
               int max = names.size();
               if( max == 0 )
               {
                  sleep(10);
                  continue;
               }
               int index = rnd.nextInt(max);
               try
               {
                  ObjectName name = (ObjectName) names.remove(index);
                  server.unregisterMBean(name);
               }
               catch(IndexOutOfBoundsException ignore)
               {
               }
            }
         }
         catch(Throwable t)
         {
            this.ex = t;
            ex.printStackTrace();
         }
      }
      Throwable getException()
      {
         return ex;
      }
   }

   class MBeanFinder implements Runnable
   {
      private Throwable ex;
      public void run()
      {
         try
         {
            ObjectName query = new ObjectName("jboss.test.*:type=simple,*");
            int count = 0;
            while( adding )
            {
               Set matches = server.queryNames(query, null);
               count ++;
               if( count % 1000 == 0 )
                  System.out.println(count+" queries, names.size="+names.size()+", matches.size="+matches.size());
            }
         }
         catch(Throwable t)
         {
            this.ex = t;
            ex.printStackTrace();
         }
      }
      Throwable getException()
      {
         return ex;
      }
   }

   static interface SimpleMBean
   {
      public ObjectName getName();
   }
   static class Simple implements SimpleMBean
   {
      private ObjectName name;
      Simple(ObjectName name)
      {
         this.name = name;
      }
      public ObjectName getName()
      {
         return name;
      }
   }
}


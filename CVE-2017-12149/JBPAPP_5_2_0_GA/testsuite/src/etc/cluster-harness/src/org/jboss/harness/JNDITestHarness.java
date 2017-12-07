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
package org.jboss.harness;

import java.io.InputStream;
import java.util.Properties;

import EDU.oswego.cs.dl.util.concurrent.CopyOnWriteArrayList;
import com.cluster.simple.sessionbeans.SimpleSessionHome;

/**
 *  This class performs repetitive lookups of an EJB that's bound
 *  during deployment.  It allows the user to compare EJB lookup
 *  performance using the local naming server vs. using HA-JNDI.
 *  
 * @author <a href="mailto:tom@jboss.org">Tom Elrod</a>
 * @version $Revision: 81036 $
 */
public class JNDITestHarness
{
   
   public static void main(String[] args)
   {
      int iterations = 10;
      int numOfClients = 10;
      boolean printLookupTime = false;

      if (args.length > 0)
      {
         iterations = Integer.parseInt(args[0]);
      }
      if (args.length > 1)
      {
         numOfClients = Integer.parseInt(args[1]);
      }
      if (args.length > 2)
      {
         printLookupTime = Boolean.valueOf(args[2]).booleanValue();
      }

      JNDITestHarness test = new JNDITestHarness();
      try
      {
         System.out.println("JNDITestHarness properties");
         Properties props = test.getPropAsResource(TestClient.SIMPLE_CONFIG_SERVER_PROP);
         props.list(System.out);
      }
      catch (Exception e)
      {
         System.out.println(e);
      }
      test.runTest(iterations, numOfClients, printLookupTime);
   }

   private void runTest(final int iterations, int numOfClients, final boolean printTime)
   {
      final CopyOnWriteArrayList timesList = new CopyOnWriteArrayList();
      final Counter counter = new Counter();
      long start = System.currentTimeMillis();

      System.out.println("Starting run now with " + numOfClients + " clients and " + iterations + " iterations.");

      for (int x = 0; x < numOfClients; x++)
      {
         new Thread()
         {
            public void run()
            {
               try
               {
                  for (int i = 0; i < iterations; i++)
                  {
                     long startTime = System.currentTimeMillis();
                     TestClient client = new TestClient();
                     client.lookup("SimpleSession", SimpleSessionHome.class);
                     long endTime = System.currentTimeMillis();
                     timesList.add(new Long(endTime - startTime));
                     if (printTime)
                        System.out.println(endTime - startTime);
                     counter.increment();
                     long value = counter.getValue();
                     if (value % 10000 == 0)
                     {
                        System.out.println("Executed " + value + " times so far.");
                     }

                  }
               }
               catch (Exception e)
               {
                  System.out.println("Error calling test client to do jndi lookup");
                  e.printStackTrace();
               }
            }
         }.start();
      }


      while (timesList.size() < (iterations * numOfClients))
      {
         try
         {
            Thread.sleep(1000);
         }
         catch (InterruptedException e)
         {
            e.printStackTrace();  //TODO: -TME Implement
         }
      }

      long end = System.currentTimeMillis();

      System.out.println("Ran test with total of " + timesList.size() + " (iterations: " + iterations +
                         ", number of clients: " + numOfClients + ") in " + (end - start) / 1000 + " seconds.");

      long total = 0;
      for (int i = 0; i < timesList.size(); i++)
      {
         total += ((Long) timesList.get(i)).longValue();
      }

      System.out.println("Average time to make lookup is " + (total / (iterations * numOfClients)) + " milliseconds.");

      //System.out.println("Counter says was executed " + counter.getValue() + " times.");

   }
   
   public Properties getPropAsResource(String name) throws Exception
   {
      InputStream is = getClass().getResourceAsStream("/META-INF/" + name);
      if (is == null)
      {
         throw new Exception("Unable to locate resource: " + name);
      }
      Properties confProp = new Properties();
      confProp.load(is);
      return confProp;
   }

   public class Counter
   {
      private long count = 0;

      public void increment()
      {
         count++;
      }

      public void decrement()
      {
         count--;
      }

      public long getValue()
      {
         return count;
      }
   }

}

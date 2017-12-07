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

import EDU.oswego.cs.dl.util.concurrent.CopyOnWriteArrayList;

import javax.naming.InitialContext;
import javax.naming.NameAlreadyBoundException;
import java.io.InputStream;
import java.util.Properties;

/**
 *  This class performs repetitive lookups of an object that's bound
 *  using the same naming provider (e.g., if run against port 1099, it 
 *  will bind and lookup using local JNDI).  It allows the user to compare
 *  lookiup performance using the local naming server vs. using HA-JNDI
 *  where the object will be found and RPC calls aren't necessary.
 *  
 * @author <a href="mailto:tom@jboss.org">Tom Elrod</a>
 * @author <a href="mailto:jgauthier@novell.com">Jerry Gauthier</a>
 * @version $Revision: 81036 $
 */
public class HAJNDITestHarness
{
   public static String SIMPLE_CONFIG_SERVER_PROP = "services.properties";
   private static String NODE1 = "ha_node1";
   private static String NODE2 = NODE1 + "/" +"ha_node2";
   private static String NODE3 = NODE2 + "/" + "ha_node3";
   private static String BINDKEY = NODE3 + "/" + "ha_bind_key";
   
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

      HAJNDITestHarness test = new HAJNDITestHarness();
      try
      {
         System.out.println("HAJNDITestHarness properties");
         Properties props = test.getPropAsResource(SIMPLE_CONFIG_SERVER_PROP);
         props.list(System.out);
         System.out.println("getting initial context");
         InitialContext ctx = new InitialContext(props);

         try
         {
            ctx.createSubcontext(NODE1);
         }
         catch (NameAlreadyBoundException e)
         {
            // do nothing
         }

         try
         {
            ctx.createSubcontext(NODE2);
         }
         catch (NameAlreadyBoundException e)
         {
            // do nothing
         }

         try
         {
            ctx.createSubcontext(NODE3);
         }
         catch (NameAlreadyBoundException e)
         {
            // do nothing
         }

         try
         {
            HABindingObject bindValue = new HABindingObject();
            bindValue.setName("John Smith");
            bindValue.setCompany("Acme Corporation");
            bindValue.setLocation("New York, NY");
            bindValue.setCountry("US");
            bindValue.setYear(1934);
            bindValue.setManager(false);
            
            ctx.bind(BINDKEY, bindValue);
         }
         catch (NameAlreadyBoundException e)
         {
            // do nothing
         }
         test.runTest(iterations, numOfClients, printLookupTime);
      }
      catch (Exception e)
      {
         System.out.println(e);
      }   
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
                     HATestClient client = new HATestClient();
                     Object o = client.lookup(BINDKEY, HABindingObject.class);
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
                  System.out.println("Error calling test client to do hajndi_binding lookup");
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
   }
   
   private Properties getPropAsResource(String name) throws Exception
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

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
package test.performance.standard;

import junit.framework.TestCase;
import test.performance.PerformanceSUITE;
import test.performance.standard.support.StandardMBeanExtension;

import javax.management.*;

public class StandardMBeanThroughputTEST extends TestCase
{

   public StandardMBeanThroughputTEST(String s)
   {
      super(s);
   }

   public void testThroughput() throws Exception
   {
      MyThread myThread = new MyThread();
      Thread t = new Thread(myThread);
      StandardMBeanExtension std = new StandardMBeanExtension();
      
      String method      = "mixedArguments";
      String[] signature = new String[] { 
                              Integer.class.getName(),
                              Integer.TYPE.getName(),
                              Object[][][].class.getName(),
                              Attribute.class.getName()
                           };
                           
      Object[] args      = new Object[] {
                              new Integer(1234),
                              new Integer(5678),
                              new Object[][][] {
                                 { 
                                    { "1x1x1", "1x1x2", "1x1x3" },
                                    { "1x2x1", "1x2x2", "1x2x3" },
                                    { "1x3x1", "1x3x2", "1x3x3" }
                                 },
                                 
                                 {
                                    { "2x1x1", "2x1x2", "2x1x3" },
                                    { "2x2x1", "2x2x2", "2x2x3" },
                                    { "2x3x1", "2x3x2", "2x3x3" }
                                 },
                                 
                                 {
                                    { "3x1x1", "3x1x2", "3x1x3" },
                                    { "3x2x1", "3x2x2", "3x2x3" },
                                    { "3x3x1", "3x3x2", "3x3x3" }
                                 }
                              },
                              new Attribute("attribute", "value")
                           };

      MBeanServer server = MBeanServerFactory.createMBeanServer();
      ObjectName name = new ObjectName("test:test=test");

      server.registerMBean(std, name);
      
      t.start();
      while(myThread.isKeepRunning())
      {
         server.invoke(name, method, args, signature);
      }

      System.out.println("\nStandardMBeanExtension Throughput: " + 
                         std.getCount() / (PerformanceSUITE.THROUGHPUT_TIME / PerformanceSUITE.SECOND) +
                         " invocations per second.");
      System.out.println("(Total: " + std.getCount() + ")\n");
   }

   class MyThread implements Runnable 
   {

      private boolean keepRunning = true;
      
      public void run() 
      {
         try
         {
            Thread.sleep(PerformanceSUITE.THROUGHPUT_TIME);
         }
         catch (InterruptedException e)
         {
            
         }
         
         keepRunning = false;
      }
      
      public boolean isKeepRunning()
      {
         return keepRunning;
      }
   }
}

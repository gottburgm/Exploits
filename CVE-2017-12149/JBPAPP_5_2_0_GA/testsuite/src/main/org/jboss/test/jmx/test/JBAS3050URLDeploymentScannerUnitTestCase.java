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

import javax.management.ObjectName;

import org.jboss.mx.util.ObjectNameFactory;
import org.jboss.system.ServiceMBean;

import junit.framework.Test;

/**
 * JBAS3050URLDeploymentScannerUnitTestCase.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 81036 $
 */
public class JBAS3050URLDeploymentScannerUnitTestCase extends AbstractURLDeploymentScannerTest
{
   ObjectName test = ObjectNameFactory.create("test:name=PauseInStart");
   
   protected class DeployInBackground implements Runnable
   {
      Throwable background;
      
      String fileName;
      
      public DeployInBackground(String fileName)
      {
         this.fileName = fileName;
      }
      
      public void run()
      {
         try
         {
            hotDeploy(fileName, 2000);
         }
         catch (Throwable t)
         {
            background = t;
         }
      }
   }
   
   public void testDeadlock() throws Exception
   {
      DeployInBackground background = new DeployInBackground("pauseinstart.sar");
      Thread thread = new Thread(background);
      thread.start();
      try
      {
         Thread.sleep(4000);
         stopScanner();
         assertEquals(new Integer(ServiceMBean.STARTED), getServer().getAttribute(test, "State"));
         hotDeploy("pauseinstart.sar");
         startScanner();
         assertEquals(new Integer(ServiceMBean.STARTED), getServer().getAttribute(test, "State"));
      }
      finally
      {
         thread.join();
         hotUndeploy("pauseinstart.sar");
      }
   }
   
   public static Test suite() throws Exception
   {
      return getTestSuite(JBAS3050URLDeploymentScannerUnitTestCase.class);
   }
   
   public JBAS3050URLDeploymentScannerUnitTestCase(String name)
   {
      super(name);
   }
}

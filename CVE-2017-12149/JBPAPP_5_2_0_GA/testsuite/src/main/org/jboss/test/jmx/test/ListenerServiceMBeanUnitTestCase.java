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

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;

import org.jboss.logging.Logger;
import org.jboss.test.JBossTestCase;

/**
 * Tests for ListenerServiceMBeanSupport and filter factories.
 * 
 * @author <a href="mailto:dimitris@jboss.org">Dimitris Andreadis</a>
 * @version $Revision: 81036 $
 */
public class ListenerServiceMBeanUnitTestCase extends JBossTestCase
{
   public ListenerServiceMBeanUnitTestCase(String name)
   {
      super(name);
   }

   /**
    * [JBAS-1959] 
    */
   public void testDeploymentInfoNotificationFilterFactory() throws Exception
   {
      /* FIXME - Commenting out the test until it is clarified if DeploymentInfo
                 notifications are still generated in JBoss 5      
      Logger log = getLog();
      log.info("+++ testDeploymentInfoNotificationFilterFactory");

      String listenerService = "listener-deploymentinfo.sar";
      String testService = "listener-simpletest.sar";
      
      try
      {
         ObjectName listener = new ObjectName("jboss.test:service=NotificationListener");         
         MBeanServerConnection server = super.getServer();
         Integer notifCount;
         
         // the "listenerService" is configured to intercept the
         // start and stop notification from SARDeployer, when
         // we deploy "targetService"
         deploy(listenerService);
         notifCount = (Integer)server.getAttribute(listener, "NotificationCount");
         assertTrue("NotifCount == 0, got " + notifCount.intValue(), notifCount.intValue() == 0);
         
         deploy(testService);
         notifCount = (Integer)server.getAttribute(listener, "NotificationCount");
         assertTrue("NotifCount == 1, got " + notifCount.intValue(), notifCount.intValue() == 1);
         
         undeploy(testService);
         notifCount = (Integer)server.getAttribute(listener, "NotificationCount");
         assertTrue("NotifCount == 2, got " + notifCount.intValue(), notifCount.intValue() == 2);         
      }
      catch (Exception e)
      {
         getLog().warn("Caught exception", e);
         fail("Unexcepted Exception, see the Log file");
      }
      finally
      {
         undeploy(testService);         
         undeploy(listenerService);
      }
      */
   }
}
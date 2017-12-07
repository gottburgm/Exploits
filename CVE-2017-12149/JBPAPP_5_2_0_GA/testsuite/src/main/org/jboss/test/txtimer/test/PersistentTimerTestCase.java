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
package org.jboss.test.txtimer.test;

import javax.naming.InitialContext;

import org.jboss.test.JBossTestCase;
import org.jboss.test.txtimer.interfaces.TimerSession;
import org.jboss.test.txtimer.interfaces.TimerSessionHome;
import org.jboss.test.txtimer.support.SimpleInfo;

/**
 * Test that timers persist across redeployments
 *
 * @author Dimitris.Andreadis@jboss.org
 * @version $Revision: 81036 $
 */
public class PersistentTimerTestCase extends JBossTestCase
{
   public PersistentTimerTestCase(String name)
   {
      super(name);
   }

   /**
    * Deploy the ejbs, create a timer on the SLSB, undeploy and wait.
    * When we redeploy, the timer must be automatically restored 
    */
   public void testTimerRestoredForSessionBean() throws Exception
   {
      try
      {
         super.deploy("ejb-txtimer.jar");
         
         InitialContext iniCtx = getInitialContext();
         TimerSessionHome home = (TimerSessionHome)iniCtx.lookup(TimerSessionHome.JNDI_NAME);
         TimerSession session = home.create();
         session.resetCallCount();
   
         // create a timer to expire in 2sec
         session.createTimer(2000, 0, new SimpleInfo("NonScoped"));
            
         // the timer shouldn't have expired yet
         assertEquals("unexpected call count", 0, session.getGlobalCallCount());
         
         // undeploy, the timer must have been persisted
         super.undeploy("ejb-txtimer.jar");
         
         // wait enough time, so the timer expires while "off-line"
         sleep(3000);
         
         // redeploy
         super.deploy("ejb-txtimer.jar");
         
         // just to be safe, give the timer thread a chance to run
         sleep(1000);
         
         // the timer must have expired!
         assertEquals("unexpected call count", 1, session.getGlobalCallCount());
      }
      finally
      {
         // cleanup
         super.undeploy("ejb-txtimer.jar");
      }
   }
}

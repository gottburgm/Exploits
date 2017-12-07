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
package org.jboss.test.ejb3.jbpapp4681.unit;

import java.util.Date;

import junit.framework.Assert;
import junit.framework.Test;

import org.jboss.logging.Logger;
import org.jboss.test.JBossTestCase;
import org.jboss.test.ejb3.jbpapp4681.SimpleTimer;
import org.jboss.test.ejb3.jbpapp4681.TimerSLSB;

/**
 * Tests that timers that are restored after a redeployment of the applications,
 * fire at the right time.
 * 
 * @see JBPAPP-4681 https://jira.jboss.org/browse/JBPAPP-4681
 *
 * @author Jaikiran Pai
 * @version $Revision: $
 */
public class TimerServiceRestoreTestCase extends JBossTestCase
{

   private static Logger logger = Logger.getLogger(TimerServiceRestoreTestCase.class);
   
   private static final String DEPLOYMENT_NAME = "jbpapp4681.jar";
   
   public TimerServiceRestoreTestCase(String name)
   {
      super(name);
   }
   
   /**
    * 
    * @return
    * @throws Exception
    */
   public static Test suite() throws Exception
   {
      return getDeploySetup(TimerServiceRestoreTestCase.class, DEPLOYMENT_NAME);
   }
   
   /**
    * Tests that a periodic timer is restored and a timeout is fired at the right time
    * on redeploy of the application
    * 
    * @throws Exception
    */
   public void testTimerRestoreForPeriodicTimer() throws Exception
   {
      // make sure the deployment was deployed successfully
      serverFound();

      SimpleTimer bean = (SimpleTimer) this.getInitialContext().lookup(TimerSLSB.JNDI_NAME);
      // cancel all existing timers
      bean.cancelAllTimers();
      
      Date now = new Date();
      long TEN_SECONDS = 10000;
      String testName = "PeriodicTimer";
      bean.createTimer(now, TEN_SECONDS, testName);
      
      // wait for the intial timeout to occur
      logger.info("Sleeping for 1 seconds for the timeout to happen");
      Thread.sleep(1000);
      
      int timeoutCount = bean.getTimeoutCount(testName);
      Assert.assertEquals("Unexpected initial timeout count", 1, timeoutCount);
      
      // redeploy the app
      this.redeploy(DEPLOYMENT_NAME);
      
      // the time from the initial timeout and the redeployment shouldn't be 10 seconds.
      // so the timeout count returned from the bean, immediately after redeployment, must
      // be 0.
      bean = (SimpleTimer) this.getInitialContext().lookup(TimerSLSB.JNDI_NAME);
      int timeoutCountImmediatelyAfterRedeploy = bean.getTimeoutCount(testName);
      Assert.assertEquals("Unexpected timeout count immediately after redeploy", 0, timeoutCountImmediatelyAfterRedeploy);
      
      // now wait for a few more seconds for the next timeout to occur (the first one after redeploy)
      logger.info("Sleeping for 10 seconds (after redeploy) for the timeout to happen");
      Thread.sleep(TEN_SECONDS);
      
      int finalTimeoutCount = bean.getTimeoutCount(testName);
      Assert.assertEquals("Unexpected final timeout count after redeploy", 1, finalTimeoutCount);
   }
   
   /**
    * Tests that a single action timer is restored and a timeout is fired at the right time
    * on redeploy of the application
    * 
    * @throws Exception
    */
   public void testTimerRestoreForSingleActionTimer() throws Exception
   {
      // make sure the deployment was deployed successfully
      serverFound();
      
      SimpleTimer bean = (SimpleTimer) this.getInitialContext().lookup(TimerSLSB.JNDI_NAME);
      
      // cancel all existing timers
      bean.cancelAllTimers();
      
      long TEN_SECONDS = 10000;
      Date tenSecondsFromNow = new Date(System.currentTimeMillis() + TEN_SECONDS);
      String testName = "SingleActionTimer";
      bean.createTimer(tenSecondsFromNow, testName);
      
      // no timeout should occur, before 10 seconds from now 
      int timeoutCount = bean.getTimeoutCount(testName);
      Assert.assertEquals("Unexpected initial timeout count", 0, timeoutCount);
      
      // redeploy the app
      this.redeploy(DEPLOYMENT_NAME);
      
      // the time from the initial timeout and the redeployment shouldn't be 10 seconds.
      // so the timeout count returned from the bean, immediately after redeployment, must
      // be 0.
      bean = (SimpleTimer) this.getInitialContext().lookup(TimerSLSB.JNDI_NAME);
      int timeoutCountImmediatelyAfterRedeploy = bean.getTimeoutCount(testName);
      Assert.assertEquals("Unexpected timeout count immediately after redeploy", 0, timeoutCountImmediatelyAfterRedeploy);
      
      // now wait for a few more seconds for the next timeout to occur (the first one after redeploy)
      logger.info("Sleeping for 10 seconds (after redeploy) for the timeout to happen");
      Thread.sleep(TEN_SECONDS);
      
      int finalTimeoutCount = bean.getTimeoutCount(testName);
      Assert.assertEquals("Unexpected final timeout count after redeploy", 1, finalTimeoutCount);
   }

}

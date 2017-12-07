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
package org.jboss.test.timer.ejb;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.ejb.EJBException;
import javax.ejb.NoSuchObjectLocalException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.ejb.TimedObject;
import javax.ejb.Timer;
import javax.ejb.TimerHandle;
import javax.ejb.TimerService;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;

import org.jboss.logging.Logger;
import org.jboss.test.timer.interfaces.TimerSLSB;
import org.jboss.ejb.txtimer.FixedDelayRetryPolicyMBean;

/**
 * Stateless Session Bean Timer Test
 *
 * @ejb:bean name="test/timer/TimerSLSB"
 *           display-name="Timer in Stateless Session Bean"
 *           type="Stateless"
 *           transaction-type="Container"
 *           view-type="remote"
 *           jndi-name="ejb/test/timer/TimerSLSB"
 *
 * @ejb:transaction type="Required"
 * @author Thomas Diesler
 * @author Scott.Stark@jboss.org
 * @version $Revision: 107174 $
 **/
public class TimerSLSBean
   implements SessionBean, TimedObject
{
   // -------------------------------------------------------------------------
   // Static
   // -------------------------------------------------------------------------
   private static HashMap timeoutCounts = new HashMap();
   private static Logger log = Logger.getLogger(TimerSLSBean.class);
   private static final String TIMER_NAME_KEY = "TimerNameKey";

   // -------------------------------------------------------------------------
   // Members
   // -------------------------------------------------------------------------
   private SessionContext context;

   // -------------------------------------------------------------------------
   // Methods
   // -------------------------------------------------------------------------

  /**
    * Start a single timer (if not already set) with the start date plus the period
    * Uses the string &quot;TimerSLSBean.startSingleTimer&quot; as the timer info data.
    *
    * @param pPeriod Time that will elapse between now and the timed event in milliseconds
    *
    * @ejb:interface-method view-type="remote"
    **/
   public void startSingleTimer(String timerName, long pPeriod)
   {
      this.startSingleTimer(timerName, pPeriod,new HashMap());
   }

    /**
    * Start a single timer (if not already set) with the start date plus the period and specified info.
    *
    * @param pPeriod Time that will elapse between now and the timed event in milliseconds
    * @param info an object to be used as the info for the timer.
    *
    * @ejb:interface-method view-type="remote"
    **/
   public void startSingleTimer(String timerName, long pPeriod, HashMap info)
   {
      log.info("TimerSLSBean.startSingleTimer(), try to get a Timer Service from the Session Context");
      TimerService ts = context.getTimerService();
      long exp = System.currentTimeMillis() + pPeriod;
      if (info == null)
      {
         info = new HashMap();
      }
      info.put(TIMER_NAME_KEY, timerName);
      Timer timer = ts.createTimer(new Date(exp), info);
      log.info("TimerSLSBean.startSingleTimer(), create a timer: "+timer);
   }

   /**
    * Start a timer (if not already set) with the start date plus the period
    * and an interval of the given period
    * Uses the string &quot;TimerSLSBean.startTimer&quot; as the timer info data.
    *
    * @param pPeriod Time that will elapse between two events in milliseconds
    *
    * @ejb:interface-method view-type="remote"
    **/
   public void startTimer(String timerName, long pPeriod)
   {
      this.startTimer(timerName, pPeriod, new HashMap());
   }

     /**
    * Start a timer (if not already set) with the start date plus the period
    * and an interval of the given period
    *
    * @param pPeriod Time that will elapse between two events in milliseconds
    * @param info an object to be used as the info for the timer.
    *
    * @ejb:interface-method view-type="remote"
    **/
   public void startTimer(String timerName, long pPeriod, HashMap info)
   {
      log.info("TimerSLSBean.startTimer(), try to get a Timer Service from the Session Context");
      TimerService ts = context.getTimerService();
      long exp = System.currentTimeMillis() + pPeriod;
      if (info == null)
      {
         info = new HashMap();
      }
      info.put(TIMER_NAME_KEY, timerName);
      Timer timer = ts.createTimer(new Date(exp), pPeriod, info);
      log.info("TimerSLSBean.startTimer(), create a timer: "+timer);
   }

   /**
    * @ejb:interface-method view-type="remote"
    **/
   public void stopTimer(String timerName)
   {
      Timer timer = getTimer(timerName);
      timer.cancel();
      log.info("TimerSLSBean.stopTimer(), create a timer: "+timer);
      synchronized( TimerSLSBean.class )
      {
         timeoutCounts.remove(timerName);
      }
   }

   /**
    * @ejb:interface-method view-type="remote"
    **/
   public int getTimeoutCount(String timerName)
   {
      Integer count = null;
      try
      {
         count = (Integer) timeoutCounts.get(timerName);
      }
      catch(NoSuchObjectLocalException e)
      {
         // Expected if the timer has been stopped
      }
      log.info("TimerSLSBean.getTimeoutCount(): " + count);
      return count !=  null ? count.intValue() : 0;
   }

   /**
    * @return Date of the next timed event
    *
    * @ejb:interface-method view-type="remote"
    **/
   public Date getNextTimeout(String timerName)
   {
      Timer timer = getTimer(timerName);
      return timer.getNextTimeout();
   }

   /**
    * @return Time remaining until next timed event in milliseconds
    *
    * @ejb:interface-method view-type="remote"
    **/
   public long getTimeRemaining(String timerName)
   {
      Timer timer = getTimer(timerName);
      return timer.getTimeRemaining();
   }

   /**
    * @return User object of the timer
    *
    * @ejb:interface-method view-type="remote"
    **/
   public Object getInfo(String timerName)
   {
      Timer timer = getTimer(timerName);
      Serializable info = timer.getInfo();
      // this shouldn't happen, because we always are dealing with HashMap in this
      // testcase
      if (!(info instanceof HashMap))
      {
         // just return the info, and let the testcase handle it
         return info;
      }
      // remove the key/value pair that this bean had inserted
      // so that the testcase is remains unaware of it
      ((HashMap)info).remove(TIMER_NAME_KEY);
      return info;
   }

   /**
    * Create the Session Bean
    *
    * @ejb:create-method view-type="both"
    **/
   public void ejbCreate()
   {
      log.info("TimerSLSBean.ejbCreate()");
   }

   public void ejbTimeout(Timer timer)
   {
      Integer count = null;
      String timerName = null; 
      synchronized( TimerSLSBean.class )
      {
         log.debug("ejbTimeout(): Timer State:" + timer);
         timerName = this.getTimerName(timer);
         count = (Integer) timeoutCounts.get(timerName);
         if( count == null )
            count = new Integer(1);
         else
            count = new Integer(1 + count.intValue());
         timeoutCounts.put(timerName, count);
         log.info("ejbTimeout(): count for timerName " + timerName + " is " + count);
      }

      log.info("ejbTimeout(), timer: " + timer+", name: "+timerName+", count: "+count);

      Object info = timer.getInfo();
      if(info instanceof Map) {
         Map mInfo = ((Map)info);
         Integer failCount = (Integer) mInfo.get(TimerSLSB.INFO_EXEC_FAIL_COUNT);
         Integer taskTime = (Integer) mInfo.get(TimerSLSB.INFO_TASK_RUNTIME);

         // If the timer is supposed to fail (testing the retry mechanism)
         // then we simply rollback the trans. Note this will still increase
         // the timeoutCounts which is what we want.
         if(failCount != null && count.compareTo(failCount) <= 0) {
            log.info("ejbTimeout(): Failing timeout because '" + TimerSLSB.INFO_EXEC_FAIL_COUNT
                  + "' is set to " + failCount + " and count is " + count);
            context.setRollbackOnly();
            return;
         }

         // Make method simulate a long running task
         // This is used to test the case in JBAS-1926
         if(taskTime != null) {
            try
            {
               log.info("ejbTimeout(): Simulating long task ("+ taskTime +"ms)");
               Thread.sleep(taskTime.intValue());
            }
            catch (InterruptedException e) {}
         }
      }
   }

   /**
    * Describes the instance and its content for debugging purpose
    *
    * @return Debugging information about the instance and its content
    **/
   public String toString()
   {
      return "TimerSLSBean [ " + " ]";
   }

   // -------------------------------------------------------------------------
   // Framework Callbacks
   // -------------------------------------------------------------------------

   public void setSessionContext(SessionContext aContext)
   {
      context = aContext;
   }

   public void ejbActivate()
   {
   }

   public void ejbPassivate()
   {
   }

   public void ejbRemove()
   {
   }

   private String getTimerName(Timer timer)
      throws EJBException
   {
      Serializable info = timer.getInfo();
      if (info != null && info instanceof HashMap)
      {
         String timerName = (String) ((HashMap) info).get(TIMER_NAME_KEY);
         if (timerName != null)
         {
            return timerName;
         }
      }
      throw new EJBException("Could not find internal timerName from timer " + timer);
   }
   
   private Timer getTimer(String timerName)
      throws NoSuchObjectLocalException, EJBException
   {
      TimerService ts = context.getTimerService();
      Collection timers = ts.getTimers();
      Iterator timersIterator = timers.iterator();
      while (timersIterator.hasNext())
      {
         Timer timer = (Timer) timersIterator.next();
         Serializable info = timer.getInfo();
         if (info != null && info instanceof HashMap)
         {
            String name = (String) ((HashMap) info).get(TIMER_NAME_KEY);
            if (timerName.equals(name))
            {
               return timer;
            }
         }
      }
      throw new NoSuchObjectLocalException("Timer with name " + timerName + " isn't available");
   }

   /**
    * Returns the value from the RetryPolicyMBean. This is used by unit tests to help determine timing
    * for some of the tests, specifically, those that test the fix for JBAS-1926.
    */
   public long getRetryTimeoutPeriod() {
      List lServers = MBeanServerFactory.findMBeanServer( null );
      MBeanServer lServer = (MBeanServer) lServers.get( 0 );
      try
      {
         Long val = (Long) lServer.getAttribute(FixedDelayRetryPolicyMBean.OBJECT_NAME, "Delay");
         return val.longValue();
      }
      catch (Exception e)
      {
         log.error(e);
         e.printStackTrace();

         return -1;
      }
   }
}

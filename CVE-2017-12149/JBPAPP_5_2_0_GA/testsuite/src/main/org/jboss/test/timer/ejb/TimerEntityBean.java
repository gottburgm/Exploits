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

import java.util.Date;
import javax.ejb.EJBException;
import javax.ejb.EntityBean;
import javax.ejb.EntityContext;
import javax.ejb.TimedObject;
import javax.ejb.Timer;
import javax.ejb.TimerHandle;
import javax.ejb.TimerService;

import org.jboss.logging.Logger;

/**
 * Entity Bean Timer Test
 *
 * @ejb:bean name="test/timer/TimerEntity"
 *           display-name="Timer in Entity Bean"
 *           type="BMP"
 *           transaction-type="Container"
 *           view-type="remote"
 *           jndi-name="ejb/test/timer/TimerEntity"
 *
 * @ejb:pk class="java.lang.Integer"
 *
 * @ejb:transaction type="Required"
 * @author Thomas Diesler
 * @author Scott.Stark@jboss.org
 * @version $Revision: 113470 $
 **/
public class TimerEntityBean
        implements EntityBean, TimedObject
{

   // -------------------------------------------------------------------------
   // Members
   // -------------------------------------------------------------------------

   private EntityContext mContext;
   //This is wrong on sooo many levels, however switch to CMP and accessing timer from TimerService 
   //requires changes to more than one test...
   //Fields are defined static. This is because persistance0type is set to "Bean" and there is no persist code.
   //Since container has a pool of ejb intances to handle calls, there is no way to be sure that one
   //instance is used to serve teest, hence random failures if container use different instances to handle
   //calls(yeah, the tests are faulty)
   private static Timer sTimer;
   private static int sCounter;

   private Logger mLog = Logger.getLogger(this.getClass().getName());

   // -------------------------------------------------------------------------
   // Methods
   // -------------------------------------------------------------------------

   /**
    * Start a single timer (if not already set) with the start date plus the period
    *
    * @param pPeriod Time that will elapse between now and the timed event in milliseconds
    *
    * @ejb:interface-method view-type="remote"
    **/
   public void startSingleTimer(long pPeriod)
   {
      mLog.info("TimerEntityBean.startSingleTimer(), try to get a Timer Service from the Entity Context");
      TimerService lService = mContext.getTimerService();
      mLog.info("TimerEntityBean.startSingleTimer(), create a timer if not already done");
      if (sTimer == null)
      {
         sTimer = lService.createTimer(new Date(new Date().getTime() + pPeriod), "TimerEntityBean");
         sCounter = 0;
      }
      else
      {
         throw new EJBException("Timer is already set");
      }
   }

   /**
    * Start a timer (if not already set) with the start date plus the period
    * and an interval of the given period
    *
    * @param pPeriod Time that will elapse between two events in milliseconds
    *
    * @ejb:interface-method view-type="remote"
    **/
   public void startTimer(long pPeriod)
   {
      mLog.info("TimerEntityBean.startTimer(), try to get a Timer Service from the Entity Context");
      TimerService lService = mContext.getTimerService();
      mLog.info("TimerEntityBean.startTimer(), create a timer if not already done");
      if (sTimer == null)
      {
         sTimer = lService.createTimer(new Date(new Date().getTime() + pPeriod), pPeriod, "TimerEntityBean");
         sCounter = 0;
      }
      else
      {
         throw new EJBException("Timer is already set");
      }
   }

   /**
    * @ejb:interface-method view-type="remote"
    **/
   public void stopTimer()
   {
      try
      {
         if (sTimer != null)
         {
            sTimer.cancel();
         }
         else
         {
            throw new EJBException("Timer is not available");
         }
      }
      finally
      {
         sTimer = null;
      }
   }

   /**
    * @ejb:interface-method view-type="remote"
    **/
   public int getTimeoutCount()
   {
      mLog.info("TimerEntityBean.getTimeoutCount(): " + sCounter);
      return sCounter;
   }

   /**
    * @return Date of the next timed event
    *
    * @ejb:interface-method view-type="remote"
    **/
   public Date getNextTimeout()
   {
      if (sTimer != null)
      {
         return sTimer.getNextTimeout();
      }
      else
      {
         return null;
      }
   }

   /**
    * @return Time remaining until next timed event in milliseconds
    *
    * @ejb:interface-method view-type="remote"
    **/
   public long getTimeRemaining()
   {
      if (sTimer != null)
      {
         return sTimer.getTimeRemaining();
      }
      else
      {
         return -1L;
      }
   }

   /**
    * @return User object of the timer
    *
    * @ejb:interface-method view-type="remote"
    **/
   public Object getInfo()
   {
      if (sTimer != null)
      {
         return (Object) sTimer.getInfo();
      }
      else
      {
         return null;
      }
   }

   /**
    * @return Date of the next timed event
    *
    * @ejb:interface-method view-type="remote"
    **/
   public TimerHandle getTimerHandle()
   {
      if (sTimer != null)
      {
         return sTimer.getHandle();
      }
      else
      {
         return null;
      }
   }

   public void ejbTimeout(Timer pTimer)
   {
      mLog.debug("ejbTimeout(), timer: " + pTimer);
      sCounter++;
   }

   /**
    * Describes the instance and its content for debugging purpose
    *
    * @return Debugging information about the instance and its content
    **/
   public String toString()
   {
      return "TimerEntityBean [ " + " ]";
   }

   // -------------------------------------------------------------------------
   // Framework Callbacks
   // -------------------------------------------------------------------------

   /**
    * @ejb:create-method view-type="both"
    **/
   public Integer ejbCreate(Integer pk)
   {
      mLog.info("ejbCreate(" + pk + ")");
      return pk;
   }

   public void ejbPostCreate(Integer pk)
   {
      mLog.info("ejbPostCreate(" + pk + ")");
   }

   /**
    * @ejb:finder view-type="both"
    **/
   public Integer ejbFindByPrimaryKey(Integer pk)
   {
      mLog.info("ejbFindByPrimaryKey(" + pk + ")");
      return pk;
   }

   public void ejbActivate()
   {
      mLog.info("ejbActivate");
   }

   public void ejbLoad()
   {
      mLog.info("ejbLoad");
   }

   public void ejbPassivate()
   {
      mLog.info("ejbPassivate");
   }

   public void ejbRemove()
   {
      mLog.info("ejbRemove");
      //cleanup
      sCounter = 0;
      sTimer= null;
   }

   public void ejbStore()
   {
      mLog.info("ejbStore");
   }

   public void setEntityContext(EntityContext ctx)
   {
      mLog.info("setEntityContext");
      this.mContext = ctx;
   }

   public void unsetEntityContext()
   {
      mLog.info("unsetEntityContext");
   }
}

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
import java.util.HashMap;

import javax.ejb.EJBException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.ejb.Timer;
import javax.ejb.TimerService;

/**
 * A session bean that does not implement the required TimedObject interface
 * to test that calling getTimerService fails.
 *
 * @author Scott.Stark@jboss.org
 * @version $Revision: 107174 $
 **/
public class NoTimedObjectBean
   implements SessionBean
{
   private SessionContext context;

   public void startSingleTimer(String timerName, long pPeriod)
   {
      startSingleTimer(timerName, pPeriod, new HashMap());
   }

   public void startSingleTimer(String timerName, long pPeriod, HashMap info)
   {
      TimerService ts = context.getTimerService();
      throw new EJBException("startSingleTimer.getTimerService should have failed");
   }

   public void startTimer(String timerName, long pPeriod)
   {
      TimerService ts = context.getTimerService();
      throw new EJBException("startSingleTimer.getTimerService should have failed");
   }

   public void startTimer(String timerName, long pPeriod, HashMap info)
   {
      TimerService ts = context.getTimerService();
      throw new EJBException("startSingleTimer.getTimerService should have failed");
   }

   public void stopTimer(String timerName)
   {
      TimerService ts = context.getTimerService();
      throw new EJBException("startSingleTimer.getTimerService should have failed");
   }

   public int getTimeoutCount(String timerName)
   {
      return 0;
   }

   public Date getNextTimeout(String timerName)
   {
      return null;
   }

   public long getTimeRemaining(String timerName)
   {
      return 0;
   }

   public Object getInfo(String timerName)
   {
      return null;
   }

   public long getRetryTimeoutPeriod()
   {
      return 0;
   }

   public void ejbCreate()
   {
   }

   public void ejbTimeout(Timer timer)
   {
   }

   public void setSessionContext(SessionContext context)
   {
      this.context = context;
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

}

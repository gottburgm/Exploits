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

import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.ejb.TimedObject;
import javax.ejb.Timer;

import org.jboss.logging.Logger;

/**
 * Stateful Session Bean Timer Test
 * @author Thomas Diesler
 * @author Scott.Stark@jboss.org
 * @version $Revision: 81036 $
 * @ejb:bean name="test/timer/TimerSFSB" display-name="Timer in Stateful Session
 * Bean" type="Stateful" transaction-type="Container" view-type="remote"
 * jndi-name="ejb/test/timer/TimerSFSB"
 * @ejb:transaction type="Required"
 */
public class TimerSFSBean
   implements SessionBean, TimedObject
{
   // -------------------------------------------------------------------------
   // Static
   // -------------------------------------------------------------------------
   private static Logger log = Logger.getLogger(TimerSFSBean.class);
   
   // -------------------------------------------------------------------------
   // Members 
   // -------------------------------------------------------------------------
   
   private SessionContext mContext;
   
   // -------------------------------------------------------------------------
   // Methods
   // -------------------------------------------------------------------------  
   
   /**
    * @ejb:interface-method view-type="both"
    */
   public void checkTimerService()
   {
      log.info("TimerSFSBean.checkTimerService(), try to get a Timer Service from the Session Context");
      mContext.getTimerService();
   }

   /**
    * Create the Session Bean
    * @ejb:create-method view-type="both"
    */
   public void ejbCreate()
   {
      log.info("TimerSFSBean.ejbCreate()");
   }

   public void ejbTimeout(Timer pTimer)
   {
      log.info("ejbTimeout(), timer: " + pTimer);
   }

   /**
    * Describes the instance and its content for debugging purpose
    * @return Debugging information about the instance and its content
    */
   public String toString()
   {
      return "TimerSFSBean [ " + " ]";
   }
   
   // -------------------------------------------------------------------------
   // Framework Callbacks
   // -------------------------------------------------------------------------
   
   public void setSessionContext(SessionContext aContext)
   {
      mContext = aContext;
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

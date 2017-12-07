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
package org.jboss.test.txtimer.ejb;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.Collection;
import java.util.Iterator;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.ejb.TimedObject;
import javax.ejb.Timer;

import org.jboss.logging.Logger;

/**
 * TxTimer test bean
 * 
 * @author Dimitris.Andreadis@jboss.org
 * @version $Revision: 81036 $
 */
public class TimerTestBean implements SessionBean, TimedObject
{
   private Logger log = Logger.getLogger(TimerTestBean.class.getName());   
   private SessionContext context;

   public TimerTestBean()
   {
      // empty
   }
   
   public void setSessionContext(SessionContext newContext) throws EJBException
   {
      context = newContext;
   }

   public void ejbCreate() throws CreateException {}   
   public void ejbRemove() throws EJBException, RemoteException {}
   public void ejbActivate() throws EJBException, RemoteException {}
   public void ejbPassivate() throws EJBException, RemoteException {}
    
   public void ejbTimeout(Timer timer)
   {
      log.info("ejbTimeout: " + timer);               
   }
   
   // Business Interface --------------------------------------------
   
   /**
    * @ejb.interface-method
    * @ejb.transaction type = "Required"
    * @throws EJBException Thrown if method fails due to system-level error.
    */
   public void startTimerInTxRequired() throws EJBException
   {
      startTimer("Required");
   }
    
   /**
    * @ejb.interface-method
    * @ejb.transaction type = "RequiresNew"
    * @throws EJBException Thrown if method fails due to system-level error.
    */
   public void startTimerInTxRequiresNew() throws EJBException
   {
      startTimer("RequiresNew");
   }
   
   /**
    * @ejb.interface-method
    * @ejb.transaction type = "Never"
    * @throws EJBException Thrown if method fails due to system-level error.
    */
   public void startTimerInTxNever() throws EJBException
   {
      startTimer("Never");
   }
    
   /**
    * @ejb.interface-method
    * @ejb.transaction type = "NotSupported"
    * @throws EJBException Thrown if method fails due to system-level error.
    */
   public void startTimerInTxNotSupported() throws EJBException
   {
      startTimer("NotSupported");
   }

   /**
    * @ejb.interface-method 
    */
   public int listAllTimers()
   {
      Collection timers = context.getTimerService().getTimers();
      String s = "Timers: ";
      for (Iterator it = timers.iterator();it.hasNext();)
      {
         Timer t = (Timer)it.next();            
         s = s + t.toString() + " ";
         try
         {
            s += t.getInfo();
         }
         catch (Exception ignore)
         {
            // empty
         }
         s += "\n";
       }
       log.info(s);
       
       return timers.size();
   }

   /**
    * @ejb.interface-method
    * @ejb.transaction type = "Required"
    * @throws EJBException Thrown if method fails due to system-level error.
    */
   public void cancelTimerInTxRequired() throws EJBException
   {
      cancelTimers();
   }
   
   /**
    * @ejb.interface-method
    * @ejb.transaction type = "RequiresNew"
    * @throws EJBException Thrown if method fails due to system-level error.
    */
   public void cancelTimerInTxRequiresNew() throws EJBException
   {
       cancelTimers();
   }
   
   /**
    * @ejb.interface-method
    * @ejb.transaction type = "Never"
    * @throws EJBException Thrown if method fails due to system-level error.
    */
   public void cancelTimerInTxNever() throws EJBException
   {
       cancelTimers();
   }
   
   /**
    * @ejb.interface-method
    * @ejb.transaction type = "NotSupported"
    * @throws EJBException Thrown if method fails due to system-level error.
    */
   public void cancelTimerInTxNotSupported() throws EJBException
   {
       cancelTimers();
   }
   
   // Private -------------------------------------------------------
   
   private void startTimer(Serializable info)
   {
      log.info("Starting timer, info=" + info);
      context.getTimerService().createTimer(10000, info);
   }
   
   private void cancelTimers()
   {
      Collection timers = context.getTimerService().getTimers();
      for (Iterator it = timers.iterator(); it.hasNext(); )
      {
         Timer t = (Timer)it.next();
         log.info("Cancelling timer " + t + " " + t.getInfo());
         t.cancel();
         log.info("Timer is now " + t);
      }   
   }

}

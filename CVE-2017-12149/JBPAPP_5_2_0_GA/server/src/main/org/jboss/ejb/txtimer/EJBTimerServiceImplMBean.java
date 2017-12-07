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
package org.jboss.ejb.txtimer;

import org.jboss.mx.util.ObjectNameFactory;
import org.jboss.system.ServiceMBean;
import org.jboss.tm.TransactionManagerFactory;

import javax.management.ObjectName;

/**
 * EJBTimerServiceImpl MBean interface.
 * 
 * @author Thomas.Diesler@jboss.org
 * @author Dimitris.Andreadis@jboss.org
 * @version $Revision: 112630 $
 * @since 07-Apr-2004
 */
public interface EJBTimerServiceImplMBean extends ServiceMBean, EJBTimerService
{
   /** The default object name */
   ObjectName OBJECT_NAME = ObjectNameFactory.create("jboss.ejb:service=EJBTimerService");

   // Attributes ----------------------------------------------------
   
   /** The object name of the retry policy */
   void setRetryPolicy(ObjectName retryPolicyName);
   ObjectName getRetryPolicy();

   /** The object name of the persistence policy */
   void setPersistencePolicy(ObjectName persistencePolicyName);   
   ObjectName getPersistencePolicy();

   /** The TimerIdGenerator class name */
   void setTimerIdGeneratorClassName(String timerIdGeneratorClassName);   
   String getTimerIdGeneratorClassName();

   /** The TimedObjectInvoker class name */
   void setTimedObjectInvokerClassName(String timedObjectInvokerClassName);   
   String getTimedObjectInvokerClassName();

   /** The TransactionManagerFactory */
   void setTransactionManagerFactory(TransactionManagerFactory factory);

   Integer getThreadPoolSize();
   /** Sets the thread pool size for running timeout callbacks, takes effect on restart of the timer service */
   void setThreadPoolSize(Integer threadPoolSize);

   // Operations ----------------------------------------------------
   
   /**
    * List the timers registered with all TimerService objects
    */
   String listTimers();

}

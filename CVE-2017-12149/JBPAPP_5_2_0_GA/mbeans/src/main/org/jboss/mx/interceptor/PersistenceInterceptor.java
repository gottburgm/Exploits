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
package org.jboss.mx.interceptor;

import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import javax.management.Descriptor;
import javax.management.PersistentMBean;
import javax.management.MBeanException;
import javax.management.InstanceNotFoundException;
import javax.management.modelmbean.ModelMBeanInfo;

import org.jboss.mx.modelmbean.ModelMBeanConstants;
import org.jboss.mx.service.ServiceConstants;
import org.jboss.mx.server.Invocation;
import org.jboss.mx.server.MBeanInvoker;

/** A peristence interceptor that uses the java.util.Timer framework for the
 * scheculed peristence policies.
 *
 * @see javax.management.PersistentMBean
 *
 * @author  <a href="mailto:juha@jboss.org">Juha Lindfors</a>.
 * @author  Scott.Stark@jboss.org
 * @author  <a href="mailto:dimitris@jboss.org">Dimitris Andreadis</a>.
 * 
 * @version $Revision: 81026 $
 */
public class PersistenceInterceptor
   extends AbstractInterceptor
   implements ModelMBeanConstants, ServiceConstants
{
   /** The HashMap<name, policy> of attribute level policies */
   private HashMap attrPersistencePolicies = new HashMap();
   /** The HashMap<name, PersistenceTimerTask> of scheduled peristence */
   private HashMap timerTaskMap = new HashMap();
   /** The bean level peristence policy */
   private String mbeanPersistencePolicy;
   /** The PersistentMBean load/store callback interface */
   private PersistentMBean callback;

   public PersistenceInterceptor()
   {
      super("Default Persistence Interceptor");
   }

   // Public --------------------------------------------------------
   public Object invoke(Invocation invocation) throws Throwable
   {
      if( callback == null )
      {
         lazyInit(invocation);
      }

      Object returnValue = invocation.nextInterceptor().invoke(invocation);
      String type = invocation.getType();
      if (type != Invocation.OP_SETATTRIBUTE )
         return returnValue;

      String attrName = invocation.getName();
      String policy = (String)attrPersistencePolicies.get(attrName);
      if (policy == null)
         policy = mbeanPersistencePolicy;

      if (policy.equalsIgnoreCase(PP_ON_UPDATE) == true)
      {
         MBeanInvoker invoker = invocation.getInvoker();
         Descriptor attrDesc = invocation.getDescriptor();
         invoker.updateAttributeInfo(attrDesc);
         callback.store();
      }
      else if(policy.equalsIgnoreCase(PP_NO_MORE_OFTEN_THAN) == true)
      {
         PersistenceTimerTask task = (PersistenceTimerTask) timerTaskMap.get(attrName);
         if( task != null )
            task.setHasUpdated(true);
      }
      return returnValue;
   }

   /**
    * 
    * @param invocation
    */ 
   private synchronized void lazyInit(Invocation invocation) throws MBeanException
   {
      // This requires the invoker to implement PersistentMBean
      MBeanInvoker invoker = invocation.getInvoker();
      callback = (PersistentMBean) invocation.getInvoker();
      ModelMBeanInfo info = (ModelMBeanInfo) invoker.getMetaData();
      Descriptor mbeanDesc = info.getMBeanDescriptor();

      String policy = (String) mbeanDesc.getFieldValue(PERSIST_POLICY);
      String persistPeriod = (String)mbeanDesc.getFieldValue(PERSIST_PERIOD);

      mbeanPersistencePolicy = PP_NEVER;      
      if (policy != null)
      {
         mbeanPersistencePolicy = policy;
         if (mbeanPersistencePolicy.equalsIgnoreCase(PP_ON_TIMER) ||
             mbeanPersistencePolicy.equalsIgnoreCase(PP_NO_MORE_OFTEN_THAN))
         {
            boolean isNoMoreOftenThan = mbeanPersistencePolicy.equalsIgnoreCase(PP_NO_MORE_OFTEN_THAN);
            schedulePersistenceNotifications(Long.parseLong(persistPeriod), MBEAN_DESCRIPTOR, isNoMoreOftenThan);
         }
      }      
      
      Descriptor[] attrDescs = info.getDescriptors(ATTRIBUTE_DESCRIPTOR);
      for (int i = 0; i < attrDescs.length; ++i)
      {
         policy = (String) attrDescs[i].getFieldValue(PERSIST_POLICY);
         persistPeriod = (String)attrDescs[i].getFieldValue(PERSIST_PERIOD);

         if (policy != null)
         {
            String name = (String) attrDescs[i].getFieldValue(NAME);
            attrPersistencePolicies.put(name, policy);

            if(policy.equalsIgnoreCase(PP_ON_TIMER) ||
               policy.equalsIgnoreCase(PP_NO_MORE_OFTEN_THAN))
            {
               boolean isNoMoreOftenThan = policy.equalsIgnoreCase(PP_NO_MORE_OFTEN_THAN);
               schedulePersistenceNotifications(Long.parseLong(persistPeriod), name, isNoMoreOftenThan);
            }
         }
      }
   }

   private void schedulePersistenceNotifications(long persistPeriod, String name,
      boolean isNoMoreOftenThan)
   {
      // @todo: unschedule on unregistration/descriptor field change
      PersistenceTimerTask task = new PersistenceTimerTask(name, isNoMoreOftenThan);
      Timer timer = new Timer(true);
      timer.scheduleAtFixedRate(task, 0, persistPeriod);
      timerTaskMap.put(name, task);
   }

   // Inner classes -------------------------------------------------
   private class PersistenceTimerTask extends TimerTask
   {
      boolean noMoreOftenThan;
      boolean hasUpdated;
      String name;
      PersistenceTimerTask(String name, boolean noMoreOftenThan)
      {
         this.name = name;
         this.noMoreOftenThan = noMoreOftenThan;
      }
      synchronized void setHasUpdated(boolean flag)
      {
         hasUpdated = flag;
      }
      public void run()
      {
         try
         {
            // @todo: add PersistenceContext field to MBean's descriptor to
            //        relay attribute name (and possibly other) info with the
            //        persistence callback
            boolean doStore = (noMoreOftenThan == true && hasUpdated == true)
               || noMoreOftenThan == false;
            if( doStore == true )
            {
               callback.store();
               setHasUpdated(false);
            }
         }
         catch (MBeanException e)
         {
            // FIXME: log exception
         }
         catch (InstanceNotFoundException e)
         {
            // FIXME: log exception
         }
      }
   }
}

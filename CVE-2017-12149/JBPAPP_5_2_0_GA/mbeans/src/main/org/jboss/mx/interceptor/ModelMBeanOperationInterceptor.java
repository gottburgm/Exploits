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

import java.util.Arrays;

import javax.management.Descriptor;
import javax.management.InvalidAttributeValueException;
import javax.management.ObjectName;

import org.jboss.mx.modelmbean.ModelMBeanConstants;
import org.jboss.mx.server.Invocation;


/** This interceptor handles the ModelMBean operation caching
 *
 * @author  <a href="mailto:adrian@jboss.org">Adrian Brock</a>.
 * @author Scott.Stark@jboss.org
 * @version $Revision: 81026 $
 */
public class ModelMBeanOperationInterceptor
      extends AbstractInterceptor
      implements ModelMBeanConstants
{
   // Constants -----------------------------------------------------
   
   // Attributes ----------------------------------------------------
   
   private boolean trace;
   
   // Constructors --------------------------------------------------
   
   public ModelMBeanOperationInterceptor()
   {
      super("ModelMBean Operation Interceptor");
      trace = log.isTraceEnabled();
   }

   
   // Public --------------------------------------------------------
   
   public Object invoke(Invocation invocation) throws Throwable
   {
      // get the operation's descriptor
      Descriptor d = invocation.getDescriptor();
      Class clazz = invocation.getReturnTypeClass();
      
      String name = null;
      ObjectName objectName = null;
      if (trace)
      {
         if (d != null)
            name = (String) d.getFieldValue(NAME);
         objectName = invocation.getInvoker().getObjectName();
      }

      if (trace)
      {
         Object args = invocation.getArgs();
         if (args != null)
            args = Arrays.asList((Object[]) args);
         log.trace("Invoking objectName=" + objectName + " oper=" + name + " args=" + args + " desc=" + d);
      }

      long limit = CACHE_NEVER_LIMIT;
      
      if (d != null && clazz != null)
      {
         String timeLimit = (String) d.getFieldValue(CURRENCY_TIME_LIMIT);
         if (timeLimit != null)
            limit = Long.parseLong(timeLimit);

         // We are never stale
         if (limit == CACHE_ALWAYS_LIMIT)
         {
            String timeStamp = (String)d.getFieldValue(LAST_UPDATED_TIME_STAMP);
            if (timeStamp != null)
            {
               Object value = d.getFieldValue(CACHED_VALUE);
               if (trace)
                  log.trace("Always cache objectName=" + objectName + " oper=" + name + " value=" + value);
               checkAssignable("Cached value in descriptor ", clazz, value);
               return value;
            }
         }

         // is caching enabled
         if (limit != CACHE_NEVER_LIMIT)
         {
            String timeStamp = (String) d.getFieldValue(LAST_UPDATED_TIME_STAMP);
            long lastUpdate = (timeStamp == null) ? 0 : Long.parseLong(timeStamp);
           
            // if the value hasn't gone stale, return from the descriptor
            long now = System.currentTimeMillis();
            long expires = lastUpdate * 1000 + limit * 1000;
            if (now < expires)
            {
               Object value = d.getFieldValue(CACHED_VALUE);
               if (trace)
                  log.trace("Using cache objectName=" + objectName + " oper=" + name + " value=" + value + " now=" + now + " expires=" + expires);
               checkAssignable("Cached value in descriptor ", clazz, value);
               return value;
            }
            else
            {
               if (trace)
                  log.trace("Cache expired objectName=" + objectName + " oper=" + name + " now=" + now + " expires=" + expires);
               d.removeField(CACHED_VALUE);
            }
         }
         else
         {
            // Unfortunatley we have to cope with stupid users
            if (trace)
               log.trace("Removing any cached value objectName=" + objectName + " oper=" + name + " descriptor=" + d);
            d.removeField(CACHED_VALUE);
         }
      }

      // we got here means either stale value in descriptior, or no caching
      Object value = invocation.invoke();            
      if (trace)
         log.trace("Got result objectName=" + objectName + " oper=" + name + " value=" + value);
      
      // update the descriptor (unless not caching)
      if (d !=null && limit != CACHE_NEVER_LIMIT)
      {
         String timestamp = Long.toString(System.currentTimeMillis()/1000);
         if (trace)
            log.trace("Cache result objectName=" + objectName + " oper=" + name + " value=" + value + " timestamp=" + timestamp);
         d.setField(CACHED_VALUE, value);
         d.setField(LAST_UPDATED_TIME_STAMP, timestamp);
      }
      return value;
   }
   
   protected void checkAssignable(String context, Class clazz, Object value) throws InvalidAttributeValueException, ClassNotFoundException
   {
      if (value != null && clazz.isAssignableFrom(value.getClass()) == false)
         throw new InvalidAttributeValueException(context + " has class " + value.getClass() + " loaded from " + value.getClass().getClassLoader() +
            " that is not assignable to attribute class " + clazz + " loaded from " + clazz.getClassLoader());
   }
}





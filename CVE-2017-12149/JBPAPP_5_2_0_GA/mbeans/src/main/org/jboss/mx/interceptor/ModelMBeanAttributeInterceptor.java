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

import javax.management.Attribute;
import javax.management.Descriptor;
import javax.management.InvalidAttributeValueException;
import javax.management.ObjectName;

import org.jboss.logging.Logger;
import org.jboss.mx.modelmbean.ModelMBeanConstants;
import org.jboss.mx.modelmbean.ModelMBeanInvoker;
import org.jboss.mx.server.Invocation;
import org.jboss.util.UnreachableStatementException;


/** This interceptor handles the ModelMBean attribute caching and setter
 * and getter dispatches.
 *
 * @author  <a href="mailto:juha@jboss.org">Juha Lindfors</a>.
 * @author Scott.Stark@jboss.org
 * @author  <a href="mailto:dimitris@jboss.org">Dimitris Andreadis</a>.
 * @version $Revision: 81026 $
 */
public class ModelMBeanAttributeInterceptor
      extends AbstractInterceptor
      implements ModelMBeanConstants
{
   // Constants -----------------------------------------------------

   private static final Logger log = Logger.getLogger(ModelMBeanAttributeInterceptor.class);
   
   // Attributes ----------------------------------------------------
   
   private boolean trace = log.isTraceEnabled();
   
   // Constructors --------------------------------------------------
   
   public ModelMBeanAttributeInterceptor()
   {
      super("ModelMBean Attribute Interceptor");
   }

   
   // Public --------------------------------------------------------
   
   public Object invoke(Invocation invocation) throws Throwable
   {
      // get the attribute's descriptor
      Descriptor d = invocation.getDescriptor();
      Class clazz = invocation.getAttributeTypeClass();
      
      String name = null;
      ObjectName objectName = null;
      if (trace)
      {
         name = (String) d.getFieldValue(NAME);
         objectName = invocation.getInvoker().getObjectName();
      }
      
      // check the invocation access point: setAttribute()   
      if (invocation.getType().equals(Invocation.OP_SETATTRIBUTE))
      {
         // setAttribute always contains one arg
         Object value = invocation.getArgs() [0];
         if (trace)
            log.trace("Setting objectName=" + objectName + " attr=" + name + " value=" + value);
         
         checkAssignable("Set attribute ", clazz, value);
         
         // remember the old value of this attribute for AVC notification
         Object oldValue = d.getFieldValue(ATTRIBUTE_VALUE);
         if (trace)
            log.trace("Setting objectName=" + objectName + " attr=" + name + " oldValue=" + value);

         // check if the attribute maps to a setter
         String setMethod = (String) d.getFieldValue(SET_METHOD);
         if (trace)
            log.trace("Setting objectName=" + objectName + " attr=" + name + " setMethod=" + setMethod);

         if (setMethod != null)
         {  
            // if setter was found, invoke the corresponding setter operation
            invocation.invoke();
         }

         // Don't cache the value or last update when not caching
         String timeLimit = (String) d.getFieldValue(CURRENCY_TIME_LIMIT);
         long limit = (timeLimit == null) ? CACHE_NEVER_LIMIT : Long.parseLong(timeLimit);
         String timestamp = Long.toString(System.currentTimeMillis()/1000);
         
         if (limit != CACHE_NEVER_LIMIT)
         {
            if (trace)
               log.trace("Setting objectName=" + objectName + " attr=" + name + " value=" + value + " timestamp=" + timestamp);
            d.setField(CACHED_VALUE, value);
            d.setField(LAST_UPDATED_TIME_STAMP, timestamp);
         }
         
         // Always store the attribute value set, for persistence and AVC purposes mainly
         // independent of whether caching is enabled or not.
         // Note that if the resource updates its internal attribute value
         // we will not know, unless set is performed through the ModelMBean
         // interface, or the following descriptor gets updated somehow.
         d.setField(ATTRIBUTE_VALUE, value);
         d.setField(LAST_UPDATED_TIME_STAMP2, timestamp);
         
         // send AVC notification
         ModelMBeanInvoker invoker = (ModelMBeanInvoker) invocation.getInvoker();
         invoker.sendAttributeChangeNotification(
            new Attribute(invocation.getName(), oldValue),
            new Attribute(invocation.getName(), value)
         );
         return null;
      }
      else if (invocation.getType().equals(Invocation.OP_GETATTRIBUTE))
      {   
         if (trace)
            log.trace("Getting objectName=" + objectName + " attr=" + name);

         String timeLimit = (String)d.getFieldValue(CURRENCY_TIME_LIMIT);
         long limit = (timeLimit == null) ? CACHE_NEVER_LIMIT : Long.parseLong(timeLimit);

         // We are never stale
         if (limit == CACHE_ALWAYS_LIMIT)
         {
            String timeStamp = (String)d.getFieldValue(LAST_UPDATED_TIME_STAMP);
            if (timeStamp != null)
            {
               Object value = d.getFieldValue(CACHED_VALUE);
               if (trace)
                  log.trace("Always cache objectName=" + objectName + " attr=" + name + " value=" + value);
               checkAssignable("Cached value in descriptor ", clazz, value);
               return value;
            }
         }

         // is caching enabled
         if (limit != CACHE_NEVER_LIMIT)
         {
            String timeStamp = (String)d.getFieldValue(LAST_UPDATED_TIME_STAMP);
            long lastUpdate = (timeStamp == null) ? 0 : Long.parseLong(timeStamp);
        
            // if the value hasn't gone stale, return from the descriptor
            long now = System.currentTimeMillis();
            long expires = lastUpdate * 1000 + limit * 1000;
            if (now < expires)
            {
               Object value = d.getFieldValue(CACHED_VALUE);
               if (trace)
                  log.trace("Using cache objectName=" + objectName + " attr=" + name + " value=" + value + " now=" + now + " expires=" + expires);
               checkAssignable("Cached value in descriptor ", clazz, value);
               return value;
            }
            else
            {
               if (trace)
                  log.trace("Cache expired objectName=" + objectName + " attr=" + name + " now=" + now + " expires=" + expires);
               d.removeField(CACHED_VALUE);
            }
         }
         else
         {
            // Unfortunatley we have to cope with stupid users
            if (trace)
               log.trace("Removing any cached value objectName=" + objectName + " attr=" + name + " descriptor=" + d);
            d.removeField(CACHED_VALUE);
         }
         
         // get the attribute's descriptor
         String getMethod = (String)d.getFieldValue(GET_METHOD);
         if (trace)
            log.trace("Get attribute objectName=" + objectName + " attr=" + name + " getMethod=" + getMethod);
         
         if (getMethod != null)
         {
            // we got here means either stale value in descriptior, or no caching
            Object value = invocation.invoke();            
            if (trace)
               log.trace("Got attribute objectName=" + objectName + " attr=" + name + " value=" + value);
            
            // update the descriptor (unless not caching)
            if (limit != CACHE_NEVER_LIMIT)
            {
               String timestamp = Long.toString(System.currentTimeMillis()/1000);                
               if (trace)
                  log.trace("Cache attribute objectName=" + objectName + " attr=" + name + " value=" + value + " timestamp=" + timestamp);
               d.setField(CACHED_VALUE, value);
               d.setField(LAST_UPDATED_TIME_STAMP, timestamp);
            }
            return value;
         }
         else
         {
            // There is no instance accessor so check for a default value
            Object value = d.getFieldValue(DEFAULT);
            if (trace)
               log.trace("Get attribute use default objectName=" + objectName + " attr=" + name + " default=" + value);
            checkAssignable("Default value ", clazz, value);
            return value;
         }
      }
      else
         throw new UnreachableStatementException(invocation.getType());
   }
   
   protected void checkAssignable(String context, Class clazz, Object value) throws InvalidAttributeValueException, ClassNotFoundException
   {
      if (value != null && clazz.isAssignableFrom(value.getClass()) == false)
         throw new InvalidAttributeValueException(context + " has class " + value.getClass() + " loaded from " + value.getClass().getClassLoader() +
            " that is not assignable to attribute class " + clazz + " loaded from " + clazz.getClassLoader());
   }
}





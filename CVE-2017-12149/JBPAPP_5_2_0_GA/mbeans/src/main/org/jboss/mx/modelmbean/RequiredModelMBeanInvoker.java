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
package org.jboss.mx.modelmbean;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.DynamicMBean;
import javax.management.InvalidAttributeValueException;
import javax.management.MBeanException;
import javax.management.ReflectionException;
import javax.management.RuntimeMBeanException;
import javax.management.RuntimeOperationsException;
import javax.management.ServiceNotFoundException;
import javax.management.modelmbean.RequiredModelMBean;

import org.jboss.mx.server.RawDynamicInvoker;

/** An invoker that handles the 'ops' that are part of the RequiredModelMBean
 * that must be handled at that level rather than its delegate.
 * 
 * @author Scott.Stark@jboss.org
 * @version $Revison:$
 */
public class RequiredModelMBeanInvoker extends RawDynamicInvoker
{
   RequiredModelMBean mbean;

   public RequiredModelMBeanInvoker(DynamicMBean resource)
   {
      super(resource);
      mbean = (RequiredModelMBean) resource;
   }

   public Object getAttribute(String name) throws AttributeNotFoundException, MBeanException, ReflectionException
   {
      try
      {
         return super.getAttribute(name);
      }
      catch (ReflectionException e)
      {
         // Another inconsistency
         Exception ex = e.getTargetException();
         if ((ex instanceof ClassNotFoundException) == false &&
             (ex instanceof NoSuchMethodException) == false)
         {
            log.debug("Rewrapping reflection exception: ", e);
            throw new MBeanException(new ServiceNotFoundException(ex.getMessage()), e.getMessage());
         }
         else
            throw e;
      }
   }

   public void setAttribute(Attribute attribute)
      throws AttributeNotFoundException, InvalidAttributeValueException, MBeanException, ReflectionException
   {
      // Another inconsistency
      if (attribute == null)
         throw new RuntimeOperationsException(new IllegalArgumentException("Null attribute"));
      try
      {
         super.setAttribute(attribute);
      }
      catch (ReflectionException e)
      {
         // Another inconsistency
         Exception ex = e.getTargetException();
         if ((ex instanceof ClassNotFoundException) == false &&
             (ex instanceof NoSuchMethodException) == false)
         {
            log.debug("Rewrapping reflection exception: ", e);
            throw new MBeanException(new ServiceNotFoundException(ex.getMessage()), e.getMessage());
         }
         else
            throw e;
      }
   }

   public AttributeList getAttributes(String[] attributes)
   {
      if (attributes == null)
         throw new RuntimeOperationsException(new IllegalArgumentException("Null attributes"));
      return super.getAttributes(attributes);
   }

   public AttributeList setAttributes(AttributeList attributes)
   {
      if (attributes == null)
         throw new RuntimeOperationsException(new IllegalArgumentException("Null attributes"));
      return super.setAttributes(attributes);
   }

   public Object invoke(String name, Object[] args, String[] signature) throws
      MBeanException, ReflectionException
   {
      Object value;

      if (name == null)
         throw new RuntimeOperationsException(new IllegalArgumentException("Null operation"));
      else if( name.equals("getNotificationInfo") )
         value = mbean.getNotificationInfo();
      else
      {
         try
         {
            value = super.invoke(name, args, signature);
         }
         catch (RuntimeMBeanException e)
         {
            // For some reason (not mentioned in the spec) these have to
            // be wrapped in MBeanExceptions for RMM
            throw new MBeanException(e.getTargetException(), e.getMessage());
         }
         catch (ReflectionException e)
         {
            // Another inconsistency
            Exception ex = e.getTargetException();
            if ((ex instanceof ClassNotFoundException) == false &&
                (ex instanceof NoSuchMethodException) == false)
            {
               log.debug("Rewrapping reflection exception: ", e);
               throw new MBeanException(new ServiceNotFoundException(ex.getMessage()), e.getMessage());
            }
            else
               throw e;
         }
      }
      return value;
   }
}

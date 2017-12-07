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
package org.jboss.mx.metadata.xb;

import javax.management.modelmbean.DescriptorSupport;
import javax.xml.namespace.QName;

import org.jboss.xb.binding.GenericValueContainer;
import org.jboss.logging.Logger;
import org.jboss.mx.modelmbean.XMBeanConstants;
import org.jboss.mx.interceptor.Interceptor;

/**
 @author Scott.Stark@jboss.org
 @version $Revision: 81026 $
 */
public class DescriptorSupportContainer
   implements GenericValueContainer
{
   private static final Logger log = Logger.getLogger(DescriptorSupportContainer.class);
   DescriptorSupport support = new DescriptorSupport();

   public Object instantiate()
   {
      return support;
   }

   public void addChild(QName name, Object value)
   {
      log.debug("addChild, " + name + "," + value);
      String localName = name.getLocalPart();
      if("name".equals(localName))
      {
         support.setField(XMBeanConstants.NAME, value);
      }
      else if (name.equals("persistence"))
      {
         PersistPolicy policy = (PersistPolicy) value;
         String persistPolicy = policy.getPersistPolicy();
         String persistPeriod = policy.getPersistPeriod();
         String persistLocation = policy.getPersistLocation();
         String persistName = policy.getPersistName();
         if (persistPolicy != null)
         {
            //validate(persistPolicy, PERSIST_POLICIES);
            support.setField(XMBeanConstants.PERSIST_POLICY, persistPolicy);
         }
         if (persistPeriod != null)
         {
            support.setField(XMBeanConstants.PERSIST_PERIOD, persistPeriod);
         }
         if (persistLocation != null)
         {
            support.setField(XMBeanConstants.PERSIST_LOCATION, persistLocation);
         }
         if (persistName != null)
         {
            support.setField(XMBeanConstants.PERSIST_NAME, persistName);
         }
      }
      else if (name.equals(XMBeanConstants.CURRENCY_TIME_LIMIT))
      {
         support.setField(XMBeanConstants.CURRENCY_TIME_LIMIT, value);
      }
      else if (name.equals(XMBeanConstants.DEFAULT))
      {
         support.setField(XMBeanConstants.DEFAULT, value);
      }
      else if (name.equals("display-name"))
      {
         support.setField(XMBeanConstants.DISPLAY_NAME, value);
      }
      else if (name.equals(XMBeanConstants.CACHED_VALUE))
      {
         support.setField(XMBeanConstants.CACHED_VALUE, value);
      }
      else if (name.equals(XMBeanConstants.PERSISTENCE_MANAGER))
      {
         support.setField(XMBeanConstants.PERSISTENCE_MANAGER, value);
      }
      else if (name.equals(XMBeanConstants.DESCRIPTOR))
      {
         Holder desc = (Holder) value;
         support.setField(desc.getName(), desc.getValue());
      }
      else if (name.equals("injection"))
      {
         Holder desc = (Holder) value;
         support.setField(desc.getName(), desc.getValue());
      }
      else if("interceptors".equals(localName))
      {
         InterceptorsHolder holder = (InterceptorsHolder) value;
         /* The value = Interceptor[] which is not compatible with the
         XMBean1.2 and earlier Descriptor[] format which only contained the
         info to create Interceptors, not the actual objects.
         */
         Interceptor[] ivalue = holder.getInterceptors();
         support.setField(XMBeanConstants.INTERCEPTORS, ivalue);
      }
   }

   public Class getTargetClass()
   {
      return DescriptorSupport.class;
   }

}

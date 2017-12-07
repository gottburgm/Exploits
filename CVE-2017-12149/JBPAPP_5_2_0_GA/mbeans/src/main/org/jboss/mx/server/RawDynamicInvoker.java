/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.mx.server;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.DynamicMBean;
import javax.management.InvalidAttributeValueException;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanRegistration;
import javax.management.MBeanServer;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;
import javax.management.ReflectionException;


/**
 * @author  <a href="mailto:juha@jboss.org">Juha Lindfors</a>.
 * @version $Revision: 78228 $
 *   
 */
public class RawDynamicInvoker
   extends AbstractMBeanInvoker
{
   
   private DynamicMBean typedRes = null;
   
   public RawDynamicInvoker(DynamicMBean resource)
   {
      super(resource);
      this.typedRes = resource;
   }
   
   // DynamicMBean overrides ----------------------------------------
   
   public void setAttribute(Attribute attribute) throws AttributeNotFoundException,
      InvalidAttributeValueException, MBeanException, ReflectionException
   {
      ClassLoader mbeanTCL = resourceEntry.getClassLoader();
      final ClassLoader ccl = TCLAction.UTIL.getContextClassLoader();
      boolean setCl = ccl != mbeanTCL && mbeanTCL != null;
      if(setCl)
      {
         TCLAction.UTIL.setContextClassLoader(mbeanTCL);
      }

      try
      {
         typedRes.setAttribute(attribute);
      }
      finally
      {
         if(setCl)
         {
            TCLAction.UTIL.setContextClassLoader(ccl);
         }
      }
   }
   
   public AttributeList setAttributes(AttributeList attributes)
   {
      ClassLoader mbeanTCL = resourceEntry.getClassLoader();
      final ClassLoader ccl = TCLAction.UTIL.getContextClassLoader();
      boolean setCl = ccl != mbeanTCL && mbeanTCL != null;
      if(setCl)
      {
         TCLAction.UTIL.setContextClassLoader(mbeanTCL);
      }

      try
      {
         return typedRes.setAttributes(attributes);
      }
      finally
      {
         if(setCl)
         {
            TCLAction.UTIL.setContextClassLoader(ccl);
         }
      }
   }
   
   public Object getAttribute(String name) throws AttributeNotFoundException,
         MBeanException, ReflectionException
   {
      ClassLoader mbeanTCL = resourceEntry.getClassLoader();
      final ClassLoader ccl = TCLAction.UTIL.getContextClassLoader();
      boolean setCl = ccl != mbeanTCL && mbeanTCL != null;
      if(setCl)
      {
         TCLAction.UTIL.setContextClassLoader(mbeanTCL);
      }

      try
      {
         return typedRes.getAttribute(name);
      }
      finally
      {
         if(setCl)
         {
            TCLAction.UTIL.setContextClassLoader(ccl);
         }
      }
   }
   
   public AttributeList getAttributes(String[] attributes)
   {
      ClassLoader mbeanTCL = resourceEntry.getClassLoader();
      final ClassLoader ccl = TCLAction.UTIL.getContextClassLoader();
      boolean setCl = ccl != mbeanTCL && mbeanTCL != null;
      if(setCl)
      {
         TCLAction.UTIL.setContextClassLoader(mbeanTCL);
      }

      try
      {
         return typedRes.getAttributes(attributes);
      }
      finally
      {
         if(setCl)
         {
            TCLAction.UTIL.setContextClassLoader(ccl);
         }
      }
   }
   
   public Object invoke(String name, Object[] args, String[] signature) throws
         MBeanException, ReflectionException
   {
      ClassLoader mbeanTCL = resourceEntry.getClassLoader();
      final ClassLoader ccl = TCLAction.UTIL.getContextClassLoader();
      boolean setCl = ccl != mbeanTCL && mbeanTCL != null;
      if(setCl)
      {
         TCLAction.UTIL.setContextClassLoader(mbeanTCL);
      }

      try
      {
         return typedRes.invoke(name, args, signature);   
      }
      finally
      {
         if(setCl)
         {
            TCLAction.UTIL.setContextClassLoader(ccl);
         }
      }
   }
   
   public MBeanInfo getMBeanInfo()
   {
      ClassLoader mbeanTCL = resourceEntry.getClassLoader();
      final ClassLoader ccl = TCLAction.UTIL.getContextClassLoader();
      boolean setCl = ccl != mbeanTCL && mbeanTCL != null;
      if(setCl)
      {
         TCLAction.UTIL.setContextClassLoader(mbeanTCL);
      }

      try
      {
         return typedRes.getMBeanInfo();
      }
      finally
      {
         if(setCl)
         {
            TCLAction.UTIL.setContextClassLoader(ccl);
         }
      }
   }
   
   // MBeanRegistration overrides -----------------------------------
   public ObjectName preRegister(MBeanServer server, ObjectName oname) throws Exception
   {
      this.resourceEntry = AbstractMBeanInvoker.getMBeanEntry();

      try
      {
         this.info = getMBeanInfo();
      }
      catch (Exception e)
      {
         // turn into a NotCompliantMBeanException
         Exception ncmbe = new NotCompliantMBeanException("Cannot obtain MBeanInfo for: " + oname);
         ncmbe.initCause(e);
         throw ncmbe;
      }

      ClassLoader mbeanTCL = resourceEntry.getClassLoader();
      final ClassLoader ccl = TCLAction.UTIL.getContextClassLoader();
      boolean setCl = ccl != mbeanTCL && mbeanTCL != null;
      if(setCl)
      {
         TCLAction.UTIL.setContextClassLoader(mbeanTCL);
      }

      try
      {
         if (getResource() instanceof MBeanRegistration)
            return ((MBeanRegistration)getResource()).preRegister(server, oname);
         else
            return oname;
      }
      finally
      {
         if(setCl)
         {
            TCLAction.UTIL.setContextClassLoader(ccl);
         }
      }
   }
   
   public void postRegister(Boolean b)
   {
      ClassLoader mbeanTCL = resourceEntry.getClassLoader();
      final ClassLoader ccl = TCLAction.UTIL.getContextClassLoader();
      boolean setCl = ccl != mbeanTCL && mbeanTCL != null;
      if(setCl)
      {
         TCLAction.UTIL.setContextClassLoader(mbeanTCL);
      }

      try
      {
         if (getResource() instanceof MBeanRegistration)
            ((MBeanRegistration)getResource()).postRegister(b);
      }
      finally
      {
         TCLAction.UTIL.setContextClassLoader(ccl);
      }
   }
   
   public void preDeregister() throws Exception
   {
      ClassLoader mbeanTCL = resourceEntry.getClassLoader();
      final ClassLoader ccl = TCLAction.UTIL.getContextClassLoader();
      boolean setCl = ccl != mbeanTCL && mbeanTCL != null;
      if(setCl)
      {
         TCLAction.UTIL.setContextClassLoader(mbeanTCL);
      }

      try
      {
         if (getResource() instanceof MBeanRegistration)
            ((MBeanRegistration)getResource()).preDeregister();
      }
      finally
      {
         if(setCl)
         {
            TCLAction.UTIL.setContextClassLoader(ccl);
         }
      }
   }
   
   public void postDeregister()
   {
      ClassLoader mbeanTCL = resourceEntry.getClassLoader();
      final ClassLoader ccl = TCLAction.UTIL.getContextClassLoader();
      boolean setCl = ccl != mbeanTCL && mbeanTCL != null;
      if(setCl)
      {
         TCLAction.UTIL.setContextClassLoader(mbeanTCL);
      }

      try
      {
         if (getResource() instanceof MBeanRegistration)
            ((MBeanRegistration)getResource()).postDeregister();
      }
      finally
      {
         if(setCl)
         {
            TCLAction.UTIL.setContextClassLoader(ccl);
         }
      }
   }
}

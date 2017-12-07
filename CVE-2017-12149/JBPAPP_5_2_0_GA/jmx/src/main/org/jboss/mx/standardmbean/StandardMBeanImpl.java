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
package org.jboss.mx.standardmbean;

import java.lang.reflect.Method;
import java.util.Iterator;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.InvalidAttributeValueException;
import javax.management.JMException;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.NotCompliantMBeanException;
import javax.management.ReflectionException;

import org.jboss.logging.Logger;
import org.jboss.mx.loading.LoaderRepository;
import org.jboss.mx.metadata.StandardMetaData;
import org.jboss.mx.server.ExceptionHandler;

/**
 * A helper class to allow standard mbeans greater control over their
 * management interface.<p>
 *
 * Extending this class actually makes the mbean a dynamic mbean, but
 * with the convenience of a standard mbean.
 *
 * @todo make this more dynamic, somehow delegate to an XMBean?
 *
 * @author  <a href="mailto:Adrian.Brock@HappeningTimes.com">Adrian Brock</a>.
 * @author  <a href="mailto:thomas.diesler@jboss.com">Thomas Diesler</a>.
 * @version $Revision: 81022 $
 */
public class StandardMBeanImpl implements StandardMBeanDelegate
{
   private static final Logger log = Logger.getLogger(StandardMBeanImpl.class);

   /**
    * The implementation object
    */
   private Object implementation;

   /**
    * The management interface
    */
   private Class mbeanInterface;

   /**
    * The cached mbeaninfo
    */
   private MBeanInfo cachedMBeanInfo;

   // Static  -----------------------------------------------------

   // Constructors ------------------------------------------------

   /**
    * Construct a DynamicMBean from the given implementation object
    * and the passed management interface class.
    *
    * @param implementation the object implementing the mbean
    * @param mbeanInterface the management interface of the mbean
    * @exception IllegalArgumentException for a null implementation
    * @exception NotCompliantMBeanException if the management interface
    *            does not follow the JMX design patterns or the implementation
    *            does not implement the interface
    */
   public StandardMBeanImpl(Object implementation, Class mbeanInterface)
      throws NotCompliantMBeanException
   {
      this.implementation = implementation;
      this.mbeanInterface = mbeanInterface;
   }

   /**
    * Construct a DynamicMBean from this object
    * and the passed management interface class.<p>
    *
    * Used in subclassing
    *
    * @param mbeanInterface the management interface of the mbean
    * @exception NotCompliantMBeanException if the management interface
    *            does not follow the JMX design patterns or this
    *            does not implement the interface
    */
   protected StandardMBeanImpl(Class mbeanInterface)
      throws NotCompliantMBeanException
   {
      this.implementation = this;
      this.mbeanInterface = mbeanInterface;
   }

   /**
    * Retrieve the implementation object
    *
    * @return the implementation
    */
   public Object getImplementation()
   {
      return implementation;
   }

   /**
    * Replace the implementation object
    *
    * @todo make this work after the mbean is registered
    * @param implementation the new implementation
    * @exception IllegalArgumentException for a null parameter
    * @exception NotCompliantMBeanException if the new implementation
    *            does not implement the interface supplied at
    *            construction
    */
   public void setImplementation(Object implementation) 
        throws NotCompliantMBeanException
   {
      if (implementation == null)
         throw new IllegalArgumentException("Null implementation");
      this.implementation = implementation;
   }

   /**
    * Retrieve the implementation class
    *
    * @return the class of the implementation
    */
   public Class getImplementationClass()
   {
      return implementation.getClass();
   }

   /**
    * Retrieve the management interface
    *
    * @return the management interface
    */
   public final Class getMBeanInterface()
   {
      return mbeanInterface;
   }

   public Object getAttribute(String attribute)
      throws AttributeNotFoundException, MBeanException, ReflectionException
   {
      try
      {
         Method method = implementation.getClass().getMethod("get" + attribute, null);
         return method.invoke(implementation, new Object[0]);
      }
      catch (Exception e)
      {
         JMException result = ExceptionHandler.handleException(e);
         if (result instanceof AttributeNotFoundException)
            throw (AttributeNotFoundException)result;
         if (result instanceof MBeanException)
            throw (MBeanException)result;
         if (result instanceof ReflectionException)
            throw (ReflectionException)result;
         throw new MBeanException(e, "Cannot get attribute: " + attribute);
      }
   }

   public void setAttribute(Attribute attribute)
      throws AttributeNotFoundException, InvalidAttributeValueException, MBeanException, ReflectionException
   {
      try
      {
         Class[] clArr = null;
         if (attribute.getValue() != null)
         {
            clArr = new Class[]{attribute.getValue().getClass()};
         }
         Method method = implementation.getClass().getMethod("set" + attribute.getName(), clArr);
         method.invoke(implementation, new Object[]{attribute.getValue()});
      }
      catch (Exception e)
      {
         JMException result = ExceptionHandler.handleException(e);
         if (result instanceof AttributeNotFoundException)
            throw (AttributeNotFoundException)result;
         if (result instanceof InvalidAttributeValueException)
            throw (InvalidAttributeValueException)result;
         if (result instanceof MBeanException)
            throw (MBeanException)result;
         if (result instanceof ReflectionException)
            throw (ReflectionException)result;
         throw new MBeanException(e, "Cannot set attribute: " + attribute);
      }
   }

   public AttributeList getAttributes(String[] attributes)
   {
      try
      {
         AttributeList attrList = new AttributeList(attributes.length);
         for (int i = 0; i < attributes.length; i++)
         {
            String name = attributes[i];
            Object value = getAttribute(name);
            attrList.add(new Attribute(name, value));
         }
         return attrList;
      }
      catch (Exception e)
      {
         JMException result = ExceptionHandler.handleException(e);
         // Why is this not throwing the same exceptions as getAttribute(String)
         throw new RuntimeException("Cannot get attributes", result);
      }
   }


   public AttributeList setAttributes(AttributeList attributes)
   {
      try
      {
         AttributeList attrList = new AttributeList(attributes.size());
         Iterator it = attributes.iterator();
         while (it.hasNext())
         {
            Attribute attr = (Attribute) it.next();
            setAttribute(attr);
            String name = attr.getName();
            Object value = getAttribute(name);
            attrList.add(new Attribute(name, value));
         }
         return attrList;
      }
      catch (Exception e)
      {
         JMException result = ExceptionHandler.handleException(e);
         // Why is this not throwing the same exceptions as setAttribute(Attribute)
         throw new RuntimeException("Cannot set attributes", result);
      }
   }

   public Object invoke(String actionName, Object[] params, String[] signature)
      throws MBeanException, ReflectionException
   {
      try
      {
         Class[] sigcl = new Class[signature.length];
         for (int i = 0; i < signature.length; i++)
         {
            sigcl[i] = loadClass(signature[i]);
         }
         Method method = implementation.getClass().getMethod(actionName, sigcl);
         return method.invoke(implementation, params);
      }
      catch (Exception e)
      {
         JMException result = ExceptionHandler.handleException(e);
         if (result instanceof MBeanException)
            throw (MBeanException)result;
         if (result instanceof ReflectionException)
            throw (ReflectionException)result;
         throw new MBeanException(e, "Cannot invoke: " + actionName);
      }
   }

   /**
    * Load a class from the classloader that loaded this MBean
    */
   private Class loadClass(String className) throws ClassNotFoundException
   {
      Class clazz = LoaderRepository.getNativeClassForName(className);
      if (clazz == null) {
         ClassLoader cl = getClass().getClassLoader();
         clazz = cl.loadClass(className);
      }
      return clazz;
   }

   public MBeanInfo getMBeanInfo()
   {
      MBeanInfo info = getCachedMBeanInfo();
      if (info == null)
      {
         try
         {
            info = buildMBeanInfo();
            cacheMBeanInfo(info);
         }
         catch (NotCompliantMBeanException e)
         {
            log.error("Unexcepted exception", e);
            throw new IllegalStateException("Unexcepted exception " + e.toString());
         }

      }
      return info;
   }

   /**
    * Retrieve the cached mbean info
    *
    * @return the cached mbean info
    */
   public MBeanInfo getCachedMBeanInfo()
   {
      return cachedMBeanInfo;
   }

   /**
    * Sets the cached mbean info
    *
    * @todo make this work after the mbean is registered
    * @param info the mbeaninfo to cache, can be null to erase the cache
    */
   public void cacheMBeanInfo(MBeanInfo info)
   {
      cachedMBeanInfo = info;
   }

   /**
    * Builds a default MBeanInfo for this MBean, using the Management Interface specified for this MBean.
    *
    * While building the MBeanInfo, this method calls the customization hooks that make it possible for subclasses to
    * supply their custom descriptions, parameter names, etc...
    */
   public MBeanInfo buildMBeanInfo()
      throws NotCompliantMBeanException
   {
      if (implementation == null)
         throw new IllegalArgumentException("Null implementation");

      StandardMetaData metaData = new StandardMetaData(implementation, mbeanInterface);
      this.mbeanInterface = metaData.getMBeanInterface();
      return metaData.build();
   }
   
   // Inner Classes -----------------------------------------------
}

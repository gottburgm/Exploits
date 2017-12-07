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
package org.jboss.system;

import java.util.List;
import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.DynamicMBean;
import javax.management.InvalidAttributeValueException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanConstructorInfo;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanNotificationInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanParameterInfo;
import javax.management.ReflectionException;

import org.jboss.logging.Logger;

/**
 * <description>
 *
 * @see <related>
 *
 * @author  <a href="mailto:sacha.labourey@cogito-info.ch">Sacha Labourey</a>.
 * @version $Revision: 81033 $
 *
 * <p><b>Revisions:</b>
 *
 * <p><b>6 janv. 2003 Sacha Labourey:</b>
 * <ul>
 * <li> First implementation </li>
 * </ul>
 */
public class ServiceDynamicMBeanSupport 
   extends ServiceMBeanSupport
   implements DynamicMBean
{

  // Constants -----------------------------------------------------
   
   // Attributes ----------------------------------------------------
   
   // Static --------------------------------------------------------
   
   // Constructors --------------------------------------------------
   
   public ServiceDynamicMBeanSupport()
   {
      super();
   }

   public ServiceDynamicMBeanSupport(Class type)
   {
      super(type);
   }

   public ServiceDynamicMBeanSupport(String category)
   {
      super(category);
   }

   public ServiceDynamicMBeanSupport(Logger log)
   {
      super(log);
   }
   
   // Public --------------------------------------------------------

   // DynamicMBean implementation -----------------------------------
   
   public Object getAttribute(String attribute)
      throws AttributeNotFoundException, MBeanException, ReflectionException
   {
      // locally managed attributes!
      //
      if("State".equals(attribute))
      {
         return new Integer(getState());
      }
      if("StateString".equals(attribute))
      {
         return getStateString();
      }
      if("Name".equals(attribute))
      {
         return getName();
      }
      
      // Wrapped attributes?
      //
      return getInternalAttribute (attribute);
      
   }

   public Object invoke(String actionName, Object[] params, String[] signature)
      throws MBeanException, ReflectionException
   {
      try 
      {
         if (ServiceController.JBOSS_INTERNAL_LIFECYCLE.equals(actionName))
         {
            jbossInternalLifecycle((String) params[0]); 
            return null;
         }
         if (params == null || params.length == 0) 
         {
            if ("create".equals(actionName)) 
            {
               create(); return null;
            }
            else if ("start".equals(actionName)) 
            {
               start(); return null;
            }
            else if ("stop".equals(actionName)) 
            {
               stop(); return null;
            }
            else if ("destroy".equals(actionName)) 
            {
               destroy(); return null;
            }
         }
      }
      catch (Exception e)
      {
         throw new MBeanException(e, "Exception in service lifecyle operation: " + actionName);
      }         
      
      // If I am here, it means that the invocation has not been handled locally
      //
      try
      {
         return internalInvoke (actionName, params, signature);
      }
      catch (Exception e)
      {
         throw new MBeanException(e, 
               "Exception invoking: " + actionName);
      }         
   }

   public void setAttribute(Attribute attribute)
      throws
         AttributeNotFoundException,
         InvalidAttributeValueException,
         MBeanException,
         ReflectionException
   {
      setInternalAttribute (attribute);
   }

   public AttributeList setAttributes(AttributeList attributes)
   {
      AttributeList list = new AttributeList();
      if (attributes == null)
         return list;
      for (int i = 0; i < attributes.size(); ++i)
      {
         Attribute attribute = (Attribute) attributes.get(i);
         try
         {
            setAttribute(attribute);
            list.add(attribute);
         }
         catch (Throwable t)
         {
            log.debug("Error setting attribute " + attribute.getName(), t);
         }
      }
      return list;
   }

   public AttributeList getAttributes(String[] attributes)
   {
      AttributeList list = new AttributeList();
      if (attributes == null)
         return list;
      for (int i = 0; i < attributes.length; ++i)
      {
         try
         {
            Object value = getAttribute(attributes[i]);
            list.add(new Attribute(attributes[i], value));
         }
         catch (Throwable t)
         {
            log.debug("Error getting attribute " + attributes[i], t);
         }
      }
      return list;
   }

   public MBeanInfo getMBeanInfo()
   {
      MBeanParameterInfo[] noParams = new MBeanParameterInfo[] {};
      
      MBeanConstructorInfo[] ctorInfo = getInternalConstructorInfo();
      
      MBeanAttributeInfo[] attrs = getInternalAttributeInfo();
      MBeanAttributeInfo[] attrInfo = new MBeanAttributeInfo[3 + attrs.length];
      attrInfo[0] = new MBeanAttributeInfo("Name",
            "java.lang.String",
            "Return the service name",
            true,
            false,
            false);
      attrInfo[1] = new MBeanAttributeInfo("State",
            "int",
            "Return the service state",
            true,
            false,
            false);
      attrInfo[2] = new MBeanAttributeInfo("StateString",
               "java.lang.String",
               "Return the service's state as a String",
               true,
               false,
               false);
      System.arraycopy(attrs, 0, attrInfo, 3, attrs.length);
      
      MBeanParameterInfo[] jbossInternalLifecycleParms = new MBeanParameterInfo[1];
      jbossInternalLifecycleParms[0] = new MBeanParameterInfo("method", String.class.getName(), "The lifecycle method");
      
      MBeanOperationInfo[] ops = getInternalOperationInfo();
      MBeanOperationInfo[] opInfo = new MBeanOperationInfo[5 + ops.length];
      opInfo[0] = new MBeanOperationInfo("create",
                                "create service lifecycle operation",
                                noParams,
                                "void",
                                MBeanOperationInfo.ACTION);
         
      opInfo[1] = new MBeanOperationInfo("start",
                                "start service lifecycle operation",
                                noParams,
                                "void",
                                MBeanOperationInfo.ACTION);
         
      opInfo[2] = new MBeanOperationInfo("stop",
                                "stop service lifecycle operation",
                                noParams,
                                "void",
                                MBeanOperationInfo.ACTION);
         
      opInfo[3] = new MBeanOperationInfo("destroy",
                                "destroy service lifecycle operation",
                                noParams,
                                "void",
                                MBeanOperationInfo.ACTION);                                
                                
      opInfo[4] = new MBeanOperationInfo(ServiceController.JBOSS_INTERNAL_LIFECYCLE,
                                "Internal lifecycle (for internal use)",
                                jbossInternalLifecycleParms,
                                "void",
                                MBeanOperationInfo.ACTION);                                

      System.arraycopy(ops, 0, opInfo, 5, ops.length);
      
      MBeanNotificationInfo[] notifyInfo = getInternalNotificationInfo();
      return new MBeanInfo(getClass().getName(), 
                           getInternalDescription(),
                           attrInfo, 
                           ctorInfo, 
                           opInfo, 
                           notifyInfo);
   }

   // Y overrides ---------------------------------------------------
   
   // Package protected ---------------------------------------------
   
   // Protected -----------------------------------------------------
   
   protected String getInternalDescription()
   {
      return "DynamicMBean Service";
   }
   
   protected MBeanConstructorInfo[] getInternalConstructorInfo()
   {
      return new MBeanConstructorInfo[0];
   }
   
   protected MBeanAttributeInfo[] getInternalAttributeInfo()
   {
      return new MBeanAttributeInfo[0];
   }
   
   protected MBeanOperationInfo[] getInternalOperationInfo()
   {
      return new MBeanOperationInfo[0];
   }
   
   protected MBeanNotificationInfo[] getInternalNotificationInfo()
   {
      return new MBeanNotificationInfo[0];
   }
   
   protected Object getInternalAttribute(String attribute)
      throws AttributeNotFoundException, MBeanException, ReflectionException
   {
      throw new AttributeNotFoundException ("Attribute not found " + attribute);
   }
   
   protected void setInternalAttribute(Attribute attribute)
       throws AttributeNotFoundException, InvalidAttributeValueException, MBeanException, ReflectionException
   {
      throw new AttributeNotFoundException ("Attribute not found " + attribute);
   }

   protected Object internalInvoke(String actionName, Object[] params, String[] signature)
      throws MBeanException, ReflectionException
   {
      StringBuffer buffer = new StringBuffer();
      buffer.append(actionName);
      buffer.append('(');
      for (int i = 0; i < signature.length; ++i)
      {
         buffer.append(signature[i]);
         if (i < signature.length - 1)
            buffer.append(", ");
      }
      buffer.append(')');
      throw new MBeanException(new Exception("Operation not found " + buffer.toString()), "Operation not found " + actionName);
   }
   
   
   // Private -------------------------------------------------------
   
   // Inner classes -------------------------------------------------
   
}

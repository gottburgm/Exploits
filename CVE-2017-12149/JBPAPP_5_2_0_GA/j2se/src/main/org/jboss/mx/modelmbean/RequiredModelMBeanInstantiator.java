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

import java.lang.reflect.Constructor;

import javax.management.modelmbean.ModelMBean;
import javax.management.modelmbean.ModelMBeanInfo;

import org.jboss.mx.server.ServerConstants;
import org.jboss.mx.util.PropertyAccess;

/**
 * ModelMBean instantiator. The ModelMBean implementation
 * can be configured by setting a <tt>jbossmx.required.modelmbean.class</tt>
 * system property.
 *
 * @see javax.management.modelmbean.ModelMBean
 *
 * @author  <a href="mailto:juha@jboss.org">Juha Lindfors</a>.
 * @author  <a href="mailto:Adrian@jboss.org">Adrian Brock</a>.
 * @version $Revision: 81019 $
 */
public class RequiredModelMBeanInstantiator
{
   public static ModelMBean instantiate()
   {
      ClassLoader cl = Thread.currentThread().getContextClassLoader();
      String className = getClassName();
      try
      {
         Class modelMBean = cl.loadClass(className);
         return (ModelMBean) modelMBean.newInstance();  
      }
      catch (ClassNotFoundException e)
      {
         throw new Error("Cannot instantiate model mbean class. Class " + className + " not found.");
      }
      catch (ClassCastException e) 
      {
         throw new Error("Cannot instantiate model mbean class. The target class is not an instance of ModelMBean interface.");
      }
      catch (Exception e) 
      {
         throw new Error("Cannot instantiate model mbean class " + className + " with default constructor: " + e.getMessage());      
      }
   }

   public static ModelMBean instantiate(ModelMBeanInfo info)
   {
      ClassLoader cl = Thread.currentThread().getContextClassLoader();
      String className = getClassName();
      try 
      {
         Class modelMBean = cl.loadClass(className);
         Constructor constructor = modelMBean.getConstructor(new Class[] { ModelMBeanInfo.class });
         return (ModelMBean) constructor.newInstance(new Object[] { info });  
      }
      catch (ClassNotFoundException e)
      {
         throw new Error("Cannot instantiate model mbean class. Class " + className + " not found.");
      }
      catch (ClassCastException e) 
      {
         throw new Error("Cannot instantiate model mbean class. The target class is not an instance of ModelMBean interface.");
      }
      catch (Exception e) 
      {
         throw new Error("Cannot instantiate model mbean class " + className + ": " + e.toString());      
      }
   }

   public static String getClassName()
   {
      return PropertyAccess.getProperty
      (
            ServerConstants.REQUIRED_MODELMBEAN_CLASS_PROPERTY,
            ServerConstants.DEFAULT_REQUIRED_MODELMBEAN_CLASS
      );
   }
}

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
package org.jboss.system.metadata;

import java.io.Serializable;

import javax.management.MBeanAttributeInfo;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.jboss.dependency.spi.ControllerState;
import org.jboss.deployment.DeploymentException;
import org.jboss.mx.util.MBeanProxyExt;
import org.jboss.system.microcontainer.LifecycleDependencyItem;
import org.jboss.system.microcontainer.ServiceControllerContext;

/**
 * ServiceDependencyValueMetaData.
 * 
 * This class is based on the old ServiceConfigurator
 *
 * @author <a href="mailto:marc@jboss.org">Marc Fleury</a>
 * @author <a href="mailto:hiram@jboss.org">Hiram Chirino</a>
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @author <a href="mailto:dimitris@jboss.org">Dimitris Andreadis</a>
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 85945 $
 */
public class ServiceDependencyValueMetaData extends AbstractMetaDataVisitorNode
   implements ServiceValueMetaData, Serializable
{
   private static final long serialVersionUID = 1;

   /** The dependency */
   private String dependency;

   /** The dependency */
   private ObjectName objectName;

   /** The proxy type */
   private String proxyType;
   
   /**
    * Create a new ServiceDependencyValueMetaData.
    */
   public ServiceDependencyValueMetaData()
   {
   }
   
   /**
    * Create a new ServiceDependencyValueMetaData.
    * 
    * @param dependency the dependents object name
    */
   public ServiceDependencyValueMetaData(String dependency)
   {
      setDependency(dependency);
   }
   
   /**
    * Create a new ServiceDependencyValueMetaData.
    * 
    * @param dependency the dependents object name
    * @param proxyType the proxy type
    */
   public ServiceDependencyValueMetaData(String dependency, String proxyType)
   {
      setDependency(dependency);
      setProxyType(proxyType);
   }

   /**
    * Get the dependency.
    * 
    * @return the dependency.
    */
   public String getDependency()
   {
      if (dependency == null)
         return objectName.getCanonicalName();
      return dependency;
   }

   /**
    * Set the dependency.
    * 
    * @param dependency the dependency.
    */
   public void setDependency(String dependency)
   {
      if (dependency == null)
         throw new IllegalArgumentException("Null dependency");
      this.dependency = dependency;
      this.objectName = null;
   }

   /**
    * Get the object name.
    * 
    * @return the object name.
    * @throws MalformedObjectNameException if the string was set with an invalid object name
    */
   public ObjectName getObjectName() throws MalformedObjectNameException
   {
      if (objectName == null)
      {
         if (dependency == null || dependency.trim().length() == 0)
            throw new MalformedObjectNameException("Missing object name in depends");
         ObjectName name = new ObjectName(dependency);
         if (name.isPattern())
            throw new MalformedObjectNameException("ObjectName patterns are not allowed in depends: " + objectName);
         objectName = new ObjectName(dependency);
         dependency = null;
      }
      return objectName;
   }

   /**
    * Set the object name
    * 
    * @param objectName the object name
    */
   public void setObjectName(ObjectName objectName)
   {
      if (objectName == null)
         throw new IllegalArgumentException("Null objectName");
      this.objectName = objectName;
   }

   /**
    * Get the proxyType.
    * 
    * @return the proxyType.
    */
   public String getProxyType()
   {
      return proxyType;
   }

   /**
    * Set the proxyType.
    * 
    * @param proxyType the proxyType.
    */
   public void setProxyType(String proxyType)
   {
      this.proxyType = proxyType;
   }

   public Object getValue(ServiceValueContext valueContext) throws Exception
   {
      MBeanAttributeInfo attributeInfo = valueContext.getAttributeInfo();
      ClassLoader cl = valueContext.getClassloader();
      MBeanServer server = valueContext.getServer();

      ObjectName objectName = getObjectName();
      
      if (proxyType != null)
      {
         if (proxyType.equals("attribute"))
         {
            proxyType = attributeInfo.getType();
            if (proxyType == null)
               throw new DeploymentException("AttributeInfo for " + attributeInfo.getName() + " has no type");
         }

         Class proxyClass = cl.loadClass(proxyType);
         return MBeanProxyExt.create(proxyClass, objectName, server, true);
      }
      
      return objectName;
   }

   public void visit(ServiceMetaDataVisitor visitor)
   {
      ServiceControllerContext context = visitor.getControllerContext();
      Object name = context.getName();
      Object other = dependency;
      try
      {
         other = getObjectName().getCanonicalName();
      }
      catch (MalformedObjectNameException ignored)
      {
      }
      // TODO visitor.addDependency(new LifecycleDependencyItem(name, other, ControllerState.CONFIGURED));
      visitor.addDependency(new LifecycleDependencyItem(name, other, ControllerState.CREATE));
      visitor.addDependency(new LifecycleDependencyItem(name, other, ControllerState.START));
      visitor.visit(this);
   }
}

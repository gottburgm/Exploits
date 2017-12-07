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
package org.jboss.system.microcontainer;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Iterator;
import javax.management.Attribute;
import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.jboss.dependency.plugins.AbstractControllerContext;
import org.jboss.dependency.spi.Controller;
import org.jboss.dependency.spi.ControllerMode;
import org.jboss.dependency.spi.ControllerState;
import org.jboss.dependency.spi.DependencyItem;
import org.jboss.dependency.spi.dispatch.LifecycleDispatchContext;
import org.jboss.system.Service;
import org.jboss.system.ServiceContext;
import org.jboss.system.ServiceController;
import org.jboss.system.metadata.ServiceMetaData;
import org.jboss.system.metadata.ServiceMetaDataVisitor;
import org.jboss.system.metadata.ServiceMetaDataVisitorNode;

/**
 * ServiceControllerContext.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @author <a href="ales.justin@jboss.com">Ales Justin</a>
 * @version $Revision: 90438 $
 */
public class ServiceControllerContext extends AbstractControllerContext implements LifecycleDispatchContext
{
   /** The ObjectName */
   private ObjectName objectName;
   
   /** The service controller */
   private ServiceController serviceController;
   
   /** The meta data */
   private ServiceMetaData serviceMetaData;
   
   /** The service context */
   private ServiceContext serviceContext = new ServiceContext();

   // Whether to include the lifecycle
   private boolean includeLifecycle = true;
   
   /** The lifecycle info */
   private volatile LifecycleInfo lifecycleInfo;

   /**
    * Create a new ServiceControllerContext.
    * 
    * @param serviceController the service controller
    * @param name the name of the context
    */
   public ServiceControllerContext(ServiceController serviceController, ObjectName name)
   {
      this(serviceController, name, true);
   }
   
   /**
    * Create a new ServiceControllerContext.
    *
    * @param serviceController the service controller
    * @param name the name of the context
    * @param includeLifecycle whether to include the lifecycle callouts
    */
   public ServiceControllerContext(ServiceController serviceController, ObjectName name, boolean includeLifecycle)
   {
      this(serviceController, name, includeLifecycle, null);
   }

   /**
    * Create a new ServiceControllerContext.
    *
    * @param serviceController the service controller
    * @param name the name of the context
    * @param includeLifecycle whether to include the lifecycle callouts
    * @param target the target
    */
   public ServiceControllerContext(ServiceController serviceController, ObjectName name, boolean includeLifecycle, Object target)
   {
      super(name.getCanonicalName(), ServiceControllerContextActions.getLifecycleOnly());
      this.objectName = name;
      serviceContext.objectName = objectName;
      this.serviceController = serviceController;
      setMode(ControllerMode.MANUAL);
      this.includeLifecycle = includeLifecycle;
      if (target != null)
         setTarget(target);
   }

   /**
    * Create a new ServiceControllerContext.
    * 
    * @param serviceController the service controller
    * @param metaData the meta data
    */
   public ServiceControllerContext(ServiceController serviceController, ServiceMetaData metaData)
   {
      super(metaData.getObjectName().getCanonicalName(), ServiceControllerContextActions.getInstance());
      this.objectName = metaData.getObjectName();
      serviceContext.objectName = objectName;
      this.serviceController = serviceController;
      ControllerMode mode = metaData.getMode();
      this.serviceMetaData = metaData;
      if (mode == null)
         setMode(ControllerMode.MANUAL);
      else
         setMode(mode);
   }
   
   /**
    * Create a new ServiceControllerContext.
    * 
    * @param serviceController the service controller
    * @param name the name
    * @param target the target
    */
   public ServiceControllerContext(ServiceController serviceController, ObjectName name, Object target)
   {
      super(name.getCanonicalName(), ServiceControllerContextActions.getInstance());
      this.objectName = name;
      serviceContext.objectName = objectName;
      this.serviceController = serviceController;
      setTarget(target);
      setMode(ControllerMode.MANUAL);
   }

   private MBeanServer getMBeanServer()
   {
      MBeanServer server = serviceController.getMBeanServer();
      if (server == null)
      {
         throw new IllegalStateException("MBeanServer not available.");
      }
      return server;
   }

   protected static String getAttributeName(String name)
   {
      if (name == null || name.length() == 0)
         throw new IllegalArgumentException("Illegal name: " + name);

      char firstCharacter = name.charAt(0);
      if (Character.isLowerCase(firstCharacter))
      {
         String attributeName = String.valueOf(Character.toUpperCase(firstCharacter));
         if (name.length() > 1)
            attributeName += name.substring(1);
         return attributeName;
      }
      return name;
   }

   public Object get(String name) throws Throwable
   {
      return getMBeanServer().getAttribute(objectName, getAttributeName(name));
   }

   public void set(String name, Object value) throws Throwable
   {
      Attribute attribute = new Attribute(getAttributeName(name), value);
      getMBeanServer().setAttribute(objectName, attribute);
   }

   public Object invoke(String name, Object parameters[], String[] signature) throws Throwable
   {
      return getMBeanServer().invoke(objectName, name, parameters, signature);
   }

   public ClassLoader getClassLoader() throws Throwable
   {
      if (serviceMetaData != null)
      {
         return getMBeanServer().getClassLoader(serviceMetaData.getClassLoaderName());
      }
      else
      {
         return getMBeanServer().getClassLoaderFor(objectName);
      }
   }

   public ControllerState lifecycleInvocation(String name, Object[] parameters, String[] signature) throws Throwable
   {
      if (lifecycleInfo == null)
         lifecycleInfo = new LifecycleInfo(this);

      return lifecycleInfo.lifecycleInvocation(name, signature);
   }

   /**
    * Get the ObjectName.
    * 
    * @return the ObjectName.
    */
   public ObjectName getObjectName()
   {
      return objectName;
   }

   /**
    * Get the serviceMetaData.
    * 
    * @return the serviceMetaData.
    */
   public ServiceMetaData getServiceMetaData()
   {
      return serviceMetaData;
   }

   /**
    * Set the serviceMetaData.
    * 
    * @param serviceMetaData the serviceMetaData.
    */
   public void setServiceMetaData(ServiceMetaData serviceMetaData)
   {
      this.serviceMetaData = serviceMetaData;
   }

   /**
    * Get the serviceController.
    * 
    * @return the serviceController.
    */
   public ServiceController getServiceController()
   {
      return serviceController;
   }
   
   /**
    * Get the service proxy
    * 
    * @return the service proxy
    * @throws Exception for any error
    */
   public Service getServiceProxy() throws Exception
   {
      if (serviceContext.proxy != null)
         return serviceContext.proxy;
      
      MBeanServer server = serviceController.getMBeanServer();
      if (server != null)
         serviceContext.proxy = ServiceProxy.getServiceProxy(objectName, server, includeLifecycle);
      
      return serviceContext.proxy;
   }
   
   /**
    * Get the service context
    * 
    * @return the service context
    */
   public ServiceContext getServiceContext()
   {
      try
      {
         serviceContext.proxy = getServiceProxy();
      }
      catch (Exception ignored)
      {
      }
      if (getError() != null)
         serviceContext.setProblem(getError());
      if (getState() == ControllerState.ERROR)
         serviceContext.state = ServiceContext.FAILED;
      return serviceContext;
   }

   // Overridden to update the service context with any failure
   public void install(ControllerState fromState, ControllerState toState) throws Throwable
   {
      try
      {
         super.install(fromState, toState);
      }
      catch (Throwable t)
      {
         serviceContext.setProblem(t);
         serviceContext.state = ServiceContext.FAILED;
         throw t;
      }
   }

   // Overridden to update the service context with the installed/not installed state
   // i.e. of the ServiceController registration
   // Not to be confused with the microcontainer's (fully) installed state
   public void setController(Controller controller)
   {
      super.setController(controller);
      if (controller != null)
      {
         preprocessMetaData();
         serviceContext.state = ServiceContext.INSTALLED;
      }
      else
         serviceContext.state = ServiceContext.NOTYETINSTALLED;
   }

   /**
    * Preprocess the metadata for this context
    */
   protected void preprocessMetaData()
   {
      if (serviceMetaData == null)
         return;
      PreprocessMetaDataVisitor visitor = new PreprocessMetaDataVisitor();
      AccessController.doPrivileged(visitor);
   }
   
   /**
    * A visitor for the metadata that looks for dependencies.
    */
   protected class PreprocessMetaDataVisitor implements ServiceMetaDataVisitor, PrivilegedAction<Object>
   {
      /** The current context for when the dependencies are required */ 
      private ControllerState contextState = ControllerState.INSTANTIATED;
      
      /**
       * Visit the bean metadata node, this is the starting point
       */
      public Object run()
      {
         serviceMetaData.visit(this);
         return null;
      }
      
      /**
       * Visit a node
       * 
       * @param node the node
       */
      public void visit(ServiceMetaDataVisitorNode node)
      {
         boolean trace = log.isTraceEnabled();
         if (trace)
            log.trace("Visit node " + node);
         
         // Visit the children of this node
         Iterator<? extends ServiceMetaDataVisitorNode> children = node.getChildren();
         if (children != null)
         {
            ControllerState restoreState = contextState;
            while (children.hasNext())
            {
               ServiceMetaDataVisitorNode child = children.next();
               try
               {
                  child.visit(this);
               }
               finally
               {
                  contextState = restoreState;
               }
            }
         }
      }

      public ServiceControllerContext getControllerContext()
      {
         return ServiceControllerContext.this;
      }
      
      public ControllerState getContextState()
      {
         return contextState;
      }
      
      public void addDependency(DependencyItem dependency)
      {
         getDependencyInfo().addIDependOn(dependency);
      }

      public void setContextState(ControllerState contextState)
      {
         this.contextState = contextState;
      }
   }
}

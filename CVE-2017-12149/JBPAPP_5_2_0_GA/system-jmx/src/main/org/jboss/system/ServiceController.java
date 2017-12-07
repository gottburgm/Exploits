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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.management.MBeanRegistration;
import javax.management.MBeanServer;
import javax.management.Notification;
import javax.management.ObjectName;

import org.jboss.dependency.spi.Controller;
import org.jboss.dependency.spi.ControllerContext;
import org.jboss.dependency.spi.ControllerMode;
import org.jboss.dependency.spi.ControllerState;
import org.jboss.dependency.spi.DependencyInfo;
import org.jboss.dependency.spi.DependencyItem;
import org.jboss.deployment.DeploymentInfo;
import org.jboss.deployment.DeploymentState;
import org.jboss.kernel.Kernel;
import org.jboss.kernel.plugins.bootstrap.basic.BasicBootstrap;
import org.jboss.kernel.spi.dependency.KernelController;
import org.jboss.logging.Logger;
import org.jboss.mx.server.ServerConstants;
import org.jboss.mx.util.JBossNotificationBroadcasterSupport;
import org.jboss.mx.util.ObjectNameFactory;
import org.jboss.system.metadata.ServiceMetaData;
import org.jboss.system.metadata.ServiceMetaDataParser;
import org.jboss.system.microcontainer.LifecycleDependencyItem;
import org.jboss.system.microcontainer.ServiceControllerContext;
import org.w3c.dom.Element;

/**
 * This is the main Service Controller. A controller can deploy a service to a
 * jboss.system It installs by delegating, it configures by delegating<p>
 *
 * This class has been rewritten to delegate to the microcontainer's
 * generic controller. Like the original ServiceController, all state
 * transitions must be handled manually, e.g. driven by the deployer
 * invoking create, start, stop, etc.
 * That is with one exception; we register ourselves an automatic context.
 *
 * @see org.jboss.system.Service
 *
 * @author <a href="mailto:marc.fleury@jboss.org">Marc Fleury</a>
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @author <a href="mailto:scott.stark@jboss.org">Scott Stark</a>
 * @author <a href="mailto:dimitris@jboss.org">Dimitris Andreadis</a>
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 81485 $
 */
public class ServiceController extends JBossNotificationBroadcasterSupport
   implements ServiceControllerMBean, MBeanRegistration
{
   /** The ObjectName of the default loader repository */
   public static final ObjectName DEFAULT_LOADER_REPOSITORY = ObjectNameFactory.create(ServerConstants.DEFAULT_LOADER_NAME);

   /** The operation name for lifecycle */
   public static final String JBOSS_INTERNAL_LIFECYCLE = "jbossInternalLifecycle";

   /** The signature for lifecycle operations */
   public static final String[] JBOSS_INTERNAL_LIFECYCLE_SIG = new String[] { String.class.getName() };

   /** Class logger. */
   private static final Logger log = Logger.getLogger(ServiceController.class);

   /** The kernel */
   protected Kernel kernel;

   /** A callback to the JMX MBeanServer */
   protected MBeanServer server;

   /** The contexts */
   protected Map<ObjectName, ServiceControllerContext> installed = new ConcurrentHashMap<ObjectName, ServiceControllerContext>(); 
   
   /** The contexts in installation order */
   protected CopyOnWriteArrayList<ServiceControllerContext> installedOrder = new CopyOnWriteArrayList<ServiceControllerContext>();

   /**
    * Rethrow an error as an exception
    * 
    * @param context the context
    * @param t the original throwable
    * @return never
    * @throws Exception always
    */
   public static Exception rethrow(String context, Throwable t) throws Exception
   {
      if (t instanceof Error)
         throw (Error) t;
      else if (t instanceof Exception)
         throw (Exception) t;
      throw new RuntimeException(context, t);
   }
   
   /**
    * Get exception that will expose stacktrace.
    *
    * @return the stracktrace exposing exception
    */
   protected Throwable getStackTrace()
   {
      //noinspection ThrowableInstanceNeverThrown
      return new Throwable("STACKTRACE");
   }

   /**
    * Get the MBeanServer
    * 
    * @return the server
    */
   public MBeanServer getMBeanServer()
   {
      return server;
   }

   /**
    * Set the server.
    * 
    * @param server the server.
    */
   public void setMBeanServer(MBeanServer server)
   {
      this.server = server;
   }

   /**
    * Get the kernel.
    * 
    * @return the kernel.
    */
   public Kernel getKernel()
   {
      return kernel;
   }

   /**
    * Set the kernel.
    * 
    * @param kernel the kernel.
    */
   public void setKernel(Kernel kernel)
   {
      this.kernel = kernel;
   }
   
   public List<ServiceContext> listDeployed()
   {
      // Retrieve the service context from all our installed contexts
      ArrayList<ServiceContext> result = new ArrayList<ServiceContext>(installedOrder.size());
      for (ServiceControllerContext context : installedOrder)
         result.add(context.getServiceContext());
      return result;
   }

   public List<ServiceContext> listIncompletelyDeployed()
   {
      // Retrieve the service contexts that are not deployed properly
      ArrayList<ServiceContext> result = new ArrayList<ServiceContext>();
      for (ServiceControllerContext context : installedOrder)
      {
         ServiceContext sc = context.getServiceContext();
         if (sc.state != ServiceContext.CREATED &&
             sc.state != ServiceContext.RUNNING &&
             sc.state != ServiceContext.STOPPED &&
             sc.state != ServiceContext.DESTROYED)
         {
            result.add(sc);
         }
      }
      return result;
   }

   public List<ObjectName> listDeployedNames()
   {
      // Get all the object names from our installed contexts
      ArrayList<ObjectName> result = new ArrayList<ObjectName>(installed.size());
      for (ObjectName name : installed.keySet())
         result.add(name);
      return result;
   }

   public String listConfiguration(ObjectName[] objectNames) throws Exception
   {
      return ServiceConfigurator.getConfiguration(server, this, objectNames);
   }

   public void validateDeploymentState(DeploymentInfo di, DeploymentState state)
   {
      List<ObjectName> mbeans = new ArrayList<ObjectName>(di.mbeans);
      if (di.deployedObject != null)
         mbeans.add(di.deployedObject);
      boolean mbeansStateIsValid = true;
      for (ObjectName serviceName : mbeans)
      {
         ServiceContext ctx = getServiceContext(serviceName);
         if (ctx != null && state == DeploymentState.STARTED)
            mbeansStateIsValid &= ctx.state == ServiceContext.RUNNING;
      }
      if (mbeansStateIsValid == true)
         di.state = state;
   }

   public List<ObjectName> install(List<ServiceMetaData> metaDatas, ObjectName loaderName) throws Exception
   {
      KernelController controller = kernel.getController();

      // Track the registered mbeans both for returning the result
      // and uninstalling in the event of an error
      List<ObjectName> result = new ArrayList<ObjectName>(metaDatas.size());
      List<ServiceControllerContext> contexts = new ArrayList<ServiceControllerContext>(metaDatas.size());

      // Go through each mbean in the passed xml
      for (ServiceMetaData metaData : metaDatas)
      {
         metaData.setClassLoaderName(loaderName);

         // Install the context to the configured level
         ServiceControllerContext context = new ServiceControllerContext(this, metaData);
         try
         {
            doInstall(controller, context);
            contexts.add(context);
            doChange(controller, context, ControllerState.CONFIGURED, "configure");
            result.add(context.getObjectName());
         }
         catch (Throwable t)
         {
            // Something went wrong
            for (ServiceControllerContext ctx : contexts)
               safelyRemoveAnyRegisteredContext(ctx);

            throw rethrow("Error during install", t);
         }
      }
      return result;
   }

   public ObjectName install(ServiceMetaData metaData, ObjectName loaderName) throws Exception
   {
      KernelController controller = kernel.getController();
      metaData.setClassLoaderName(loaderName);
      ObjectName name = metaData.getObjectName();
      
      // Install the context to the configured level
      ServiceControllerContext context = new ServiceControllerContext(this, metaData);
      try
      {
         doInstall(controller, context);
         doChange(controller, context, ControllerState.CONFIGURED, "configure");
         return context.getObjectName();
      }
      catch (Throwable t)
      {
         throw rethrow("Error during install " + name, t);
      }
   }

   public List<ObjectName> install(Element config, ObjectName loaderName) throws Exception
   {
      // Parse the xml
      ServiceMetaDataParser parser = new ServiceMetaDataParser(config);
      List<ServiceMetaData> metaDatas = parser.parse();
      return install(metaDatas, loaderName);
   }
   
   /**
    * Install an MBean without any meta data
    * 
    * @param name the object name
    * @param object the mbean object
    * @throws Exception for any error
    */
   public void install(ObjectName name, Object object) throws Exception
   {
      if (name == null)
         throw new IllegalArgumentException("Null name");
      if (object == null)
         throw new IllegalArgumentException("Null object");

      KernelController controller = kernel.getController();

      ServiceControllerContext context = new ServiceControllerContext(this, name, object); 
      try
      {
         doInstall(controller, context);
         doChange(controller, context, ControllerState.CONFIGURED, "configure");
      }
      catch (Throwable t)
      {
         // Something went wrong
         safelyRemoveAnyRegisteredContext(context);

         throw rethrow("Error during install", t);
      }
   }
   
   public void register(ObjectName serviceName) throws Exception
   {
      register(serviceName, null);
   }

   public void register(ObjectName serviceName, Collection<ObjectName> depends)  throws Exception
   {
      register(serviceName, depends, true);
   }

   /**
    * Register the mbean against the microkernel with dependencies.
    *
    * @param serviceName the object name
    * @param depends the dependencies
    * @param includeLifecycle the includes lifecycle flag
    * @throws Exception for any error
    */
   public void register(ObjectName serviceName, Collection<ObjectName> depends, boolean includeLifecycle)  throws Exception
   {
      register(serviceName, depends, includeLifecycle, null);
   }

   /**
    * Register the mbean against the microkernel with dependencies.
    *
    * @param serviceName the object name
    * @param depends the dependencies
    * @param includeLifecycle the includes lifecycle flag
    * @param target the target
    * @throws Exception for any error
    */
   public void register(ObjectName serviceName, Collection<ObjectName> depends, boolean includeLifecycle, Object target)  throws Exception
   {
      if (serviceName == null)
      {
         log.warn("Ignoring request to register null service: ", getStackTrace());
         return;
      }
      
      log.debug("Registering service " + serviceName);

      // This is an already registered mbean
      KernelController controller = kernel.getController();
      ServiceControllerContext context = new ServiceControllerContext(this, serviceName, includeLifecycle, target);
      if (depends != null)
         addDependencies(context, depends);

      // Install the context to the configured level
      try
      {
         doInstall(controller, context);
         doChange(controller, context, ControllerState.CONFIGURED, "configure");
      }
      catch (Throwable t)
      {
         // Something went wrong
         safelyRemoveAnyRegisteredContext(context);
         
         throw rethrow("Error during register: " + serviceName, t);
      }
   }
   
   public void create(ObjectName serviceName) throws Exception
   {
      create(serviceName, null);
   }

   public void create(ObjectName serviceName, Collection<ObjectName> depends) throws Exception
   {
      if (serviceName == null)
      {
         log.warn("Ignoring request to create null service: ", getStackTrace());
         return;
      }
      
      log.debug("Creating service " + serviceName);
      
      // Register if not already done so
      ServiceControllerContext context = installed.get(serviceName);
      if (context == null)
      {
         register(serviceName, depends);
         context = installed.get(serviceName);
      }
      ServiceContext ctx = context.getServiceContext();

      // If we are already created (can happen in dependencies) or failed just return
      if (ctx.state == ServiceContext.CREATED
            || ctx.state == ServiceContext.RUNNING
            || ctx.state == ServiceContext.FAILED)
      {
         log.debug("Ignoring create request for service: " + ctx.objectName + " at state " + ctx.getStateString());
         return;
      }

      // Request the mbean go to the created state
      KernelController controller = kernel.getController();
      try
      {
         doChange(controller, context, ControllerState.CREATE, "create");
      }
      catch (Throwable t)
      {
         log.warn("Problem creating service " + serviceName, t);
      }
   }
   
   public void start(ObjectName serviceName) throws Exception
   {
      if (serviceName == null)
      {
         log.warn("Ignoring request to start null service: ", getStackTrace());
         return;
      }

      log.debug("starting service " + serviceName);
      
      // Register if not already done so
      ServiceControllerContext context = installed.get(serviceName);
      if (context == null)
      {
         register(serviceName, null);
         context = installed.get(serviceName);
      }
      ServiceContext ctx = context.getServiceContext();

      // If we are already started (can happen in dependencies) just return
      if (ctx.state == ServiceContext.RUNNING || ctx.state == ServiceContext.FAILED)
      {
         log.debug("Ignoring start request for service: " + ctx.objectName + " at state " + ctx.getStateString());
         return;
      }

      // Request the mbean go to the fully installed state
      KernelController controller = kernel.getController();
      try
      {
         doChange(controller, context, ControllerState.INSTALLED, "start");
      }
      catch (Throwable t)
      {
         log.warn("Problem starting service " + serviceName, t);
      }
   }

   public void restart(ObjectName serviceName) throws Exception
   {
      if (serviceName == null)
      {
         log.warn("Ignoring request to restart null service: ", getStackTrace());
         return;
      }

      log.debug("restarting service " + serviceName);
      stop(serviceName);
      start(serviceName);
   }

   public void stop(ObjectName serviceName) throws Exception
   {
      if (serviceName == null)
      {
         log.warn("Ignoring request to stop null service: ", getStackTrace());
         return;
      }

      log.debug("stopping service: " + serviceName);

      ServiceControllerContext context = installed.get(serviceName);
      if (context == null)
      {
         log.warn("Ignoring request to stop nonexistent service: " + serviceName);
         return;
      }

      // If we are already stopped (can happen in dependencies) just return
      ServiceContext ctx = context.getServiceContext();
      if (ctx.state != ServiceContext.RUNNING)
      {
         log.debug("Ignoring stop request for service: " + ctx.objectName + " at state " + ctx.getStateString());
         return;
      }

      // Request the mbean go back to the created state
      KernelController controller = kernel.getController();
      try
      {
         doChange(controller, context, ControllerState.CREATE, null);
      }
      catch (Throwable t)
      {
         log.warn("Problem stopping service " + serviceName, t);
      }
   }

   public void destroy(ObjectName serviceName) throws Exception
   {
      if (serviceName == null)
      {
         log.warn("Ignoring request to destroy null service: ", getStackTrace());
         return;
      }

      log.debug("destroying service: " + serviceName);

      ServiceControllerContext context = installed.get(serviceName);
      if (context == null)
      {
         log.warn("Ignoring request to destroy nonexistent service: " + serviceName);
         return;
      }

      // If we are already destroyed (can happen in dependencies) just return
      ServiceContext ctx = context.getServiceContext();
      if (ctx.state == ServiceContext.DESTROYED || ctx.state == ServiceContext.NOTYETINSTALLED || ctx.state == ServiceContext.FAILED)
      {
         log.debug("Ignoring destroy request for service: " + ctx.objectName + " at state " + ctx.getStateString());
         return;
      }

      // Request the mbean go the configured state
      KernelController controller = kernel.getController();
      try
      {
         doChange(controller, context, ControllerState.CONFIGURED, null);
      }
      catch (Throwable t)
      {
         log.warn("Problem stopping service " + serviceName, t);
      }
   }

   public void remove(ObjectName objectName) throws Exception
   {
      if (objectName == null)
      {
         log.warn("Ignoring request to remove null service: ", getStackTrace());
         return;
      }

      // Removal can be attempted twice, this is because ServiceMBeanSupport does a "double check" 
      // to make sure the ServiceController is tidied up
      // However, if the tidyup is done correctly, it invokes this method recursively:
      // ServiceController::remove -> MBeanServer::unregisterMBean
      // ServiceMBeanSupport::postDeregister -> ServiceController::remove
      ServiceControllerContext context = installed.remove(objectName);
      if (context == null)
      {
         log.trace("Ignoring request to remove nonexistent service: " + objectName);
         return;
      }
      installedOrder.remove(context);
      log.debug("removing service: " + objectName);

      // Uninstall the context
      safelyRemoveAnyRegisteredContext(context);
   }

   public ServiceContext getServiceContext(ObjectName serviceName)
   {
      ServiceControllerContext context = installed.get(serviceName);
      if (context != null)
         return context.getServiceContext();
      return null;
   }
   
   public void shutdown()
   {
      log.debug("Stopping " + installedOrder.size() + " services");

      KernelController controller = kernel.getController();
      
      int serviceCounter = 0;

      // Uninstall all the contexts we know about
      ListIterator<ServiceControllerContext> iterator = installedOrder.listIterator(installedOrder.size());
      while (iterator.hasPrevious())
      {
         ServiceControllerContext context = iterator.previous();
         controller.uninstall(context.getName());
         ++serviceCounter;
      }
      log.debug("Stopped " + serviceCounter + " services");
      
      // Shutdown ourselves
      controller.uninstall(ServiceControllerMBean.OBJECT_NAME.getCanonicalName());
   }

   public ObjectName preRegister(MBeanServer server, ObjectName name)
         throws Exception
   {
      this.server = server;

      if( kernel == null )
      {
         // Bootstrap the microcontainer. 
         BasicBootstrap bootstrap = new BasicBootstrap();
         bootstrap.run();
         kernel = bootstrap.getKernel();
      }

      log.debug("Controller MBean online");
      return name == null ? OBJECT_NAME : name;
   }

   public void postRegister(Boolean registrationDone)
   {
      if (registrationDone == false)
         log.fatal("Registration of ServiceController failed");
      else
      {
         // Register the ServiceController as a running service
         KernelController controller = kernel.getController();
         ServiceControllerContext context = new ServiceControllerContext(this, ServiceControllerMBean.OBJECT_NAME);
         context.setMode(ControllerMode.AUTOMATIC);
         try
         {
            controller.install(context);
         }
         catch (Throwable t)
         {
            log.fatal("Error registering service controller", t);
         }
      }
   }

   public void preDeregister()
         throws Exception
   {
   }

   public void postDeregister()
   {
      installed.clear();
      installedOrder.clear();
      server = null;
   }
   
   /**
    * Install a context
    * 
    * @param controller the controller
    * @param context the context
    * @throws Throwable for any error
    */
   private void doInstall(KernelController controller, ServiceControllerContext context) throws Throwable
   {
      controller.install(context);
      installed.put(context.getObjectName(), context);
      installedOrder.add(context);
   }
   
   /**
    * Change a context
    * 
    * @param controller the controller
    * @param context the context
    * @param requiredState the require state
    * @param logWait log the waiting dependencies
    * @throws Throwable for any error
    */
   private void doChange(KernelController controller, ServiceControllerContext context, ControllerState requiredState, String logWait) throws Throwable
   {
      if (ControllerMode.ON_DEMAND.equals(context.getMode()) == false)
      {
         controller.change(context, requiredState);
         ControllerState state = context.getState();
         if (logWait != null && requiredState.equals(state) == false && state != ControllerState.ERROR)
            log.debug("Waiting in " + logWait + " of " + context.getObjectName() + " on " + getUnresolvedDependencies(context, requiredState));
      }
   }

   /**
    * Sends outs controller notifications about service lifecycle events
    * 
    * @param type the notification type
    * @param serviceName the service name
    */
   public void sendControllerNotification(String type, ObjectName serviceName)
   {
      Notification notification = new Notification(type, this, super.nextNotificationSequenceNumber());
      notification.setUserData(serviceName);
      sendNotification(notification);
   }

   /**
    * Add the passed lifecycle dependencies to the context
    * 
    * @param context the context
    * @param depends the dependencies
    */
   private void addDependencies(ServiceControllerContext context, Collection<ObjectName> depends)
   {
      DependencyInfo info = context.getDependencyInfo();
      for (ObjectName other : depends)
      {
         info.addIDependOn(new LifecycleDependencyItem(context.getName(), other.getCanonicalName(), ControllerState.CREATE));
         info.addIDependOn(new LifecycleDependencyItem(context.getName(), other.getCanonicalName(), ControllerState.START));
      }
   }

   /**
    * Get the unresolved dependencies
    * 
    * @param context the context
    * @param state the state we want to move to
    * @return the unresolved dependencies
    */
   private String getUnresolvedDependencies(ServiceControllerContext context, ControllerState state)
   {
      boolean first = true;
      
      StringBuilder builder = new StringBuilder();
      for (DependencyItem item : context.getDependencyInfo().getUnresolvedDependencies(null))
      {
         if (item.isResolved() == false && item.getWhenRequired() == state)
         {
            if (first)
               first = false;
            else
               builder.append(' ');
            builder.append(item.getIDependOn());
         }
      }
      return builder.toString();
   }

   /**
    * Safely remove any potentially registered context (usually after an error)
    * 
    * @param ctx the context
    */
   private void safelyRemoveAnyRegisteredContext(ServiceControllerContext ctx)
   {
      // First the context must have a controller
      Controller controller = ctx.getController();
      if (controller != null)
      {
         // The name must be registered and it must be our context
         Object name = ctx.getName();
         ControllerContext registered = controller.getContext(name, null);
         if (registered == ctx)
            controller.uninstall(name);
      }
   }
}

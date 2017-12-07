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
package org.jboss.test.system.controller.legacy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import javax.management.JMException;
import javax.management.MBeanInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanRegistration;
import javax.management.MBeanServer;
import javax.management.Notification;
import javax.management.ObjectName;

import org.jboss.deployment.DeploymentException;
import org.jboss.deployment.DeploymentInfo;
import org.jboss.deployment.DeploymentState;
import org.jboss.logging.Logger;
import org.jboss.mx.server.ServerConstants;
import org.jboss.mx.util.JBossNotificationBroadcasterSupport;
import org.jboss.mx.util.JMXExceptionDecoder;
import org.jboss.mx.util.ObjectNameFactory;
import org.jboss.system.Service;
import org.jboss.system.ServiceContext;
import org.jboss.system.ServiceFactory;
import org.jboss.system.ServiceMBean;
import org.w3c.dom.Element;

/**
 * This is the main Service Controller. A controller can deploy a service to a
 * jboss.system It installs by delegating, it configures by delegating
 *
 * @see org.jboss.system.Service
 *
 * @author <a href="mailto:marc.fleury@jboss.org">Marc Fleury</a>
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @author <a href="mailto:scott.stark@jboss.org">Scott Stark</a>
 * @author <a href="mailto:dimitris@jboss.org">Dimitris Andreadis</a>
 * @version $Revision: 85945 $
 */
public class OldServiceController extends JBossNotificationBroadcasterSupport
   implements OldServiceControllerMBean, MBeanRegistration
{
   /** The ObjectName of the default loader repository */
   public static final ObjectName DEFAULT_LOADER_REPOSITORY = ObjectNameFactory.create(ServerConstants.DEFAULT_LOADER_NAME);

   /** The operation name for lifecycle */
   public static final String JBOSS_INTERNAL_LIFECYCLE = "jbossInternalLifecycle";

   /** The signature for lifecycle operations */
   public static final String[] JBOSS_INTERNAL_LIFECYCLE_SIG = new String[] { String.class.getName() };

   /** Class logger. */
   private static final Logger log = Logger.getLogger(OldServiceController.class);

   /** A callback to the JMX MBeanServer */
   protected MBeanServer server;

   /** Creator, helper class to instantiate MBeans **/
   protected OldServiceCreator creator;

   /** Configurator, helper class to configure MBeans **/
   protected OldServiceConfigurator configurator;

   /** ObjectName to ServiceContext map **/
   protected Map<ObjectName, ServiceContext> nameToServiceMap = Collections.synchronizedMap(new HashMap<ObjectName, ServiceContext>());

   /** A linked list of services in the order they were created **/
   protected List<ServiceContext> installedServices = new LinkedList<ServiceContext>();

   public List<ServiceContext> listDeployed()
   {
      return new ArrayList<ServiceContext>(installedServices);
   }

   public List<ServiceContext> listIncompletelyDeployed()
   {
      List<ServiceContext> id = new ArrayList<ServiceContext>();
      for (Iterator<ServiceContext> i = installedServices.iterator(); i.hasNext();)
      {
         ServiceContext sc = i.next();
         if ( sc.state != ServiceContext.CREATED &&
              sc.state != ServiceContext.RUNNING &&
              sc.state != ServiceContext.STOPPED &&
              sc.state != ServiceContext.DESTROYED )
         {
            id.add(sc);
         }
      }
      return id;
   }

   public List<ObjectName> listDeployedNames()
   {
      List<ObjectName> names = new ArrayList<ObjectName>(installedServices.size());
      for (Iterator<ServiceContext> i = installedServices.iterator(); i.hasNext();)
      {
         ServiceContext ctx = i.next();
         names.add(ctx.objectName);
      }

      return names;
   }

   public String listConfiguration(ObjectName[] objectNames) throws Exception
   {
      return configurator.getConfiguration(objectNames);
   }

   public void validateDeploymentState(DeploymentInfo di, DeploymentState state)
   {
      ArrayList<ObjectName> mbeans = new ArrayList<ObjectName>(di.mbeans);
      if (di.deployedObject != null)
         mbeans.add(di.deployedObject);
      boolean mbeansStateIsValid = true;
      for (int m = 0; m < mbeans.size(); m++)
      {
         ObjectName serviceName = mbeans.get(m);
         ServiceContext ctx = this.getServiceContext(serviceName);
         if (ctx != null && state == DeploymentState.STARTED)
            mbeansStateIsValid &= ctx.state == ServiceContext.RUNNING;
      }
      if (mbeansStateIsValid == true)
         di.state = state;
   }

   public synchronized List<ObjectName> install(Element config, ObjectName loaderName) throws DeploymentException
   {
      List<ObjectName> mbeans = configurator.install(config, loaderName);
      for (Iterator<ObjectName> i = mbeans.iterator(); i.hasNext();)
      {
         ObjectName mbean = i.next();
         installedServices.add(createServiceContext(mbean));
      }
      return mbeans;
   }

   public synchronized void register(ObjectName serviceName) throws Exception
   {
      register(serviceName, null);
   }

   public synchronized void register(ObjectName serviceName, Collection<ObjectName> depends)
         throws Exception
   {
      if (serviceName == null)
      {
         log.warn("Ignoring request to register null service: ", new Exception("STACKTRACE"));
         return;
      }
      
      log.debug("Registering service " + serviceName);
      ServiceContext ctx = createServiceContext(serviceName);

      register(ctx, depends);
   }

   public synchronized void create(ObjectName serviceName) throws Exception
   {
      create(serviceName, null);
   }

   public synchronized void create(ObjectName serviceName, Collection<ObjectName> depends)
         throws Exception
   {
      if (serviceName == null)
      {
         log.warn("Ignoring request to create null service: ", new Exception("STACKTRACE"));
         return;
      }
      
      log.debug("Creating service " + serviceName);
      ServiceContext ctx = createServiceContext(serviceName);

      // Register the context and its dependencies if necessary
      register(ctx, depends);

      // If we are already created (can happen in dependencies) or failed just return
      if (ctx.state == ServiceContext.CREATED
            || ctx.state == ServiceContext.RUNNING
            || ctx.state == ServiceContext.FAILED)
      {
         log.debug("Ignoring create request for service: " + ctx.objectName);
         return;
      }

      // JSR 77, and to avoid circular dependencies
      int oldState = ctx.state;
      ctx.state = ServiceContext.CREATED;

      // Are all the mbeans I depend on created?   if not just return
      for (Iterator iterator = ctx.iDependOn.iterator(); iterator.hasNext();)
      {
         ServiceContext sc = (ServiceContext) iterator.next();
         int state = sc.state;

         // A dependent is not created or running
         if (!(state == ServiceContext.CREATED || state == ServiceContext.RUNNING))
         {
            log.debug("waiting in create of " + serviceName +
                  " waiting on " + sc.objectName);
            ctx.state = oldState;
            return;
         }
      }

      // Call create on the service Proxy
      try
      {
         ctx.proxy.create();
         sendControllerNotification(ServiceMBean.CREATE_EVENT, serviceName);            
      }
      catch (Throwable e)
      {
         ctx.state = ServiceContext.FAILED;
         ctx.problem = e;
         log.warn("Problem creating service " + serviceName, e);
         return;
      }

      // Those that depend on me are waiting for my creation, recursively create them
      log.debug("Creating dependent components for: " + serviceName
            + " dependents are: " + ctx.dependsOnMe);
      ArrayList<ServiceContext> tmp = new ArrayList<ServiceContext>(ctx.dependsOnMe);
      for (int n = 0; n < tmp.size(); n++)
      {
         // marcf fixme circular dependencies?
         ServiceContext ctx2 = tmp.get(n);
         create(ctx2.objectName);
      }
      tmp.clear();
   }

   public synchronized void start(ObjectName serviceName) throws Exception
   {
      if (serviceName == null)
      {
         log.warn("Ignoring request to start null service: ", new Exception("STACKTRACE"));
         return;
      }

      log.debug("starting service " + serviceName);

      ServiceContext ctx = createServiceContext(serviceName);

      if (!installedServices.contains(ctx))
         installedServices.add(ctx);

      // If we are already started (can happen in dependencies) just return
      if (ctx.state == ServiceContext.RUNNING || ctx.state == ServiceContext.FAILED)
      {
         log.debug("Ignoring start request for service: " + ctx.objectName);
         return;
      }

      // Start() is called before create(), so call create() to compensate
      if (ctx.state != ServiceContext.CREATED && ctx.state != ServiceContext.STOPPED)
      {
         log.debug("Start requested before create, calling create now for service: " + serviceName);
         create(serviceName);
      }
      
      // Get the fancy service proxy (for the lifecycle API)
      if (ctx.proxy == null)
         ctx.proxy = getServiceProxy(ctx.objectName, null);

      // JSR 77, and to avoid circular dependencies
      int oldState = ctx.state;
      ctx.state = ServiceContext.RUNNING;

      // Are all the mbeans I depend on started?   if not just return
      for (Iterator iterator = ctx.iDependOn.iterator(); iterator.hasNext();)
      {
         ServiceContext sctx = (ServiceContext) iterator.next();

         int state = sctx.state;

         // A dependent is not running
         if (!(state == ServiceContext.RUNNING))
         {
            log.debug("waiting in start " + serviceName + " on " + sctx.objectName);
            ctx.state = oldState;
            return;
         }
      }

      // Call start on the service Proxy
      try
      {
         ctx.proxy.start();
         sendControllerNotification(ServiceMBean.START_EVENT, serviceName);            
      }
      catch (Throwable e)
      {
         ctx.state = ServiceContext.FAILED;
         ctx.problem = e;
         log.warn("Problem starting service " + serviceName, e);
         return;
      }
      // Those that depend on me are waiting for my start, recursively start them
      log.debug("Starting dependent components for: " + serviceName
            + " dependent components: " + ctx.dependsOnMe);
      ArrayList<ServiceContext> tmp = new ArrayList<ServiceContext>(ctx.dependsOnMe);
      for (int n = 0; n < tmp.size(); n++)
      {
         // marcf fixme circular dependencies?
         ServiceContext ctx2 = tmp.get(n);
         start(ctx2.objectName);
      }
      tmp.clear();
   }

   public void restart(ObjectName serviceName) throws Exception
   {
      if (serviceName == null)
      {
         log.warn("Ignoring request to restart null service: ", new Exception("STACKTRACE"));
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
         log.warn("Ignoring request to stop null service: ", new Exception("STACKTRACE"));
         return;
      }

      ServiceContext ctx = nameToServiceMap.get(serviceName);
      log.debug("stopping service: " + serviceName);

      if (ctx == null)
      {
         log.warn("Ignoring request to stop nonexistent service: " + serviceName);
         return;
      }

      // If we are already stopped (can happen in dependencies) just return
      if (ctx.state != ServiceContext.RUNNING) return;

      // JSR 77 and to avoid circular dependencies
      ctx.state = ServiceContext.STOPPED;

      log.debug("stopping dependent services for: " + serviceName
            + " dependent services are: " + ctx.dependsOnMe);
      
      ArrayList<ServiceContext> tmp = new ArrayList<ServiceContext>(ctx.dependsOnMe);
      for (int n = 0; n < tmp.size(); n++)
      {
         // stop all the mbeans that depend on me
         ServiceContext ctx2 = tmp.get(n);
         ObjectName other = ctx2.objectName;
         stop(other);
      }
      tmp.clear();

      // Call stop on the service Proxy
      if (ctx.proxy != null)
      {
         try
         {
            ctx.proxy.stop();
            sendControllerNotification(ServiceMBean.STOP_EVENT, serviceName);
         }
         catch (Throwable e)
         {
            ctx.state = ServiceContext.FAILED;
            ctx.problem = e;
            log.warn("Problem stopping service " + serviceName, e);
         }
      }
   }

   public void destroy(ObjectName serviceName) throws Exception
   {
      if (serviceName == null)
      {
         log.warn("Ignoring request to destroy null service: ", new Exception("STACKTRACE"));
         return;
      }

      ServiceContext ctx = nameToServiceMap.get(serviceName);
      log.debug("destroying service: " + serviceName);

      if (ctx == null)
      {
         log.warn("Ignoring request to destroy nonexistent service: " + serviceName);
         return;
      }

      // If we are already destroyed (can happen in dependencies) just return
      if (ctx.state == ServiceContext.DESTROYED ||
          ctx.state == ServiceContext.NOTYETINSTALLED)
         return;
      
      // If we are still running, stop service first
      if (ctx.state == ServiceContext.RUNNING)
      {
         log.debug("Destroy requested before stop, calling stop now for service: " + serviceName);
         stop(serviceName);
      }

      // JSR 77, and to avoid circular dependencies
      ctx.state = ServiceContext.DESTROYED;

      log.debug("destroying dependent services for: " + serviceName
            + " dependent services are: " + ctx.dependsOnMe);
      
      ArrayList<ServiceContext> tmp = new ArrayList<ServiceContext>(ctx.dependsOnMe);
      for (int n = 0; n < tmp.size(); n++)
      {
         // destroy all the mbeans that depend on me
         ServiceContext ctx2 = tmp.get(n);
         ObjectName other = ctx2.objectName;
         destroy(other);
      }
      tmp.clear();

      // Call destroy on the service Proxy
      if (ctx.proxy != null)
      {
         try
         {
            ctx.proxy.destroy();
            sendControllerNotification(ServiceMBean.DESTROY_EVENT, serviceName);
         }
         catch (Throwable e)
         {
            ctx.state = ServiceContext.FAILED;
            ctx.problem = e;
            log.warn("Problem destroying service " + serviceName, e);
         }
      }
   }

   public void remove(ObjectName objectName) throws Exception
   {
      if (objectName == null)
      {
         log.warn("Ignoring request to remove null service: ", new Exception("STACKTRACE"));
         return;
      }

      ServiceContext ctx = nameToServiceMap.get(objectName);
      if (ctx == null)
      {
         log.debug("Ignoring request to remove nonexistent service: " + objectName);
         return;
      }
      log.debug("removing service: " + objectName);

      // Notify those that think I depend on them
      Iterator iterator = ctx.iDependOn.iterator();
      while (iterator.hasNext())
      {
         ServiceContext iDependOnContext = (ServiceContext) iterator.next();
         iDependOnContext.dependsOnMe.remove(ctx);

         // Remove any context whose only reason for existence is that
         // we depend on it, i.e. it otherwise unknown to the system
         if (iDependOnContext.state == ServiceContext.NOTYETINSTALLED
               && iDependOnContext.dependsOnMe.size() == 0)
         {
            nameToServiceMap.remove(iDependOnContext.objectName);
            log.debug("Removing context for nonexistent service it is " +
                  "no longer recording dependencies: " + iDependOnContext);
         }
      }
      //We remove all traces of our dependency configuration, since we
      //don't know what will show up the next time we are deployed.
      ctx.iDependOn.clear();

      // Do we have a deployed MBean?
      if (server.isRegistered(objectName))
      {
         log.debug("removing " + objectName + " from server");

         // Remove the context, unless it is still recording dependencies
         if (ctx.dependsOnMe.size() == 0)
            nameToServiceMap.remove(objectName);
         else
         {
            log.debug("Context not removed, it is recording " +
                  "dependencies: " + ctx);
            ctx.proxy = null;
         }

         // remove the mbean from the instaled ones
         installedServices.remove(ctx);
         creator.remove(objectName);
      }
      else
      {
         // Remove the context, unless it is still recording dependencies
         installedServices.remove(ctx);
         if (ctx.dependsOnMe.size() == 0)
         {
            log.debug("removing already unregistered " + objectName + " from server");
            nameToServiceMap.remove(objectName);
         }
         else
         {
            log.debug("no need to remove " + objectName + " from server");
            ctx.proxy = null;
         }
      }
      // This context is no longer installed, but it may still exist
      // to record dependent services
      ctx.state = ServiceContext.NOTYETINSTALLED;
   }

   /**
    * Lookup the ServiceContext for the given serviceName
    *
    * @jmx.managed-operation
    */
   public ServiceContext getServiceContext(ObjectName serviceName)
   {
      ServiceContext ctx = nameToServiceMap.get(serviceName);
      return ctx;
   }
   
   public void shutdown()
   {
      log.debug("Stopping " + nameToServiceMap.size() + " services");

      List<ServiceContext> servicesCopy = new ArrayList<ServiceContext>(installedServices);

      int serviceCounter = 0;
      ObjectName name = null;

      ListIterator i = servicesCopy.listIterator(servicesCopy.size());
      while (i.hasPrevious())
      {
         ServiceContext ctx = (ServiceContext) i.previous();
         name = ctx.objectName;

         // Go through the full stop/destroy cycle
         try
         {
            stop(name);
         }
         catch (Throwable e)
         {
            log.error("Could not stop mbean: " + name, e);
         }
         try
         {
            destroy(name);
         }
         catch (Throwable e)
         {
            log.error("Could not destroy mbean: " + name, e);
         }
         try
         {
            remove(name);
            serviceCounter++;
         }
         catch (Throwable e)
         {
            log.error("Could not remove mbean: " + name, e);
         }
      }
      log.debug("Stopped " + serviceCounter + " services");
   }
   
   // MBeanRegistration implementation ----------------------------------------

   /**
    * #Description of the Method
    *
    * @param server Description of Parameter
    * @param name Description of Parameter
    * @return Description of the Returned Value
    * @exception Exception Description of Exception
    */
   public ObjectName preRegister(MBeanServer server, ObjectName name)
         throws Exception
   {
      this.server = server;

      creator = new OldServiceCreator(server);
      configurator = new OldServiceConfigurator(server, this, creator);

      // Register the ServiceController as a running service
      ServiceContext sc = this.createServiceContext(name);
      sc.state = ServiceContext.RUNNING;

      log.debug("Controller MBean online");
      return name == null ? OBJECT_NAME : name;
   }

   public void postRegister(Boolean registrationDone)
   {
      if (!registrationDone.booleanValue())
      {
         log.info("Registration of ServiceController failed");
      }
   }

   public void preDeregister()
         throws Exception
   {
   }

   public void postDeregister()
   {
      nameToServiceMap.clear();
      installedServices.clear();
      creator.shutdown();
      creator = null;
      configurator = null;
      server = null;
   }

   // Package Protected ---------------------------------------------
   
   // Create a Service Context for the service, or get one if it exists
   synchronized ServiceContext createServiceContext(ObjectName objectName)
   {
      // If it is already there just return it
      if (nameToServiceMap.containsKey(objectName))
         return nameToServiceMap.get(objectName);

      // If not create it, add it and return it
      ServiceContext ctx = new ServiceContext();
      ctx.objectName = objectName;

      // we keep track of these here
      nameToServiceMap.put(objectName, ctx);

      return ctx;
   }

   void registerDependency(ObjectName needs, ObjectName used)
   {
      log.debug("recording that " + needs + " depends on " + used);
      ServiceContext needsCtx = createServiceContext(needs);
      ServiceContext usedCtx = createServiceContext(used);


      if (!needsCtx.iDependOn.contains(usedCtx))
      {
         // needsCtx depends on usedCtx
         needsCtx.iDependOn.add(usedCtx);
         // UsedCtx needs to know I depend on him
         usedCtx.dependsOnMe.add(needsCtx);
      }
   }

   // Private -------------------------------------------------------
   
   /**
    * Register the service context and its dependencies against the microkernel.
    * If the context is already registered it does nothing.
    * 
    * @param ctx the ServiceContext to register
    * @param depends a collection of ObjectNames of services the registered service depends on
    */
   private void register(ServiceContext ctx, Collection depends) throws Exception
   {
      if (!installedServices.contains(ctx))
         installedServices.add(ctx);
      
      if (depends != null)
      {
         log.debug("adding depends in ServiceController.register: " + depends);
         for (Iterator i = depends.iterator(); i.hasNext();)
         {
            registerDependency(ctx.objectName, (ObjectName) i.next());
         }
      }

      // Get the fancy service proxy (for the lifecycle API), if needed
      if (ctx.proxy == null)
         ctx.proxy = getServiceProxy(ctx.objectName, null);
   }
   
   /**
    * Get the Service interface through which the mbean given by objectName
    * will be managed.
    *
    * @param objectName
    * @param serviceFactory
    * @return The Service value
    *
    * @throws ClassNotFoundException
    * @throws InstantiationException
    * @throws IllegalAccessException
    */
   private Service getServiceProxy(ObjectName objectName, String serviceFactory)
         throws ClassNotFoundException, InstantiationException,
         IllegalAccessException, JMException
   {
      Service service = null;
      ClassLoader loader = Thread.currentThread().getContextClassLoader();
      if (serviceFactory != null && serviceFactory.length() > 0)
      {
         Class clazz = loader.loadClass(serviceFactory);
         ServiceFactory factory = (ServiceFactory) clazz.newInstance();
         service = factory.createService(server, objectName);
      }
      else
      {
         MBeanInfo info = server.getMBeanInfo(objectName);
         MBeanOperationInfo[] opInfo = info.getOperations();
         Class[] interfaces = {Service.class};
         InvocationHandler handler = new ServiceProxy(objectName, opInfo);
         service = (Service) Proxy.newProxyInstance(Service.class.getClassLoader(), interfaces, handler);
      }

      return service;
   }

   /**
    * Sends outs controller notifications about service lifecycle events 
    */
   private void sendControllerNotification(String type, ObjectName serviceName)
   {
      Notification notification = new Notification(type, this, super.nextNotificationSequenceNumber());
      notification.setUserData(serviceName);
      sendNotification(notification);
   }
   
   // Inner classes -------------------------------------------------

   /**
    * A mapping from the Service interface method names to the corresponding
    * index into the ServiceProxy.hasOp array.
    */
   private static HashMap<String, Integer> serviceOpMap = new HashMap<String, Integer>();

   /**
    * An implementation of InvocationHandler used to proxy of the Service
    * interface for mbeans. It determines which of the start/stop
    * methods of the Service interface an mbean implements by inspecting its
    * MBeanOperationInfo values. Each Service interface method that has a
    * matching operation is forwarded to the mbean by invoking the method
    * through the MBeanServer object.
    */
   public class ServiceProxy implements InvocationHandler
   {
      private boolean[] hasOp = {false, false, false, false};
      private ObjectName objectName;

      /** Whether we have the lifecycle method */
      private boolean hasJBossInternalLifecycle;

      /**
       * Go through the opInfo array and for each operation that matches on of
       * the Service interface methods set the corresponding hasOp array value
       * to true.
       *
       * @param objectName
       * @param opInfo
       */
      public ServiceProxy(ObjectName objectName, MBeanOperationInfo[] opInfo)
      {
         this.objectName = objectName;

         for (int op = 0; op < opInfo.length; op++)
         {
            MBeanOperationInfo info = opInfo[op];
            String name = info.getName();

            if (name.equals(JBOSS_INTERNAL_LIFECYCLE))
            {
               hasJBossInternalLifecycle = true;
               continue;
            }

            Integer opID = serviceOpMap.get(name);
            if (opID == null)
            {
               continue;
            }

            // Validate that is a no-arg void return type method
            if (info.getReturnType().equals("void") == false)
            {
               continue;
            }
            if (info.getSignature().length != 0)
            {
               continue;
            }

            hasOp[opID.intValue()] = true;
         }
      }

      /**
       * Map the method name to a Service interface method index and if the
       * corresponding hasOp array element is true, dispatch the method to the
       * mbean we are proxying.
       *
       * @param proxy
       * @param method
       * @param args
       * @return             Always null.
       * @throws Throwable
       */
      public Object invoke(Object proxy, Method method, Object[] args)
            throws Throwable
      {
         String name = method.getName();

         if (hasJBossInternalLifecycle)
         {
            try
            {
               server.invoke(objectName, JBOSS_INTERNAL_LIFECYCLE, new Object[] { name }, JBOSS_INTERNAL_LIFECYCLE_SIG);
               return null;
            }
            catch (Exception e)
            {
               throw JMXExceptionDecoder.decode(e);
            }
         }

         Integer opID = serviceOpMap.get(name);

         if (opID != null && hasOp[opID.intValue()] == true)
         {
            // deal with those pesky JMX exceptions
            try
            {
               String[] sig = {};
               server.invoke(objectName, name, args, sig);
            }
            catch (Exception e)
            {
               throw JMXExceptionDecoder.decode(e);
            }
         }

         return null;
      }
   }

   /**
    * Initialize the service operation map.
    */
   static
   {
      serviceOpMap.put("create", new Integer(0));
      serviceOpMap.put("start", new Integer(1));
      serviceOpMap.put("destroy", new Integer(2));
      serviceOpMap.put("stop", new Integer(3));
   }
}

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

import javax.management.AttributeChangeNotification;
import javax.management.JMException;
import javax.management.MBeanInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanRegistration;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.jboss.beans.metadata.api.annotations.Create;
import org.jboss.beans.metadata.api.annotations.Destroy;
import org.jboss.beans.metadata.api.annotations.Start;
import org.jboss.beans.metadata.api.annotations.Stop;
import org.jboss.dependency.spi.Controller;
import org.jboss.dependency.spi.ControllerState;
import org.jboss.deployment.DeploymentInfo;
import org.jboss.deployment.SARDeployerMBean;
import org.jboss.kernel.spi.dependency.KernelControllerContext;
import org.jboss.kernel.spi.dependency.KernelControllerContextAware;
import org.jboss.logging.Logger;
import org.jboss.mx.util.JBossNotificationBroadcasterSupport;

/**
 * An abstract base class JBoss services can subclass to implement a
 * service that conforms to the ServiceMBean interface. Subclasses must
 * override {@link #getName} method and should override 
 * {@link #startService}, and {@link #stopService} as approriate.
 *
 * @see ServiceMBean
 * 
 * @author <a href="mailto:rickard.oberg@telkel.com">Rickard Öberg</a>
 * @author Scott.Stark@jboss.org
 * @author <a href="mailto:andreas@jboss.org">Andreas Schaefer</a>
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @version $Revision: 81033 $
 */
public class ServiceMBeanSupport
   extends JBossNotificationBroadcasterSupport
   implements ServiceMBean, MBeanRegistration, KernelControllerContextAware
{
   /** The signature for service controller operations */
   public static final String[] SERVICE_CONTROLLER_SIG = new String[] { ObjectName.class.getName() };
   
   /**
    * The instance logger for the service.  Not using a class logger
    * because we want to dynamically obtain the logger name from
    * concreate sub-classes.
    */
   protected Logger log;
   
   /** The MBeanServer which we have been register with. */
   protected MBeanServer server;

   /** The object name which we are registsred under. */
   protected ObjectName serviceName;

   /** The current state this service is in. */
   private int state = UNREGISTERED;

   /** For backwards compatibility */
   private boolean isJBossInternalLifecycleExposed = false;

   /** The controller context */
   private KernelControllerContext controllerContext;
   
   /**
    * Construct a <t>ServiceMBeanSupport</tt>.
    *
    * <p>Sets up logging.
    */
   public ServiceMBeanSupport()
   {
      // can not call this(Class) because we need to call getClass()
      this.log = Logger.getLogger(getClass().getName());
      log.trace("Constructing");
   }

   /**
    * Construct a <t>ServiceMBeanSupport</tt>.
    *
    * <p>Sets up logging.
    *
    * @param type   The class type to determine category name from.
    */
   @SuppressWarnings("unchecked")
   public ServiceMBeanSupport(final Class type)
   {
      this(type.getName());
   }
   
   /**
    * Construct a <t>ServiceMBeanSupport</tt>.
    *
    * <p>Sets up logging.
    *
    * @param category   The logger category name.
    */
   public ServiceMBeanSupport(final String category)
   {
      this(Logger.getLogger(category));
   }

   /**
    * Construct a <t>ServiceMBeanSupport</tt>.
    *
    * @param log   The logger to use.
    */
   public ServiceMBeanSupport(final Logger log)
   {
      this.log = log;
      log.trace("Constructing");
   }
   
   public void setKernelControllerContext(KernelControllerContext controllerContext) throws Exception
   {
      this.controllerContext = controllerContext;
   }

   public void unsetKernelControllerContext(KernelControllerContext controllerContext) throws Exception
   {
      this.controllerContext = null;
   }

   /**
    * Use the short class name as the default for the service name.
    * 
    * @return a description of the mbean
    */
   public String getName()
   {
      // TODO: Check if this gets called often, if so cache this or remove if not used
      // use the logger so we can better be used as a delegate instead of sub-class
      return org.jboss.util.Classes.stripPackageName(log.getName());
   }
   
   public ObjectName getServiceName()
   {
      return serviceName;
   }

   /**
    * Provide access to the service DeploymentInfo. This is only available
    * after the service has passed its create step.
    * 
    * @return The service DeploymentInfo if found registered under the SARDeployer.
    * @throws JMException - thrown on failure to invoke
    *    SARDeployer.getService(ObjectName)
    */ 
   public DeploymentInfo getDeploymentInfo()
      throws JMException
   {
      Object[] args = {serviceName};
      String[] sig = {serviceName.getClass().getName()};
      DeploymentInfo sdi = (DeploymentInfo) server.invoke(SARDeployerMBean.OBJECT_NAME,
         "getService", args, sig);
      return sdi;
   }

   public MBeanServer getServer()
   {
      return server;
   }
   
   public int getState()
   {
      return state;
   }
   
   public String getStateString()
   {
      return states[state];
   }
   
   public Logger getLog()
   {
      return log;
   }


   ///////////////////////////////////////////////////////////////////////////
   //                             State Mutators                            //
   ///////////////////////////////////////////////////////////////////////////

   @Create
   public void pojoCreate() throws Exception
   {
      jbossInternalCreate();
   }
   
   @Start
   public void pojoStart() throws Exception
   {
      jbossInternalStart();
   }

   @Stop
   public void pojoStop() throws Exception
   {
      jbossInternalStop();
   }
   
   @Destroy
   public void pojoDestroy() throws Exception
   {
      jbossInternalDestroy();
   }

   protected void pojoChange(ControllerState state)
   {
      Controller controller = controllerContext.getController();
      try
      {
         controller.change(controllerContext, state);
      }
      catch (RuntimeException e)
      {
         throw e;
      }
      catch (Error e)
      {
         throw e;
      }
      catch (Throwable t)
      {
         throw new RuntimeException("Error changing state of " + controllerContext.getName() + " to " + state.getStateString(), t);
      }
   }
   
   public void create() throws Exception
   {
      if (controllerContext != null)
         pojoChange(ControllerState.CREATE);
      else if (serviceName != null && isJBossInternalLifecycleExposed)
         server.invoke(ServiceController.OBJECT_NAME, "create", new Object[] { serviceName }, SERVICE_CONTROLLER_SIG);
      else
         jbossInternalCreate();
   }
   
   public void start() throws Exception
   {
      if (controllerContext != null)
         pojoChange(ControllerState.START);
      else if (serviceName != null && isJBossInternalLifecycleExposed)
         server.invoke(ServiceController.OBJECT_NAME, "start", new Object[] { serviceName }, SERVICE_CONTROLLER_SIG);
      else
         jbossInternalStart();
   }
   
   public void stop()
   {
      try
      {
         if (controllerContext != null)
            pojoChange(ControllerState.CREATE);
         else if (serviceName != null && isJBossInternalLifecycleExposed)
            server.invoke(ServiceController.OBJECT_NAME, "stop", new Object[] { serviceName }, SERVICE_CONTROLLER_SIG);
         else
            jbossInternalStop();
      }
      catch (Throwable t)
      {
         log.warn("Error in stop " + jbossInternalDescription(), t);
      }
   }
   
   public void destroy()
   {
      try
      {
         if (controllerContext != null)
            pojoChange(ControllerState.CONFIGURED);
         else if (serviceName != null && isJBossInternalLifecycleExposed)
            server.invoke(ServiceController.OBJECT_NAME, "destroy", new Object[] { serviceName }, SERVICE_CONTROLLER_SIG);
         else
            jbossInternalDestroy();
      }
      catch (Throwable t)
      {
         log.warn("Error in destroy " + jbossInternalDescription(), t);
      }
   }
   
   protected String jbossInternalDescription()
   {
      if (serviceName != null)
         return serviceName.toString();
      else
         return getName();
   }
   
   public void jbossInternalLifecycle(String method) throws Exception
   {
      if (method == null)
         throw new IllegalArgumentException("Null method name");
      
      if (method.equals("create"))
         jbossInternalCreate();
      else if (method.equals("start"))
         jbossInternalStart();
      else if (method.equals("stop"))
         jbossInternalStop();
      else if (method.equals("destroy"))
         jbossInternalDestroy();
      else
         throw new IllegalArgumentException("Unknown lifecyle method " + method);
   }
   
   protected void jbossInternalCreate() throws Exception
   {
      if (state == CREATED || state == STARTING || state == STARTED
         || state == STOPPING || state == STOPPED)
      {
         log.debug("Ignoring create call; current state is " + getStateString());
         return;
      }
      
      log.debug("Creating " + jbossInternalDescription());
      
      try
      {
         createService();
         state = CREATED;
      }
      catch (Exception e)
      {
         log.debug("Initialization failed " + jbossInternalDescription(), e);
         throw e;
      }
      
      log.debug("Created " + jbossInternalDescription());
   }

   protected void jbossInternalStart() throws Exception
   {
      if (state == STARTING || state == STARTED || state == STOPPING)
      {
         log.debug("Ignoring start call; current state is " + getStateString());
         return;
      }
      
      if (state != CREATED && state != STOPPED && state != FAILED)
      {
         log.debug("Start requested before create, calling create now");         
         create();
      }
      
      state = STARTING;
      sendStateChangeNotification(STOPPED, STARTING, getName() + " starting", null);
      log.debug("Starting " + jbossInternalDescription());

      try
      {
         startService();
      }
      catch (Exception e)
      {
         state = FAILED;
         sendStateChangeNotification(STARTING, FAILED, getName() + " failed", e);
         log.debug("Starting failed " + jbossInternalDescription(), e);
         throw e;
      }

      state = STARTED;
      sendStateChangeNotification(STARTING, STARTED, getName() + " started", null);
      log.debug("Started " + jbossInternalDescription());
   }
   
   protected void jbossInternalStop()
   {
      if (state != STARTED)
      {
         log.debug("Ignoring stop call; current state is " + getStateString());
         return;
      }
      
      state = STOPPING;
      sendStateChangeNotification(STARTED, STOPPING, getName() + " stopping", null);
      log.debug("Stopping " + jbossInternalDescription());

      try
      {
         stopService();
      }
      catch (Throwable e)
      {
         state = FAILED;
         sendStateChangeNotification(STOPPING, FAILED, getName() + " failed", e);
         log.warn("Stopping failed " + jbossInternalDescription(), e);
         return;
      }
      
      state = STOPPED;
      sendStateChangeNotification(STOPPING, STOPPED, getName() + " stopped", null);
      log.debug("Stopped " + jbossInternalDescription());
   }

   protected void jbossInternalDestroy()
   {
      if (state == DESTROYED)
      {
         log.debug("Ignoring destroy call; current state is " + getStateString());
         return;
      }
      
      if (state == STARTED)
      {
         log.debug("Destroy requested before stop, calling stop now");
         stop();
      }
      
      log.debug("Destroying " + jbossInternalDescription());
      
      try
      {
         destroyService();
      }
      catch (Throwable t)
      {
         log.warn("Destroying failed " + jbossInternalDescription(), t);
      }
      state = DESTROYED;
      log.debug("Destroyed " + jbossInternalDescription());
   }


   ///////////////////////////////////////////////////////////////////////////
   //                                JMX Hooks                              //
   ///////////////////////////////////////////////////////////////////////////
   
   /**
    * Callback method of {@link MBeanRegistration}
    * before the MBean is registered at the JMX Agent.
    * 
    * <p>
    * <b>Attention</b>: Always call this method when you overwrite it in a subclass
    *                   because it saves the Object Name of the MBean.
    *
    * @param server    Reference to the JMX Agent this MBean is registered on
    * @param name      Name specified by the creator of the MBean. Note that you can
    *                  overwrite it when the given ObjectName is null otherwise the
    *                  change is discarded (maybe a bug in JMX-RI).
    * @return the ObjectName
    * @throws Exception for any error
    */
   public ObjectName preRegister(MBeanServer server, ObjectName name)
      throws Exception
   {
      this.server = server;

      serviceName = getObjectName(server, name);
      
      return serviceName;
   }
   
   public void postRegister(Boolean registrationDone)
   {
      if (!registrationDone.booleanValue())
      {
         log.info( "Registration is not done -> stop" );
         stop();
      }
      else
      {
         state = REGISTERED;
         // This is for backwards compatibility - see whether jbossInternalLifecycle is exposed
         try
         {
            MBeanInfo info = server.getMBeanInfo(serviceName);
            MBeanOperationInfo[] ops = info.getOperations();
            for (int i = 0; i < ops.length; ++i)
            {
               if (ops[i] != null && ServiceController.JBOSS_INTERNAL_LIFECYCLE.equals(ops[i].getName()))
               {
                  isJBossInternalLifecycleExposed = true;
                  break;
               }
            }
         }
         catch (Throwable t)
         {
            log.warn("Unexcepted error accessing MBeanInfo for " + serviceName, t);
         }
      }
   }

   public void preDeregister() throws Exception
   {
   }
   
   public void postDeregister()
   {
      server = null;
      serviceName = null;
      state = UNREGISTERED;
   }

   /**
    * The <code>getNextNotificationSequenceNumber</code> method returns 
    * the next sequence number for use in notifications.
    *
    * @return a <code>long</code> value
    */
   protected long getNextNotificationSequenceNumber()
   {
      return nextNotificationSequenceNumber();
   }


   ///////////////////////////////////////////////////////////////////////////
   //                       Concrete Service Overrides                      //
   ///////////////////////////////////////////////////////////////////////////

   /**
    * Sub-classes should override this method if they only need to set their
    * object name during MBean pre-registration.
    * 
    * @param server the mbeanserver
    * @param name the suggested name, maybe null
    * @return the object name
    * @throws MalformedObjectNameException for a bad object name
    */
   protected ObjectName getObjectName(MBeanServer server, ObjectName name)
      throws MalformedObjectNameException
   {
      return name;
   }

   /**
    * Sub-classes should override this method to provide
    * custum 'create' logic.
    *
    * <p>This method is empty, and is provided for convenience
    *    when concrete service classes do not need to perform
    *    anything specific for this state change.
    * 
    * @throws Exception for any error
    */
   protected void createService() throws Exception {}
   
   /**
    * Sub-classes should override this method to provide
    * custum 'start' logic.
    * 
    * <p>This method is empty, and is provided for convenience
    *    when concrete service classes do not need to perform
    *    anything specific for this state change.
    * 
    * @throws Exception for any error
    */
   protected void startService() throws Exception {}
   
   /**
    * Sub-classes should override this method to provide
    * custum 'stop' logic.
    * 
    * <p>This method is empty, and is provided for convenience
    *    when concrete service classes do not need to perform
    *    anything specific for this state change.
    * 
    * @throws Exception for any error
    */
   protected void stopService() throws Exception {}
   
   /**
    * Sub-classes should override this method to provide
    * custum 'destroy' logic.
    * 
    * <p>This method is empty, and is provided for convenience
    *    when concrete service classes do not need to perform
    *    anything specific for this state change.
    * 
    * @throws Exception for any error
    */
   protected void destroyService() throws Exception {}
   
   // Private -------------------------------------------------------
   
   /**
    * Helper for sending out state change notifications
    */
   private void sendStateChangeNotification(int oldState, int newState, String msg, Throwable t)
   {
      long now = System.currentTimeMillis();
      
      AttributeChangeNotification stateChangeNotification = new AttributeChangeNotification(
         this,
         getNextNotificationSequenceNumber(), now, msg,
         "State", "java.lang.Integer",
         new Integer(oldState), new Integer(newState)
         );
      stateChangeNotification.setUserData(t);
      
      sendNotification(stateChangeNotification);      
   }
}

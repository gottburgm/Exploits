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
package org.jboss.test.system.controller;

import java.lang.reflect.UndeclaredThrowableException;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ObjectName;

import junit.framework.AssertionFailedError;

import org.jboss.deployment.IncompleteDeploymentException;
import org.jboss.mx.server.ServerConstants;
import org.jboss.system.ServiceContext;
import org.jboss.system.ServiceControllerMBean;
import org.jboss.test.AbstractSystemTest;
import org.jboss.test.AbstractTestDelegate;
import org.jboss.test.system.controller.support.Order;

/**
 * ControllerTestDelegate.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 85945 $
 */
public abstract class ControllerTestDelegate extends AbstractTestDelegate
{
   private MBeanServer server; 
   
   private ServiceControllerMBean serviceController;

   private SimpleSARDeployer deployer;
   
   public ControllerTestDelegate(Class clazz)
   {
      super(clazz);
   }

   public MBeanServer getServer()
   {
      return server;
   }

   public ServiceControllerMBean getController()
   {
      return serviceController;
   }

   /**
    * Override to:
    * Set javax.management.builder.initial to org.jboss.mx.server.MBeanServerBuilderImpl
    * Create jboss domain MBeanServer
    * Create and register jboss.system:service=ServiceController mbean
    * Create SimpleSARDeployer
    * deploy the test service mbeans
    * @see #deploy()
    */
   public void setUp() throws Exception
   {
      super.setUp();
      
      System.setProperty(ServerConstants.MBEAN_SERVER_BUILDER_CLASS_PROPERTY, ServerConstants.DEFAULT_MBEAN_SERVER_BUILDER_CLASS);
      server = createMBeanServer();
      serviceController = createServiceController();
      server.registerMBean(serviceController, ServiceControllerMBean.OBJECT_NAME);
      
      deployer = new SimpleSARDeployer(server, serviceController);

      Order.reset();
      
      deploy();
      
      if (isValidateAtSetUp())
         validate();
   }

   // TODO Temporary until the IncompleteDeploymentDescription is integrated 
   protected boolean isValidateAtSetUp()
   {
      return true;
   }

   protected MBeanServer createMBeanServer()
   {
      return MBeanServerFactory.newMBeanServer("jboss");
   }

   public abstract ServiceControllerMBean createServiceController() throws Exception;
   
   public void tearDown() throws Exception
   {
      deployer.uninstall();
      super.tearDown();
   }

   protected void uninstallTemporary() throws Exception
   {
      deployer.uninstallTemporary();
   }
   
   protected void validate() throws Exception
   {
      Collection<ServiceContext> waitingForClasses = new HashSet<ServiceContext>();
      Collection<ServiceContext> waitingForDepends = serviceController.listIncompletelyDeployed();
      Collection<ServiceContext> allServices = serviceController.listDeployed();
      
      // Weed services that are waiting for other deployments
      Collection<ServiceContext> rootCause = new HashSet<ServiceContext>(waitingForDepends);
      Collection<ServiceContext> missing = new HashSet<ServiceContext>();
      for (Iterator i = rootCause.iterator(); i.hasNext();)
      {
         ServiceContext ctx = (ServiceContext) i.next();
         for (Iterator j = ctx.iDependOn.iterator(); j.hasNext(); )
         {
            ServiceContext dependee = (ServiceContext) j.next();
            if (dependee.state != ServiceContext.RUNNING)
            {
               // Add missing mbean
               if (allServices.contains(dependee) == false)
                  missing.add(dependee);
               // We are not a root cause
               i.remove();
               break;
            }
         }
      }
      // Add missing mbeans to the root cause
      rootCause.addAll(missing);
      
      IncompleteDeploymentException ide = new IncompleteDeploymentException(
         waitingForClasses,
         waitingForDepends,
         rootCause,
         Collections.emptyList(),
         Collections.emptyList());

      if (ide.isEmpty() == false)
         throw ide;
   }
   
   /**
    * Deploy the beans by looking for an xml descriptor named
    * clazz.getName().replace('.', '/') + ".xml". If the test
    * clazz includes a NewUnitTestCase or OldUnitTestCase suffix,
    * this is removed.
    * 
    * @throws Exception for any error
    */
   protected void deploy() throws Exception
   {
      String testName = clazz.getName();
      testName = testName.replace('.', '/');

      int index = testName.indexOf("NewUnitTestCase");
      if (index != -1)
         testName = testName.substring(0, index);
      index = testName.indexOf("OldUnitTestCase");
      if (index != -1)
         testName = testName.substring(0, index);
      
      testName += ".xml";
      
      URL url = clazz.getClassLoader().getResource(testName);
      if (url != null)
         deploy(url, false);
      else
         log.debug("No test specific deployment " + testName);
   }

   protected List<ObjectName> deploy(URL url, boolean temporary) throws Exception
   {
      return deployer.deploy(url, temporary);
   }
   
   protected void undeploy(List<ObjectName> objectNames)
   {
      deployer.undeploy(objectNames);
   }

   protected List<ObjectName> install(URL url) throws Exception
   {
      return deployer.install(url);
   }
   
   protected void uninstall(List<ObjectName> objectNames)
   {
      deployer.uninstall(objectNames);
   }
   
   public abstract void assertMBeanFailed(ObjectName name, boolean registered) throws Exception;
   
   protected IncompleteDeploymentException assertInvalidDeployments() throws Exception
   {
      try
      {
         validate();
         throw new AssertionFailedError("Deployments should not be valid!");
      }
      catch (IncompleteDeploymentException expected)
      {
         log.debug("Got expected " + expected.getClass().getName());
         return expected;
      }
   }
   
   protected void assertInitialDeployFailure(URL url, ObjectName name, Class<? extends Throwable> expected) throws Exception
   {
      try
      {
         deploy(url, true);
         throw new AssertionFailedError("Should have got a " + expected.getName());
      }
      catch (Throwable t)
      {
         AbstractSystemTest.checkThrowableDeep(expected, t);
      }
      if (name != null)
         assertNoService(name);
   }

   public List<ObjectName> assertDeployFailure(URL url, ObjectName name, Class<? extends Throwable> expected) throws Exception
   {
      List<ObjectName> result = deploy(url, true);
      IncompleteDeploymentException e = assertInvalidDeployments();
      checkIncomplete(e, name, expected);
      return result;
   }
   
   public List<ObjectName> assertMaybeDeployFailure(URL url, ObjectName name, Class<? extends Throwable> expected) throws Exception
   {
      return assertDeployFailure(url, name, expected);
   }
   
   public void assertMaybeParseFailure(URL url, ObjectName name, Class<? extends Throwable> expected) throws Exception
   {
      assertDeployFailure(url, name, expected);
      assertServiceFailed(name);
   }
   
   protected void checkIncomplete(IncompleteDeploymentException e, ObjectName name, Class<? extends Throwable> expected) throws Exception
   {
      Collection incomplete = e.getMbeansWaitingForDepends();
      for (Iterator i = incomplete.iterator(); i.hasNext();)
      {
         ServiceContext ctx = (ServiceContext) i.next();
         if (name.equals(ctx.objectName))
         {
            Throwable problem = ctx.getProblem();
            if (e != null || expected != null)
            {
               if (expected != null && problem == null)
                  throw new AssertionFailedError("Did not get expected " + expected.getName() + " for " + ctx);
               if (expected == null && problem != null)
               {
                  if (problem instanceof Exception)
                     throw (Exception) problem;
                  if (problem instanceof Error)
                     throw (Error) problem;
                  throw new UndeclaredThrowableException(problem);
               }
               if (expected != null)
               {
                  AbstractSystemTest.checkThrowableDeep(expected, problem);
                  if (ServiceContext.FAILED != ctx.state)
                     throw new AssertionFailedError("Context is not in the FAILED state: " + ctx);
               }
            }
            return;
         }
      }
      
      throw new AssertionFailedError("Did not find " + name + " in incomplete deployments " + incomplete);
   }
   
   protected ServiceContext getServiceContext(ObjectName name) throws Exception
   {
      return getController().getServiceContext(name);
   }
   
   protected void assertNoService(ObjectName name) throws Exception
   {
      ServiceContext ctx = getServiceContext(name);
      if (ctx != null && ctx.state != ServiceContext.NOTYETINSTALLED)
         throw new AssertionFailedError("Should not be a service context for " + ctx);
   }

   protected void assertServiceFailed(ObjectName name) throws Exception
   {
      assertServiceFailed(name, AbstractControllerTest.OLD_REGISTERED);
   }
   
   protected void assertServiceFailed(ObjectName name, boolean registered) throws Exception
   {
      assertServiceState(name, ServiceContext.FAILED, registered);
      assertMBeanFailed(name, registered);
   }
   
   protected void assertServiceState(ObjectName name, int expectedState, boolean registered) throws Exception
   {
      ServiceContext ctx = getServiceContext(name);
      if (registered == false && ctx == null)
         return;
      if (ctx == null)
         throw new AssertionFailedError("Incorrect state for " + name + " expected " + ServiceContext.getStateString(expectedState) + " but there is no context/state");
      if (expectedState != ctx.state)
         throw new AssertionFailedError("Incorrect state for " + name + " expected " + ServiceContext.getStateString(expectedState) + " got " + ctx.getStateString());
   }
}

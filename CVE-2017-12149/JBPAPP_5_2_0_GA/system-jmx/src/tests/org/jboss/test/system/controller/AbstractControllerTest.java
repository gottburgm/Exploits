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

import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.List;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.jboss.system.ServiceContext;
import org.jboss.system.ServiceControllerMBean;
import org.jboss.test.AbstractSystemTest;
import org.jboss.test.AbstractTestDelegate;
import org.jboss.test.system.controller.support.Order;
import org.jboss.test.system.controller.support.Simple;
import org.jboss.test.system.controller.support.SimpleMBean;

/**
 * A Controller Test.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 85945 $
 */
public abstract class AbstractControllerTest extends AbstractSystemTest
{
   public static boolean OLD_NOT_REGISTERED = false; 
   public static boolean OLD_REGISTERED = true;
   
   /**
    * Create a new ContainerTest.
    * 
    * @param name the test name
    */
   public AbstractControllerTest(String name)
   {
      super(name);
   }
   
   public static AbstractTestDelegate getNewControllerDelegate(Class clazz) throws Exception
   {
      ControllerTestDelegate delegate = new NewControllerTestDelegate(clazz);
      // @todo delegate.enableSecurity = true;
      return delegate;
   }
   
   public static AbstractTestDelegate getOldControllerDelegate(Class clazz) throws Exception
   {
      ControllerTestDelegate delegate = new OldControllerTestDelegate(clazz);
      // @todo delegate.enableSecurity = true;
      return delegate;
   }
   
   protected void setUp() throws Exception
   {
      super.setUp();
      Order.reset();
   }

   protected void tearDown() throws Exception
   {
      getControllerDelegate().uninstallTemporary();
      super.tearDown();
   }

   protected ControllerTestDelegate getControllerDelegate()
   {
      return (ControllerTestDelegate) getDelegate();
   }
   
   protected MBeanServer getServer()
   {
      return getControllerDelegate().getServer();
   }
   
   protected ServiceControllerMBean getController()
   {
      return getControllerDelegate().getController();
   }
   
   protected List<ObjectName> deploy(URL url) throws Exception
   {
      return getControllerDelegate().deploy(url, true);
   }
   
   protected List<ObjectName> deploy(String resource) throws Exception
   {
      URL url = getResourceURL(resource);
      return deploy(url);
   }
   
   protected void undeploy(List<ObjectName> objectNames)
   {
      getControllerDelegate().undeploy(objectNames);
   }
   
   protected List<ObjectName> install(String resource) throws Exception
   {
      URL url = getResourceURL(resource);
      return install(url);
   }
   
   protected List<ObjectName> install(URL url) throws Exception
   {
      return getControllerDelegate().install(url);
   }
   
   protected void uninstall(List<ObjectName> objectNames)
   {
      getControllerDelegate().uninstall(objectNames);
   }

   protected void assertInstall(ObjectName name) throws Exception
   {
      
      String resource = getName();
      resource = resource.substring(4) + "_install.xml";
      install(resource);
      
      assertServiceConfigured(name);
      assertRegistered(name);
   }

   protected void assertUninstall(ObjectName name) throws Exception
   {
      uninstall(Collections.singletonList(name));
      assertNoService(name);
      assertNotRegistered(name);
   }

   protected List<ObjectName> assertDeploy(ObjectName name) throws Exception
   {
      
      String resource = getName();
      resource = resource.substring(4) + "_install.xml";
      List<ObjectName> result = deploy(resource);
      
      assertServiceRunning(name);
      assertRegistered(name);
      
      return result;
   }

   protected void assertUndeploy(ObjectName name) throws Exception
   {
      assertUndeploy(name, Collections.singletonList(name));
   }

   protected void assertUndeploy(ObjectName name, List<ObjectName> names) throws Exception
   {
      uninstall(names);
      assertNoService(name);
      assertNotRegistered(name);
   }
   
   protected void validate() throws Exception
   {
      getControllerDelegate().validate();
   }
   
   protected void assertInvalidDeployments() throws Exception
   {
      getControllerDelegate().assertInvalidDeployments();
   }
   
   protected void assertInitialDeployFailure(String resource, ObjectName name, Class<? extends Throwable> expected) throws Exception
   {
      URL url = getResourceURL(resource);
      getControllerDelegate().assertInitialDeployFailure(url, name, expected);
   }
   
   protected List<ObjectName> assertDeployFailure(ObjectName name, Class<? extends Throwable> expected) throws Exception
   {
      return assertDeployFailure(name, ServiceContext.FAILED, expected);
   }
   
   protected List<ObjectName> assertDeployFailure(ObjectName name, int expectedState, Class<? extends Throwable> expected) throws Exception
   {
      String resource = getName();
      resource = resource.substring(4) + "_bad.xml";
      return assertDeployFailure(resource, name, expectedState, expected);
   }
   
   protected List<ObjectName> assertDeployFailure(String resource, ObjectName name, Class<? extends Throwable> expected) throws Exception
   {
      return assertDeployFailure(resource, name, ServiceContext.FAILED, expected);
   }
   
   protected List<ObjectName> assertDeployFailure(String resource, ObjectName name, int expectedState, Class<? extends Throwable> expected) throws Exception
   {
      URL url = getResourceURL(resource);
      List<ObjectName> result = getControllerDelegate().assertDeployFailure(url, name, expected);
      if (expectedState == ServiceContext.FAILED)
         assertServiceFailed(name, OLD_REGISTERED);
      else
         assertServiceState(name, expectedState);
      return result;
   }

   protected void redeployAfterDeployFailure(ObjectName name, Class<? extends Throwable> expected) throws Exception
   {
      String root = getName();
      root = root.substring(4);

      List<ObjectName> names = assertDeployFailure(root + "_bad.xml", SimpleMBean.OBJECT_NAME, expected);
      undeploy(names);
      deploy(root + "_good.xml");
      assertServiceRunning(name);
   }

   protected void redeployAfterUndeployFailure(ObjectName name) throws Exception
   {
      String root = getName();
      root = root.substring(4);

      List<ObjectName> names = deploy(root + "_bad.xml");
      assertServiceRunning(name);
      undeploy(names);
      deploy(root + "_good.xml");
      assertServiceRunning(name);
   }
   
   protected List<ObjectName> assertMaybeDeployFailure(ObjectName name, Class<? extends Throwable> expected) throws Exception
   {
      String resource = getName();
      resource = resource.substring(4) + "_bad.xml";
      return assertMaybeDeployFailure(resource, name, expected);
   }
   
   protected List<ObjectName> assertMaybeDeployFailure(String resource, ObjectName name, Class<? extends Throwable> expected) throws Exception
   {
      URL url = getResourceURL(resource);
      List<ObjectName> result = getControllerDelegate().assertMaybeDeployFailure(url, name, expected);
      assertServiceFailed(name, OLD_NOT_REGISTERED);
      return result;
   }

   protected void redeployAfterMaybeDeployFailure(ObjectName name, Class<? extends Throwable> expected) throws Exception
   {
      String root = getName();
      root = root.substring(4);

      List<ObjectName> names = assertMaybeDeployFailure(root + "_bad.xml", SimpleMBean.OBJECT_NAME, expected);
      undeploy(names);
      deploy(root + "_good.xml");
      assertServiceRunning(name);
   }

   protected void assertMaybeParseFailure(ObjectName name, Class<? extends Throwable> expected) throws Exception
   {
      String resource = getName();
      resource = resource.substring(4) + "_bad.xml";
      assertMaybeParseFailure(resource, name, expected);
   }
   
   protected void assertMaybeParseFailure(String resource, ObjectName name, Class<? extends Throwable> expected) throws Exception
   {
      URL url = getResourceURL(resource);
      getControllerDelegate().assertMaybeParseFailure(url, name, expected);
      assertServiceFailed(name, OLD_NOT_REGISTERED);
   }
   
   protected ServiceContext getServiceContext(ObjectName name) throws Exception
   {
      assertNotNull(name);
      return getControllerDelegate().getServiceContext(name);
   }
   
   protected void assertServiceFailed(ObjectName name) throws Exception
   {
      assertServiceFailed(name, OLD_REGISTERED);
   }
   
   protected void assertServiceFailed(ObjectName name, boolean registered) throws Exception
   {
      getControllerDelegate().assertServiceFailed(name, registered);
   }
   
   protected void assertServiceInstalled(ObjectName name) throws Exception
   {
      assertServiceState(name, ServiceContext.INSTALLED, true);
   }
   
   protected void assertServiceConfigured(ObjectName name) throws Exception
   {
      assertServiceState(name, ServiceContext.CONFIGURED, true);
   }
   
   protected void assertServiceCreated(ObjectName name) throws Exception
   {
      assertServiceState(name, ServiceContext.CREATED, true);
   }
   
   protected void assertServiceRunning(ObjectName name) throws Exception
   {
      assertServiceState(name, ServiceContext.RUNNING, true);
   }
   
   protected void assertServiceStopped(ObjectName name) throws Exception
   {
      assertServiceState(name, ServiceContext.STOPPED, true);
   }
   
   protected void assertServiceDestroyed(ObjectName name) throws Exception
   {
      assertServiceState(name, ServiceContext.DESTROYED, true);
   }
   
   protected void assertServiceState(ObjectName name, int expectedState) throws Exception
   {
      ServiceContext ctx = getServiceContext(name);
      assertTrue("Incorrect state for " + name + " expected " + ServiceContext.getStateString(expectedState) + " got " + ctx.getStateString(), expectedState == ctx.state);
   }
   
   protected void assertServiceState(ObjectName name, int expectedState, boolean registered) throws Exception
   {
      getControllerDelegate().assertServiceState(name, expectedState, registered);
   }
   
   protected void assertNoService(ObjectName name) throws Exception
   {
      ServiceContext ctx = getServiceContext(name);
      assertNull("Should not be a service context for " + name, ctx);
   }
   
   protected URL getResourceURL(String resource) throws Exception
   {
      URL url = getClass().getResource(resource);
      if (url == null)
         throw new IOException(resource + " not found");
      return url;
   }
   
   protected void assertRegistered(ObjectName name) throws Exception
   {
      MBeanServer server = getServer();
      assertTrue(name + " should be registered in the MBeanServer", server.isRegistered(name));
   }
   
   protected void assertNotRegistered(ObjectName name) throws Exception
   {
      MBeanServer server = getServer();
      assertFalse(name + " should NOT be registered in the MBeanServer", server.isRegistered(name));
   }

   protected <T> T getMBean(Class<T> expected, ObjectName name, String attribute) throws Exception
   {
      MBeanServer server = getServer();
      Object object = server.getAttribute(name, attribute);
      assertNotNull(object);
      return assertInstanceOf(expected, object);
   }

   protected Simple getSimple() throws Exception
   {
      return getMBean(Simple.class, SimpleMBean.OBJECT_NAME, "Instance");
   }
   
   protected void FAILS_IN_OLD()
   {
      getLog().debug("This test fails with the old service controller, ignoring.");
   }
}

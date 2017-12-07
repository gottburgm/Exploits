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
package org.jboss.test.jmx.test;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;

import junit.framework.Test;

import org.jboss.test.JBossTestCase;

/**
 * Tests implicit call of create() upon start() and stop() upon destroy() 
 * 
 * [JBAS-2022] 
 *
 * @author <a href="mailto:dimitris@jboss.org">Dimitris Andreadis</a>
 * @version $Revision: 81036 $
 */
public class ServiceLifecycleUnitTestCase extends JBossTestCase
{
   public static final Object[] EMPTY_OBJECT_ARRAY = new Object[] {};
   public static final String[] EMPTY_STRING_ARRAY = new String[] {};
   
   public ServiceLifecycleUnitTestCase(String test)
   {
      super(test);
   }

   public static Test suite() throws Exception
   {
      return getDeploySetup(ServiceLifecycleUnitTestCase.class, "jmx-simpleservice.sar");
   }
   
   public void testCreateStopImplicitlyCalledJBossInternalLifecyleExposed() throws Exception
   {
      ObjectName target = new ObjectName("jboss.test:service=SimpleService");         
      MBeanServerConnection server = super.getServer();

      // call 'destroy' directly to see if 'stop' was called
      server.invoke(target, "destroy", EMPTY_OBJECT_ARRAY, EMPTY_STRING_ARRAY);
      Boolean stopCalled = (Boolean)server.getAttribute(target, "StopCalled");
      assertTrue("stop() not called upon direct destroy", stopCalled.booleanValue());
      
      // reset the service memory
      server.invoke(target, "resetLifecycleMemory", EMPTY_OBJECT_ARRAY, EMPTY_STRING_ARRAY);
      
      // call 'start' directly to see if 'create' will be called
      server.invoke(target, "start", EMPTY_OBJECT_ARRAY, EMPTY_STRING_ARRAY);
      Boolean createCalled = (Boolean)server.getAttribute(target, "CreateCalled");
      assertTrue("create() not called upon direct start()", createCalled.booleanValue());      
   }
   
   public void testCreateStopImplicitlyCalledJBossInternalLifecycleHidden() throws Exception
   {
      ObjectName target = new ObjectName("jboss.test:service=SimpleService2");         
      MBeanServerConnection server = super.getServer();

      // call 'destroy' directly to see if 'stop' was called
      server.invoke(target, "destroy", EMPTY_OBJECT_ARRAY, EMPTY_STRING_ARRAY);
      Boolean stopCalled = (Boolean)server.getAttribute(target, "StopCalled");
      assertTrue("stop() not called upon direct destroy()", stopCalled.booleanValue());
      
      // reset the service memory
      server.invoke(target, "resetLifecycleMemory", EMPTY_OBJECT_ARRAY, EMPTY_STRING_ARRAY);
      
      // call 'start' directly to see if 'create' will be called
      server.invoke(target, "start", EMPTY_OBJECT_ARRAY, EMPTY_STRING_ARRAY);
      Boolean createCalled = (Boolean)server.getAttribute(target, "CreateCalled");
      assertTrue("create() not called upon direct start()", createCalled.booleanValue());
   }
   
   public void testStopCreateImplicitlyCalledWithDependencyAndLifecycleExposed() throws Exception
   {
      // target1 depends on target2
      ObjectName target1 = new ObjectName("jboss.test:service=SimpleService3");
      ObjectName target2 = new ObjectName("jboss.test:service=SimpleService4"); 
      MBeanServerConnection server = super.getServer();
      
      // call 'destroy' directly on target2 to see if 'stop' is called on target1
      server.invoke(target2, "destroy", EMPTY_OBJECT_ARRAY, EMPTY_STRING_ARRAY);
      Boolean stopCalled = (Boolean)server.getAttribute(target1, "StopCalled");
      assertTrue("stop() not called on target1 upon indirect destroy() of target2", stopCalled.booleanValue());
      
      // reset the services memory
      server.invoke(target1, "resetLifecycleMemory", EMPTY_OBJECT_ARRAY, EMPTY_STRING_ARRAY);
      server.invoke(target2, "resetLifecycleMemory", EMPTY_OBJECT_ARRAY, EMPTY_STRING_ARRAY);
      
      // call 'start' directly on target2 to see if 'create' is called on target1
      server.invoke(target2, "start", EMPTY_OBJECT_ARRAY, EMPTY_STRING_ARRAY);
      Boolean createCalled = (Boolean)server.getAttribute(target1, "CreateCalled");
      assertTrue("create() not called on target1 upon indirect start() of target2", createCalled.booleanValue());        
   }
   
   public void testStopCreateImplicitlyCalledWithDependencyAndLifecycleHidden() throws Exception
   {
      // target1 depends on target2
      // target1 hides jbossInternalLifecycle, however, target2 must expose
      // jbossInternalLifecycle, otherwise the ServiceController is not
      // invoked to alter the state of target1 in tandem with target2
      ObjectName target1 = new ObjectName("jboss.test:service=SimpleService5");
      ObjectName target2 = new ObjectName("jboss.test:service=SimpleService6"); 
      MBeanServerConnection server = super.getServer();
      
      // call 'destroy' directly on target2 to see if 'stop' is called on target1
      server.invoke(target2, "destroy", EMPTY_OBJECT_ARRAY, EMPTY_STRING_ARRAY);
      Boolean stopCalled = (Boolean)server.getAttribute(target1, "StopCalled");
      assertTrue("stop() not called on target1 upon indirect destroy() of target2", stopCalled.booleanValue());
      
      // reset the services memory
      server.invoke(target1, "resetLifecycleMemory", EMPTY_OBJECT_ARRAY, EMPTY_STRING_ARRAY);
      server.invoke(target2, "resetLifecycleMemory", EMPTY_OBJECT_ARRAY, EMPTY_STRING_ARRAY);
      
      // call 'start' directly on target2 to see if 'create' is called on target1
      server.invoke(target2, "start", EMPTY_OBJECT_ARRAY, EMPTY_STRING_ARRAY);
      Boolean createCalled = (Boolean)server.getAttribute(target1, "CreateCalled");
      assertTrue("create() not called on target1 upon indirect start() of target2", createCalled.booleanValue());        
   }   
}
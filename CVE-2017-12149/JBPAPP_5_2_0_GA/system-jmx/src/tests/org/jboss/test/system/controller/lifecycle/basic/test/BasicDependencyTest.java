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
package org.jboss.test.system.controller.lifecycle.basic.test;

import java.util.List;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.jboss.mx.util.ObjectNameFactory;
import org.jboss.system.ServiceControllerMBean;
import org.jboss.test.system.controller.AbstractControllerTest;
import org.jboss.test.system.controller.support.Simple;

/**
 * BasicLifecycleTest.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 85945 $
 */
public abstract class BasicDependencyTest extends AbstractControllerTest
{
   private static ObjectName NAME_ONE = ObjectNameFactory.create("test:name=1"); 
   private static ObjectName NAME_TWO = ObjectNameFactory.create("test:name=2"); 
   
   private String resourceName;
   
   public BasicDependencyTest(String name)
   {
      super(name);

      resourceName = getClass().getName();
      int index= resourceName.lastIndexOf('.'); 
      if (index != -1)
         resourceName= resourceName.substring(index + 1, resourceName.length());
      index = resourceName.indexOf("NewUnitTestCase");
      if (index != -1)
         resourceName = resourceName.substring(0, index);
      index = resourceName.indexOf("OldUnitTestCase");
      if (index != -1)
         resourceName = resourceName.substring(0, index);
      
      resourceName = resourceName + "_NotAutomatic.xml";
   }
   
   public void testBasicDependency() throws Exception
   {
      Simple test1 = null;
      Simple test2 = null;
      
      boolean error = false;
      
      List<ObjectName> mbeans = deploy(resourceName);
      try
      {
         assertServiceRunning(NAME_ONE);
         assertServiceRunning(NAME_TWO);
         
         MBeanServer server = getServer();
         test1 = (Simple) server.getAttribute(NAME_ONE, "Instance");
         assertEquals("()", test1.constructorUsed);
         assertEquals(2, test1.createOrder);
         assertEquals(4, test1.startOrder);
         assertEquals(0, test1.stopOrder);
         assertEquals(0, test1.destroyOrder);
         test2 = (Simple) server.getAttribute(NAME_TWO, "Instance");
         assertEquals("()", test2.constructorUsed);
         assertEquals(1, test2.createOrder);
         assertEquals(3, test2.startOrder);
         assertEquals(0, test2.stopOrder);
         assertEquals(0, test2.destroyOrder);
      }
      catch (Exception e)
      {
         error = true;
         throw e;
      }
      catch (Error e)
      {
         error = true;
         throw e;
      }
      finally
      {
         undeploy(mbeans);

         if (error == false)
         {
            assertNoService(NAME_ONE);
            assertNotRegistered(NAME_ONE);
            assertEquals(2, test1.createOrder);
            assertEquals(4, test1.startOrder);
            assertEquals(5, test1.stopOrder);
            assertEquals(7, test1.destroyOrder);
            assertNoService(NAME_TWO);
            assertNotRegistered(NAME_TWO);
            assertEquals(1, test2.createOrder);
            assertEquals(3, test2.startOrder);
            assertEquals(6, test2.stopOrder);
            assertEquals(8, test2.destroyOrder);
         }
      }
   }
   
   public void testBasicRedeploy() throws Exception
   {
      Simple test1 = null;
      Simple test2 = null;
      
      boolean error = false;
      
      List<ObjectName> mbeans = deploy(resourceName);
      try
      {
         assertServiceRunning(NAME_ONE);
         assertServiceRunning(NAME_TWO);
         
         MBeanServer server = getServer();
         test1 = (Simple) server.getAttribute(NAME_ONE, "Instance");
         assertEquals("()", test1.constructorUsed);
         assertEquals(2, test1.createOrder);
         assertEquals(4, test1.startOrder);
         assertEquals(0, test1.stopOrder);
         assertEquals(0, test1.destroyOrder);
         test2 = (Simple) server.getAttribute(NAME_TWO, "Instance");
         assertEquals("()", test2.constructorUsed);
         assertEquals(1, test2.createOrder);
         assertEquals(3, test2.startOrder);
         assertEquals(0, test2.stopOrder);
         assertEquals(0, test2.destroyOrder);
      }
      catch (Exception e)
      {
         error = true;
         throw e;
      }
      catch (Error e)
      {
         error = true;
         throw e;
      }
      finally
      {
         undeploy(mbeans);

         if (error == false)
         {
            assertNoService(NAME_ONE);
            assertNotRegistered(NAME_ONE);
            assertEquals(2, test1.createOrder);
            assertEquals(4, test1.startOrder);
            assertEquals(5, test1.stopOrder);
            assertEquals(7, test1.destroyOrder);
            assertNoService(NAME_TWO);
            assertNotRegistered(NAME_TWO);
            assertEquals(1, test2.createOrder);
            assertEquals(3, test2.startOrder);
            assertEquals(6, test2.stopOrder);
            assertEquals(8, test2.destroyOrder);
         }
      }
      
      mbeans = deploy(resourceName);
      try
      {
         assertServiceRunning(NAME_ONE);
         assertServiceRunning(NAME_TWO);
         
         MBeanServer server = getServer();
         test1 = (Simple) server.getAttribute(NAME_ONE, "Instance");
         assertEquals("()", test1.constructorUsed);
         assertEquals(10, test1.createOrder);
         assertEquals(12, test1.startOrder);
         assertEquals(0, test1.stopOrder);
         assertEquals(0, test1.destroyOrder);
         test2 = (Simple) server.getAttribute(NAME_TWO, "Instance");
         assertEquals("()", test2.constructorUsed);
         assertEquals(9, test2.createOrder);
         assertEquals(11, test2.startOrder);
         assertEquals(0, test2.stopOrder);
         assertEquals(0, test2.destroyOrder);
      }
      catch (Exception e)
      {
         error = true;
         throw e;
      }
      catch (Error e)
      {
         error = true;
         throw e;
      }
      finally
      {
         undeploy(mbeans);

         if (error == false)
         {
            assertNoService(NAME_ONE);
            assertNotRegistered(NAME_ONE);
            assertEquals(10, test1.createOrder);
            assertEquals(12, test1.startOrder);
            assertEquals(13, test1.stopOrder);
            assertEquals(15, test1.destroyOrder);
            assertNoService(NAME_TWO);
            assertNotRegistered(NAME_TWO);
            assertEquals(9, test2.createOrder);
            assertEquals(11, test2.startOrder);
            assertEquals(14, test2.stopOrder);
            assertEquals(16, test2.destroyOrder);
         }
      }
   }
   
   public void testBasicDependencyManualLifecycle() throws Exception
   {
      Simple test1 = null;
      Simple test2 = null;
      
      boolean error = false;
      
      List<ObjectName> mbeans = deploy(resourceName);
      try
      {
         assertServiceRunning(NAME_ONE);
         assertServiceRunning(NAME_TWO);
         
         MBeanServer server = getServer();
         test1 = (Simple) server.getAttribute(NAME_ONE, "Instance");
         assertEquals("()", test1.constructorUsed);
         assertEquals(2, test1.createOrder);
         assertEquals(4, test1.startOrder);
         assertEquals(0, test1.stopOrder);
         assertEquals(0, test1.destroyOrder);
         test2 = (Simple) server.getAttribute(NAME_TWO, "Instance");
         assertEquals("()", test2.constructorUsed);
         assertEquals(1, test2.createOrder);
         assertEquals(3, test2.startOrder);
         assertEquals(0, test2.stopOrder);
         assertEquals(0, test2.destroyOrder);
         
         ServiceControllerMBean controller = getController();
         
         controller.stop(NAME_ONE);
         assertServiceStopped(NAME_ONE);
         assertServiceRunning(NAME_TWO);
         assertEquals(2, test1.createOrder);
         assertEquals(4, test1.startOrder);
         assertEquals(5, test1.stopOrder);
         assertEquals(0, test1.destroyOrder);
         assertEquals(1, test2.createOrder);
         assertEquals(3, test2.startOrder);
         assertEquals(0, test2.stopOrder);
         assertEquals(0, test2.destroyOrder);
         
         controller.destroy(NAME_ONE);
         assertServiceDestroyed(NAME_ONE);
         assertServiceRunning(NAME_TWO);
         assertEquals(2, test1.createOrder);
         assertEquals(4, test1.startOrder);
         assertEquals(5, test1.stopOrder);
         assertEquals(6, test1.destroyOrder);
         assertEquals(1, test2.createOrder);
         assertEquals(3, test2.startOrder);
         assertEquals(0, test2.stopOrder);
         assertEquals(0, test2.destroyOrder);
         
         controller.create(NAME_ONE);
         assertServiceCreated(NAME_ONE);
         assertServiceRunning(NAME_TWO);
         assertEquals(7, test1.createOrder);
         assertEquals(4, test1.startOrder);
         assertEquals(5, test1.stopOrder);
         assertEquals(6, test1.destroyOrder);
         assertEquals(1, test2.createOrder);
         assertEquals(3, test2.startOrder);
         assertEquals(0, test2.stopOrder);
         assertEquals(0, test2.destroyOrder);
         
         controller.start(NAME_ONE);
         assertServiceRunning(NAME_ONE);
         assertServiceRunning(NAME_TWO);
         assertEquals(7, test1.createOrder);
         assertEquals(8, test1.startOrder);
         assertEquals(5, test1.stopOrder);
         assertEquals(6, test1.destroyOrder);
         assertEquals(1, test2.createOrder);
         assertEquals(3, test2.startOrder);
         assertEquals(0, test2.stopOrder);
         assertEquals(0, test2.destroyOrder);
         
         controller.stop(NAME_TWO);
         assertServiceStopped(NAME_ONE);
         assertServiceStopped(NAME_TWO);
         assertEquals(7, test1.createOrder);
         assertEquals(8, test1.startOrder);
         assertEquals(9, test1.stopOrder);
         assertEquals(6, test1.destroyOrder);
         assertEquals(1, test2.createOrder);
         assertEquals(3, test2.startOrder);
         assertEquals(10, test2.stopOrder);
         assertEquals(0, test2.destroyOrder);
         
         controller.destroy(NAME_TWO);
         assertServiceDestroyed(NAME_ONE);
         assertServiceDestroyed(NAME_TWO);
         assertEquals(7, test1.createOrder);
         assertEquals(8, test1.startOrder);
         assertEquals(9, test1.stopOrder);
         assertEquals(11, test1.destroyOrder);
         assertEquals(1, test2.createOrder);
         assertEquals(3, test2.startOrder);
         assertEquals(10, test2.stopOrder);
         assertEquals(12, test2.destroyOrder);
         
         controller.create(NAME_TWO);
         assertServiceCreated(NAME_ONE);
         assertServiceCreated(NAME_TWO);
         assertEquals(14, test1.createOrder);
         assertEquals(8, test1.startOrder);
         assertEquals(9, test1.stopOrder);
         assertEquals(11, test1.destroyOrder);
         assertEquals(13, test2.createOrder);
         assertEquals(3, test2.startOrder);
         assertEquals(10, test2.stopOrder);
         assertEquals(12, test2.destroyOrder);
         
         controller.start(NAME_TWO);
         assertServiceRunning(NAME_ONE);
         assertServiceRunning(NAME_TWO);
         assertEquals(14, test1.createOrder);
         assertEquals(16, test1.startOrder);
         assertEquals(9, test1.stopOrder);
         assertEquals(11, test1.destroyOrder);
         assertEquals(13, test2.createOrder);
         assertEquals(15, test2.startOrder);
         assertEquals(10, test2.stopOrder);
         assertEquals(12, test2.destroyOrder);
      }
      catch (Exception e)
      {
         error = true;
         throw e;
      }
      catch (Error e)
      {
         error = true;
         throw e;
      }
      finally
      {
         undeploy(mbeans);

         if (error == false)
         {
            assertNoService(NAME_ONE);
            assertNotRegistered(NAME_ONE);
            assertEquals(14, test1.createOrder);
            assertEquals(16, test1.startOrder);
            assertEquals(17, test1.stopOrder);
            assertEquals(19, test1.destroyOrder);
            assertNoService(NAME_TWO);
            assertNotRegistered(NAME_TWO);
            assertEquals(13, test2.createOrder);
            assertEquals(15, test2.startOrder);
            assertEquals(18, test2.stopOrder);
            assertEquals(20, test2.destroyOrder);
         }
      }
   }
}

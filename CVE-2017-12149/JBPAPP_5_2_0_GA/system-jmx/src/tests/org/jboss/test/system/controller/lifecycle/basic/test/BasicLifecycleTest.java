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

import org.jboss.system.ServiceControllerMBean;
import org.jboss.test.system.controller.AbstractControllerTest;
import org.jboss.test.system.controller.support.Simple;
import org.jboss.test.system.controller.support.SimpleMBean;

/**
 * BasicLifecycleTest.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 85945 $
 */
public abstract class BasicLifecycleTest extends AbstractControllerTest
{
   public BasicLifecycleTest(String name)
   {
      super(name);
   }
   
   public void testBasicLifecyle() throws Exception
   {
      ObjectName name = SimpleMBean.OBJECT_NAME;
      Simple test = null;
      
      boolean error = false;
      
      List<ObjectName> mbeans = deploy("BasicLifecycle_NotAutomatic.xml");
      try
      {
         assertServiceRunning(name);
         
         MBeanServer server = getServer();
         test = (Simple) server.getAttribute(name, "Instance");
         assertEquals("()", test.constructorUsed);
         assertEquals(1, test.createOrder);
         assertEquals(2, test.startOrder);
         assertEquals(0, test.stopOrder);
         assertEquals(0, test.destroyOrder);
      }
      catch (Exception e)
      {
         error = true;
         throw e;
      }
      finally
      {
         undeploy(mbeans);

         if (error == false)
         {
            assertNoService(name);
            assertNotRegistered(name);
            
            assertEquals(1, test.createOrder);
            assertEquals(2, test.startOrder);
            assertEquals(3, test.stopOrder);
            assertEquals(4, test.destroyOrder);
         }
      }
   }
   
   public void testBasicRedeploy() throws Exception
   {
      ObjectName name = SimpleMBean.OBJECT_NAME;
      Simple test = null;
      
      boolean error = false;
      
      List<ObjectName> mbeans = deploy("BasicLifecycle_NotAutomatic.xml");
      try
      {
         assertServiceRunning(name);
         
         MBeanServer server = getServer();
         test = (Simple) server.getAttribute(name, "Instance");
         assertEquals("()", test.constructorUsed);
         assertEquals(1, test.createOrder);
         assertEquals(2, test.startOrder);
         assertEquals(0, test.stopOrder);
         assertEquals(0, test.destroyOrder);
      }
      catch (Exception e)
      {
         error = true;
         throw e;
      }
      finally
      {
         undeploy(mbeans);

         if (error == false)
         {
            assertNoService(name);
            assertNotRegistered(name);
            
            assertEquals(1, test.createOrder);
            assertEquals(2, test.startOrder);
            assertEquals(3, test.stopOrder);
            assertEquals(4, test.destroyOrder);
         }
      }
      
      mbeans = deploy("BasicLifecycle_NotAutomatic.xml");
      try
      {
         assertServiceRunning(name);
         
         MBeanServer server = getServer();
         test = (Simple) server.getAttribute(name, "Instance");
         assertEquals("()", test.constructorUsed);
         assertEquals(5, test.createOrder);
         assertEquals(6, test.startOrder);
         assertEquals(0, test.stopOrder);
         assertEquals(0, test.destroyOrder);
      }
      catch (Exception e)
      {
         error = true;
         throw e;
      }
      finally
      {
         undeploy(mbeans);

         if (error == false)
         {
            assertNoService(name);
            assertNotRegistered(name);
            
            assertEquals(5, test.createOrder);
            assertEquals(6, test.startOrder);
            assertEquals(7, test.stopOrder);
            assertEquals(8, test.destroyOrder);
         }
      }
   }
   
   public void testBasicManualLifecycle() throws Exception
   {
      ObjectName name = SimpleMBean.OBJECT_NAME;
      Simple test = null;
      
      boolean error = false;
      
      List<ObjectName> mbeans = deploy("BasicLifecycle_NotAutomatic.xml");
      try
      {
         assertServiceRunning(name);

         MBeanServer server = getServer();
         test = (Simple) server.getAttribute(name, "Instance");
         assertEquals("()", test.constructorUsed);
         assertEquals(1, test.createOrder);
         assertEquals(2, test.startOrder);
         assertEquals(0, test.stopOrder);
         assertEquals(0, test.destroyOrder);

         ServiceControllerMBean controller = getController();

         controller.stop(name);
         assertServiceStopped(name);
         assertEquals(1, test.createOrder);
         assertEquals(2, test.startOrder);
         assertEquals(3, test.stopOrder);
         assertEquals(0, test.destroyOrder);

         controller.destroy(name);
         assertServiceDestroyed(name);
         assertEquals(1, test.createOrder);
         assertEquals(2, test.startOrder);
         assertEquals(3, test.stopOrder);
         assertEquals(4, test.destroyOrder);

         controller.create(name);
         assertServiceCreated(name);
         assertEquals(5, test.createOrder);
         assertEquals(2, test.startOrder);
         assertEquals(3, test.stopOrder);
         assertEquals(4, test.destroyOrder);

         controller.start(name);
         assertServiceRunning(name);
         assertEquals(5, test.createOrder);
         assertEquals(6, test.startOrder);
         assertEquals(3, test.stopOrder);
         assertEquals(4, test.destroyOrder);
      }
      catch (Exception e)
      {
         error = true;
         throw e;
      }
      finally
      {
         undeploy(mbeans);

         if (error == false)
         {
            assertNoService(name);
            assertNotRegistered(name);
            
            assertEquals(5, test.createOrder);
            assertEquals(6, test.startOrder);
            assertEquals(7, test.stopOrder);
            assertEquals(8, test.destroyOrder);
         }
      }
   }
}

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
package org.jboss.test.system.controller.lifecycle.seperated.test;

import java.util.List;

import javax.management.ObjectName;

import org.jboss.mx.util.ObjectNameFactory;
import org.jboss.test.AbstractTestDelegate;
import org.jboss.test.system.controller.AbstractControllerTest;
import org.jboss.test.system.controller.support.Simple;

/**
 * NewOnDemandDependencyTest.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 85945 $
 */
public abstract class NewOnDemandDependencyTest extends AbstractControllerTest
{
   private static ObjectName NAME_ONE = ObjectNameFactory.create("test:name=1"); 
   private static ObjectName NAME_TWO = ObjectNameFactory.create("test:name=2"); 
   
   protected String resourceName1;
   protected String resourceName2;
   
   public NewOnDemandDependencyTest(String name)
   {
      super(name);
   }

   public static AbstractTestDelegate getDelegate(Class clazz) throws Exception
   {
      return getNewControllerDelegate(clazz);
   }
   
   public void testDeployCorrectOrder() throws Exception
   {
      Simple test1 = null;
      Simple test2 = null;
      
      boolean error = false;
      
      List<ObjectName> mbeans1 = deploy(resourceName1);
      try
      {
         assertServiceInstalled(NAME_ONE);
         assertNotRegistered(NAME_ONE);
         assertNoService(NAME_TWO);

         List<ObjectName> mbeans2 = deploy(resourceName2);
         try
         {
            assertServiceRunning(NAME_ONE);
            assertServiceRunning(NAME_TWO);
            
            test1 = getMBean(Simple.class, NAME_ONE, "Instance");
            assertEquals(1, test1.createOrder);
            assertEquals(3, test1.startOrder);
            assertEquals(0, test1.stopOrder);
            assertEquals(0, test1.destroyOrder);
            test2 = getMBean(Simple.class, NAME_TWO, "Instance");
            assertEquals("()", test2.constructorUsed);
            assertEquals(2, test2.createOrder);
            assertEquals(4, test2.startOrder);
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
            undeploy(mbeans2);

            if (error == false)
            {
               assertServiceRunning(NAME_ONE);
               assertEquals(1, test1.createOrder);
               assertEquals(3, test1.startOrder);
               assertEquals(0, test1.stopOrder);
               assertEquals(0, test1.destroyOrder);
               assertNoService(NAME_TWO);
               assertNotRegistered(NAME_TWO);
               assertEquals(2, test2.createOrder);
               assertEquals(4, test2.startOrder);
               assertEquals(5, test2.stopOrder);
               assertEquals(6, test2.destroyOrder);
            }
         }
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
         undeploy(mbeans1);

         if (error == false)
         {
            assertNoService(NAME_ONE);
            assertNotRegistered(NAME_ONE);
            assertEquals(1, test1.createOrder);
            assertEquals(3, test1.startOrder);
            assertEquals(7, test1.stopOrder);
            assertEquals(8, test1.destroyOrder);
            assertNoService(NAME_TWO);
            assertNotRegistered(NAME_TWO);
            if (test2 != null)
            {
               assertEquals(2, test2.createOrder);
               assertEquals(4, test2.startOrder);
               assertEquals(5, test2.stopOrder);
               assertEquals(6, test2.destroyOrder);
            }
         }
      }
   }
   
   public void testDeployWrongOrder() throws Exception
   {
      Simple test1 = null;
      Simple test2 = null;
      
      boolean error = false;
      
      List<ObjectName> mbeans2 = deploy(resourceName2);
      try
      {
         assertServiceConfigured(NAME_TWO);
         
         test2 = getMBean(Simple.class, NAME_TWO, "Instance");
         assertEquals("()", test2.constructorUsed);
         assertEquals(0, test2.createOrder);
         assertEquals(0, test2.startOrder);
         assertEquals(0, test2.stopOrder);
         assertEquals(0, test2.destroyOrder);

         List<ObjectName> mbeans1 = deploy(resourceName1);
         try
         {
            assertServiceRunning(NAME_TWO);
            assertServiceRunning(NAME_ONE);
            
            assertEquals(2, test2.createOrder);
            assertEquals(4, test2.startOrder);
            assertEquals(0, test2.stopOrder);
            assertEquals(0, test2.destroyOrder);
            test1 = getMBean(Simple.class, NAME_ONE, "Instance");
            assertEquals("()", test1.constructorUsed);
            assertEquals(1, test1.createOrder);
            assertEquals(3, test1.startOrder);
            assertEquals(0, test1.stopOrder);
            assertEquals(0, test1.destroyOrder);
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
            undeploy(mbeans1);

            if (error == false)
            {
               assertServiceDestroyed(NAME_TWO);
               assertEquals(2, test2.createOrder);
               assertEquals(4, test2.startOrder);
               assertEquals(5, test2.stopOrder);
               assertEquals(7, test2.destroyOrder);
               assertNotRegistered(NAME_ONE);
               assertEquals(1, test1.createOrder);
               assertEquals(3, test1.startOrder);
               assertEquals(6, test1.stopOrder);
               assertEquals(8, test1.destroyOrder);
            }
         }
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
         undeploy(mbeans2);

         if (error == false)
         {
            assertNoService(NAME_TWO);
            assertNotRegistered(NAME_TWO);
            assertEquals(2, test2.createOrder);
            assertEquals(4, test2.startOrder);
            assertEquals(5, test2.stopOrder);
            assertEquals(7, test2.destroyOrder);
            assertNoService(NAME_ONE);
            assertNotRegistered(NAME_ONE);
            if (test1 != null)
            {
               assertEquals(1, test1.createOrder);
               assertEquals(3, test1.startOrder);
               assertEquals(6, test1.stopOrder);
               assertEquals(8, test1.destroyOrder);
            }
         }
      }
   }
   
   public void testRedeployOne() throws Exception
   {
      Simple test1 = null;
      Simple test2 = null;
      
      boolean error = false;
      
      List<ObjectName> mbeans1 = deploy(resourceName1);
      try
      {
         List<ObjectName> mbeans2 = deploy(resourceName2);
         try
         {
            assertServiceRunning(NAME_ONE);
            assertServiceRunning(NAME_TWO);
            
            test1 = getMBean(Simple.class, NAME_ONE, "Instance");
            assertEquals("()", test1.constructorUsed);
            assertEquals(1, test1.createOrder);
            assertEquals(3, test1.startOrder);
            assertEquals(0, test1.stopOrder);
            assertEquals(0, test1.destroyOrder);
            test2 = getMBean(Simple.class, NAME_TWO, "Instance");
            assertEquals("()", test2.constructorUsed);
            assertEquals(2, test2.createOrder);
            assertEquals(4, test2.startOrder);
            assertEquals(0, test2.stopOrder);
            assertEquals(0, test2.destroyOrder);
            
            undeploy(mbeans1);

            assertNotRegistered(NAME_ONE);
            assertServiceDestroyed(NAME_TWO);
            
            assertEquals(1, test1.createOrder);
            assertEquals(3, test1.startOrder);
            assertEquals(6, test1.stopOrder);
            assertEquals(8, test1.destroyOrder);
            assertEquals(2, test2.createOrder);
            assertEquals(4, test2.startOrder);
            assertEquals(5, test2.stopOrder);
            assertEquals(7, test2.destroyOrder);
            
            mbeans1 = deploy(resourceName1);

            assertServiceRunning(NAME_ONE);
            assertServiceRunning(NAME_TWO);
            
            test1 = getMBean(Simple.class, NAME_ONE, "Instance");
            assertEquals("()", test1.constructorUsed);
            assertEquals(9, test1.createOrder);
            assertEquals(11, test1.startOrder);
            assertEquals(0, test1.stopOrder);
            assertEquals(0, test1.destroyOrder);
            assertEquals(10, test2.createOrder);
            assertEquals(12, test2.startOrder);
            assertEquals(5, test2.stopOrder);
            assertEquals(7, test2.destroyOrder);
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
            undeploy(mbeans2);

            if (error == false)
            {
               assertServiceRunning(NAME_ONE);
               assertEquals(9, test1.createOrder);
               assertEquals(11, test1.startOrder);
               assertEquals(0, test1.stopOrder);
               assertEquals(0, test1.destroyOrder);
               assertNoService(NAME_TWO);
               assertNotRegistered(NAME_TWO);
               assertEquals(10, test2.createOrder);
               assertEquals(12, test2.startOrder);
               assertEquals(13, test2.stopOrder);
               assertEquals(14, test2.destroyOrder);
            }
         }
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
         undeploy(mbeans1);

         if (error == false)
         {
            assertNoService(NAME_ONE);
            assertNotRegistered(NAME_ONE);
            assertEquals(9, test1.createOrder);
            assertEquals(11, test1.startOrder);
            assertEquals(15, test1.stopOrder);
            assertEquals(16, test1.destroyOrder);
            assertNoService(NAME_TWO);
            assertNotRegistered(NAME_TWO);
            if (test2 != null)
            {
               assertEquals(10, test2.createOrder);
               assertEquals(12, test2.startOrder);
               assertEquals(13, test2.stopOrder);
               assertEquals(14, test2.destroyOrder);
            }
         }
      }
   }
   
   public void testRedeployTwo() throws Exception
   {
      Simple test1 = null;
      Simple test2 = null;
      
      boolean error = false;
      
      List<ObjectName> mbeans1 = deploy(resourceName1);
      try
      {
         List<ObjectName> mbeans2 = deploy(resourceName2);
         try
         {
            assertServiceRunning(NAME_ONE);
            assertServiceRunning(NAME_TWO);
            
            test1 = getMBean(Simple.class, NAME_ONE, "Instance");
            assertEquals("()", test1.constructorUsed);
            assertEquals(1, test1.createOrder);
            assertEquals(3, test1.startOrder);
            assertEquals(0, test1.stopOrder);
            assertEquals(0, test1.destroyOrder);
            test2 = getMBean(Simple.class, NAME_TWO, "Instance");
            assertEquals("()", test2.constructorUsed);
            assertEquals(2, test2.createOrder);
            assertEquals(4, test2.startOrder);
            assertEquals(0, test2.stopOrder);
            assertEquals(0, test2.destroyOrder);
            
            undeploy(mbeans2);

            assertServiceRunning(NAME_ONE);
            assertNoService(NAME_TWO);
            
            assertEquals(1, test1.createOrder);
            assertEquals(3, test1.startOrder);
            assertEquals(0, test1.stopOrder);
            assertEquals(0, test1.destroyOrder);
            assertEquals(2, test2.createOrder);
            assertEquals(4, test2.startOrder);
            assertEquals(5, test2.stopOrder);
            assertEquals(6, test2.destroyOrder);
            
            mbeans2 = deploy(resourceName2);

            assertServiceRunning(NAME_ONE);
            assertServiceRunning(NAME_TWO);
            
            assertEquals(1, test1.createOrder);
            assertEquals(3, test1.startOrder);
            assertEquals(0, test1.stopOrder);
            assertEquals(0, test1.destroyOrder);
            test2 = getMBean(Simple.class, NAME_TWO, "Instance");
            assertEquals("()", test1.constructorUsed);
            assertEquals(7, test2.createOrder);
            assertEquals(8, test2.startOrder);
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
            undeploy(mbeans2);

            if (error == false)
            {
               assertServiceRunning(NAME_ONE);
               assertEquals(1, test1.createOrder);
               assertEquals(3, test1.startOrder);
               assertEquals(0, test1.stopOrder);
               assertEquals(0, test1.destroyOrder);
               assertNoService(NAME_TWO);
               assertNotRegistered(NAME_TWO);
               assertEquals(7, test2.createOrder);
               assertEquals(8, test2.startOrder);
               assertEquals(9, test2.stopOrder);
               assertEquals(10, test2.destroyOrder);
            }
         }
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
         undeploy(mbeans1);

         if (error == false)
         {
            assertNoService(NAME_ONE);
            assertNotRegistered(NAME_ONE);
            assertEquals(1, test1.createOrder);
            assertEquals(3, test1.startOrder);
            assertEquals(11, test1.stopOrder);
            assertEquals(12, test1.destroyOrder);
            assertNoService(NAME_TWO);
            assertNotRegistered(NAME_TWO);
            if (test2 != null)
            {
               assertEquals(7, test2.createOrder);
               assertEquals(8, test2.startOrder);
               assertEquals(9, test2.stopOrder);
               assertEquals(10, test2.destroyOrder);
            }
         }
      }
   }
}

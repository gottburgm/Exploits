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
import org.jboss.test.system.controller.AbstractControllerTest;

/**
 * DependsBrokenTest.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 85945 $
 */
public abstract class DependsBrokenTest extends AbstractControllerTest
{
   private static ObjectName NAME_ONE = ObjectNameFactory.create("test:name=1"); 
   private static ObjectName NAME_TWO = ObjectNameFactory.create("test:name=2"); 
   
   private String resourceName1;
   private String resourceName2;

   public DependsBrokenTest(String name)
   {
      super(name);

      String resourceName = getClass().getName();
      int index= resourceName.lastIndexOf('.'); 
      if (index != -1)
         resourceName= resourceName.substring(index + 1, resourceName.length());
      index = resourceName.indexOf("NewUnitTestCase");
      if (index != -1)
         resourceName = resourceName.substring(0, index);
      index = resourceName.indexOf("OldUnitTestCase");
      if (index != -1)
         resourceName = resourceName.substring(0, index);
      
      resourceName1 = resourceName + "_bad.xml";
      resourceName2 = resourceName + "_good.xml";
   }
   
   public void deployBrokenFirstMaybeDeployFailure(int expectedState, Class<? extends Throwable> expected) throws Exception
   {
      boolean error = false;

      List<ObjectName> mbeans1 = assertMaybeDeployFailure(resourceName1, NAME_ONE, expected);
      try
      {
         List<ObjectName> mbeans2 = deploy(resourceName2);
         try
         {
            assertServiceState(NAME_TWO, expectedState);
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
            assertNoService(NAME_TWO);
            assertNotRegistered(NAME_TWO);
         }
      }
   }
   
   public void deployBrokenSecondMaybeDeployFailure(int expectedState, Class<? extends Throwable> expected) throws Exception
   {
      boolean error = false;

      List<ObjectName> mbeans2 = deploy(resourceName2);
      try
      {
         assertServiceState(NAME_TWO, expectedState);
         assertRegistered(NAME_TWO);

         List<ObjectName> mbeans1 = assertMaybeDeployFailure(resourceName1, NAME_ONE, expected);
         try
         {
            assertServiceState(NAME_TWO, expectedState);
            assertRegistered(NAME_TWO);
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
               assertServiceState(NAME_TWO, expectedState);
               assertRegistered(NAME_TWO);
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
            assertNoService(NAME_ONE);
            assertNotRegistered(NAME_ONE);
            assertNoService(NAME_TWO);
            assertNotRegistered(NAME_TWO);
         }
      }
   }
   
   public void deployBrokenFirstDeployFailure(int expectedState, Class<? extends Throwable> expected) throws Exception
   {
      boolean error = false;

      List<ObjectName> mbeans1 = assertDeployFailure(resourceName1, NAME_ONE, expected);
      try
      {
         List<ObjectName> mbeans2 = deploy(resourceName2);
         try
         {
            assertServiceState(NAME_TWO, expectedState);
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
            assertNoService(NAME_TWO);
            assertNotRegistered(NAME_TWO);
         }
      }
   }
   
   public void deployBrokenSecondDeployFailure(int expectedState, Class<? extends Throwable> expected) throws Exception
   {
      deployBrokenSecondDeployFailure(expectedState, expectedState, expectedState, expected);
   }
   
   public void deployBrokenSecondDeployFailure(int expectedState, int brokenExpectedState, Class<? extends Throwable> expected) throws Exception
   {
      deployBrokenSecondDeployFailure(expectedState, expectedState, brokenExpectedState, expected);
   }
   
   public void deployBrokenSecondDeployFailure(int beforeExpectedState, int afterExpectedState, int brokenExpectedState, Class<? extends Throwable> expected) throws Exception
   {
      boolean error = false;

      List<ObjectName> mbeans2 = deploy(resourceName2);
      try
      {
         assertServiceState(NAME_TWO, beforeExpectedState);
         assertRegistered(NAME_TWO);

         List<ObjectName> mbeans1 = assertDeployFailure(resourceName1, NAME_ONE, expected);
         try
         {
            assertServiceState(NAME_TWO, afterExpectedState);
            assertRegistered(NAME_TWO);
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
               assertServiceState(NAME_TWO, brokenExpectedState);
               assertRegistered(NAME_TWO);
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
            assertNoService(NAME_ONE);
            assertNotRegistered(NAME_ONE);
            assertNoService(NAME_TWO);
            assertNotRegistered(NAME_TWO);
         }
      }
   }
   
   public void deployBrokenSecondInitialDeployFailure(int expectedState, Class<? extends Throwable> expected) throws Exception
   {
      boolean error = false;

      List<ObjectName> mbeans2 = deploy(resourceName2);
      try
      {
         assertServiceState(NAME_TWO, expectedState);
         assertRegistered(NAME_TWO);

         assertInitialDeployFailure(resourceName1, NAME_ONE, expected);
         try
         {
            assertServiceState(NAME_TWO, expectedState);
            assertRegistered(NAME_TWO);
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
            if (error == false)
            {
               assertServiceState(NAME_TWO, expectedState);
               assertRegistered(NAME_TWO);
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
            assertNoService(NAME_ONE);
            assertNotRegistered(NAME_ONE);
            assertNoService(NAME_TWO);
            assertNotRegistered(NAME_TWO);
         }
      }
   }
}

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
package org.jboss.test.lock.test;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.jboss.test.JBossTestCase;

/**
 * #Description of the Class
 */
public class EnterpriseEntityStressTestCase
       extends JBossTestCase
{
   /**
    * Constructor for the EnterpriseEntityStressTestCase object
    *
    * @param name  Description of Parameter
    */
   public EnterpriseEntityStressTestCase(String name)
   {
      super(name);
   }

   /**
    * Setup the test suite.
    *
    * @return   The test suite
    */
   public static Test suite() throws Exception
   {
      TestSuite suite = new TestSuite();


      // Test ejb.plugins.lock.QueuedPessimisticEJBLock
      suite.addTest(new TestSuite(Entity_Option_A_Test.class));
      suite.addTest(new TestSuite(Entity_Option_B_Test.class));
      suite.addTest(new TestSuite(Entity_Option_C_Test.class));
      /* FIXME: These tests are broken
      suite.addTest(new TestSuite(Entity_Option_D_Test.class));

      suite.addTest(new TestSuite(Entity_Option_C_Multi_Test.class));
      */

      return getDeploySetup(suite, "locktest.jar");
   }

   /**
    * #Description of the Class
    */
   public static class Entity_Option_A_Test
          extends EnterpriseEntityTest
   {
      /**
       * Constructor for the Entity_Option_A_Test object
       *
       * @param name  Description of Parameter
       */
      public Entity_Option_A_Test(String name)
      {
         super(name, "EnterpriseEntity_A");
      }
   }

   /**
    * #Description of the Class
    */
   public static class Entity_Option_B_Test
          extends EnterpriseEntityTest
   {
      /**
       * Constructor for the Entity_Option_B_Test object
       *
       * @param name  Description of Parameter
       */
      public Entity_Option_B_Test(String name)
      {
         super(name, "EnterpriseEntity_B");
      }
      
      public void testB2B() throws Exception
      {
         // This test will not work with commit-option B, because
         // all fields of the entity bean are nulled out on activation
      }
      
   }

   /**
    * #Description of the Class
    */
   public static class Entity_Option_C_Test
          extends EnterpriseEntityTest
   {
      /**
       * Constructor for the Entity_Option_C_Test object
       *
       * @param name  Description of Parameter
       */
      public Entity_Option_C_Test(String name)
      {
         super(name, "EnterpriseEntity_C");
      }
      public void testB2B() throws Exception
      {
         // This test will not work with commit-option C, because
         // all fields of the entity bean are nulled out on activation
      }
   }

   /**
    * #Description of the Class
    */
   public static class Entity_Option_D_Test
          extends EnterpriseEntityTest
   {
      // This test will not work a cache invalidation nulls the data

      /**
       * Constructor for the Entity_Option_D_Test object
       *
       * @param name  Description of Parameter
       */
      public Entity_Option_D_Test(String name)
      {
         super(name, "EnterpriseEntity_D");
      }
   }

   /**
    * #Description of the Class
    */
   public static class Entity_Option_B_Multi_Test
          extends EnterpriseEntityTest
   {
      /**
       * Constructor for the Entity_Option_B_Multi_Test object
       *
       * @param name  Description of Parameter
       */
      public Entity_Option_B_Multi_Test(String name)
      {
         super(name, "EnterpriseEntity_B_Multi");
      }
      public void testB2B() throws Exception
      {
         // This test will not work with commit-option B, because
         // all fields of the entity bean are nulled out on activation
      }
   }

   /**
    * #Description of the Class
    */
   public static class Entity_Option_C_Multi_Test
          extends EnterpriseEntityTest
   {
      /**
       * Constructor for the Entity_Option_C_Multi_Test object
       *
       * @param name  Description of Parameter
       */
      public Entity_Option_C_Multi_Test(String name)
      {
         super(name, "EnterpriseEntity_C_Multi");
      }
      public void testB2B() throws Exception
      {
         // This test will not work with commit-option C, because
         // all fields of the entity bean are nulled out on activation
      }
   }

}


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

import javax.ejb.CreateException;
import javax.ejb.RemoveException;
import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;

import junit.framework.Test;

import org.jboss.logging.Logger;
import org.jboss.test.JBossTestCase;
import org.jboss.test.lock.interfaces.EnterpriseEntity;
import org.jboss.test.lock.interfaces.EnterpriseEntityHome;

/**
 * Test of EJB call invocation overhead.
 *
 * @author    Scott.Stark@jboss.org
 * @version   $Revision: 81036 $
 */
public class SpinUnitTestCase extends JBossTestCase
{
   /**
    * Constructor for the SpinUnitTestCase object
    *
    * @param name  Description of Parameter
    */
   public SpinUnitTestCase(String name)
   {
      super(name);
   }

   static void create() throws Exception
   {
      InitialContext jndiContext = new InitialContext();
      Object obj = jndiContext.lookup("EnterpriseEntity_A");
      obj = PortableRemoteObject.narrow(obj, EnterpriseEntityHome.class);
      EnterpriseEntityHome home = (EnterpriseEntityHome)obj;
      try
      {
         home.create("Bean1");
      }
      catch (CreateException e)
      {
      }
   }

   static void remove() throws Exception
   {
      InitialContext jndiContext = new InitialContext();
      Object obj = jndiContext.lookup("EnterpriseEntity_A");
      obj = PortableRemoteObject.narrow(obj, EnterpriseEntityHome.class);
      EnterpriseEntityHome home = (EnterpriseEntityHome)obj;
      try
      {
         home.remove("Bean1");
      }
      catch (RemoveException e)
      {
      }
   }

   /**
    * A unit test for JUnit
    *
    * @exception Exception  Description of Exception
    */
   public void testContention() throws Exception
   {
      getLog().debug("+++ testContention()");
      InitialContext jndiContext = new InitialContext();
      Object obj = jndiContext.lookup("EnterpriseEntity_A");
      obj = PortableRemoteObject.narrow(obj, EnterpriseEntityHome.class);
      EnterpriseEntityHome home = (EnterpriseEntityHome)obj;
      getLog().debug("Found EnterpriseEntityHome @ jndiName=EnterpriseEntity");
      Run r0 = new Run(home.findByPrimaryKey("Bean1"), getLog());
      Run r1 = new Run(home.findByPrimaryKey("Bean1"), getLog());
      Run r2 = new Run(home.findByPrimaryKey("Bean1"), getLog());
      Thread t0 = new Thread(r0);
      Thread t1 = new Thread(r1);
      Thread t2 = new Thread(r2);
      t0.start();
      Thread.sleep(100);
      t1.start();
      Thread.sleep(100);
      t2.start();
      getLog().debug("Waiting for t0...");
      try
      {
         t0.join(5000);
         assertTrue(r0.ex == null);
      }
      catch (InterruptedException e)
      {
         getLog().debug("Timed out waiting for t1");
      }
      getLog().debug("Waiting for t1...");
      try
      {
         t1.join(5000);
         assertTrue(r1.ex == null);
      }
      catch (InterruptedException e)
      {
         getLog().debug("Timed out waiting for t1");
      }
      getLog().debug("Waiting for t2...");
      try
      {
         t2.join(5000);
         assertTrue(r2.ex == null);
      }
      catch (InterruptedException e)
      {
         getLog().debug("Timed out waiting for t2");
      }

      getLog().debug("End threads");
   }

   /**
    * The JUnit setup method
    *
    * @exception Exception  Description of Exception
    */
   protected void setUp() throws Exception
   {
      try
      {
         create();
      }
      catch (Exception e)
      {
         getLog().error("setup error in create: ", e);
         throw e;
      }
   }

   /**
    * The teardown method for JUnit
    *
    * @exception Exception  Description of Exception
    */
   protected void tearDown() throws Exception
   {
      try
      {
         remove();
      }
      catch (Exception e)
      {
         getLog().error("teardown error in remove: ", e);
         throw e;
      }
   }

   public static Test suite() throws Exception
   {
      return getDeploySetup(SpinUnitTestCase.class, "locktest.jar");
   }



   /**
    * #Description of the Class
    */
   static class Run implements Runnable
   {
      EnterpriseEntity bean;
      Exception ex;
      private Logger log;

      Run(EnterpriseEntity bean, Logger log)
      {
         this.bean = bean;
         this.log = log;
      }

      /**
       * Main processing method for the Run object
       */
      public synchronized void run()
      {
         notifyAll();
         try
         {
            long start = System.currentTimeMillis();
            bean.sleep(5000);
            long end = System.currentTimeMillis();
            long elapsed = end - start;
            log.debug(" bean.sleep() time = " + elapsed + " ms");
         }
         catch (Exception e)
         {
            ex = e;
         }
      }
   }

}

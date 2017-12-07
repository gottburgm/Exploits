/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.embedded.tutorial.junit;

import javax.naming.InitialContext;
import javax.persistence.EntityManager;
import javax.transaction.TransactionManager;

import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.framework.Test;
import junit.extensions.TestSetup;
import org.jboss.embedded.tutorial.junit.beans.CustomerDAORemote;
import org.jboss.embedded.tutorial.junit.beans.CustomerDAOLocal;
import org.jboss.embedded.tutorial.junit.beans.Customer;
import org.jboss.embedded.tutorial.junit.beans.CustomerDAOBean;
import org.jboss.embedded.junit.EmbeddedTestSetup;
import org.jboss.embedded.Bootstrap;
import org.jboss.deployers.spi.DeploymentException;
import org.jboss.virtual.plugins.context.vfs.AssembledDirectory;
import org.jboss.virtual.plugins.context.vfs.AssembledContextFactory;

/**
 * Comment
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @version $Revision: 85945 $
 */
public class EjbTestCase extends TestCase
{
   public EjbTestCase()
   {
      super("EjbTestCase");
   }

   private static AssembledDirectory jar;
   private static boolean globalSetup = false;

   private static void deploy()
   {
      jar = AssembledContextFactory.getInstance().create("ejbTestCase.jar");
      jar.addClass(Customer.class);
      jar.addClass(CustomerDAOBean.class);
      jar.addClass(CustomerDAOLocal.class);
      jar.addClass(CustomerDAORemote.class);
      jar.mkdir("META-INF").addResource("tutorial-persistence.xml", "persistence.xml");
      try
      {
         Bootstrap.getInstance().deploy(jar);
      }
      catch (DeploymentException e)
      {
         throw new RuntimeException("Unable to deploy", e);
      }
   }

   private static void undeploy()
   {
      try
      {
         Bootstrap.getInstance().undeploy(jar);
         AssembledContextFactory.getInstance().remove(jar);
      }
      catch (DeploymentException e)
      {
         throw new RuntimeException("Unable to undeploy", e);
      }
   }

   

   public static Test suite()
   {
      TestSuite suite = new TestSuite();
      suite.addTestSuite(EjbTestCase.class);
      globalSetup = true;

      return new TestSetup(suite)
      {
         @Override
         protected void setUp() throws Exception
         {
            super.setUp();
            if (!Bootstrap.getInstance().isStarted())
            {
               Bootstrap.getInstance().bootstrap();
            }
            deploy();
         }

         @Override
         protected void tearDown() throws Exception
         {
            undeploy();
            if (System.getProperty("shutdown.embedded.jboss") != null) Bootstrap.getInstance().shutdown();
            super.tearDown();
         }
      };
   }




   @Override
   protected void setUp() throws Exception
   {
      if (globalSetup) return;
      Bootstrap.getInstance().bootstrap();
      deploy();
   }

   @Override
   protected void tearDown() throws Exception
   {
      if (globalSetup) return;
      undeploy();
   }


   public void testEJBs() throws Exception
   {
      InitialContext ctx = new InitialContext();
      CustomerDAOLocal local = (CustomerDAOLocal) ctx.lookup("CustomerDAOBean/local");
      CustomerDAORemote remote = (CustomerDAORemote) ctx.lookup("CustomerDAOBean/remote");

      int id = local.createCustomer("Gavin");
      Customer cust = local.findCustomer(id);
      assertNotNull(cust);
      System.out.println("Successfully created and found Gavin from @Local interface");

      id = remote.createCustomer("Emmanuel");
      cust = remote.findCustomer(id);
      assertNotNull(cust);
      System.out.println("Successfully created and found Emmanuel from @Remote interface");
   }

   public void testEntityManager() throws Exception
   {
      // This is a transactionally aware EntityManager and must be accessed within a JTA transaction
      // Why aren't we using javax.persistence.Persistence?  Well, our persistence.xml file uses
      // jta-datasource which means that it is created by the EJB container/embedded JBoss.
      // using javax.persistence.Persistence will just cause us an error
      EntityManager em = (EntityManager) new InitialContext().lookup("java:/EntityManagers/custdb");

      // Obtain JBoss transaction
      TransactionManager tm = (TransactionManager) new InitialContext().lookup("java:/TransactionManager");

      tm.begin();

      Customer cust = new Customer();
      cust.setName("Bill");
      em.persist(cust);

      assertTrue(cust.getId() > 0);

      int id = cust.getId();

      System.out.println("created bill in DB with id: " + id);

      tm.commit();

      tm.begin();
      cust = em.find(Customer.class, id);
      assertNotNull(cust);
      tm.commit();
   }
}

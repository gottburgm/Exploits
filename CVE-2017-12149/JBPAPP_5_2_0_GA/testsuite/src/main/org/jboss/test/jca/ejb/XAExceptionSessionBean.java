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
package org.jboss.test.jca.ejb;

import javax.ejb.EJBException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.naming.InitialContext;
import javax.resource.ResourceException;
import javax.resource.cci.Connection;
import javax.resource.cci.ConnectionFactory;
import javax.transaction.xa.XAException;

import org.jboss.logging.Logger;
import org.jboss.mx.util.MBeanServerLocator;
import org.jboss.test.jca.adapter.TestConnection;
import org.jboss.test.jca.adapter.TestConnectionFactory;

/**
 * XAExceptionSessionBean.java
 *
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @author <a href="mailto:adrian@jboss.com">Adrian Brock</a>
 * @version <tt>$Revision: 81036 $</tt>
 *
 * @ejb:bean   name="XAExceptionSession"
 *             jndi-name="test/XAExceptionSessionHome"
 *             local-jndi-name="test/XAExceptionSessionLocalHome"
 *             view-type="both"
 *             type="Stateless"
 * @ejb.transaction type="Required"
 *
 */
public class XAExceptionSessionBean implements SessionBean
{
   /** The serialVersionUID */
   private static final long serialVersionUID = 1L;

   private Logger log = Logger.getLogger(XAExceptionSessionBean.class);
   
   private SessionContext sessionContext;

   /**
    * Describe <code>ejbCreate</code> method here.
    * @ejb.interface-method
    */
   public void ejbCreate()
   {
   }
   

   /**
    * Describe <code>testXAException</code> method here.
    * @ejb.interface-method
    */
   public void testXAExceptionToTransactionRolledbackException()
   {
      try
      {

         InitialContext ctx = new InitialContext();
         ConnectionFactory cf1 = (ConnectionFactory) ctx.lookup("java:/JBossTestCF");
         ConnectionFactory cf2 = (ConnectionFactory) ctx.lookup("java:/JBossTestCF2");
         Connection c1 = cf1.getConnection();
         try
         {
            TestConnection c2 = (TestConnection) cf2.getConnection();
            try
            {
               c2.setFailInPrepare(true, XAException.XA_RBROLLBACK);
            }
            finally
            {
               c2.close();
            }
         }
         finally
         {
            c1.close();
         }
      }
      catch (Exception e)
      {
         log.warn("Unexpected: ", e);
         throw new EJBException("unexpected exception: " + e);
      }
   }

   /**
    * Describe <code>testXAException</code> method here.
    * @ejb.interface-method
    */
   public void testRMERRInOnePCToTransactionRolledbackException()
   {
      try
      {

         InitialContext ctx = new InitialContext();
         ConnectionFactory cf1 = (ConnectionFactory) ctx.lookup("java:/JBossTestCF");
         TestConnection c1 = (TestConnection) cf1.getConnection();
         try
         {
            c1.setFailInCommit(true, XAException.XAER_RMERR);

         }
         finally
         {
            c1.close();
         }

      }
      catch (Exception e)
      {
         log.warn("Unexpected: ", e);
         throw new EJBException("unexpected exception: " + e);
      }
   }

   /**
    * Similate a connection failure
    *
    * @ejb.interface-method
    */
   public void simulateConnectionError()
   {
      log.info("Simulating connection error");
      try
      {
         InitialContext ctx = new InitialContext();
         ConnectionFactory cf = (ConnectionFactory) ctx.lookup("java:/JBossTestCF");
         TestConnection c = (TestConnection) cf.getConnection();
         try
         {
            c.simulateConnectionError();
         }
         finally
         {
            c.close();
         }
      }
      catch (Exception e)
      {
         if (e.getMessage().equals("Simulated exception") == false)
         {
            log.warn("Unexpected: ", e);
            throw new EJBException(e.getMessage());
         }
         else
         {
            sessionContext.setRollbackOnly();
         }
      }
   }

   /**
    * Similate a connection failure
    *
    * @ejb.interface-method
    */
   public void simulateConnectionErrorWithTwoHandles()
   {
      log.info("Simulating connection error with two handles");
      try
      {
         InitialContext ctx = new InitialContext();
         ConnectionFactory cf = (ConnectionFactory) ctx.lookup("java:/JBossTestCFByTx");
         TestConnection c1 = (TestConnection) cf.getConnection();
         TestConnection c2 = (TestConnection) cf.getConnection();
         try
         {
            c2.simulateConnectionError();
         }
         finally
         {
            try
            {
               c1.close();
            }
            catch (Throwable ignored)
            {
            }
            try
            {
               c2.close();
            }
            catch (Throwable ignored)
            {
            }
         }
      }
      catch (Exception e)
      {
         if (e.getMessage().equals("Simulated exception") == false)
         {
            log.warn("Unexpected: ", e);
            throw new EJBException(e.getMessage());
         }
         else
         {
            sessionContext.setRollbackOnly();
         }
      }
   }

   /**
    * Similate an exception
    *
    * @ejb.interface-method
    */
   public void simulateError(String failure, int count)
   {
      log.info(failure + " teststart");
      try
      {
         long available = getAvailableConnections();
         InitialContext ctx = new InitialContext();
         TestConnectionFactory cf = (TestConnectionFactory) ctx.lookup("java:/JBossTestCF");
         for (int i = 0; i < count; ++i)
         {
            try
            {
               TestConnection c = (TestConnection) cf.getConnection(failure);
               c.close();
            }
            catch (ResourceException expected)
            {
            }
         }
         if (available != getAvailableConnections())
            throw new EJBException("Expected " + available + " got " + getAvailableConnections() + " connections");
      }
      catch (Exception e)
      {
         log.warn("Unexpected: ", e);
         throw new EJBException(e.getMessage());
      }
   }

   /**
    * Similate an exception
    *
    * @ejb.interface-method
    */
   public void simulateFactoryError(String failure, int count)
   {
      log.info(failure + " start");
      TestConnectionFactory cf = null;
      try
      {
         long available = getAvailableConnections();
         InitialContext ctx = new InitialContext();
         cf = (TestConnectionFactory) ctx.lookup("java:/JBossTestCF");
         cf.setFailure(failure);
         for (int i = 0; i < count; ++i)
         {
            try
            {
               TestConnection c = (TestConnection) cf.getConnection(failure);
               c.close();
            }
            catch (ResourceException expected)
            {
            }
         }
         if (available != getAvailableConnections())
            throw new EJBException("Expected " + available + " got " + getAvailableConnections() + " connections");
      }
      catch (Exception e)
      {
         log.warn("Unexpected: ", e);
         throw new EJBException(e.getMessage());
      }
      finally
      {
         sessionContext.setRollbackOnly();
         if (cf != null)
            cf.setFailure(null);
      }
   }

   public long getAvailableConnections() throws Exception
   {
      MBeanServer server = MBeanServerLocator.locateJBoss();
      return ((Long) server.getAttribute(new ObjectName("jboss.jca:service=ManagedConnectionPool,name=JBossTestCF"),
            "AvailableConnectionCount")).longValue();
   }

   public void ejbActivate()
   {
   }

   public void ejbPassivate()
   {
   }

   public void ejbRemove()
   {
   }

   public void setSessionContext(SessionContext ctx)
   {
      sessionContext = ctx;
   }

   public void unsetSessionContext()
   {
      sessionContext = null;
   }

}

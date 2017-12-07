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
import javax.ejb.TransactionRolledbackLocalException;
import javax.naming.InitialContext;
import javax.transaction.TransactionRolledbackException;

import org.jboss.logging.Logger;
import org.jboss.test.jca.interfaces.XAExceptionSession;
import org.jboss.test.jca.interfaces.XAExceptionSessionHome;
import org.jboss.test.jca.interfaces.XAExceptionSessionLocal;
import org.jboss.test.jca.interfaces.XAExceptionSessionLocalHome;

/**
 * XAExceptionTestSessionBean.java
 *
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @version <tt>$Revision: 81036 $</tt>
 *
 * @ejb:bean   name="XAExceptionTestSession"
 *             jndi-name="test/XAExceptionTestSessionHome"
 *             view-type="remote"
 *             type="Stateless"
 * @ejb.transaction type="Never"
 */
public class XAExceptionTestSessionBean implements SessionBean
{

   /** The serialVersionUID */
   private static final long serialVersionUID = 1L;

   private Logger log = Logger.getLogger(getClass());

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
         XAExceptionSessionHome xh = (XAExceptionSessionHome) new InitialContext()
               .lookup("test/XAExceptionSessionHome");
         XAExceptionSession x = xh.create();
         try
         {
            x.testXAExceptionToTransactionRolledbackException();
         }
         catch (TransactionRolledbackException tre)
         {
            log.info("Test worked");
            return;
         }
      }
      catch (Exception e)
      {
         log.info("unexpected exception", e);
         throw new EJBException("Unexpected exception: " + e);
      }
      throw new EJBException("No exception");
   }

   /**
    * Describe <code>testXAException</code> method here.
    * @ejb.interface-method
    */
   public void testXAExceptionToTransactionRolledbackLocalException()
   {
      try
      {
         XAExceptionSessionLocalHome xh = (XAExceptionSessionLocalHome) new InitialContext()
               .lookup("test/XAExceptionSessionLocalHome");
         XAExceptionSessionLocal x = xh.create();
         try
         {
            x.testXAExceptionToTransactionRolledbackException();
         }
         catch (TransactionRolledbackLocalException tre)
         {
            log.info("Test worked");
            return;
         }
      }
      catch (Exception e)
      {
         log.info("unexpected exception", e);
         throw new EJBException("Unexpected exception: " + e);
      }
      throw new EJBException("No exception");
   }

   /**
    * Describe <code>testXAException</code> method here.
    * @ejb.interface-method
    */
   public void testRMERRInOnePCToTransactionRolledbackException()
   {
      try
      {
         XAExceptionSessionHome xh = (XAExceptionSessionHome) new InitialContext()
               .lookup("test/XAExceptionSessionHome");
         XAExceptionSession x = xh.create();
         try
         {
            x.testRMERRInOnePCToTransactionRolledbackException();
         }
         catch (TransactionRolledbackException tre)
         {
            log.info("Test worked");
            return;
         }
      }
      catch (Exception e)
      {
         log.info("unexpected exception", e);
         throw new EJBException("Unexpected exception: " + e);
      }
      throw new EJBException("No exception");
   }

   /**
    * Describe <code>testXAException</code> method here.
    * @ejb.interface-method
    */
   public void testXAExceptionToTransactionRolledbacLocalkException()
   {
      try
      {
         XAExceptionSessionLocalHome xh = (XAExceptionSessionLocalHome) new InitialContext()
               .lookup("test/XAExceptionSessionLocalHome");
         XAExceptionSessionLocal x = xh.create();
         try
         {
            x.testRMERRInOnePCToTransactionRolledbackException();
         }
         catch (TransactionRolledbackLocalException tre)
         {
            log.info("Test worked");
            return;
         }
      }
      catch (Exception e)
      {
         log.info("unexpected exception", e);
         throw new EJBException("Unexpected exception: " + e);
      }
      throw new EJBException("No exception");
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
   }

   public void unsetSessionContext()
   {
   }

}

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
package org.jboss.test.classloader.scoping.transaction.ejb;

import java.sql.Connection;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.naming.InitialContext;
import javax.sql.DataSource;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;

import org.jboss.resource.adapter.jdbc.WrappedConnection;
import org.jboss.test.classloader.scoping.transaction.interfaces.TestSession;

public class TestSessionBean
   implements SessionBean
{
   SessionContext context;

   public void runTest()
   {
      try
      {
         Transaction tx = getTransaction();
         if (tx == null)
            throw new RuntimeException("No transaction");
         String id = tx.toString();

         TestSession next = (TestSession) context.getEJBObject();
         int hashCode = next.invokeNext(id);
         if (hashCode != getConnectionHashCode())
            throw new RuntimeException("Not using same connection - assumes track by connection");
      }
      catch (Exception e)
      {
         throw new EJBException(e);
      }
   }

   public int invokeNext(String id)
   {
      try
      {
         Transaction tx = getTransaction();
         if (tx == null)
            throw new RuntimeException("No transaction");
         if (tx.toString().equals(id) == false)
            throw new RuntimeException("Expected " + id + " got " + tx.toString());

         return getConnectionHashCode();
      }
      catch (Exception e)
      {
         throw new EJBException(e);
      }
   }

   public void ejbCreate()
      throws CreateException
   {
   }
   
   public void setSessionContext( SessionContext context )
   {
      this.context = context;
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

   private Transaction getTransaction()
      throws Exception
   {
      InitialContext ctx = new InitialContext();
      TransactionManager tm = (TransactionManager) ctx.lookup("java:/TransactionManager"); // FIXME
      return tm.getTransaction();
   }

   private int getConnectionHashCode()
      throws Exception
   {
      InitialContext ctx = new InitialContext();
      DataSource ds = (DataSource) ctx.lookup("java:/DefaultDS"); // FIXME
      Connection c = ds.getConnection();
      try
      {
         WrappedConnection wc = (WrappedConnection) c;
         return System.identityHashCode(wc.getUnderlyingConnection());
      }
      finally
      {
         c.close();
      }
   }
}

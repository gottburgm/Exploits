/*
* JBoss, Home of Professional Open Source
* Copyright 2007, JBoss Inc., and individual contributors as indicated
* by the @authors tag. See the copyright.txt in the distribution for a
* full listing of individual contributors.
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
package org.jboss.resource.adapter.jms;

import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

/**
 * JmsXAResource.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 94030 $
 */
public class JmsXAResource implements XAResource
{
   /** The managed connection */
   private JmsManagedConnection managedConnection;
   
   /** The resource */
   private XAResource xaResource;

   /**
    * Create a new JmsXAResource.
    * 
    * @param managedConnection the managed connection
    * @param xaResource the xa resource
    */
   public JmsXAResource(JmsManagedConnection managedConnection, XAResource xaResource)
   {
      this.managedConnection = managedConnection;
      this.xaResource = xaResource;
   }

   public void start(Xid xid, int flags) throws XAException
   {
      managedConnection.lock();
      try
      {
         xaResource.start(xid, flags);
      }
      finally
      {
         managedConnection.unlock();
      }
   }

   public void end(Xid xid, int flags) throws XAException
   {
      managedConnection.lock();
      try
      {
         xaResource.end(xid, flags);
      }
      finally
      {
         managedConnection.unlock();
      }
   }

   public int prepare(Xid xid) throws XAException
   {
      managedConnection.lock();
      try
      {
         return xaResource.prepare(xid);
      }
      finally
      {
         managedConnection.unlock();
      }
   }

   public void commit(Xid xid, boolean onePhase) throws XAException
   {
      managedConnection.lock();
      try
      {
         xaResource.commit(xid, onePhase);
      }
      finally
      {
         managedConnection.unlock();
      }
   }

   public void rollback(Xid xid) throws XAException
   {
      managedConnection.lock();
      try
      {
         xaResource.rollback(xid);
      }
      finally
      {
         managedConnection.unlock();
      }
   }

   public void forget(Xid xid) throws XAException
   {
      managedConnection.lock();
      try
      {
         xaResource.forget(xid);
      }
      finally
      {
         managedConnection.unlock();
      }
   }

   public XAResource getUnderlyingXAResource()
   {
      return xaResource;
   }
  
   public boolean isSameRM(XAResource xaRes) throws XAException
   {
      if (xaRes instanceof JmsXAResource)
         xaRes = ((JmsXAResource) xaRes).getUnderlyingXAResource();
  
      return xaResource.isSameRM(xaRes);
   }

   public Xid[] recover(int flag) throws XAException
   {
      return xaResource.recover(flag);
   }

   public int getTransactionTimeout() throws XAException
   {
      return xaResource.getTransactionTimeout();
   }

   public boolean setTransactionTimeout(int seconds) throws XAException
   {
      return xaResource.setTransactionTimeout(seconds);
   }
   
   
}

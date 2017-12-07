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
package org.jboss.test.deployers.rar.mcf1;

import java.io.PrintWriter;
import java.util.Iterator;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.resource.ResourceException;
import javax.resource.spi.ConnectionEvent;
import javax.resource.spi.ConnectionEventListener;
import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.LocalTransaction;
import javax.resource.spi.ManagedConnection;
import javax.resource.spi.ManagedConnectionMetaData;
import javax.security.auth.Subject;
import javax.transaction.xa.XAResource;

public class MCF1ManagedConnection implements ManagedConnection, LocalTransaction
{
   private AtomicBoolean destroyed = new AtomicBoolean(false); 
   private CopyOnWriteArraySet connections = new CopyOnWriteArraySet();
   private CopyOnWriteArraySet listeners = new CopyOnWriteArraySet();
   //private MCF1ManagedConnectionFactory mcf;
   
   public MCF1ManagedConnection(MCF1ManagedConnectionFactory mcf)
   {
      //this.mcf = mcf;
   }
   
   public void addConnectionEventListener(ConnectionEventListener listener)
   {
      checkDestroyed();
      listeners.add(listener);
   }

   public void associateConnection(Object connection) throws ResourceException
   {
      checkDestroyed();
      if (connection instanceof MCF1ConnectionImpl)
         throw new ResourceException("Wrong object");
      ((MCF1ConnectionImpl) connection).setManagedConnection(this);
      connections.add(connection);
   }

   public void cleanup() throws ResourceException
   {
      for (Iterator i = connections.iterator(); i.hasNext();)
      {
         MCF1ConnectionImpl connection = (MCF1ConnectionImpl) i.next();
         connection.setManagedConnection(null);
      }
      connections.clear();
   }

   public void destroy() throws ResourceException
   {
      destroyed.set(true);
      cleanup();
   }

   public Object getConnection(Subject subject, ConnectionRequestInfo cxRequestInfo) throws ResourceException
   {
      MCF1ConnectionImpl connection = new MCF1ConnectionImpl();
      connection.setManagedConnection(this);
      return connection;
   }

   public LocalTransaction getLocalTransaction() throws ResourceException
   {
      return this;
   }

   public PrintWriter getLogWriter() throws ResourceException
   {
      return null;
   }

   public ManagedConnectionMetaData getMetaData() throws ResourceException
   {
      return null;
   }

   public XAResource getXAResource() throws ResourceException
   {
      return null;
   }

   public void removeConnectionEventListener(ConnectionEventListener listener)
   {
      listeners.remove(listener);
   }

   public void setLogWriter(PrintWriter out) throws ResourceException
   {
   }

   public void begin() throws ResourceException
   {
   }

   public void commit() throws ResourceException
   {
   }

   public void rollback() throws ResourceException
   {
   }

   protected void checkDestroyed()
   {
      if (destroyed.get())
         throw new IllegalStateException("Destroyed");
   }
   
   void closeHandle(MCF1ConnectionImpl connection)
   {
      if (destroyed.get())
         return;
      
      connections.remove(connection);
      
      ConnectionEvent ce = new ConnectionEvent(this, ConnectionEvent.CONNECTION_CLOSED);
      ce.setConnectionHandle(connection);
      for (Iterator i = listeners.iterator(); i.hasNext();)
      {
         ConnectionEventListener listener = (ConnectionEventListener) i.next();
         listener.connectionClosed(ce);
      }
   }
}

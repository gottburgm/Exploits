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
package org.jboss.test.jca.fs;

import java.util.ArrayList;
import java.io.PrintWriter;
import javax.resource.spi.ManagedConnection;
import javax.resource.spi.ConnectionEventListener;
import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.LocalTransaction;
import javax.resource.spi.ManagedConnectionMetaData;
import javax.resource.spi.ConnectionEvent;
import javax.resource.ResourceException;
import javax.transaction.xa.XAResource;
import javax.security.auth.Subject;

import org.jboss.logging.Logger;

/**
 *
 * @author  Scott.Stark@jboss.org
 * @version $Revision: 81036 $
 */
public class FSManagedConnection implements ManagedConnection
{
   static Logger log = Logger.getLogger(FSManagedConnection.class);
   ArrayList listeners = new ArrayList();
   FSDirContext conn;

   /** Creates new FSManagedConnection */
   public FSManagedConnection(Subject subject,
      FSRequestInfo fsInfo)
   {
      log.debug("ctor, fsInfo="+fsInfo);
   }

   public void addConnectionEventListener(ConnectionEventListener connectionEventListener)
   {
      log.debug("addConnectionEventListener, listener="+connectionEventListener,
         new Exception("CalledBy:"));
      listeners.add(connectionEventListener);
   }
   public void removeConnectionEventListener(ConnectionEventListener connectionEventListener)
   {
      log.debug("removeConnectionEventListener, listener="+connectionEventListener,
         new Exception("CalledBy:"));
      listeners.remove(connectionEventListener);
   }

   public void associateConnection(Object obj) throws ResourceException
   {
      log.debug("associateConnection, obj="+obj, new Exception("CalledBy:"));
      conn = (FSDirContext) obj;
      conn.setManagedConnection(this);
   }

   public void cleanup() throws ResourceException
   {
      log.debug("cleanup");
   }
   
   public void destroy() throws ResourceException
   {
      log.debug("destroy");
   }
   
   public Object getConnection(Subject subject, ConnectionRequestInfo info)
      throws ResourceException
   {
      log.debug("getConnection, subject="+subject+", info="+info,
         new Exception("CalledBy:"));
      if( conn == null )
         conn = new FSDirContext(this);
      return conn;
   }

   public LocalTransaction getLocalTransaction() throws ResourceException
   {
      log.debug("getLocalTransaction");
      return null;
   }
   
   public ManagedConnectionMetaData getMetaData() throws ResourceException
   {
      log.debug("getMetaData");
      return new FSManagedConnectionMetaData();
   }
   
   public XAResource getXAResource() throws ResourceException
   {
      log.debug("getXAResource");
      return null;
   }

   public PrintWriter getLogWriter() throws ResourceException
   {
      return null;
   }
   public void setLogWriter(PrintWriter out) throws ResourceException
   {
   }

   protected void close()
   {
      ConnectionEvent ce = new ConnectionEvent(this, ConnectionEvent.CONNECTION_CLOSED);
      ce.setConnectionHandle(conn);
      fireConnectionEvent(ce);
   }

   protected void fireConnectionEvent(ConnectionEvent evt)
   {
      for(int i=listeners.size()-1; i >= 0; i--)
      {
         ConnectionEventListener listener = (ConnectionEventListener) listeners.get(i);
         if(evt.getId() == ConnectionEvent.CONNECTION_CLOSED)
            listener.connectionClosed(evt);
         else if(evt.getId() == ConnectionEvent.CONNECTION_ERROR_OCCURRED)
            listener.connectionErrorOccurred(evt);
      }
   }
}

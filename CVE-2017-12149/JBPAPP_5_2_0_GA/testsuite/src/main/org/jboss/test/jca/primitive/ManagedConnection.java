/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.test.jca.primitive;

import java.io.PrintWriter;

import javax.resource.ResourceException;
import javax.resource.spi.ConnectionEventListener;
import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.LocalTransaction;
import javax.resource.spi.ManagedConnectionMetaData;
import javax.security.auth.Subject;
import javax.transaction.xa.XAResource;

public class ManagedConnection implements javax.resource.spi.ManagedConnection
{

   public void addConnectionEventListener(ConnectionEventListener listener)
   {
   }

   public void associateConnection(Object connection) throws ResourceException
   {
   }

   public void cleanup() throws ResourceException
   {
   }

   public void destroy() throws ResourceException
   {
   }

   public Object getConnection(Subject subject, ConnectionRequestInfo cxRequestInfo) throws ResourceException
   {
      return null;
   }

   public LocalTransaction getLocalTransaction() throws ResourceException
   {
      return null;
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
   }

   public void setLogWriter(PrintWriter out) throws ResourceException
   {
   }
}

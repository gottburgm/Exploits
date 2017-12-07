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
import java.util.Set;

import javax.resource.ResourceException;
import javax.resource.spi.ConnectionManager;
import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.ManagedConnection;
import javax.resource.spi.ManagedConnectionFactory;
import javax.security.auth.Subject;

public class TestConnectionFactory implements ManagedConnectionFactory
{
   private static final long serialVersionUID = 1L;

   private int someIntProperty;

   private Integer someIntegerObjectProperty;

   private boolean someBooleanProperty;

   private Boolean someBooleanObjectProperty;

   public Object createConnectionFactory() throws ResourceException
   {
      return null;
   }

   public Object createConnectionFactory(ConnectionManager cxManager) throws ResourceException
   {
      return new ConnectionFactory();
   }

   public ManagedConnection createManagedConnection(Subject subject, ConnectionRequestInfo cxRequestInfo)
         throws ResourceException
   {
      return new org.jboss.test.jca.primitive.ManagedConnection();
   }

   public PrintWriter getLogWriter() throws ResourceException
   {
      return null;
   }

   @SuppressWarnings("unchecked")
   public ManagedConnection matchManagedConnections(Set connectionSet, Subject subject,
         ConnectionRequestInfo cxRequestInfo) throws ResourceException
   {
      return null;
   }

   public void setLogWriter(PrintWriter out) throws ResourceException
   {

   }

   public int getSomeIntProperty()
   {
      return someIntProperty;
   }

   public void setSomeIntProperty(int someIntProperty)
   {
      this.someIntProperty = someIntProperty;
   }

   public Integer getSomeIntegerObjectProperty()
   {
      return someIntegerObjectProperty;
   }

   public void setSomeIntegerObjectProperty(Integer someIntegerObjectProperty)
   {
      this.someIntegerObjectProperty = someIntegerObjectProperty;
   }

   public boolean isSomeBooleanProperty()
   {
      return someBooleanProperty;
   }

   public void setSomeBooleanProperty(boolean someBooleanProperty)
   {
      this.someBooleanProperty = someBooleanProperty;
   }

   public Boolean getSomeBooleanObjectProperty()
   {
      return someBooleanObjectProperty;
   }

   public void setSomeBooleanObjectProperty(Boolean someBooleanObjectProperty)
   {
      this.someBooleanObjectProperty = someBooleanObjectProperty;
   }
}

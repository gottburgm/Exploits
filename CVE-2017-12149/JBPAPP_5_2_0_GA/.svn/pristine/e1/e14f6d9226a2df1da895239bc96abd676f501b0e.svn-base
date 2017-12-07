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
package org.jboss.test.jca.adapter;

import javax.naming.NamingException;
import javax.naming.Reference;
import javax.resource.ResourceException;
import javax.resource.cci.Connection;
import javax.resource.cci.ConnectionFactory;
import javax.resource.cci.ConnectionSpec;
import javax.resource.cci.RecordFactory;
import javax.resource.cci.ResourceAdapterMetaData;
import javax.resource.spi.ConnectionManager;
import javax.resource.Referenceable;

import org.jboss.resource.connectionmanager.BaseConnectionManager2;

/**
 * TestConnectionFactory.java
 *
 *
 * Created: Tue Jan  1 01:02:16 2002
 *
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @version
 */

public class TestConnectionFactory implements ConnectionFactory, Referenceable
{

   /** The serialVersionUID */
   private static final long serialVersionUID = 1L;

   private final ConnectionManager cm;

   private final TestManagedConnectionFactory mcf;

   private Reference ref;
   
   public TestConnectionFactory(final ConnectionManager cm, final TestManagedConnectionFactory mcf)
   {
      this.cm = cm;
      this.mcf = mcf;
   }

   // implementation of javax.resource.Referenceable interface

   /**
    *
    * @param param1 <description>
    */
   public void setReference(Reference ref)
   {
      this.ref = ref;
   }

   // implementation of javax.naming.Referenceable interface

   /**
    *
    * @return <description>
    * @exception javax.naming.NamingException <description>
    */
   public Reference getReference() throws NamingException
   {
      return ref;
   }

   // implementation of javax.resource.cci.ConnectionFactory interface

   /**
    *
    * @return <description>
    * @exception javax.resource.ResourceException <description>
    */
   public Connection getConnection() throws ResourceException
   {
      return (Connection) cm.allocateConnection(mcf, null);
   }

   /**
    *
    * @param param1 <description>
    * @return <description>
    * @exception javax.resource.ResourceException <description>
    */
   public Connection getConnection(ConnectionSpec ignore) throws ResourceException
   {
      return (Connection) cm.allocateConnection(mcf, null);
   }

   public Connection getConnection(String failure) throws ResourceException
   {
      return (Connection) cm.allocateConnection(mcf, new TestConnectionRequestInfo(failure));
   }

   /**
    *
    * @return <description>
    * @exception javax.resource.ResourceException <description>
    */
   public RecordFactory getRecordFactory() throws ResourceException
   {
      // TODO: implement this javax.resource.cci.ConnectionFactory method
      return null;
   }

   /**
    *
    * @return <description>
    * @exception javax.resource.ResourceException <description>
    */
   public ResourceAdapterMetaData getMetaData() throws ResourceException
   {
      // TODO: implement this javax.resource.cci.ConnectionFactory method
      return null;
   }

   public void setFailure(String failure)
   {
      mcf.setFailure(failure);
   }

}

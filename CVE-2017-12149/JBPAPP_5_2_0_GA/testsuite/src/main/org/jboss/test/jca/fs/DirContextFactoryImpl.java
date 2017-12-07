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

import javax.resource.spi.ConnectionManager;
import javax.resource.spi.ManagedConnectionFactory;
import javax.resource.ResourceException;
import javax.naming.NamingException;
import javax.naming.Reference;
import javax.naming.directory.DirContext;

import org.jboss.logging.Logger;

/**
 * @author Scott.Stark@jboss.org
 * @version $Revision: 81036 $
 */
public class DirContextFactoryImpl implements DirContextFactory
{
   static Logger log = Logger.getLogger(DirContextFactoryImpl.class);
   private transient ConnectionManager manager;
   private transient ManagedConnectionFactory factory;
   private transient FSRequestInfo fsInfo;
   private Reference reference;

   DirContextFactoryImpl(ConnectionManager manager,
      ManagedConnectionFactory factory, FSRequestInfo fsInfo)
   {
      this.manager = manager;
      this.factory = factory;
      this.fsInfo = fsInfo;
      log.debug("ctor, fsInfo="+fsInfo);
   }

   public DirContext getConnection() throws NamingException
   {
      log.debug("getConnection", new Exception("CalledBy:"));
      DirContext dc = null;
      try
      {
         dc = (DirContext) manager.allocateConnection(factory, fsInfo);
      }
      catch(ResourceException e)
      {
         throw new NamingException("Unable to get Connection: "+e);
      }
      return dc;
   }
   public DirContext getConnection(String user, String password) throws NamingException
   {
      log.debug("getConnection, user="+user);
      DirContext dc = null;
      try
      {
         dc = (DirContext) manager.allocateConnection(factory, fsInfo);
      }
      catch(ResourceException e)
      {
         throw new NamingException("Unable to get Connection: "+e);
      }
      return dc;
   }

   public void setReference(Reference reference)
   {
      log.debug("setReference, reference="+reference, new Exception("CalledBy:"));
      this.reference = reference;
   }

   public Reference getReference() throws NamingException
   {
      log.debug("getReference");
      return reference;
   }
}

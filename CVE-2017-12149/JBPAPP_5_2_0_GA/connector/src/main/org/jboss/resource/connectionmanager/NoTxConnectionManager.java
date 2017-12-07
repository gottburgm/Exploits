/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.resource.connectionmanager;

import javax.resource.ResourceException;
import javax.resource.spi.ConnectionEvent;
import javax.resource.spi.ManagedConnection;

import org.jboss.logging.Logger;

/**
 * The NoTxConnectionManager is an simple extension class of the BaseConnectionManager2
 * for use with jca adapters with no transaction support.
 *  It includes functionality to obtain managed connections from
 * a ManagedConnectionPool mbean, find the Subject from a SubjectSecurityDomain,
 * and interact with the CachedConnectionManager for connections held over
 * transaction and method boundaries.  Important mbean references are to a
 * ManagedConnectionPool supplier (typically a JBossManagedConnectionPool), and a
 * RARDeployment representing the ManagedConnectionFactory.
 *
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @author <a href="mailto:adrian@jboss.org">Adrian Brock</a>
 * @version $Revision: 77961 $
 */
public class NoTxConnectionManager  extends BaseConnectionManager2
{
   public NoTxConnectionManager()
   {
   }

   /**
    * Creates a new NoTxConnectionManager instance.
    * for TESTING ONLY! not a managed operation.
    * 
    * @param ccm a <code>CachedConnectionManager</code> value
    * @param poolingStrategy a <code>ManagedConnectionPool</code> value
    */
   public NoTxConnectionManager(CachedConnectionManager ccm,
                                ManagedConnectionPool poolingStrategy)
   {
      super(ccm, poolingStrategy);
   }

   public ConnectionListener createConnectionListener(ManagedConnection mc, Object context)
   {
      ConnectionListener cli = new NoTxConnectionEventListener(mc, poolingStrategy, context, log);
      mc.addConnectionEventListener(cli);
      return cli;
   }

   protected void managedConnectionDisconnected(ConnectionListener cl) throws ResourceException
   {
      //if there are no more handles, we can return to pool.
      if (cl.isManagedConnectionFree())
         returnManagedConnection(cl, false);
   }

   private class NoTxConnectionEventListener extends BaseConnectionEventListener
   {
      private NoTxConnectionEventListener(final ManagedConnection mc, final ManagedConnectionPool mcp, final Object context, Logger log)
      {
         super(mc, mcp, context, log);
      }

      public void connectionClosed(ConnectionEvent ce)
      {
         try
         {
            getCcm().unregisterConnection(NoTxConnectionManager.this, ce.getConnectionHandle());
         }
         catch (Throwable t)
         {
            log.info("Throwable from unregisterConnection", t);
         }

         unregisterAssociation(this, ce.getConnectionHandle());
         if (isManagedConnectionFree())
         {
           returnManagedConnection(this, false);
         }
      }

      public void localTransactionStarted(ConnectionEvent ce)
      {
         //nothing to do.
      }

      public void localTransactionCommitted(ConnectionEvent ce)
      {
         //nothing to do.
      }

      public void localTransactionRolledback(ConnectionEvent ce)
      {
         //nothing to do.
      }
   }
}

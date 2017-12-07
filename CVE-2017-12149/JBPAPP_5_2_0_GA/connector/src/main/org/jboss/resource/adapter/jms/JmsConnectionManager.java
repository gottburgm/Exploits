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
package org.jboss.resource.adapter.jms;

import javax.resource.ResourceException;
import javax.resource.spi.ConnectionManager;
import javax.resource.spi.ManagedConnectionFactory;
import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.ManagedConnection;

import org.jboss.logging.Logger;

/**
 * The resource adapters own ConnectionManager, used in non-managed
 * environments.
 * 
 * <p>Will handle some of the houskeeping an appserver nomaly does.
 *
 * @author <a href="mailto:peter.antman@tim.se">Peter Antman</a>.
 * @author <a href="mailto:adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 71554 $
 */
public class JmsConnectionManager
   implements ConnectionManager
{
   private static final long serialVersionUID = -3638293323045716739L;

   private static final Logger log = Logger.getLogger(JmsConnectionManager.class);
   
   /**
    * Construct a <tt>JmsConnectionManager</tt>.
    */
   public JmsConnectionManager() {
      super();
   }

   /**
    * Allocate a new connection.
    *
    * @param mcf
    * @param cxRequestInfo
    * @return                   A new connection
    *
    * @throws ResourceException Failed to create connection.
    */
   public Object allocateConnection(ManagedConnectionFactory mcf,
                                    ConnectionRequestInfo cxRequestInfo) 
      throws ResourceException
   {
      boolean trace = log.isTraceEnabled();
      if (trace)
         log.trace("Allocating connection; mcf=" + mcf + ", cxRequestInfo=" + cxRequestInfo);
      
      ManagedConnection mc = mcf.createManagedConnection(null, cxRequestInfo);
      Object c = mc.getConnection(null, cxRequestInfo);

      if (trace)
         log.trace("Allocated connection: " + c + ", with managed connection: " + mc);
      
      return c;
   }
}

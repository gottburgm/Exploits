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
package org.jboss.invocation.unified.server;

import org.jboss.system.ServiceMBean;
import org.jboss.remoting.ServerInvocationHandler;
import org.jboss.remoting.transport.ConnectorMBean;

/**
 * @author <a href="mailto:tom.elrod@jboss.com">Tom Elrod</a>
 */
public interface UnifiedInvokerMBean extends ServiceMBean, ServerInvocationHandler
{
   public String getInvokerLocator();

   /**
    * If set to true, this will cause the UnifiedInvokerProxy (on the client side) to
    * wrap all RemoteExceptions thrown from the server in a new ServerException.  If false,
    * will unwrap the original exception thrown from withint the RemoteException and throw that.
    * The default is false.
    * @param isStrict
    */
   void setStrictRMIException(boolean isStrict);

   /**
    * A return of true means that the UnifiedInvokerProxy (on the client side) will wrap all
    * RemoteExceptions within a new ServerException.  A return of false, will unwrap the original
    * exception thrown from within the RemoteException and throw that.  The default, if not explicitly set,
    * is false.
    * @return
    */
   boolean getStrictRMIException();

   /**
    * This may be called if set depends in config with optional-attribute-name.
    * @param connector
    */
   void setConnector(ConnectorMBean connector);

   /**
    * Gets the subsystem that the invoker will be registered under within remoting connector
    * @return
    */
   String getSubSystem();

   /**
    * Sets the subsystem that the invoker will be registered under within remoting connector.
    * @param subsystem
    */
   void setSubSystem(String subsystem);
}
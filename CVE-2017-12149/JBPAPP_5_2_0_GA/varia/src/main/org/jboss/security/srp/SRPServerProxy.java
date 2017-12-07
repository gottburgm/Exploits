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
package org.jboss.security.srp;

import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.jboss.security.srp.SRPServerInterface;

/** A serializable proxy that is bound into JNDI with a reference to the
 RMI implementation of a SRPServerInterface. This allows a client to lookup
 the interface and not have the RMI stub for the server as it will be downloaded
 to them when the SRPServerProxy is unserialized.

 @author Scott.Stark@jboss.org
 @version $Revision: 81038 $
 */
public class SRPServerProxy implements InvocationHandler, Serializable
{
   /** The serial version UID @since 1.3 */
   private static final long serialVersionUID = 5255628656806648070L;

   private SRPServerInterface server;

   /** Create a SRPServerProxy given the SRPServerInterface that method
    invocations are to be delegated to.
    */
   SRPServerProxy(SRPServerInterface server)
   {
      this.server = server;
   }

   /** The InvocationHandler invoke method. All calls are simply delegated to
    the SRPServerInterface server object.
    */
   public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
   {
      Object ret = null;
      try
      {
         ret = method.invoke(server, args);
      }
      catch (InvocationTargetException e)
      {
         throw e.getTargetException();
      }
      catch (Throwable e)
      {
         e.printStackTrace();
         throw e;
      }
      return ret;
   }
}

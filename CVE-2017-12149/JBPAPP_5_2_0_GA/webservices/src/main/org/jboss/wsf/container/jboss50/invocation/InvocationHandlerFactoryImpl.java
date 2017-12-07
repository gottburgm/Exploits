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
package org.jboss.wsf.container.jboss50.invocation;

import org.jboss.wsf.spi.invocation.*;

/**
 * The default invocation model factory fro AS 5.0.
 *
 * @author Heiko.Braun@jboss.com
 *         Created: Jul 19, 2007
 */
public class InvocationHandlerFactoryImpl extends InvocationHandlerFactory
{
   public InvocationHandler newInvocationHandler(InvocationType type)
   {
      InvocationHandler handler = null;

      switch(type)
      {
         case JAXRPC_JSE:
            handler = new InvocationHandlerJAXRPC();
            break;
         case JAXRPC_EJB21:
            handler = new InvocationHandlerEJB21();
            break;
         case JAXRPC_MDB21:
            handler = new InvocationHandlerMDB21();
            break;
         case JAXWS_JSE:
            handler = new InvocationHandlerJAXWS();
            break;
         case JAXWS_EJB3:
            handler = new InvocationHandlerEJB3();
            break;
         case JAXWS_MDB3:
            handler = new InvocationHandlerMDB3();
            break;
      }

      if(null == handler)
         throw new IllegalArgumentException("Unable to resolve spi.invocation.InvocationHandler for type " +type);

      return handler;
   }
}

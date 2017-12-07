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
package org.jboss.proxy;

import java.lang.reflect.Method;

import org.jboss.invocation.InvocationContext;

/** An extension of ClientContainer that allows one to access the client
 * container invocation context and interceptors.
 * 
 * @author Scott.Stark@jboss.org
 * @version $Revision: 60890 $
 */
public class ClientContainerEx extends ClientContainer
   implements IClientContainer
{
   /** @since 4.2.0 */
   static final long serialVersionUID = -2563480209681946859L;
   
   public ClientContainerEx()
   {
      super();
   }

   public ClientContainerEx(InvocationContext context)
   {
      super(context);
   }

   /**
    * Overriden to handle the IClientContainer methods
    * @param proxy
    * @param m - the proxied method
    * @param args - the proxied method args
    * @return 
    * @throws Throwable
    */ 
   public Object invoke(final Object proxy, final Method m, Object[] args)
      throws Throwable
   {
      if( m.getDeclaringClass() == IClientContainer.class )
      {
         return m.invoke(this, args);
      }
      return super.invoke(proxy, m, args);
   }
}

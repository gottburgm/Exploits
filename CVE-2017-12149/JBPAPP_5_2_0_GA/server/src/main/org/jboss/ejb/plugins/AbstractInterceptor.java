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
package org.jboss.ejb.plugins;


import org.jboss.ejb.Container;
import org.jboss.ejb.Interceptor;
import org.jboss.invocation.Invocation;
import org.jboss.logging.Logger;

import java.lang.reflect.Method;

/**
 * An abstract base class for container interceptors.
 *
 * @author <a href="mailto:rickard.oberg@telkel.com">Rickard Oberg</a>
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @author Scott.Stark@jboss.org
 * @version $Revision: 81030 $
 */
public abstract class AbstractInterceptor
      implements Interceptor
{
   // Constants -----------------------------------------------------

   // Attributes ----------------------------------------------------

   /** The next interceptor in the chain. */
   protected Interceptor nextInterceptor;
   /** Logging instance */
   protected Logger log = Logger.getLogger(this.getClass());
   /** The container the interceptor is associated with */
   protected Container container;

   // Static --------------------------------------------------------

   // Constructors --------------------------------------------------

   // Public --------------------------------------------------------

   // Interceptor implementation ------------------------------------

   public void setContainer(Container container)
   {
      this.container = container;
   }

   public Container getContainer()
   {
      return container;
   }

   public void setNext(final Interceptor interceptor)
   {
      // assert interceptor != null
      nextInterceptor = interceptor;
   }

   public Interceptor getNext()
   {
      return nextInterceptor;
   }

   public void create() throws Exception
   {
      // empty
   }

   public void start() throws Exception
   {
      // empty
   }

   public void stop()
   {
      // empty
   }

   public void destroy()
   {
      // empty
   }

   public Object invokeHome(final Invocation mi) throws Exception
   {
      // assert mi != null;
      return getNext().invokeHome(mi);
   }

   public Object invoke(final Invocation mi) throws Exception
   {
      // assert mi != null;
      return getNext().invoke(mi);
   }

   /**
    See if the given exception e is compatible with an exception declared
    as thrown by the invocation method.

    @param invocation - the current invocation
    @param e - the exception thrown by the invocation
    @return true if e is a declared exception, false otherwise
    */
   public boolean isAppException(Invocation invocation, Throwable e)
   {
      Method m = invocation.getMethod();
      Class[] exceptions = m.getExceptionTypes();
      boolean isAppException = false;
      for(int n = 0; isAppException == false && n < exceptions.length; n ++)
      {
         Class exType = exceptions[n];
         isAppException = exType.isInstance(e);
      }
      return isAppException;
   }

   // Protected -----------------------------------------------------
}

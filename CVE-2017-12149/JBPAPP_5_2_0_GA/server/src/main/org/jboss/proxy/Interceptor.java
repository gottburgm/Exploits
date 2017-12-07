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
package org.jboss.proxy;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import org.jboss.invocation.Invocation;

/**
 * The base class for all interceptors.
 * 
 * @author <a href="mailto:marc.fleury@jboss.org">Marc Fleury</a>
 * @version $Revision: 81030 $
 */
public abstract class Interceptor
   implements Externalizable
{
   /** The serialVersionUID. @since 1.2 */
   private static final long serialVersionUID = 4358098404672505200L;

   /** The next interceptor in the chain. */
   protected Interceptor nextInterceptor;
 
   /**
    * Set the next interceptor in the chain.
    * 
    * <p>
    * String together the interceptors
    * We return the passed interceptor to allow for 
    * interceptor1.setNext(interceptor2).setNext(interceptor3)... constructs.
    */
   public Interceptor setNext(final Interceptor interceptor) {
      // assert interceptor != null
      nextInterceptor = interceptor;
      return interceptor;
   }
   
   public Interceptor getNext() {
      return nextInterceptor;
   }

   public abstract Object invoke(Invocation mi) throws Throwable;
   
   /**
    * Writes the next interceptor.
    */
   public void writeExternal(final ObjectOutput out)
      throws IOException
   {
      out.writeObject(nextInterceptor);
   }

   /**
    * Reads the next interceptor.
    */
   public void readExternal(final ObjectInput in)
      throws IOException, ClassNotFoundException
   {
      nextInterceptor = (Interceptor)in.readObject();
   }
}

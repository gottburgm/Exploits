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
package org.jboss.invocation;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
* An InvokerInterceptor that does not optimize remote invocations.<p>
*
* This interceptor implements spec compliant behaviour.<p>
*
* @todo The performance could be improved by simulating marshalling
*       for "local" remote, rather than going straight for the invoker
* 
* @author <a href="mailto:adrian.brock@happeningtimes.com">Adrian Brock</a>
* @version $Revision: 85945 $
*/
public class ByValueInvokerInterceptor
   extends InvokerInterceptor
   implements Externalizable
{
   /** Serial Version Identifier. @since 1.1.4.1 */
   private static final long serialVersionUID = -6402069656713307195L;

   public ByValueInvokerInterceptor()
   {
      // For externalization to work
   }
   
   // Public --------------------------------------------------------

   /**
    * Are you local?
    */
   public boolean isLocal(Invocation invocation)
   {
      InvocationType type = invocation.getType();
      if (type == InvocationType.LOCAL || type == InvocationType.LOCALHOME)
         return true;
      return false;
   }
   
   /**
    * Invoke using the invoker for remote invocations
    */
   public Object invoke(Invocation invocation)
      throws Exception
   {
      // local interface
      if (isLocal(invocation))
         // The payload as is is good
         return localInvoker.invoke(invocation);
      else
         // through the invoker
         return invocation.getInvocationContext().getInvoker().invoke(invocation);
   }
   
   /**
    *  Externalize this instance.
    */
   public void writeExternal(final ObjectOutput out)
      throws IOException
   { 
      // We have no state
   }
   
   /**
    *  Un-externalize this instance.
    */
   public void readExternal(final ObjectInput in)
      throws IOException, ClassNotFoundException
   {
      // We have no state
   }
   
   // Private -------------------------------------------------------
   
   // Inner classes -------------------------------------------------
}

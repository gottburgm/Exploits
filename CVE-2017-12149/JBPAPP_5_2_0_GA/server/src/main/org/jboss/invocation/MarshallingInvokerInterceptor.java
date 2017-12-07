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
package org.jboss.invocation;

/**
* An InvokerInterceptor that does not optimize in VM invocations
*
* @author Scott.Stark@jboss.org
* @version $Revision: 81179 $
*/
public class MarshallingInvokerInterceptor
   extends InvokerInterceptor
{
   /** Serial Version Identifier. @since 1.1.4.1 */
   private static final long serialVersionUID = -6473336704093435358L;

   public MarshallingInvokerInterceptor()
   {
      // For externalization to work
   }

   // Public --------------------------------------------------------

   /**
    * Use marshalled invocations when the target is colocated.
    */
   public Object invoke(Invocation invocation)
      throws Exception
   {

      /*
          if(isLocal(invocation))
             return invokeLocalMarshalled(invocation);
          else
             return invokeInvoker(invocation);

          invokeLocalMarshalled is an optimized method for call-by-values. we don't need to serialize the entire Invocation
          for having call-by-value.
      */

      if(isLocal(invocation))
         return invokeLocalMarshalled(invocation);
      else
         return invokeInvoker(invocation);
   }
}

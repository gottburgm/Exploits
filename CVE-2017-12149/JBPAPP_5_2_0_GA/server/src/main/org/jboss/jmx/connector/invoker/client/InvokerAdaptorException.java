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
package org.jboss.jmx.connector.invoker.client;

import java.io.Serializable;

/**
 * An exception for holding jmx exception so the invokers
 * don't unwrap them.
 * 
 * @author <a href="mailto:adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 81030 $
 */
public class InvokerAdaptorException extends Exception implements Serializable
{
   // Constants -----------------------------------------------------
   
   private static final long serialVersionUID = 24842201105890823L;
   
   // Attributes ----------------------------------------------------
   
   /** The wrapped exception */
   private Throwable wrapped;
   
   // Constructors --------------------------------------------------

   public InvokerAdaptorException()
   {
      // For serialization
   }
   
   public InvokerAdaptorException(Throwable wrapped)
   {
      this.wrapped = wrapped;
   }
   
   // Public --------------------------------------------------------

   public Throwable getWrapped() 
   {
      return wrapped;
   }

   // Private -------------------------------------------------------
   
   // Inner classes -------------------------------------------------
}

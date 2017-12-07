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
package org.jboss.proxy.ejb;

import java.util.Hashtable;

import org.jboss.naming.NamingContextFactory;

/** An interceptor that will retry failed invocations by restoring the 
 * InvocationContext invoker. This is triggered by a ServiceUnavailableException
 * which causes the interceptor to retry the
 * lookup of the transport invoker using the jndi name obtained from the
 * invocation context under the key InvocationKey.JNDI_NAME, with the additional
 * extension of "-RemoteInvoker" if the invocation type is InvocationType.REMOTE
 * and "-HomeInvoker" if the invocation type is InvocationType.HOME.
 * 
 * The JNDI environment used for the lookup can be set via the setRetryEnv.
 * Typically this is an HA-JNDI configuration with one or more bootstrap
 * urls. If not set, an attempt will be made to use
 * {@link NamingContextFactory#getInitialContext(Hashtable)} to find the 
 * JNDI environment.  This will only be useful if java.naming.factory.initial
 * was set to org.jboss.naming.NamingContextFactory.  If neither of the above
 * steps yield a set of naming environment properties, a default InitialContext
 * will be used. 
 * 
 * @author brian.stansberry@jboss.org
 * @version $Id: SingleRetryInterceptor.java 85945 2009-03-16 19:45:12Z dimitris@jboss.org $
 */
public class SingleRetryInterceptor extends RetryInterceptor
{
   /** Serial Version Identifier. @since 1.0 */
   private static final long serialVersionUID = 1;
   /** The current externalized data version */

   /**
    * No-argument constructor for externalization.
    */
   public SingleRetryInterceptor()
   {
      super(1, 100);
   }
}

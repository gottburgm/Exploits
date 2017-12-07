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
package org.jboss.embedded.jndi;

import java.util.Hashtable;
import javax.naming.Context;
import javax.naming.NamingException;

import org.jboss.embedded.Bootstrap;
import org.jboss.naming.JBossRemotingContextFactory;

/**
 * InitialContextFactory that will bootstrap embedded jboss.
 *
 * If Context.SECURITY_PRINCIPAL and Context.SECURITY_CREDENTIALS are set,
 * this InitialContextFactory implementation combines the
 * authentication phase with the InitialContext creation. During the
 * getInitialContext callback from the JNDI naming, layer security context
 * identity is populated with the username obtained from the
 * Context.SECURITY_PRINCIPAL env property and the credentials from the
 * Context.SECURITY_CREDENTIALS env property. There is no actual authentication
 * of this information. It is merely made available to the jboss transport
 * layer for incorporation into subsequent invocations. Authentication and
 * authorization will occur on the server.
 *
 * If Context.SECURITY_PROTOCOL is provided as well as the principal and credentials,
 * then a JAAS login will be performed instead using the security domain provided with the
 * SECURITY_PROTOCOL variable.
 *
 * @author <a href="bill@jboss.com">Bill Burke</a>
 * @version $Revision: 85945 $
 */
public class KernelInitializingContextFactory extends JBossRemotingContextFactory
{
   public static boolean initialized = false;
   public static final String BOOTSTRAP_RESOURCE_PATH = "jboss.embedded.resource.path";

   public static synchronized void bootstrapEmbeddedKernel(Hashtable env)
   {
      if (initialized == false)
      {
         initialized = true;
         String bootstrapResourcePath = (String) env.get(BOOTSTRAP_RESOURCE_PATH);
         try
         {
            if (bootstrapResourcePath != null)
            {
               Bootstrap.getInstance().bootstrap(bootstrapResourcePath);
            }
            else
            {
               Bootstrap.getInstance().bootstrap();
            }
         }
         catch (Exception e)
         {
            throw new RuntimeException("Unable to bootstrap JBoss kernel", e);
         }
      }
   }

   // InitialContextFactory implementation --------------------------
   public Context getInitialContext(Hashtable env) throws NamingException
   {
      KernelInitializingContextFactory.bootstrapEmbeddedKernel(env);
      return super.getInitialContext(env);
   }
}

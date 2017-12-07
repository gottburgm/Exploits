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
package org.jboss.test.naming.test;

import java.util.Hashtable;
import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.InitialContext;
import javax.naming.spi.InitialContextFactory;

import org.jnp.interfaces.Naming;
import org.jnp.interfaces.NamingContext;
import org.jboss.logging.Logger;

/** A naming provider InitialContextFactory implementation that obtains a
 Naming proxy from a JNDI binding using the default InitialContext. This
 is only useful for testing secondary naming services.

 @see InitialContextFactory

 @author Scott.Stark@jboss.org
 @version $Revision: 81036 $
 */
public class BootstrapNamingContextFactory
   implements InitialContextFactory
{
   static Logger log = Logger.getLogger(BootstrapNamingContextFactory.class);

   // InitialContextFactory implementation --------------------------
   public Context getInitialContext(Hashtable env)
      throws NamingException
   {
      Naming namingServer = null;
      try
      {
         Hashtable env2 = (Hashtable) env.clone();
         env2.put(Context.INITIAL_CONTEXT_FACTORY, "org.jnp.interfaces.NamingContextFactory");
         // Retrieve the Naming interface
         String location = (String) env.get("bootstrap-binding");
         namingServer = (Naming) new InitialContext(env2).lookup(location);
         log.debug("Found naming proxy:"+namingServer);
      }
      catch(Exception e)
      {
         log.debug("Lookup failed", e);
         NamingException ex = new NamingException("Failed to retrieve Naming interface");
         ex.setRootCause(e);
         throw ex;
      }

      // Copy the context env
      env = (Hashtable) env.clone();
      return new NamingContext(env, null, namingServer);
   }
}

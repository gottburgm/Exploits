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
package org.jboss.security.jndi;

import java.util.Hashtable;
import java.security.Principal;
import javax.naming.AuthenticationException;
import javax.naming.Context;
import javax.naming.NamingException;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;

import org.jnp.interfaces.NamingContextFactory;
import org.jboss.security.auth.callback.UsernamePasswordHandler;

/** A naming provider InitialContextFactory implementation that combines the authentication phase
 * with the InitialContext creation. During the getInitialContext callback from the JNDI naming
 * layer a JAAS LoginContext is created using the login configuration name passed in as
 * the Context.SECURITY_PROTOCOL env property. The CallbackHandler used is a
 * org.jboss.security.auth.callback.UsernamePasswordHandler that is populated
 * with the username obtained from the Context.SECURITY_PRINCIPAL env property
 * and the credentials from the Context.SECURITY_CREDENTIALS env property.
 *
 * @see javax.naming.spi.InitialContextFactory
 *
 * @author Scott.Stark@jboss.org
 * @version $Revision: 85945 $
 */
public class LoginInitialContextFactory extends NamingContextFactory
{
   // InitialContextFactory implementation --------------------------

   /** Create new initial context by invoking the NamingContextFactory version of this
    * method after performing a JAAS login.
    *
    */
   public Context getInitialContext(Hashtable env)
      throws NamingException
   {
      // Get the login configuration name to use, defaulting to "other"
      String protocol = "other";
      Object prop = env.get(Context.SECURITY_PROTOCOL);
      if( prop != null )
         protocol = prop.toString();

      // Get the login principal and credentials from the JNDI env
      Object credentials = env.get(Context.SECURITY_CREDENTIALS);
      Object principal = env.get(Context.SECURITY_PRINCIPAL);
      try
      {
         // Get the principal username
         String username;
         if( principal instanceof Principal )
         {
            Principal p = (Principal) principal;
            username = p.getName();
         }
         else
         {
            username = principal.toString();
         }
         UsernamePasswordHandler handler = new UsernamePasswordHandler(username,
            credentials);
         // Do the JAAS login
         LoginContext lc = new LoginContext(protocol, handler);
         lc.login();
      }
      catch(LoginException e)
      {
         AuthenticationException ex = new AuthenticationException("Failed to login using protocol="+protocol);
         ex.setRootCause(e);
         throw ex;
      }

      // Now return the context using the standard jnp naming context factory
      Context iniCtx = super.getInitialContext(env);
      return iniCtx;
   }

}

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
package org.jboss.naming;

import java.security.Principal;
import java.util.Hashtable;

import javax.naming.AuthenticationException;
import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NamingException;
import javax.naming.Reference;
import javax.naming.spi.InitialContextFactory;
import javax.naming.spi.ObjectFactory;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;

import org.jboss.aop.advice.Interceptor;
import org.jboss.aspects.remoting.InvokeRemoteInterceptor;
import org.jboss.aspects.remoting.IsLocalInterceptor;
import org.jboss.aspects.remoting.Remoting;
import org.jboss.aspects.security.SecurityClientInterceptor;
import org.jboss.security.SimplePrincipal;
import org.jboss.security.auth.callback.UsernamePasswordHandler;
import org.jnp.interfaces.Naming;
import org.jnp.interfaces.NamingContext;

/**
 * Creating a JNDI connection using JBoss Remoting.
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
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @author <a href="mailto:scott@jboss.org">Scott Stark</a>
 * @author adrian@jboss.org
 * @version $Revision: 85945 $
 */
public class JBossRemotingContextFactory implements InitialContextFactory, ObjectFactory
{
   static void associateSecurityInformation(Object principal, Object credentials)
   {
      Principal securityPrincipal;
      // See if the principal is a Principal or String
      if( principal instanceof Principal )
      {
         securityPrincipal = (Principal) principal;
      }
      else
      {
         // Simply convert this to a name using toString
         String username = principal.toString();
         securityPrincipal = new SimplePrincipal(username);
      }
      // Associate this security context
      SecurityAssociationActions.setPrincipalInfo(securityPrincipal, credentials);
   }

   static void login(Object principal, Object credentials, Object prop) throws AuthenticationException
   {
      String protocol = prop.toString();
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

   }

   // InitialContextFactory implementation
   public Context getInitialContext(Hashtable env) throws NamingException
   {
      Class[] interfaces = {Naming.class};
      Interceptor[] interceptors;
      Naming naming;

      // Get the login principal and credentials from the JNDI env
      Object credentials = env.get(Context.SECURITY_CREDENTIALS);
      Object principal = env.get(Context.SECURITY_PRINCIPAL);
      Object protocol = env.get(Context.SECURITY_PROTOCOL);

      if (principal != null && credentials != null)
      {
         if (protocol != null)
         {
            login(principal, credentials, protocol);
         }
         else
         {
            associateSecurityInformation(principal, credentials);
         }
         Interceptor[] tmp = {IsLocalInterceptor.singleton, SecurityClientInterceptor.singleton, InvokeRemoteInterceptor.singleton};
         interceptors = tmp;
      }
      else
      {
         Interceptor[] tmp = {IsLocalInterceptor.singleton, InvokeRemoteInterceptor.singleton};
         interceptors = tmp;
      }

      Naming local = NamingContext.getLocal();
      if (local != null) return new NamingContext(env, null, local);

      String providerUrl = (String) env.get(Context.PROVIDER_URL);
      if (providerUrl == null)
      {
         throw new RuntimeException("PROVIDER_URL not provided in jndi.properties.  Automatic discovery not implemented yet.");
      }
      try
      {
         naming = (Naming) Remoting.createPojiProxy("JNDI", interfaces, providerUrl, interceptors);
      }
      catch (Exception e)
      {
         throw new RuntimeException("Unable to create Naming proxy", e);
      }

      return new NamingContext(env, null, naming);
   }

   // ObjectFactory implementation ----------------------------------
   public Object getObjectInstance(Object obj,
                                   Name name,
                                   Context nameCtx,
                                   Hashtable environment)
           throws Exception
   {
      Context ctx = getInitialContext(environment);
      Reference ref = (Reference) obj;
      return ctx.lookup((String) ref.get("URL").getContent());
   }
}

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
package org.jboss.security.jndi;

import org.jboss.naming.NamingContextFactory;
import org.jboss.security.SecurityContext;
import org.jboss.security.SimplePrincipal;

import javax.naming.Context;
import javax.naming.NamingException;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.security.Principal;
import java.util.Hashtable;

/** A naming provider InitialContextFactory implementation that combines the
 * authentication phase with the InitialContext creation. During the
 * getInitialContext callback from the JNDI naming, layer security context
 * identity is populated with the username obtained from the
 * Context.SECURITY_PRINCIPAL env property and the credentials from the
 * Context.SECURITY_CREDENTIALS env property. There is no actual authentication
 * of this information. It is merely made available to the jboss transport
 * layer for incorporation into subsequent invocations. Authentication and
 * authorization will occur on the server.
 *
 * @see javax.naming.spi.InitialContextFactory
 *
 * @author Scott.Stark@jboss.org
 * @version $Revision: 105904 $
 */
public class JndiLoginInitialContextFactory extends NamingContextFactory
{
   // InitialContextFactory implementation --------------------------

   /** Take the env Context.SECURITY_PRINCIPAL and Context.SECURITY_CREDENTIALS
    * and propagate these to the SecurityAssociation principal and credential.
    * If Context.SECURITY_PRINCIPAL is a java.security.Principal then it is
    * used as is, otherwise its treated as a name using toString and a
    * SimplePrincipal is created. The Context.SECURITY_CREDENTIALS is passed
    * as is.
    * @param env
    * @throws NamingException
    */
   public Context getInitialContext(Hashtable env)
      throws NamingException
   {
      // Get the login principal and credentials from the JNDI env
      Object credentials = env.get(Context.SECURITY_CREDENTIALS);
      Object principal = env.get(Context.SECURITY_PRINCIPAL);
      Principal securityPrincipal = null;
      /** Flag indicating if the SecurityAssociation existing at login should
      be restored on logout.
      */
      String flag = (String) env.get("jnp.multi-threaded");
      if (Boolean.valueOf(flag).booleanValue() == true)
      {
         /* Turn on the server mode which uses thread local storage for
            the principal information.
         */
         SecurityAssociationActions.setServer();
      }
      boolean restoreLoginIdentity = false;
      flag = (String) env.get("jnp.restoreLoginIdentity");
      if( flag != null )
         restoreLoginIdentity = Boolean.parseBoolean(flag);
      SecurityContext initialSC = null;
      if (restoreLoginIdentity)
    	  initialSC = SecurityAssociationActions.getSecurityContext();
      
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
      SecurityContext sc = SecurityAssociationActions.createSecurityContext(securityPrincipal, credentials, null);
      SecurityAssociationActions.setSecurityContext(sc);
      // Now return the context using the standard jnp naming context factory
      Context iniCtx = super.getInitialContext(env);
      if( restoreLoginIdentity )
      {
         // Use a proxy to pop the stack when the context is closed
         ClassLoader loader = SecurityAssociationActions.getContextClassLoader();
         ContextProxy handler = new ContextProxy(iniCtx, initialSC);
         Class[] ifaces = {Context.class};
         iniCtx = (Context) Proxy.newProxyInstance(loader, ifaces, handler);
      }
      return iniCtx;
   }

   /**
    * 
    */
   public static class ContextProxy implements InvocationHandler
   {
      private Context delegate;
      private SecurityContext sc;
      ContextProxy(Context delegate, SecurityContext sc)
      {
         this.delegate = delegate;
         this.sc = sc;
      }
      public Object invoke(Object proxy, Method method, Object[] args)
         throws Throwable
      {
         boolean close = false;
         try
         {
            close = method.getName().equals("close");
            return method.invoke(delegate, args);
         }
         catch(InvocationTargetException e)
         {
            throw e.getTargetException();
         }
         finally
         {
            if( close )
            {
               // Pop the security context on close
               try
               {
                  SecurityAssociationActions.setSecurityContext(sc);
               }
               catch(Throwable ignore)
               {
               }
            }
         }
      }
      
   }
}

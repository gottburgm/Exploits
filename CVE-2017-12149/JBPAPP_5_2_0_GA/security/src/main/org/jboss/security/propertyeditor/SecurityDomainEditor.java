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
package org.jboss.security.propertyeditor;

import java.beans.PropertyEditorSupport;
import java.security.KeyStore;
import java.security.Principal;
import java.util.Map;
import java.util.Set;

import javax.naming.InitialContext;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManagerFactory;
import javax.security.auth.Subject;

import org.jboss.logging.Logger;
import org.jboss.security.AuthenticationManager;
import org.jboss.security.SecurityDomain;

/** A property editor for org.jboss.security.SecurityDomain types. This editor
 * transforms a jndi name string to a SecurityDomain by looking up the binding.
 * The only unusual aspect of this editor is that the jndi name is usually of
 * the form java:/jaas/xxx and the java:/jaas context is a dynamic ObjectFactory
 * that will create a binding for any xxx. If there is an attempt to lookup a
 * binding before it has been created by the underlying service that provides
 * the SecurityDomain, the lookup will return the default security service
 * which typically does not implement SecurityDomain. In this case, the editor
 * will create a proxy that delays the lookup of the SecurityDomain until the
 * first method invocation against the proxy.
 *
 * @author Scott.Stark@jboss.org
 * @version $Revision: 85945 $
 */
public class SecurityDomainEditor
   extends PropertyEditorSupport
{
   private static Logger log = Logger.getLogger(SecurityDomainEditor.class);
   private String domainName;

   /** Get the SecurityDomain from the text which is the jndi name of the
    * SecurityDomain binding. This may have to create a proxy if the current
    * value of the binding is not a SecurityDomain.
    * @param text - the name of the Principal
    */
   public void setAsText(final String text)
   {
      this.domainName = text;
      try
      {
         InitialContext ctx = new InitialContext();
         Object ref = ctx.lookup(text);
         SecurityDomain domain = null;
         if( ref instanceof SecurityDomain )
         {
            domain = (SecurityDomain) ref;
         }
         else
         {
            // Create a proxy to delay the lookup until needed
            domain = new SecurityDomainProxy(domainName);
         }
         setValue(domain);
      }
      catch(Exception e)
      {
         log.error("Failed to lookup SecurityDomain, "+domainName, e);
      }
   }

   /** Return the original security domain jndi name since we cannot get
    * this back from the SecurityDomain itself.
    * @return
    */ 
   public String getAsText()
   {
      return domainName;
   }

   /** A proxy that delays the lookup of the SecurityDomain until there
    * is a SecurityDomain method invocation. This gets around the problem
    * of a service not exposing its SecurityDomain binding until its started.
    */ 
   static class SecurityDomainProxy implements SecurityDomain
   {
      SecurityDomain delegate;
      private String jndiName;

      SecurityDomainProxy(String jndiName)
      {
         this.jndiName = jndiName;
      }

      private synchronized void initDelegate()
      {
         if( delegate == null )
         {
            try
            {
            InitialContext ctx = new InitialContext();
            delegate = (SecurityDomain) ctx.lookup(jndiName);
            }
            catch(Exception e)
            {
               log.error("Exception in initDelegate ",e);
               SecurityException se = new SecurityException("Failed to lookup SecurityDomain, "+jndiName);
               se.initCause(e);
               throw se;
            }
         }
      }

      public KeyStore getKeyStore() throws SecurityException
      {
         initDelegate();
         return delegate.getKeyStore();
      }

      public KeyManagerFactory getKeyManagerFactory() throws SecurityException
      {
         initDelegate();
         return delegate.getKeyManagerFactory();
      }

      public KeyStore getTrustStore() throws SecurityException
      {
         initDelegate();
         return delegate.getTrustStore();
      }

      public TrustManagerFactory getTrustManagerFactory() throws SecurityException
      {
         initDelegate();
         return delegate.getTrustManagerFactory();
      }

      public String getSecurityDomain()
      {
         initDelegate();
         return delegate.getSecurityDomain();
      }

      public boolean isValid(Principal principal, Object credential)
      {
         return this.isValid(principal, credential, null);
      }

      public boolean isValid(Principal principal, Object credential,
         Subject activeSubject)
      {
         initDelegate();
         return delegate.isValid(principal, credential, activeSubject);
      }
      
      public Subject getActiveSubject()
      {
         initDelegate();
         return delegate.getActiveSubject();
      }

      public Principal getPrincipal(Principal principal)
      {
         initDelegate();
         return delegate.getPrincipal(principal);
      }
      
      /**
       * @see AuthenticationManager#getTargetPrincipal(Principal,Map)
       */
      public Principal getTargetPrincipal(Principal anotherDomainPrincipal, Map contextMap)
      {
         throw new RuntimeException("Not implemented yet");
      }

      public boolean doesUserHaveRole(Principal principal, Set roles)
      {
         initDelegate();
         return delegate.doesUserHaveRole(principal, roles);
      }

      public Set getUserRoles(Principal principal)
      {
         initDelegate();
         return delegate.getUserRoles(principal);
      }
   }
}

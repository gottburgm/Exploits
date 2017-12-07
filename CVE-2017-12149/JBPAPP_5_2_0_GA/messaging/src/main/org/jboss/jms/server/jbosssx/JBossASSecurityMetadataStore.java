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
package org.jboss.jms.server.jbosssx;

import java.security.AccessController;
import java.security.Principal;
import java.security.PrivilegedAction;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.jms.JMSSecurityException;
import javax.naming.NamingException;
import javax.security.auth.Subject;

import org.jboss.jms.server.SecurityStore;
import org.jboss.jms.server.security.CheckType;
import org.jboss.jms.server.security.SecurityMetadata;
import org.jboss.logging.Logger;
import org.jboss.security.AuthenticationManager;
import org.jboss.security.AuthorizationManager;
import org.jboss.security.ISecurityManagement;
import org.jboss.security.SimplePrincipal;
import org.w3c.dom.Element;

/**
 * A security metadate store for JMS. Stores security information for destinations and delegates authentication and
 * authorization to a JaasSecurityManager.
 * 
 * @author Peter Antman
 * @author <a href="mailto:Scott.Stark@jboss.org">Scott Stark</a>
 * @author <a href="mailto:tim.fox@jboss.com">Tim Fox</a>
 * @author <a href="mailto:ovidiu@feodorov.com">Ovidiu Feodorov</a>
 * @author Anil.Saldhana@redhat.com
 * @version $Revision: 85945 $
 * 
 * $Id: JBossASSecurityMetadataStore.java 85945 2009-03-16 19:45:12Z dimitris@jboss.org $
 */
@SuppressWarnings("unchecked")
public class JBossASSecurityMetadataStore implements SecurityStore, JBossASSecurityMetadataStoreMBean
{
   // Constants -----------------------------------------------------

   private static final Logger log = Logger.getLogger(JBossASSecurityMetadataStore.class);

   public static final String DEFAULT_SUCKER_USER_PASSWORD = "CHANGE ME!!";

   // Attributes ----------------------------------------------------

   private final boolean trace = log.isTraceEnabled();

   private final Map queueSecurityConf;

   private final Map topicSecurityConf;

   private Element defaultSecurityConfig;

   private String securityDomain = "messaging";

   private String suckerPassword;

   private ISecurityManagement securityManagement = null;

   // Static --------------------------------------------------------

   // Constructors --------------------------------------------------

   public JBossASSecurityMetadataStore()
   {
      queueSecurityConf = new HashMap();
      topicSecurityConf = new HashMap();
   }

   // SecurityManager implementation --------------------------------

   public SecurityMetadata getSecurityMetadata(boolean isQueue, String destName)
   {
      SecurityMetadata m = (SecurityMetadata) (isQueue ? queueSecurityConf.get(destName) : topicSecurityConf
            .get(destName));

      if (m == null)
      {
         // No SecurityMetadata was configured for the destination, apply the default
         if (defaultSecurityConfig != null)
         {
            log.debug("No SecurityMetadadata was available for " + destName + ", using default security config");
            try
            {
               m = new SecurityMetadata(defaultSecurityConfig);
            }
            catch (Exception e)
            {
               log.warn("Unable to apply default security for destName, using guest " + destName, e);
               m = new SecurityMetadata();
            }
         }
         else
         {
            // default to guest
            log.warn("No SecurityMetadadata was available for " + destName + ", adding guest");
            m = new SecurityMetadata();
         }

         // don't cache it! this way the callers will be able to take advantage of default security
         // configuration updates
         // securityConf.put(destName, m);
      }
      return m;
   }

   public void setSecurityConfig(boolean isQueue, String destName, Element conf) throws Exception
   {
      if (trace)
      {
         log.trace("adding security configuration for " + (isQueue ? "queue " : "topic ") + destName);
      }

      if (conf == null)
      {
         clearSecurityConfig(isQueue, destName);
      }
      else
      {
         SecurityMetadata m = new SecurityMetadata(conf);

         if (isQueue)
         {
            queueSecurityConf.put(destName, m);
         }
         else
         {
            topicSecurityConf.put(destName, m);
         }
      }
   }

   public void clearSecurityConfig(boolean isQueue, String name) throws Exception
   {
      if (trace)
      {
         log.trace("clearing security configuration for " + (isQueue ? "queue " : "topic ") + name);
      }

      if (isQueue)
      {
         queueSecurityConf.remove(name);
      }
      else
      {
         topicSecurityConf.remove(name);
      }
   }

   public Subject authenticate(String user, String password) throws JMSSecurityException
   {
      if (trace)
      {
         log.trace("authenticating user " + user);
      }

      SimplePrincipal principal = new SimplePrincipal(user);
      char[] passwordChars = null;
      if (password != null)
      {
         passwordChars = password.toCharArray();
      }

      Subject subject = new Subject();

      boolean authenticated = false;

      if (SUCKER_USER.equals(user))
      {
         if (trace)
         {
            log.trace("Authenticating sucker user");
         }

         checkDefaultSuckerPassword(password);

         // The special user SUCKER_USER is used for creating internal connections that suck messages between nodes

         authenticated = suckerPassword.equals(password);
      }
      else
      {
         if (securityManagement == null)
            throw new SecurityException("SecurityManagement has not been set");
         AuthenticationManager authenticationManager = securityManagement.getAuthenticationManager(securityDomain);
         if (authenticationManager == null)
            throw new SecurityException("AuthenticationManager is null for domain=" + securityDomain);
         authenticated = authenticationManager.isValid(principal, passwordChars, subject);
      }

      if (authenticated)
      {
         // Warning! This "taints" thread local. Make sure you pop it off the stack as soon as
         // you're done with it.
         SecurityActions.pushSubjectContext(principal, passwordChars, subject, securityDomain);
         return subject;
      }
      else
      {
         throw new JMSSecurityException("User " + user + " is NOT authenticated");
      }
   }

   public boolean authorize(String user, final Set rolePrincipals, CheckType checkType)
   {
      if (trace)
      {
         log.trace("authorizing user " + user + " for role(s) " + rolePrincipals.toString());
      }

      if (SUCKER_USER.equals(user))
      {
         // The special user SUCKER_USER is used for creating internal connections that suck messages between nodes
         // It has automatic read/write access to all destinations
         return (checkType.equals(CheckType.READ) || checkType.equals(CheckType.WRITE));
      }

      final Principal principal = user == null ? null : new SimplePrincipal(user);
      if (securityManagement == null)
         throw new SecurityException("SecurityManagement has not been set");
      final AuthorizationManager authorizationManager = securityManagement.getAuthorizationManager(securityDomain);
      if (authorizationManager == null)
         throw new SecurityException("AuthorizationManager is null for domain=" + securityDomain);
      boolean hasRole = AccessController.doPrivileged(new PrivilegedAction<Boolean>()
      {
         public Boolean run()
         {
             return authorizationManager.doesUserHaveRole(principal, rolePrincipals);
         }});

      if (trace)
      {
         log.trace("user " + user + (hasRole ? " is " : " is NOT ") + "authorized");
      }

      return hasRole;
   }

   // Public --------------------------------------------------------

   public void setSuckerPassword(String password)
   {
      checkDefaultSuckerPassword(password);

      this.suckerPassword = password;
   }

   /**
    * @see JBossASSecurityMetadataStoreMBean#setSecurityManagement(ISecurityManagement)
    */
   public void setSecurityManagement(ISecurityManagement securityManagement)
   {
      this.securityManagement = securityManagement;
   }

   public void start() throws NamingException
   {
   }

   public void stop() throws Exception
   {
   }

   public String getSecurityDomain()
   {
      return this.securityDomain;
   }

   public void setSecurityDomain(String securityDomain)
   {
      this.securityDomain = securityDomain;
   }

   public Element getDefaultSecurityConfig()
   {
      return this.defaultSecurityConfig;
   }

   public void setDefaultSecurityConfig(Element conf) throws Exception
   {
      // Force a parse
      new SecurityMetadata(conf);
      defaultSecurityConfig = conf;
   }

   // Protected -----------------------------------------------------

   // Package Private -----------------------------------------------

   // Private -------------------------------------------------------

   private void checkDefaultSuckerPassword(String password)
   {
      // Sanity check
      if (DEFAULT_SUCKER_USER_PASSWORD.equals(password))
      {
         log
               .warn("WARNING! POTENTIAL SECURITY RISK. It has been detected that the MessageSucker component "
                     + "which sucks messages from one node to another has not had its password changed from the installation default. "
                     + "Please see the JBoss Messaging user guide for instructions on how to do this.");
      }
   }
}
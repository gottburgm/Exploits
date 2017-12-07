/*
 * JBoss, Home of Professional Open Source
 * Copyright 2007, Red Hat Middleware LLC, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
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

import java.security.Principal;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.jms.JMSSecurityException;
import javax.security.auth.Subject;

import org.jboss.security.AuthenticationManager;
import org.jboss.security.RealmMapping;
import org.jboss.security.SimplePrincipal;
import org.jboss.jms.server.SecurityStore;
import org.jboss.jms.server.security.CheckType;
import org.jboss.jms.server.security.SecurityMetadata;
import org.w3c.dom.Element;


/**
 * An implementation of the messaging SecurityStore.
 * 
 * @author Scott.Stark@jboss.org
 * @version $Revision: 85945 $
 */
public class MemorySecurityStore
   implements SecurityStore, MemorySecurityStoreMBean
{
   private Map<String, SecurityMetadata> queueSecurityConf
      = new ConcurrentHashMap<String, SecurityMetadata>();
   private Map<String, SecurityMetadata> topicSecurityConf
      = new ConcurrentHashMap<String, SecurityMetadata>();
   private SecurityMetadata defaultSecurityConfig;
   private String securityDomain;
   private String suckerPassword;
   private AuthenticationManager authenticationMgr;
   private RealmMapping authorizationMgr;

   public SecurityMetadata getDefaultSecurityConfig()
   {
      return defaultSecurityConfig;
   }

   public void setDefaultSecurityConfig(SecurityMetadata defaultSecurityConfig)
   {
      this.defaultSecurityConfig = defaultSecurityConfig;
   }

   public String getSecurityDomain()
   {
      return securityDomain;
   }

   public void setSecurityDomain(String securityDomain)
   {
      this.securityDomain = securityDomain;
   }

   public String getSuckerPassword()
   {
      return suckerPassword;
   }

   public void setSuckerPassword(String suckerPassword)
   {
      this.suckerPassword = suckerPassword;
   }

   public AuthenticationManager getAuthenticationMgr()
   {
      return authenticationMgr;
   }

   public void setAuthenticationMgr(AuthenticationManager authenticationMgr)
   {
      this.authenticationMgr = authenticationMgr;
   }

   public RealmMapping getAuthorizationMgr()
   {
      return authorizationMgr;
   }
   public void setAuthorizationMgr(RealmMapping authorizationMgr)
   {
      this.authorizationMgr = authorizationMgr;
   }

   public Subject authenticate(String user, String password)
      throws JMSSecurityException
   {
      Subject subject = new Subject();
      SimplePrincipal principal = new SimplePrincipal(user);
      if(authenticationMgr.isValid(principal, password, subject) == false)
         subject = null;
      return subject;
   }

   public boolean authorize(String user, Set rolePrincipals, CheckType checkType)
   {
      if (SecurityStore.SUCKER_USER.equals(user))
      {
         //The special user SUCKER_USER is used for creating internal connections that suck messages between nodes
         //It has automatic read/write access to all destinations
         return (checkType.equals(CheckType.READ) || checkType.equals(CheckType.WRITE));
      }

      Principal principal = user == null ? null : new SimplePrincipal(user);
      
      boolean hasRole = authorizationMgr.doesUserHaveRole(principal, rolePrincipals);

      return hasRole;
   }

   public void clearSecurityConfig(boolean isQueue, String destName)
         throws Exception
   {
      if(isQueue)
         queueSecurityConf.remove(destName);
      else
         topicSecurityConf.remove(destName);      
   }

   public SecurityMetadata getSecurityMetadata(boolean isQueue, String destName)
   {
      SecurityMetadata smd = defaultSecurityConfig;
      if(isQueue)
         smd = queueSecurityConf.get(destName);
      else
         smd = topicSecurityConf.get(destName);
      return smd;
   }

   public void setSecurityConfig(boolean isQueue, String destName, Element conf)
         throws Exception
   {
      SecurityMetadata smd = new SecurityMetadata(conf);
      if(isQueue)
         queueSecurityConf.put(destName, smd);
      else
         topicSecurityConf.put(destName, smd);      
   }

}

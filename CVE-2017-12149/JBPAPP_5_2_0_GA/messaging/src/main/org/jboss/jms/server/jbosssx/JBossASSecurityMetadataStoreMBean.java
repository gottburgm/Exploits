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

import java.security.Principal;
import java.util.Set;

import javax.jms.JMSSecurityException;
import javax.security.auth.Subject;

import org.jboss.jms.server.security.CheckType;
import org.jboss.jms.server.security.SecurityMetadata;
import org.jboss.security.ISecurityManagement;
import org.w3c.dom.Element;

/**
 * The SecurityMetadataStore mbean interface
 * 
 * @author Scott.Stark@jboss.org
 * @version $Revision: 85945 $
 */
public interface JBossASSecurityMetadataStoreMBean
{
   public String getSecurityDomain();
   public void setSecurityDomain(String securityDomain);
   public void setDefaultSecurityConfig(Element conf) throws Exception;
   public Element getDefaultSecurityConfig();

   public void setSuckerPassword(String password);
   public void start() throws Exception;
   public void stop() throws Exception;
   /**
    * @return the security meta-data for the given destination.
    */
   SecurityMetadata getSecurityMetadata(boolean isQueue, String destName);
   
   /**
    * Inject a SecurityManagement instance
    * (Locator for Security Managers for authentication
    * and authorization)
    * @param securityManagement
    */
   void setSecurityManagement(ISecurityManagement securityManagement);

   void setSecurityConfig(boolean isQueue, String destName, Element conf) throws Exception; 
   
   void clearSecurityConfig(boolean isQueue, String name) throws Exception;

   /**
    * Authenticate the specified user with the given password. Implementations are most likely to
    * delegates to a JBoss AuthenticationManager.
    *
    * Successful authentication will place a new SubjectContext on thread local, which will be used
    * in the authorization process. However, we need to make sure we clean up thread local
    * immediately after we used the information, otherwise some other people security my be screwed
    * up, on account of thread local security stack being corrupted.
    *
    * @throws JMSSecurityException if the user is not authenticated
    */
   Subject authenticate(String user, String password) throws JMSSecurityException;

   /**
    * Authorize that the subject has at least one of the specified roles. Implementations are most
    * likely to delegates to a JBoss AuthenticationManager.
    *
    * @param rolePrincipals - The set of roles allowed to read/write/create the destination.
    * @return true if the subject is authorized, or false if not.
    */
   boolean authorize(String user, Set<Principal> rolePrincipals, CheckType checkType);
}
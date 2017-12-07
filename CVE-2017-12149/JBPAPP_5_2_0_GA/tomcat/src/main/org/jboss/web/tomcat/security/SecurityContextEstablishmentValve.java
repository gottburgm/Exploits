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
package org.jboss.web.tomcat.security;

import java.io.IOException;
import java.security.PrivilegedActionException;

import javax.servlet.ServletException;

import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.valves.ValveBase;
import org.jboss.security.ISecurityManagement;
import org.jboss.security.SecurityContext;
import org.jboss.security.SecurityRolesAssociation;
import org.jboss.security.SecurityUtil;
import org.jboss.servlet.http.HttpEvent;


/**
 *  Establish the Security Context
 *  @author Anil.Saldhana@redhat.com
 *  @since  Sep 12, 2007 
 *  @version $Revision: 85945 $
 */
public class SecurityContextEstablishmentValve extends ValveBase
{  
   private String configuredSecurityDomainName;
   private ISecurityManagement securityManagement;
   
   private Class<?> securityContextClass;

   public SecurityContextEstablishmentValve(String configuredSecurityDomain, 
         String defaultSecurityDomain, Class<?> contextClass,
         ISecurityManagement securityManagement)
   {
      String securityDomain = defaultSecurityDomain;
      
      if(configuredSecurityDomain != null)
        securityDomain  = configuredSecurityDomain; 
      
      this.configuredSecurityDomainName = SecurityUtil.unprefixSecurityDomain(securityDomain);  
      this.securityContextClass = contextClass;
      this.securityManagement = securityManagement;
   }
   
   @Override
   public void invoke(Request request, Response response)
   throws IOException, ServletException
   {
      this.process(request, response, null);     
   } 
   
   private SecurityContext createSecurityContext()
   {
      SecurityContext securityContext = null;
      try
      {
         securityContext = 
            SecurityAssociationActions.createSecurityContext(this.configuredSecurityDomainName,
               this.securityContextClass); 
      }
      catch (PrivilegedActionException e)
      {
         throw new RuntimeException(e);
      } 
      
      securityContext.setSecurityManagement(securityManagement);
      return securityContext; 
   }

   @Override
   public void event(Request request, Response response, HttpEvent event)
      throws IOException, ServletException
   {
      process(request,response,event);
   }
   
   private void process(Request request, Response response, HttpEvent event)
   throws IOException, ServletException
   {
      SecurityContext cachedContext = null;
      
      boolean createdSecurityContext = false;
      //Set the security context if one is unavailable
      SecurityContext sc = SecurityAssociationActions.getSecurityContext();
      if(sc != null && 
            sc.getSecurityDomain().equals(configuredSecurityDomainName) == false)
      {
         cachedContext = sc;
         SecurityContext newSC = createSecurityContext();
         SecurityAssociationActions.setSecurityContext(newSC);
         createdSecurityContext = true;
      }
      
      if(sc == null)
      {
         sc = createSecurityContext();
         SecurityAssociationActions.setSecurityContext(sc);
         createdSecurityContext = true;
      }
      
      try
      { 
         // Perform the request
         if(event == null)
            getNext().invoke(request, response);
         else
            getNext().event(request, response, event);
      }
      finally
      { 
         SecurityRolesAssociation.setSecurityRoles(null); 
         if(createdSecurityContext)
         {
            SecurityAssociationActions.clearSecurityContext();
         }
         if(cachedContext != null)
            SecurityAssociationActions.setSecurityContext(cachedContext);
      }
   }
}
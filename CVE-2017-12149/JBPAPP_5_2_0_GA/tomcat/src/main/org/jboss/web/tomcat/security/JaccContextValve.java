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
import java.security.CodeSource;
import java.util.Map;
import java.util.Set;

import javax.security.jacc.PolicyContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.valves.ValveBase;
import org.jboss.logging.Logger;
import org.jboss.metadata.web.jboss.JBossWebMetaData;
import org.jboss.security.SecurityRolesAssociation;
import org.jboss.servlet.http.HttpEvent;
import org.jboss.web.tomcat.service.request.ActiveRequestResponseCacheValve;

/**
 * A Valve that sets the JACC context id and HttpServletRequest policy
 * context handler value. The context id needs to be established prior to
 * any authorization valves.
 *
 * @author Scott.Stark@jboss.org
 * @author Anil.Saldhana@redhat.com
 * @version $Revision: 112356 $
 */
public class JaccContextValve extends ValveBase
{
   private static Logger log = Logger.getLogger(JaccContextValve.class);
   public static ThreadLocal<CodeSource> activeCS = new ThreadLocal<CodeSource>();

   /** The web app metadata */
   private String contextID;
   /** The web app deployment code source */
   private CodeSource warCS;
   private JBossWebMetaData metaData;
   private boolean trace;

   public JaccContextValve(JBossWebMetaData wmd, CodeSource cs)
   {
      this.metaData = wmd;
      this.contextID = metaData.getJaccContextID();
      this.warCS = cs;
      this.trace = log.isTraceEnabled();
   } 

   public void invoke(Request request, Response response)
      throws IOException, ServletException
   { 
      SecurityAssociationValve.activeWebMetaData.set(metaData);
      activeCS.set(warCS);
      HttpServletRequest httpRequest = (HttpServletRequest) request.getRequest();

      //Set the customized rolename-principalset mapping in jboss-app.xml
      Map<String, Set<String>> principalToRoleSetMap = metaData.getPrincipalVersusRolesMap();
      SecurityRolesAssociation.setSecurityRoles(principalToRoleSetMap);
      if(trace)
         log.trace("MetaData:"+metaData+":principalToRoleSetMap"+principalToRoleSetMap);  
      
      try
      {
         // Set the JACC context id
         PolicyContext.setContextID(contextID);
         // Set the JACC HttpServletRequest PolicyContextHandler data
         HttpServletRequestPolicyContextHandler.setRequest(httpRequest);
         if(ActiveRequestResponseCacheValve.activeRequest.get() == null)
        	 ActiveRequestResponseCacheValve.activeRequest.set(request);
         if(ActiveRequestResponseCacheValve.activeResponse.get() == null)
        	 ActiveRequestResponseCacheValve.activeResponse.set(response);
         // Perform the request
         getNext().invoke(request, response);
      }
      finally
      {
         SecurityAssociationValve.activeWebMetaData.set(null);
         ActiveRequestResponseCacheValve.activeRequest.set(null);
         ActiveRequestResponseCacheValve.activeResponse.set(null);
         SecurityAssociationActions.clear();
         activeCS.set(null);
         SecurityRolesAssociation.setSecurityRoles(null);
         HttpServletRequestPolicyContextHandler.setRequest(null); 
      }
   }

   public void event(Request request, Response response, HttpEvent event)
      throws IOException, ServletException
   {
      SecurityAssociationValve.activeWebMetaData.set(metaData);
      activeCS.set(warCS);
      HttpServletRequest httpRequest = (HttpServletRequest) request.getRequest();

      //Set the customized rolename-principalset mapping in jboss-app.xml
      Map<String, Set<String>> principalToRoleSetMap = metaData.getPrincipalVersusRolesMap();
      SecurityRolesAssociation.setSecurityRoles(principalToRoleSetMap);
      if(trace)
         log.trace("MetaData:"+metaData+":principalToRoleSetMap"+principalToRoleSetMap);  
      
      try
      {
         // Set the JACC context id
         PolicyContext.setContextID(contextID);
         // Set the JACC HttpServletRequest PolicyContextHandler data
         HttpServletRequestPolicyContextHandler.setRequest(httpRequest);
         if(ActiveRequestResponseCacheValve.activeRequest.get() == null)
        	 ActiveRequestResponseCacheValve.activeRequest.set(request);
         if(ActiveRequestResponseCacheValve.activeResponse.get() == null)
        	 ActiveRequestResponseCacheValve.activeResponse.set(response);
         // Perform the request
         getNext().event(request, response, event);
      }
      finally
      {
         SecurityAssociationValve.activeWebMetaData.set(null);
         ActiveRequestResponseCacheValve.activeRequest.set(null);
         ActiveRequestResponseCacheValve.activeResponse.set(null);
         SecurityAssociationActions.clear();
         activeCS.set(null);
         SecurityRolesAssociation.setSecurityRoles(null);
         HttpServletRequestPolicyContextHandler.setRequest(null); 
      }
   }
   
}
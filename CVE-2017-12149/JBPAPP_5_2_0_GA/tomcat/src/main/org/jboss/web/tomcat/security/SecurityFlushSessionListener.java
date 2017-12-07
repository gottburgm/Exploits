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

import java.security.Principal;
import java.security.acl.Group;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.management.JMException;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.naming.InitialContext;
import javax.security.auth.Subject;
import javax.security.jacc.PolicyContext;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import org.jboss.logging.Logger;
import org.jboss.mx.util.MBeanServerLocator;
import org.jboss.security.SubjectSecurityManager;

/**
 *  JBAS-2151: Look into implementing flushOnSessionInvalidation 
 *  using a session listener
 *  @author < a href="mailto:Anil.Saldhana@jboss.org">Anil Saldhana</a>
 *  @since  Jan 13, 2006
 *  @version $Revision: 96167 $
 */
public class SecurityFlushSessionListener implements HttpSessionListener
{ 
   private static Logger log = Logger.getLogger(SecurityFlushSessionListener.class);
   
   private boolean trace = log.isTraceEnabled(); 
   
   private String securityDomain = null;
   
   private static final String JBOSS_PRINCIPAL = "org.jboss.web.tomcat.security.principal";
   
   /**
    * 
    * Create a new SecurityFlushSessionListener.
    *
    */
   public SecurityFlushSessionListener()
   { 
   }
   
   public void sessionCreated(HttpSessionEvent httpSessionEvent)
   { 
      if(trace)
         log.trace("Session Created with id=" + httpSessionEvent.getSession().getId());
   }
   
   public void sessionDestroyed(HttpSessionEvent httpSessionEvent)
   {
      if(trace)
         log.trace("Session Destroy with id=" + httpSessionEvent.getSession().getId()); 
      try
      {
         Subject subject = getSubjectAndSecurityDomain();
         if(trace)
            log.trace("securityDomain="+ securityDomain);
         if(securityDomain == null) 
            log.debug("Unable to obtain SecurityDomain"); 
         Principal principal = getPrincipal(subject);
         if(principal == null)
         {
                 if(trace)
                         log.trace("Searching for principal in the session");
                 principal = (Principal) httpSessionEvent.getSession().getAttribute(JBOSS_PRINCIPAL);
         }
         if(principal != null && securityDomain != null)
            flushAuthenticationCache(principal);
      }catch(Exception e)
      {
         log.error("Exception in sessionDestroyed:",e); 
      } 
   } 
   
   /**
    * Given the security domain and the Principal,
    * flush the authentication cache
    * 
    * @param principal
    * @throws JMException
    */
   private void flushAuthenticationCache(Principal principal) throws JMException
   {
      MBeanServer server = MBeanServerLocator.locateJBoss();
      ObjectName on = new ObjectName("jboss.security:service=JaasSecurityManager");
      Object[] obj = new Object[] {securityDomain, principal};
      String[] sig = new String[]{"java.lang.String", "java.security.Principal"};
      if(trace) 
         logAuthenticatedPrincipals(on, true);
      
      //Flush the Authentication Cache
      server.invoke(on,"flushAuthenticationCache", obj, sig);
      if(trace)
         logAuthenticatedPrincipals(on, false);  
   }
   
   /**
    * Get the Principal given the authenticated Subject
    * Currently the first principal that is not of type
    * java.security.acl.Group is considered
    * 
    * @param subject
    * @return the authenticated principal
    */ 
   private Principal getPrincipal(Subject subject)
   {
      Principal principal = null;
      if(subject != null)
      { 
         Set principals = subject.getPrincipals();
         if(principals != null || !principals.isEmpty())
         {
            Iterator iter = principals.iterator();
            while(iter.hasNext())
            {
               principal = (Principal)iter.next();
               if(principal instanceof Group == false)
                  break;
            }
         } 
      }
      if(trace)
         log.trace("Authenticated Principal=" + principal);
      return principal;
   }
   
   /**
    * Method that sets the securityDomain
    * and then returns the authenticated subject
    * First preference is given to the subject available
    * from the Jacc SubjectContextPolicyContextHandler.
    * As, a fallback, the Subject is obtained from the
    * Security Manager Service
    * 
    * @return
    */
   private Subject getSubjectAndSecurityDomain() throws Exception
   {
      SubjectSecurityManager mgr = null;
      try
      {
         mgr = getSecurityManagerService(); 
      }catch(Exception e)
      {
         log.debug("Obtaining SecurityManagerService failed::",e);
      }
      //First get the JACC Subject
      String SUBJECT_CONTEXT_KEY = "javax.security.auth.Subject.container"; 
      Subject subject = (Subject) PolicyContext.getContext(SUBJECT_CONTEXT_KEY);
      if(trace)
         log.trace("Jacc Subject = " + subject);
      if(mgr != null)
         securityDomain = mgr.getSecurityDomain(); 
        
      //Fallback
      if(subject == null && mgr != null)
      { 
         subject = mgr.getActiveSubject();
         if(trace)
            log.trace("Active Subject from security mgr service = " + subject);
      }
      return subject;
   }
   
   /**
    * Get the Security Manager Service
    * 
    * @return
    * @throws Exception
    */
   private SubjectSecurityManager getSecurityManagerService() throws Exception
   {
      //Get the SecurityManagerService from JNDI
      InitialContext ctx = new InitialContext();
      return (SubjectSecurityManager) ctx.lookup("java:comp/env/security/securityMgr");
   } 
   
   /**
    * Method used to log authenticated principals
    * remaining in cache (only when TRACE level logging is enabled)
    * 
    * @param on ObjectName of the JaasSecurityManagerService
    * @param isBeforeFlush Is the logging done before the auth cache flush
    */
   private void logAuthenticatedPrincipals(ObjectName on, boolean isBeforeFlush)
   throws JMException
   {
      if(isBeforeFlush)
        log.trace("Before flush of authentication cache::");
      else 
         log.trace("After flush of authentication cache::");
      MBeanServer server = MBeanServerLocator.locateJBoss();
      
      List list = (List)server.invoke(on,"getAuthenticationCachePrincipals", 
            new Object[]{securityDomain}, new String[] {"java.lang.String"} );
      
      int len = list != null ? list.size() : 0;
      log.trace("Number of authenticated principals remaining in cache=" + len);
      for(int i = 0 ; i < len; i++)
         log.trace("Authenticated principal in cache=" + list.get(i)); 
   }
}

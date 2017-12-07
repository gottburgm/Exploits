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
package org.jboss.test.web.security;

import java.io.IOException;  

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException; 
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.message.config.AuthConfigFactory;
import javax.security.auth.message.config.AuthConfigProvider;
import javax.security.auth.message.config.ServerAuthConfig;
import javax.security.auth.message.config.ServerAuthContext;
import javax.security.jacc.PolicyContext;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
 
import org.jboss.logging.Logger;
import org.jboss.security.AuthenticationManager;
import org.jboss.security.SecurityConstants;
import org.jboss.security.SimplePrincipal; 
import org.jboss.security.auth.callback.SecurityAssociationHandler;
import org.jboss.security.auth.login.XMLLoginConfigImpl; 
import org.jboss.security.auth.message.config.JBossAuthConfigProvider;


//$Id: JASPISecurityFilter.java 81036 2008-11-14 13:36:39Z dimitris@jboss.org $

/**
 *  Servlet Filter that is used to test the JASPI Security Framework
 *  You can customize the behavior based on the init params
 *  @author <a href="mailto:Anil.Saldhana@jboss.org">Anil Saldhana</a>
 *  @since  Jan 6, 2006 
 *  @version $Revision: 81036 $
 */
public class JASPISecurityFilter implements Filter
{ 
   private static Logger log = Logger.getLogger(JASPISecurityFilter.class);
   
   private FilterConfig filterConfig = null;
   
   private boolean testJASPIServerAuthContext = false;
   
   private String securityDomain = null;
   
   private String configFile = null;
   
   public void init(FilterConfig filterConfig) 
   throws ServletException 
   {
      this.filterConfig = filterConfig;
      String testJASPIServerAuthContextStr = filterConfig.getInitParameter("testJASPIServerAuthContext");
      if(testJASPIServerAuthContextStr != null)
      {
         testJASPIServerAuthContext = Boolean.valueOf(testJASPIServerAuthContextStr).booleanValue();
      }
      securityDomain = filterConfig.getInitParameter("securityDomain");
      if(securityDomain == null)
         securityDomain = "java:/jbsx/other";
      
      configFile = filterConfig.getInitParameter("configFile");
      if(configFile == null)
         throw new ServletException("Param configFile is missing for the filter:" +JASPISecurityFilter.class );
   }
   
   public void destroy() 
   {
      this.filterConfig = null;
   }

   public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) 
   throws IOException, ServletException
   { 
      if(this.testJASPIServerAuthContext)
            testServerAuthContext(request); 
   }
   
   //PRIVATE METHODS
   private void testServerAuthContext(ServletRequest request) throws ServletException 
   {
      try
      {
         //Establish the configuration
         generateConfiguration();
         String contextId = PolicyContext.getContextID(); 
         //Establish the contextid-securitydomain mapping
         //with the JASPISecurityManager Service
         MBeanServerConnection server = getMBeanServerConnection();
         ObjectName oname = new ObjectName("jboss.security:service=JASPISecurityManager");
         String regSecDom = (String)server.invoke(oname,"getSecurityDomain", 
               new Object[]{contextId},
               new String[] {"java.lang.String"} ); 
         if(regSecDom == null)
         {
            server.invoke(oname,"registerSecurityDomain", 
                  new Object[]{securityDomain,contextId},
                  new String[] {"java.lang.String", "java.lang.String"} ); 
         } 
         AuthConfigFactory factory = AuthConfigFactory.getFactory(); 
         AuthConfigProvider acp = factory.getConfigProvider(SecurityConstants.SERVLET_LAYER,
                                            contextId,null);
         if(acp == null)
         {
            acp = new JBossAuthConfigProvider(null);
         }  
         CallbackHandler cbh = new SecurityAssociationHandler();
         ServerAuthConfig sc = acp.getServerAuthConfig(SecurityConstants.SERVLET_LAYER,
                                            contextId,cbh); 
         if(sc == null)
            throw new ServletException("ServerAuthConfig is null"); 
         ServerAuthContext sa = sc.getAuthContext(null,null, null); 
         if(sa == null)
            throw new ServletException("ServerAuthContext obtained is null");
         String username = request.getParameter("user");
         String pass = request.getParameter("pass");
         AuthenticationManager am = (AuthenticationManager)sa;
         boolean isValid = am.isValid(new SimplePrincipal(username),pass);
         if(isValid == false)
            throw new ServletException("Validation failed for username=" + username);
         else
            log.error("Validation passed for username="+username+". This is good!");
      }catch(Exception e)
      {
         throw new ServletException(e);
      } 
   } 
   
   private void generateConfiguration() throws IOException
   {
      // Install the custom JAAS configuration
      XMLLoginConfigImpl config = XMLLoginConfigImpl.getInstance();
      config.setConfigResource(configFile);
      config.loadConfig(); 
   }
   
   private MBeanServerConnection getMBeanServerConnection() throws NamingException
   { 
      Context ctx = new InitialContext();
      return (MBeanServerConnection)ctx.lookup("jmx/invoker/RMIAdaptor");
   }
} 

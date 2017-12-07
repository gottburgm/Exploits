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
package org.jboss.test.jmx.test;

import java.net.URL;

import javax.management.MBeanInfo;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.naming.InitialContext;
import javax.security.auth.login.LoginContext;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.jboss.test.JBossTestCase;
import org.jboss.test.JBossTestSetup;
import org.jboss.test.util.AppCallbackHandler;

//$Id: RMIAdaptorAuthorizationUnitTestCase.java 85945 2009-03-16 19:45:12Z dimitris@jboss.org $

/**
 *  Authorization of the RMI Adaptor
 *  Especially tests the usage of the authorization delegate
 *  called as org.jboss.jmx.connector.invoker.ExternalizableRolesAuthorization
 *  @author <a href="mailto:Anil.Saldhana@jboss.org">Anil Saldhana</a>
 *  @since  May 10, 2006
 *  @version $Revision: 85945 $
 */
public class RMIAdaptorAuthorizationUnitTestCase extends JBossTestCase
{ 
   public RMIAdaptorAuthorizationUnitTestCase(String name)
   {
      super(name); 
   }  
   
   /**
    * Test that a valid jmx-console domain user can invoke operations
    * through the jmx/invoker/AuthenticatedRMIAdaptor
    * @throws Exception
    */ 
   public void testConfigurableRolesAuthorizedAccess() throws Exception
   {
      LoginContext lc = login("admin", "admin".toCharArray());
      InitialContext ctx = getInitialContext();
      MBeanServerConnection conn = (MBeanServerConnection) ctx.lookup("jmx/invoker/ConfigurableAuthorizedRMIAdaptor");
      ObjectName server = new ObjectName("jboss.system:type=Server");
      String version = (String) conn.getAttribute(server, "Version");
      log.info("Obtained server version: "+version);
      MBeanInfo info = conn.getMBeanInfo(server); 
      assertNotNull("MBeanInfo != null", info);
      Integer mbeanCount = conn.getMBeanCount();
      assertNotNull("mbeanCount != null", mbeanCount);
      lc.logout();
   }
   
   /**
    * Test invalid access
    * @throws Exception
    */ 
   public void testUnAuthorizedAccess() throws Exception
   {
      InitialContext ctx = getInitialContext();
      MBeanServerConnection conn = (MBeanServerConnection) ctx.lookup("jmx/invoker/ConfigurableAuthorizedRMIAdaptor");
      ObjectName server = new ObjectName("jboss.system:type=Server");
      try
      {
         String version = (String) conn.getAttribute(server, "Version");
         log.info("Obtained server version: "+version);
         fail("Was able to get server Version attribute");
      }
      catch(Exception e)
      {
         log.info("Access failed as expected", e);
      }
   }
   
   public static Test suite()
   throws Exception
   {
      TestSuite suite = new TestSuite();
      suite.addTest(new TestSuite(RMIAdaptorAuthorizationUnitTestCase.class));
      
      JBossTestSetup wrapper = new JBossTestSetup(suite)
      {
         protected void setUp() throws Exception
         {
            super.setUp();
            deploymentException = null;
            try
            {
               this.delegate.init();
               redeploy("jmxinvoker-authorization-test.jar");
               // deploy the comma seperated list of jars 
               redeploy(getResourceURL("jmx/jmxadaptor/authorization-jmx-invoker-service.xml"));
               redeploy(getResourceURL("jmx/jmxadaptor/jaas-service.xml")); 
            }
            catch (Exception ex)
            {
               // Throw this in testServerFound() instead.
               deploymentException = ex;
            }
         }
         
         protected void tearDown() throws Exception
         {            
            undeploy(getResourceURL("jmx/jmxadaptor/authorization-jmx-invoker-service.xml"));
            undeploy("jmxinvoker-authorization-test.jar"); 
            undeploy(getResourceURL("jmx/jmxadaptor/jaas-service.xml")); 
         }
      };
      return wrapper; 
   } 

   private LoginContext login(String username, char[] password) throws Exception
   { 
      String confName = System.getProperty("conf.name", "other");
      String conf = System.getProperty("java.security.auth.login.config");
      if( conf == null )
      {
         // Set the config url to the security/auth.conf resource
         ClassLoader loader = Thread.currentThread().getContextClassLoader();
         URL authURL = loader.getResource("security/auth.conf");
         System.setProperty("java.security.auth.login.config", authURL.toString());
      }
      AppCallbackHandler handler = new AppCallbackHandler(username, password);
      log.debug("Creating LoginContext("+confName+")");
      LoginContext lc = new LoginContext(confName, handler);
      lc.login();
      log.debug("Created LoginContext, subject="+lc.getSubject());
      return lc;
   }
}

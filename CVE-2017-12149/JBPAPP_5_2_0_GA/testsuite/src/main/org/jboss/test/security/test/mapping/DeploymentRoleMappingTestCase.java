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
package org.jboss.test.security.test.mapping;
 
import java.net.HttpURLConnection;
import java.net.URL; 
import java.rmi.RemoteException;

import javax.rmi.PortableRemoteObject;
import javax.security.auth.login.Configuration;
import javax.security.auth.login.LoginContext;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;
 
import org.jboss.security.auth.login.XMLLoginConfigImpl;
import org.jboss.test.JBossTestCase;
import org.jboss.test.JBossTestSetup;
import org.jboss.test.security.interfaces.StatelessSession;
import org.jboss.test.security.interfaces.StatelessSessionHome; 
import org.jboss.test.util.AppCallbackHandler;
import org.jboss.test.util.web.HttpUtils;

/**
 *  Tests deployment level role mapping
 *  Define security roles in jboss-app.xml that get merged with
 *  roles for the web/ejb layers
 *  @author <a href="mailto:Anil.Saldhana@jboss.org">Anil Saldhana</a>
 *  @since  Nov 14, 2006 
 *  @version $Revision: 85945 $
 */
public class DeploymentRoleMappingTestCase extends JBossTestCase
{  
   static String username = "jduke";
   static char[] password = "theduke".toCharArray(); 
   
   LoginContext lc;
   boolean loggedIn;
   
   public DeploymentRoleMappingTestCase(String name)
   {
      super(name); 
   } 
   
   public void testEJBApplication() throws Exception
   {
      Object obj = getInitialContext().lookup("DeploymentLevelRoleMappingBean");
      obj = PortableRemoteObject.narrow(obj, StatelessSessionHome.class);
      StatelessSessionHome home = (StatelessSessionHome) obj;
      log.debug("Found Unsecure StatelessSessionHome");
      StatelessSession bean = null;
      try
      { 
         bean = home.create(); 
         log.debug("Created spec.UnsecureStatelessSession2");
         bean.echo("Hello from nobody?");  
         fail("Should not be allowed");
      }
      catch(RemoteException re)
      { 
      }
      finally
      {
         if(bean != null)
           bean.remove();  
      }
      
      login();
      obj = getInitialContext().lookup("DeploymentLevelRoleMappingBean");
      obj = PortableRemoteObject.narrow(obj, StatelessSessionHome.class);
      home = (StatelessSessionHome) obj;
      log.debug("Found spec.StatelessSession2");
      bean = home.create();
      log.debug("Created spec.StatelessSession2");
      // Test that the Entity bean sees username as its principal
      String echo = bean.echo("jduke");
      log.debug("bean.echo(username) = "+echo);
      assertTrue("username == echo", echo.equals("jduke"));
      bean.remove();
      logout();
   }
   
   public void testWebApplication() throws Exception
   {   
      String baseURL = HttpUtils.getBaseURL("jduke", "theduke"); 
      URL url = new URL(baseURL + "deployment-rolemapping/RequestInfoServlet");
      HttpUtils.accessURL(url, "JBoss Realm", HttpURLConnection.HTTP_OK);
   } 
    
  private void login() throws Exception
  {
     login("jduke", "theduke".toCharArray());
  }
  private void login(String username, char[] password) throws Exception
  {
     if( loggedIn )
        return;
     
     lc = null;
     String confName = System.getProperty("conf.name", "other");
     AppCallbackHandler handler = new AppCallbackHandler(username, password);
     log.debug("Creating LoginContext("+confName+")");
     lc = new LoginContext(confName, handler);
     lc.login();
     log.debug("Created LoginContext, subject="+lc.getSubject());
     loggedIn = true;
  }
  private void logout() throws Exception
  {
     if( loggedIn )
     {
        loggedIn = false;
        lc.logout();
     }
  }
  
  /**
   * Setup the test suite.
   */
  public static Test suite() throws Exception
  {
     TestSuite suite = new TestSuite();
     suite.addTest(new TestSuite(DeploymentRoleMappingTestCase.class));

     // Create an initializer for the test suite
     TestSetup wrapper = new JBossTestSetup(suite)
     {
        protected void setUp() throws Exception
        {
           super.setUp();
           Configuration.setConfiguration(XMLLoginConfigImpl.getInstance());
           deploy(getResourceURL("security-spi/deploymentlevel/deploymentlevel-test-service.xml")); 
           redeploy("deployment-rolemapping.ear");
           flushAuthCache();
        }
        protected void tearDown() throws Exception
        {
           undeploy(getResourceURL("security-spi/deploymentlevel/deploymentlevel-test-service.xml"));  
           undeploy("deployment-rolemapping.ear");
           super.tearDown();
        
        }
     };
     return wrapper;
  } 
}

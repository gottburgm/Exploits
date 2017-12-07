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
package org.jboss.test.security.container.auth;

import java.util.HashMap;

import javax.security.auth.message.config.AuthConfigFactory;
import javax.security.auth.message.config.AuthConfigProvider; 
import javax.security.auth.message.config.RegistrationListener;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.jboss.security.SecurityConstants;
import org.jboss.security.auth.message.config.JBossAuthConfigFactory;
import org.jboss.test.JBossTestCase;

/**
 *  JSR-196 Tests
 *  Test the static factory class AuthConfigFactory
 *  @author <mailto:Anil.Saldhana@jboss.org>Anil Saldhana
 *  @since  Dec 6, 2005
 */
public class AuthConfigFactoryTestCase extends JBossTestCase
{ 
   private String baseURLNoAuth = "http://" + getServerHost() + ":" + Integer.getInteger("web.port", 8080) + "/"; 
   private HttpClient httpConn = new HttpClient();
   String allLayerProvider = "org.jboss.test.security.container.auth.AllLayerAuthConfigProvider";
   String allACProvider = "org.jboss.test.security.container.auth.AllAppContextAuthConfigProvider";
   
   public AuthConfigFactoryTestCase(String name)
   {
      super(name); 
   }

   protected AuthConfigFactory factory = null;
   
   protected void setUp() throws Exception
   {
      super.setUp();
      factory = AuthConfigFactory.getFactory();
      assertNotNull("AuthContextFactory is null?", factory);
   } 
   
   protected void tearDown() throws Exception
   {
      factory = null;
   }
   
   public void testGetFactory()
   { 
      assertTrue("AuthConfigFactory instance of JBossAuthConfigFactory",
            factory instanceof JBossAuthConfigFactory);
   } 
   
   public void testSetFactory()
   {
      AuthConfigFactory.setFactory(new TestAuthConfigFactory());
      factory = AuthConfigFactory.getFactory();
      assertNotNull("AuthConfigFactory is null?", factory);
      assertTrue("AuthConfigFactory instance of TestAuthConfigFactory",
            factory instanceof TestAuthConfigFactory);
      //Lets remove the test factory
      AuthConfigFactory.setFactory(null);
      assertTrue("AuthConfigFactory instance of JBossAuthConfigFactory",
            AuthConfigFactory.getFactory() instanceof JBossAuthConfigFactory); 
   }
    
   
   public void testServerAuthContext() throws Exception
   {
      //Key tester is the JASPISecurityFilter that does programmatic jaspi security
      this.deploy("jbosssx-jaspi-web.war");
      try
      {
         //Check successful validation
         String str = "jbosssx-jaspi-web/DebugServlet?user=jduke&pass=theduke";
         GetMethod indexGet = new GetMethod(baseURLNoAuth+str);
         int responseCode = httpConn.executeMethod(indexGet);
         assertTrue("Response code == 200?", responseCode == 200);
         //Test unsuccessful validation
         str = "jbosssx-jaspi-web/DebugServlet?user=jduke&pass=bad";
         indexGet = new GetMethod(baseURLNoAuth+str);
         responseCode = httpConn.executeMethod(indexGet); 
         assertTrue("Response code == 500?", responseCode == 500);    
      }
      catch(Throwable t)
      {
         throw new Exception(t.getMessage());
      }
      finally
      { 
         this.undeploy("jbosssx-jaspi-web.war");
      } 
   }
   
   public void testConfigProviderRegistration() throws Exception
   {
      String registrationID = null;
      String layer = null;
      String appContext = "testAppContext";
      factory = AuthConfigFactory.getFactory();
      //Register an AuthConfigProvider for all layers
      registrationID = factory.registerConfigProvider(allLayerProvider,
                              new HashMap(),layer,appContext, "This is a test provider");
      AuthConfigProvider acp = factory.getConfigProvider("TestLayer",appContext, 
                      null);
      assertTrue("ACP instanceof AllLayerAuthConfigProvider", 
                       acp instanceof AllLayerAuthConfigProvider);
      acp = factory.getConfigProvider(layer,appContext, null);
      assertTrue("ACP instanceof AllLayerAuthConfigProvider", 
                       acp instanceof AllLayerAuthConfigProvider);
      
      assertTrue("Registration removed", factory.removeRegistration(registrationID));
      layer = SecurityConstants.SERVLET_LAYER;
      //Register an AuthConfigProvider for all appcontext in a layer
      registrationID = factory.registerConfigProvider(allACProvider, new HashMap(),
                                layer, null, "This is a test provider");
      acp = factory.getConfigProvider(layer,"testAppContext", null);
      assertTrue("ACP instanceof AllAppContextAuthConfigProvider", 
                       acp instanceof AllAppContextAuthConfigProvider);
      acp = factory.getConfigProvider(layer,"testAppContext", null);
      assertTrue("ACP instanceof AllAppContextAuthConfigProvider", 
                       acp instanceof AllAppContextAuthConfigProvider);
      acp = factory.getConfigProvider(layer,"testOtherAppContext", null);
      assertTrue("ACP instanceof AllAppContextAuthConfigProvider", 
                       acp instanceof AllAppContextAuthConfigProvider); 
   }
   
   public void testRegistrationListener() throws Exception
   {
      RegistrationListener rl = new RegistrationListener()
      { 
         public void notify(String layer, String appContext)
         {
         } 
      };
      
      String layer = SecurityConstants.SERVLET_LAYER; 
      //Register an AuthConfigProvider for all appcontext in a layer
      factory.registerConfigProvider( allACProvider, new HashMap(),
                                  layer, null, "This is a test provider");
      AuthConfigProvider acp = factory.getConfigProvider(layer,"testAppContext", rl);
      String[] ids = factory.getRegistrationIDs(acp);
      String[] detachedIds = factory.detachListener(rl, layer, "testAppContext");
      checkStringArrayEquals(ids, detachedIds);
   }
   
   private void checkStringArrayEquals(String[] a, String[] b)
   {
     if(a == null && b == null)
        return;
     assertEquals("Length should be equal",a.length,b.length); 
     int len = a.length;
     for(int i=0; i < len; i++)
        assertEquals(a[i],b[i]);
   } 
}

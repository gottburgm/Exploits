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
package org.jboss.test.profileservice.testsecure;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.jboss.deployers.spi.management.ManagementView;
import org.jboss.managed.api.ComponentType;
import org.jboss.managed.api.ManagedComponent;
import org.jboss.managed.api.ManagedProperty;
import org.jboss.metatype.api.types.GenericMetaType;
import org.jboss.metatype.api.types.MetaType;
import org.jboss.metatype.api.values.MetaValue;
import org.jboss.profileservice.spi.ProfileKey;
import org.jboss.profileservice.spi.ProfileService;
import org.jboss.test.profileservice.test.AbstractProfileServiceTest;
import org.jboss.test.util.AppCallbackHandler;

/** Basic tests of using ProfileService via secured ejb facades

 @author Scott.Stark@jboss.org
 @version $Revision: 90532 $
 */
public class ProfileServiceUnitTestCase extends AbstractProfileServiceTest
{
   private static final String JNDI_LOGIN_INITIAL_CONTEXT_FACTORY = "org.jboss.security.jndi.JndiLoginInitialContextFactory";

   private static final String SECURE_PROFILE_SERVICE_JNDI_NAME = "SecureProfileService/remote";
   private static final String SECURE_MANAGEMENT_VIEW_JNDI_NAME = "SecureManagementView/remote";
   private static final String SECURE_DEPLOYMENT_MANAGER_JNDI_NAME = "SecureDeploymentManager/remote";
   
   private static final String PROFILE_SERVICE_PRINCIPAL = "admin";
   private static final String PROFILE_SERVICE_CREDENTIALS = "admin";

   private LoginContext loginContext;

   /**
    * We need to define the order in which tests runs
    * @return
    * @throws Exception
    */
   public static Test suite() throws Exception
   {
      TestSuite suite = new TestSuite(ProfileServiceUnitTestCase.class);

      return suite;
   }

   public ProfileServiceUnitTestCase(String name)
   {
      super(name);
      // set the login config file if it hasn't been set yet.
      if (System.getProperty("java.security.auth.login.config") == null)
         System.setProperty("java.security.auth.login.config", "output/resources/security/auth.conf");
   }

   /**
    * Basic test of accessing the ProfileService and checking the
    * available profile keys.
    */
   public void testNonAdminProfileKeys()
      throws Exception
   {
      login("jduke", "theduke".toCharArray());
      ProfileService ps = getProfileService();
      try
      {
         Collection<ProfileKey> keys = ps.getProfileKeys();
         fail("jduke was able to invoke getProfileKeys, keys: "+keys);
      }
      catch(Exception e)
      {
         log.info("jduke calling getProfileKeys failed as expected, "+e.getMessage());
      }
      finally
      {
         logout();
      }
   }

   /**
    * Basic test of accessing the ProfileService and checking the
    * available profile keys.
    */
   public void testProfileKeys()
      throws Exception
   {
      login(PROFILE_SERVICE_PRINCIPAL, PROFILE_SERVICE_CREDENTIALS.toCharArray());
      ProfileService ps = getProfileService();
      Collection<ProfileKey> keys = ps.getProfileKeys();
      log.info("getProfileKeys: "+keys);
      ProfileKey defaultKey = new ProfileKey("all");
      assertTrue("keys contains default", keys.contains(defaultKey));
      logout();
   }

   /**
    * Basic test of accessing the ProfileService and checking the
    * available component types.
    */
   public void testComponentTypes()
      throws Exception
   {
      login(PROFILE_SERVICE_PRINCIPAL, PROFILE_SERVICE_CREDENTIALS.toCharArray());
      ProfileService ps = getProfileService();
      ManagementView mgtView = ps.getViewManager();
      mgtView.load();
      Set<ComponentType> types = mgtView.getComponentTypes();
      log.info("getComponentTypes: "+types);
      assertTrue("types.size() > 0", types.size() > 0);
      logout();
   }

   /**
    * Try to 
    * @throws Exception
    */
   public void testNonAdminDefaultDSComponentCount()
      throws Exception
   {
      login("jduke", "theduke".toCharArray());
      ManagementView mgtView = getManagementView();
      ComponentType type = new ComponentType("DataSource", "LocalTx");
      try
      {
         Set<ManagedComponent> comps = mgtView.getComponentsForType(type);
         fail("jduke was able to invoke getComponentsForType");
      }
      catch(Exception e)
      {
         log.info("jduke calling getComponentsForType failed as expected, "+e.getMessage());
      }
      finally
      {
         logout();
      }
   }

   /**
    * Validate that there is only 1 DefaultDS ManagedComponent
    * @throws Exception
    */
   public void testDefaultDSComponentCount()
      throws Exception
   {
      login(PROFILE_SERVICE_PRINCIPAL, PROFILE_SERVICE_CREDENTIALS.toCharArray());
      ManagementView mgtView = getManagementView();
      ComponentType type = new ComponentType("DataSource", "LocalTx");
      Set<ManagedComponent> comps = mgtView.getComponentsForType(type);
      int count = 0;
      for (ManagedComponent comp : comps)
      {
        String cname = comp.getName();
        if( cname.endsWith("DefaultDS") )
        {
           count ++;
        }
      }
      assertEquals("There is 1 DefaultDS ManagedComponent", 1, 1);
      logout();
   }

   /**
    * Validate that there is only 1 DefaultDS ManagedComponent
    * @throws Exception
    */
   public void testDefaultDSComponentCountUsingJLICF()
      throws Exception
   {
      ManagementView mgtView = getManagementViewJLICF();
      ComponentType type = new ComponentType("DataSource", "LocalTx");
      Set<ManagedComponent> comps = mgtView.getComponentsForType(type);
      int count = 0;
      for (ManagedComponent comp : comps)
      {
        String cname = comp.getName();
        if( cname.endsWith("DefaultDS") )
        {
           count ++;
        }
      }
      assertEquals("There is 1 DefaultDS ManagedComponent", 1, 1);
   }
   
   // Private and protected

   @Override
   protected String getProfileName()
   {
      return "profileservice";
   }

   protected InitialContext getInitialContextJLICF()
      throws NamingException
   {
      Properties env = new Properties();
      env.setProperty(Context.INITIAL_CONTEXT_FACTORY, JNDI_LOGIN_INITIAL_CONTEXT_FACTORY);
      env.setProperty(Context.SECURITY_PRINCIPAL, PROFILE_SERVICE_PRINCIPAL);
      env.setProperty(Context.SECURITY_CREDENTIALS, PROFILE_SERVICE_CREDENTIALS);      
      
      InitialContext ctx = new InitialContext(env);
      return ctx;
   }
   /**
    * 
    * @return
    * @throws Exception
    */
   protected ProfileService getProfileService()
      throws Exception
   {
      InitialContext ctx = super.getInitialContext();
      ProfileService ps = (ProfileService) ctx.lookup("SecureProfileService/remote");
      return ps;
   }
   
   /**
    * Lookup of the ProfileService using a JNDI login InitialContextFactory
    * @return
    * @throws Exception
    */
   protected ProfileService getProfileServiceJLICF()
      throws Exception
   {
      InitialContext ctx = getInitialContextJLICF();
      Object ref = ctx.lookup(SECURE_PROFILE_SERVICE_JNDI_NAME);
      log.debug(SECURE_PROFILE_SERVICE_JNDI_NAME+": "+ref);
      ProfileService ps = (ProfileService) ref;
      return ps;
   }

   @Override
   protected ManagementView getManagementView()
      throws Exception
   {
      InitialContext ctx = super.getInitialContext();
      ManagementView mgtView = (ManagementView) ctx.lookup("SecureManagementView/remote");
      return mgtView;
   }
   protected ManagementView getManagementViewJLICF()
      throws Exception
   {
      InitialContext ctx = getInitialContextJLICF();
      ManagementView mgtView = (ManagementView) ctx.lookup("SecureManagementView/remote");
      return mgtView;
   }

   /**
    * Authenticates the client identified by the given {@code username} using the specified {@code password}.
    * 
    * @param username identifies the client that is being logged in.
    * @param password the password that asserts the client's identity.
    * @throws LoginException if an error occurs while authenticating the client.
    */
   protected void login(String username, char[] password) throws LoginException
   {
      // get the conf name from a system property - default is profileservice.
      String confName = System.getProperty("conf.name", "profileservice");
      AppCallbackHandler handler = new AppCallbackHandler(username, password);
      this.loginContext = new LoginContext(confName, handler);
      this.loginContext.login();
   }

   /**
    * Perform a logout of the current user.
    * 
    * @throws LoginException if an error occurs while logging the user out.
    */
   protected void logout() throws LoginException
   {
      this.loginContext.logout();
   }

   protected void validatePropertyMetaValues(Map<String, ManagedProperty> props)
   {
      HashMap<String, Object> invalidValues = new HashMap<String, Object>();
      HashMap<String, Object> nullValues = new HashMap<String, Object>();
      for(ManagedProperty prop : props.values())
      {
         Object value = prop.getValue();
         if((value instanceof MetaValue) == false)
         {
            if(value == null)
               nullValues.put(prop.getName(), value);
            else
               invalidValues.put(prop.getName(), value);
         }
      }
      log.info("Propertys with null values: "+nullValues);
      assertEquals("InvalidPropertys: "+invalidValues, 0, invalidValues.size());

      // Validate more details on specific properties
      ManagedProperty securityDomain = props.get("security-domain");
      assertNotNull("security-domain", securityDomain);
      MetaType securityDomainType = securityDomain.getMetaType();
      assertTrue("security-domain type("+securityDomainType+") is a GenericMetaType", securityDomainType instanceof GenericMetaType);
      log.debug("security-domain type: "+securityDomainType);
   }
}

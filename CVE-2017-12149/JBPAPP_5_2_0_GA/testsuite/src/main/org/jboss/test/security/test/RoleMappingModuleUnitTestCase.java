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
package org.jboss.test.security.test;

import java.io.File;
import java.io.FileWriter; 
import java.io.IOException;
import java.lang.reflect.Method;
import java.security.Principal;
import java.util.HashMap;
import java.util.Iterator;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.AppConfigurationEntry;
import javax.security.auth.login.Configuration;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;

import junit.framework.TestCase;

import org.jboss.logging.Logger;
import org.jboss.logging.XLevel; 
import org.jboss.security.SimpleGroup;
import org.jboss.security.SimplePrincipal;

//$Id: RoleMappingModuleUnitTestCase.java 81036 2008-11-14 13:36:39Z dimitris@jboss.org $

/**
 *  JBAS-3323: Role Mapping Login Module that maps application role to 
 *  declarative role
 *  @author <a href="mailto:Anil.Saldhana@jboss.org">Anil Saldhana</a>
 *  @since  Jun 22, 2006
 *  @version $Revision: 81036 $
 */
public class RoleMappingModuleUnitTestCase extends TestCase
{  
   private static String tmpDir = System.getProperty("java.io.tmpdir");
   private static String rolesFile = "file:" + tmpDir + "/rolesmapping-roles.properties";
   
   static class TestConfig extends Configuration
   {
      public void refresh()
      {
      }
      
      public AppConfigurationEntry[] getAppConfigurationEntry(String name)
      {
         AppConfigurationEntry[] entry = null;
         try
         {
            Class[] parameterTypes = {};
            Method m = getClass().getDeclaredMethod(name, parameterTypes);
            Object[] args = {};
            entry = (AppConfigurationEntry[]) m.invoke(this, args);
         }
         catch(Exception e)
         {
         }
         return entry;
      } 
      
      AppConfigurationEntry[] testRoleMapping()
      {
         AppConfigurationEntry ace = getIdentityLoginModuleEntry();
         
         String name2 = "org.jboss.security.auth.spi.RoleMappingLoginModule";
         HashMap options2 = new HashMap();
         options2.put("rolesProperties", rolesFile); 
         AppConfigurationEntry ace2 = new AppConfigurationEntry(name2,
               AppConfigurationEntry.LoginModuleControlFlag.OPTIONAL, options2);
         
         AppConfigurationEntry[] entry = {ace,ace2};
         return entry;
      } 
      
      AppConfigurationEntry[] testRoleMappingWithReplace()
      {
         AppConfigurationEntry ace = getIdentityLoginModuleEntry();
         
         String name2 = "org.jboss.security.auth.spi.RoleMappingLoginModule";
         HashMap options2 = new HashMap();
         options2.put("rolesProperties", rolesFile);
         options2.put("replaceRole", "true");
         AppConfigurationEntry ace2 = new AppConfigurationEntry(name2,
               AppConfigurationEntry.LoginModuleControlFlag.OPTIONAL, options2);
         
         AppConfigurationEntry[] entry = {ace,ace2};
         return entry;
      }

      private AppConfigurationEntry getIdentityLoginModuleEntry()
      {
         String name = "org.jboss.security.auth.spi.IdentityLoginModule";
         HashMap options = new HashMap();
         options.put("principal", "stark");
         options.put("roles", "Role3,Role4");
         AppConfigurationEntry ace = new AppConfigurationEntry(name,
               AppConfigurationEntry.LoginModuleControlFlag.REQUIRED, options);
         return ace;
      } 
   }
   
   public RoleMappingModuleUnitTestCase(String name)
   {
      super(name); 
   } 
   
   protected void setUp() throws Exception
   {
      // Install the custom JAAS configuration
      Configuration.setConfiguration(new TestConfig());
   }
   
   /**
    * Test the RoleMappingLoginModule with no option to replace the role
    * @throws Exception
    */
   public void testRoleMappingModule() throws Exception
   {
      File file = createRolesFile(); 
      assertTrue("File exists",file.exists()); 
      processLogin("testRoleMapping", false); 
      clearRolesFile(file);
   }
   
   /**
    * Test the RoleMappingLoginModule with an option to replace the role
    * @throws Exception
    */
   public void testRoleMappingModuleWithReplace() throws Exception
   {
      File file = createRolesFile(); 
      assertTrue("File exists",file.exists()); 
      processLogin("testRoleMappingWithReplace",true); 
      clearRolesFile(file);
   }

   /**
    * Do the JAAS Login that includes the RoleMappingLoginModule
    * @param config Jaas Configuration Name
    * @param replaceRole flag whether the role has been replaced in the subject
    * @throws LoginException
    */
   private void processLogin(String config, boolean replaceRole) throws LoginException
   {
      Subject subject = new Subject();
      LoginContext lc = new LoginContext(config,subject, new TestCallbackHandler());
      lc.login();
      subject = lc.getSubject();
      Iterator iter = subject.getPrincipals().iterator();
      boolean ranAsserts = false;
      while(iter.hasNext())
      {
         Principal p = (Principal)iter.next();
         if(p instanceof SimpleGroup)
         {
            SimpleGroup sg = (SimpleGroup)p;
            ranAsserts = true;
            assertTrue("testRole exists?", sg.isMember(new SimplePrincipal("testRole")));
            assertTrue("testRole2 exists?", sg.isMember(new SimplePrincipal("testRole2")));
            assertTrue("Role4 exists?", sg.isMember(new SimplePrincipal("Role4")));
            if(replaceRole)
              assertFalse("Role3 does not exist?", sg.isMember(new SimplePrincipal("Role3")));
            else
               assertTrue("Role3 exists?", sg.isMember(new SimplePrincipal("Role3")));
         }
      } 
      assertTrue("Ran Asserts?",ranAsserts);
   }

   /**
    * Delete the properties file created for the test
    * @param file
    */
   private void clearRolesFile(File file)
   {
      if(file.exists())
         file.delete(); 
      assertFalse("File does not exist",file.exists());
   }

   /**
    * Create a properties file for the test
    * @return
    * @throws IOException
    */
   private File createRolesFile() throws IOException
   { 
      File file = new File(tmpDir + "/rolesmapping-roles.properties");
      clearRolesFile(file); //Delete residual files (if any)
      FileWriter fw = new FileWriter(file);
      fw.write("Role3=testRole,testRole2");
      fw.close();
      return file;
   }
   
   /**
    * 
    * A TestCallbackHandler.
    * Does not do anything.
    * @author <a href="anil.saldhana@jboss.com">Anil Saldhana</a>
    * @version $Revision: 81036 $
    */
   private class TestCallbackHandler implements CallbackHandler
   { 
      public void handle(Callback[] arg0) throws IOException, 
      UnsupportedCallbackException
      { 
      } 
   }
}

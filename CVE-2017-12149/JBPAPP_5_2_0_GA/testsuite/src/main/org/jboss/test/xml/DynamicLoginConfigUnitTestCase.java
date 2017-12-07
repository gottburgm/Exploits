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
package org.jboss.test.xml;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import javax.security.auth.login.AppConfigurationEntry;

import junit.framework.TestCase;
import org.jboss.security.auth.spi.Users;
import org.jboss.security.auth.container.config.AuthModuleEntry;
import org.jboss.security.auth.login.JASPIAuthenticationInfo; 
import org.jboss.security.auth.login.LoginModuleStackHolder; 
import org.jboss.security.config.PolicyConfig;
import org.jboss.security.auth.login.AuthenticationInfo;
import org.jboss.security.config.ApplicationPolicy;
import org.jboss.xb.binding.JBossXBException;
import org.jboss.xb.binding.Unmarshaller;
import org.jboss.xb.binding.UnmarshallerFactory;
import org.jboss.xb.binding.sunday.unmarshalling.SchemaBindingResolver;
import org.jboss.xb.binding.sunday.unmarshalling.SchemaBinding;
import org.jboss.xb.binding.sunday.unmarshalling.XsdBinder;
import org.w3c.dom.ls.LSInput;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;

//$Id: DynamicLoginConfigUnitTestCase.java 81036 2008-11-14 13:36:39Z dimitris@jboss.org $

/**
 * Test unmarshalling xml documents conforming to mbean-service_1_0.xsd into
 * the org.jboss.test.xml.mbeanserver.Services and related objects.
 * 
 * @author Scott.Stark@jboss.org
 * @author Anil.Saldhana@jboss.org
 * @version $Revision: 81036 $
 */
public class DynamicLoginConfigUnitTestCase
   extends TestCase
{
   /**
    * A test of unmarshalling an element from a document without any knowledge
    * of the associated schema.
    * 
    * @throws Exception
    */ 
   public void testConfig() throws Exception
   {
      // Set the jboss url protocol handler path
      System.setProperty("java.protocol.handler.pkgs", "org.jboss.net.protocol");
      InputStream is = getResource("xml/loginconfig/config.xml");

      Object root = getParsedRoot(is);

      PolicyConfig config = (PolicyConfig) root;
      is.close();

      // Validate the bindings
      ApplicationPolicy aPolicy = (ApplicationPolicy)config.get("conf1");
      AuthenticationInfo info = (AuthenticationInfo)aPolicy.getAuthenticationInfo();
      validateJaasBindings(info);
   }
   
   /**
    * A test of unmarshalling an element from a document without any knowledge
    * of the associated schema. (JASPI Version based on the security-config_5_0.xsd
    * 
    * @throws Exception
    */ 
   public void testJASPIConfig() throws Exception
   {
      // Set the jboss url protocol handler path
      System.setProperty("java.protocol.handler.pkgs", "org.jboss.net.protocol");
      InputStream is = getResource("xml/loginconfig/jaspi-config.xml");

      Object root = getParsedRoot(is);

      PolicyConfig config = (PolicyConfig) root;
      is.close();

      // Validate the bindings
      ApplicationPolicy aPolicy = (ApplicationPolicy)config.get("conf1");
      AuthenticationInfo info = (AuthenticationInfo)aPolicy.getAuthenticationInfo();
      validateJaasBindings(info);
      
      //Validate the JASPI bindings
      aPolicy = (ApplicationPolicy)config.get("conf-jaspi");
      JASPIAuthenticationInfo jaspiInfo = (JASPIAuthenticationInfo)aPolicy.getAuthenticationInfo();
      validateJASPIBindings( jaspiInfo );
   } 

   // Private  
   private Object getParsedRoot(InputStream is) throws JBossXBException
   {
      /* Parse the element content using the Unmarshaller starting with an
      empty schema since we don't know anything about it. This is not quite
      true as we set the schema baseURI to the resources/xml/loginconfig/ directory
      so that the xsds can be found, but this baseURI
      can be easily specified to the SARDeployer, or the schema can be made
      available to the entity resolver via some other configuration.
      */
      final URL url = Thread.currentThread().getContextClassLoader().getResource("xml/loginconfig/");
      Unmarshaller unmarshaller = UnmarshallerFactory.newInstance().newUnmarshaller();
      unmarshaller.setEntityResolver(new EntityResolver(){
         public InputSource resolveEntity(String publicId, String systemId)
         {
            return null;
         }
      });
      Object root = unmarshaller.unmarshal(is, new SchemaBindingResolver(){
         public String getBaseURI()
         {
            throw new UnsupportedOperationException("getBaseURI is not implemented.");
         }

         public void setBaseURI(String baseURI)
         {
            throw new UnsupportedOperationException("setBaseURI is not implemented.");
         }

         public SchemaBinding resolve(String nsUri, String baseURI, String schemaLocation)
         {
            return XsdBinder.bind(url.toExternalForm() + schemaLocation, this);
         }

         public LSInput resolveAsLSInput(String nsUri, String baseUri, String schemaLocation)
         {
            throw new UnsupportedOperationException("resolveAsLSInput is not implemented.");
         }
      });
      return root;
   }
   
   
   private void validateJaasBindings( AuthenticationInfo info )
   {
      assertNotNull("conf1", info);
      AppConfigurationEntry[] entry = info.getAppConfigurationEntry();
      assertTrue("entry.length == 1", entry.length == 1);
      assertTrue("entry[0].getLoginModuleName() == XMLLoginModule",
         entry[0].getLoginModuleName().equals("org.jboss.security.auth.spi.XMLLoginModule"));
      Map options = entry[0].getOptions();
      assertTrue("There are two options", options.size() == 2);
      String unauthenticatedIdentity = (String) options.get("unauthenticatedIdentity");
      assertNotNull("options.unauthenticatedIdentity exists", unauthenticatedIdentity);
      assertTrue("options.unauthenticatedIdentity == guest",
         unauthenticatedIdentity.equals("guest"));

      Users users = (Users) options.get("userInfo");
      assertNotNull("options.userInfo is a Users", users);
      assertTrue("Users.size("+users.size()+") is 6", users.size() == 6);
      Users.User jduke = users.getUser("jduke");
      assertNotNull("jduke is a user", jduke);
      assertTrue("jduke.password == theduke", jduke.getPassword().equals("theduke"));
      String[] roleNames = jduke.getRoleNames("Roles");
      HashSet roles = new HashSet(Arrays.asList(roleNames));
      assertTrue("jduke has roles", roles.size() == 3);
      assertTrue("Role1 is a role", roles.contains("Role1"));
      assertTrue("Role2 is a role", roles.contains("Role2"));
      assertTrue("Echo is a role", roles.contains("Echo"));
   }
   
   private void validateJASPIBindings( JASPIAuthenticationInfo jaspiInfo )
   {
      assertNotNull("conf-jaspi", jaspiInfo);
      AuthModuleEntry[] authEntry = jaspiInfo.getAuthModuleEntry();
      assertTrue("entry.length == 2", authEntry.length == 2);
      //Get the first AuthModule
      AuthModuleEntry aEntry1 = authEntry[0];
      validateAuthModule1(aEntry1); 
      //Get the second AuthModule
      AuthModuleEntry aEntry2 = authEntry[1];
      validateAuthModule2(aEntry2);
   }
   
   private void validateAuthModule1(AuthModuleEntry aEntry1)
   { 
      assertEquals("auth.module1.class.name", aEntry1.getAuthModuleName());
      Map aEntry1Options = aEntry1.getOptions();
      assertNotNull("Options in the first AuthModule != null", aEntry1Options);
      assertTrue( "Length of options == 3", aEntry1Options.size() == 3); 
      String usersProperties = (String) aEntry1Options.get("usersProperties");
      assertNotNull("options.usersProperties exists", usersProperties);
      assertTrue("options.usersProperties == props/jbossws-users.properties",
            usersProperties.equals("props/jbossws-users.properties"));
      String rolesProperties = (String) aEntry1Options.get("rolesProperties");
      assertNotNull("options.rolesProperties exists", rolesProperties);
      assertTrue("options.rolesProperties == props/jbossws-roles.properties",
            rolesProperties.equals("props/jbossws-roles.properties"));
   }
   
   private void validateAuthModule2(AuthModuleEntry aEntry2)
   { 
      assertEquals("auth.module2.class.name", aEntry2.getAuthModuleName());
      LoginModuleStackHolder lmsh = aEntry2.getLoginModuleStackHolder();
      assertNotNull("LoginModuleStackHolder  != null", lmsh);
      assertEquals("lm-stack", lmsh.getName());
   }

   private InputStream getResource(String path)
      throws IOException
   {
      URL url = Thread.currentThread().getContextClassLoader().getResource(path);
      if(url == null)
      {
         fail("URL not found: " + path);
      }
      return url.openStream();
   }
}

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
package org.jboss.test.jacc.test;
 
import java.io.InputStream;
import java.net.URL;
import java.security.Permissions;

import javax.security.jacc.WebResourcePermission;
import javax.security.jacc.WebRoleRefPermission;
import javax.security.jacc.WebUserDataPermission;

import org.jboss.metadata.web.jboss.JBossWebMetaData;
import org.jboss.metadata.web.spec.Web24MetaData;
import org.jboss.metadata.web.spec.WebMetaData;
import org.jboss.test.JBossTestCase;
import org.jboss.util.xml.JBossEntityResolver; 
import org.jboss.web.WebPermissionMapping;
import org.jboss.xb.binding.JBossXBException;
import org.jboss.xb.binding.Unmarshaller;
import org.jboss.xb.binding.UnmarshallerFactory;
import org.jboss.xb.binding.sunday.unmarshalling.SchemaBinding;
import org.jboss.xb.builder.JBossXBBuilder;

//$Id: WebPermissionsValidationTestCase.java 85945 2009-03-16 19:45:12Z dimitris@jboss.org $

/**
 *  Validate the parsing of web.xml and the creation of JACC Permissions
 *  @author <a href="mailto:Anil.Saldhana@jboss.org">Anil Saldhana</a>
 *  @since  Dec 18, 2006 
 *  @version $Revision: 85945 $
 */
public class WebPermissionsValidationTestCase extends JBossTestCase
{ 
   public WebPermissionsValidationTestCase(String name)
   {
      super(name); 
   }
   
   public WebMetaData getWebMetaData(InputStream webxml) 
      throws JBossXBException
   { 
      UnmarshallerFactory unmarshallerFactory = UnmarshallerFactory.newInstance();
      Unmarshaller unmarshaller = unmarshallerFactory.newUnmarshaller();
      SchemaBinding schema = JBossXBBuilder.build(Web24MetaData.class);
      JBossEntityResolver entityResolver = new JBossEntityResolver();
      unmarshaller.setEntityResolver(entityResolver);

      return (WebMetaData) unmarshaller.unmarshal(webxml, schema); 
   } 
   
   public void testWebPermissions() throws Exception 
   {
      ClassLoader cl = Thread.currentThread().getContextClassLoader();
      URL webxml = cl.getResource("security/jacc/webperm/web.xml");
      assertNotNull("web.xml exists?", webxml);
      WebMetaData wmd = getWebMetaData(webxml.openStream());
      JBossWebMetaData jwmd = new JBossWebMetaData();
      jwmd.merge(null, wmd);
      TestJBossPolicyConfiguration tpc = new TestJBossPolicyConfiguration("dummy");
      WebPermissionMapping.createPermissions(jwmd, tpc); 
      checkUncheckedPermissions(tpc.getUncheckedPolicy());
      checkExcludedPermissions(tpc.getExcludedPolicy());
      checkAddToRoleForAdministrator(tpc.getPermissionsForRole("Administrator")); 
      checkAddToRoleForManager(tpc.getPermissionsForRole("Manager")); 
      checkAddToRoleForEmployee(tpc.getPermissionsForRole("Employee"));
   }  
    
   
   private void checkUncheckedPermissions(Permissions p)
   {
      assertTrue(p.implies(new WebResourcePermission("/sslprotected.jsp", "!GET,POST")));
      assertTrue(p.implies(new WebResourcePermission("/:/secured.jsp:/unchecked.jsp:/excluded.jsp:/sslprotected.jsp",
            (String) null)));
      assertTrue(p.implies(new WebResourcePermission("/excluded.jsp", "!GET,POST")));
      assertTrue(p.implies(new WebResourcePermission("/secured.jsp", "!GET,POST")));
      assertTrue(p.implies(new WebResourcePermission("/unchecked.jsp", (String) null)));
      
      assertTrue(p.implies(new WebUserDataPermission("/sslprotected.jsp", "GET,POST:CONFIDENTIAL")));
      assertTrue(p.implies(new WebUserDataPermission("/excluded.jsp", "!GET,POST")));
      assertTrue(p.implies(new WebUserDataPermission("/sslprotected.jsp", "!GET,POST")));
      assertTrue(p.implies(new WebUserDataPermission("/secured.jsp", (String) null)));
      assertTrue(p.implies(new WebUserDataPermission("/:/unchecked.jsp:/secured.jsp:/sslprotected.jsp:/excluded.jsp",
            (String) null)));
      assertTrue(p.implies(new WebUserDataPermission("/unchecked.jsp", (String) null)));
   }
   
   private void checkExcludedPermissions(Permissions p)
   {
      assertTrue(p.implies(new WebResourcePermission("/excluded.jsp", "GET,POST"))); 
      assertTrue(p.implies(new WebUserDataPermission("/excluded.jsp", "GET,POST")));
   }
   
   private void checkAddToRoleForManager(Permissions p)
   { 
      assertTrue(p.implies(new WebRoleRefPermission("secured", "Manager")));
      assertTrue(p.implies(new WebRoleRefPermission("sslprotected", "MGR")));
      assertTrue(p.implies(new WebRoleRefPermission("sslprotected", "Manager")));
      assertTrue(p.implies(new WebRoleRefPermission("unchecked", "Manager")));
      assertTrue(p.implies(new WebRoleRefPermission("excluded", "Manager")));
      //Jacc1.1 
      assertTrue(p.implies(new WebRoleRefPermission("", "Manager"))); 
   } 
   
   private void checkAddToRoleForAdministrator(Permissions p)
   {
      assertTrue(p.implies(new WebResourcePermission("/secured.jsp", "GET,POST")));
      assertTrue(p.implies(new WebResourcePermission("/sslprotected.jsp", "GET,POST")));
      
      assertTrue(p.implies(new WebRoleRefPermission("secured", "ADM")));
      assertTrue(p.implies(new WebRoleRefPermission("secured", "Administrator")));
      assertTrue(p.implies(new WebRoleRefPermission("sslprotected", "ADM")));
      assertTrue(p.implies(new WebRoleRefPermission("sslprotected", "Administrator")));
      assertTrue(p.implies(new WebRoleRefPermission("unchecked", "Administrator")));
      assertTrue(p.implies(new WebRoleRefPermission("excluded", "Administrator")));
      //Jacc1.1
      assertTrue(p.implies(new WebRoleRefPermission("", "Administrator"))); 
   }
   
   private void checkAddToRoleForEmployee(Permissions p)
   {
      assertTrue(p.implies(new WebRoleRefPermission("secured", "Employee")));
      assertTrue(p.implies(new WebRoleRefPermission("sslprotected", "Employee")));
      assertTrue(p.implies(new WebRoleRefPermission("unchecked", "Employee")));
      assertTrue(p.implies(new WebRoleRefPermission("excluded", "Employee"))); 
      //  Jacc1.1
      assertTrue(p.implies(new WebRoleRefPermission("", "Employee"))); 
   }
}

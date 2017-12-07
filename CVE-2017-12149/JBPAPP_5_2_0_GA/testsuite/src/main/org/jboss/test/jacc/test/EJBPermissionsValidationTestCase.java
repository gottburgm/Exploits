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

import javax.security.jacc.EJBMethodPermission;

import org.jboss.ejb.EJBPermissionMapping;
import org.jboss.metadata.ejb.jboss.JBoss50MetaData;
import org.jboss.metadata.ejb.jboss.JBossEnterpriseBeansMetaData;
import org.jboss.metadata.ejb.spec.EjbJar21MetaData;
import org.jboss.test.JBossTestCase;
import org.jboss.util.xml.JBossEntityResolver;
import org.jboss.xb.binding.JBossXBException;
import org.jboss.xb.binding.Unmarshaller;
import org.jboss.xb.binding.UnmarshallerFactory;
import org.jboss.xb.binding.sunday.unmarshalling.SchemaBinding;
import org.jboss.xb.builder.JBossXBBuilder;

//$Id: EJBPermissionsValidationTestCase.java 85945 2009-03-16 19:45:12Z dimitris@jboss.org $

/**
 *  Validate the parsing of ejb-jar.xml and the creation of JACC Permissions
 *  @author <a href="mailto:Anil.Saldhana@jboss.org">Anil Saldhana</a>
 *  @since  Dec 18, 2006 
 *  @version $Revision: 85945 $
 */
public class EJBPermissionsValidationTestCase extends JBossTestCase
{ 
   public EJBPermissionsValidationTestCase(String name)
   {
      super(name); 
   }
   
   public EjbJar21MetaData getEJBMetaData(InputStream ejbJarXml) 
      throws JBossXBException
   { 
      UnmarshallerFactory unmarshallerFactory = UnmarshallerFactory.newInstance();
      Unmarshaller unmarshaller = unmarshallerFactory.newUnmarshaller();
      SchemaBinding schema = JBossXBBuilder.build(EjbJar21MetaData.class);
      JBossEntityResolver entityResolver = new JBossEntityResolver();
      unmarshaller.setEntityResolver(entityResolver);

      return (EjbJar21MetaData) unmarshaller.unmarshal(ejbJarXml, schema); 
   } 
   
   public void testEJBPermissions() throws Exception 
   {
      ClassLoader cl = Thread.currentThread().getContextClassLoader();
      URL ejbxml = cl.getResource("security/jacc/ejbperm/jacc_ejb_jar.xml");
      assertNotNull("ejb-jar.xml exists?", ejbxml);
      EjbJar21MetaData emd = getEJBMetaData(ejbxml.openStream());
      
      JBoss50MetaData jmd = new JBoss50MetaData(); 
      jmd.merge(null, emd);
      TestJBossPolicyConfiguration tpc = new TestJBossPolicyConfiguration("dummy");
      
      JBossEnterpriseBeansMetaData jes = jmd.getEnterpriseBeans();
      assertEquals(jes.size(), 2);
      EJBPermissionMapping.createPermissions(jes.get("JACCSession"), tpc);
      EJBPermissionMapping.createPermissions(jes.get("JACCEntity"), tpc); 
      
      checkUncheckedPermissions(tpc.getUncheckedPolicy());
      checkExcludedPermissions(tpc.getExcludedPolicy());
      checkAddToRoleForAdministrator(tpc.getPermissionsForRole("Administrator"));  
      checkAddToRoleForEmployee(tpc.getPermissionsForRole("Employee"));
   }  
    
   
   private void checkUncheckedPermissions(Permissions p)
   {
      assertTrue(p.implies(new EJBMethodPermission("JACCEntity", "findByPrimaryKey,Home")));
      assertTrue(p.implies(new EJBMethodPermission("JACCEntity", "create,Home")));
      assertTrue(p.implies(new EJBMethodPermission("JACCEntity", "getEJBMetaData,Home")));
      assertTrue(p.implies(new EJBMethodPermission("JACCEntity", "remove,Home,java.lang.Object")));
      assertTrue(p.implies(new EJBMethodPermission("JACCEntity", "remove,Home,javax.ejb.Handle")));
      assertTrue(p.implies(new EJBMethodPermission("JACCEntity", "getHomeHandle,Home")));
      assertTrue(p.implies(new EJBMethodPermission("JACCEntity", "getPrimaryKey,Remote")));
      assertTrue(p.implies(new EJBMethodPermission("JACCEntity", "getEJBHome,Remote")));
      assertTrue(p.implies(new EJBMethodPermission("JACCEntity", "getArg2,Remote")));
      assertTrue(p.implies(new EJBMethodPermission("JACCEntity", "accessJACCSession_getCallerName,Remote")));
      assertTrue(p.implies(new EJBMethodPermission("JACCEntity", "remove,Remote")));
      assertTrue(p.implies(new EJBMethodPermission("JACCEntity", "isIdentical,Remote")));
      assertTrue(p.implies(new EJBMethodPermission("JACCEntity", "getHandle,Remote")));
      
      assertTrue(p.implies(new EJBMethodPermission("JACCSession", "create,Home")));
      assertTrue(p.implies(new EJBMethodPermission("JACCSession", "remove,Home,java.lang.Object")));
      assertTrue(p.implies(new EJBMethodPermission("JACCSession", "remove,Home,javax.ejb.Handle")));
      assertTrue(p.implies(new EJBMethodPermission("JACCSession", "getEJBMetaData,Home")));
      assertTrue(p.implies(new EJBMethodPermission("JACCSession", "getHomeHandle,Home")));
      assertTrue(p.implies(new EJBMethodPermission("JACCSession", "getEJBHome,Remote")));
      assertTrue(p.implies(new EJBMethodPermission("JACCSession", "getPrimaryKey,Remote")));
      assertTrue(p.implies(new EJBMethodPermission("JACCSession", "remove,Remote")));
      assertTrue(p.implies(new EJBMethodPermission("JACCSession", "isIdentical,Remote")));
      assertTrue(p.implies(new EJBMethodPermission("JACCSession", "getCallerName,Remote")));
      assertTrue(p.implies(new EJBMethodPermission("JACCSession", "getHandle,Remote")));
      assertTrue(p.implies(new EJBMethodPermission("JACCSession", "getArg2,Remote")));
   }
   
   private void checkExcludedPermissions(Permissions p)
   {
      assertTrue(p.implies(new EJBMethodPermission("JACCSession", "getArg3,Remote"))); 
      assertTrue(p.implies(new EJBMethodPermission("JACCEntity", "getArg3,Remote")));
   }   
   
   private void checkAddToRoleForAdministrator(Permissions p)
   {
      assertTrue(p.implies(new EJBMethodPermission("JACCSession", "getArg1,Remote"))); 
      assertTrue(p.implies(new EJBMethodPermission("JACCEntity", "getArg1,Remote"))); 
   }
   
   private void checkAddToRoleForEmployee(Permissions p)
   {
      assertTrue(p.implies(new EJBMethodPermission("JACCSession", "getArg1,Remote"))); 
      assertTrue(p.implies(new EJBMethodPermission("JACCEntity", "getArg1,Remote"))); 
   }
}

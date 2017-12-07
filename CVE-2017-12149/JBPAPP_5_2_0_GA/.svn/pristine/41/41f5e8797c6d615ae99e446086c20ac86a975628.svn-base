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
package org.jboss.test.jacc.test.portal;
 
import java.security.Policy;

import javax.security.jacc.PolicyConfiguration;
import javax.security.jacc.PolicyContext;

/**
 *  JBPORTAL-565: Create Testcase for JACC Usage
 *  Tests Portal Usecase of
 *  a) PolicyConfiguration per portal component
 *  
 *  @author <mailto:Anil.Saldhana@jboss.org>Anil Saldhana
 *  @since  Jan 17, 2006
 */
public class PortalJaccTestCase extends BasePortalJaccTestCase
{  
   
   public PortalJaccTestCase(String name)
   {
      super(name);  
   }  
   
   public void testPolicyConfigurationPerPortalComponent() throws Exception
   { 
      PolicyConfiguration pc = getPolicyConfiguration("portal-context");
      
      //Create a PortalObjectPermission
      PortalObjectPermission portalperm = new PortalObjectPermission("/default", "view");
      pc.addToRole("employee",portalperm);
      
      PortalObjectPermission windowPerm = new PortalObjectPermission("/default/default/a","view");
      pc.addToRole("janitor", windowPerm);
      
      PortalObjectPermission contextPerm = new PortalObjectPermission("/","view");
      pc.addToRole("admin", contextPerm);
      pc.commit();
      
      Policy policy = Policy.getPolicy();
      policy.refresh();
      
      //Act like the Portal Container and check perm for portal
      PolicyContext.setContextID("portal-context");
      checkAdminAccess(policy);
      checkEmployeeAccess(policy);
      checkJanitorAccess(policy);
      checkBadEmployeeAccess(policy);
   } 
}

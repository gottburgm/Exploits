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

import java.security.PermissionCollection;
import java.security.Policy;

import javax.security.jacc.PolicyConfiguration;
import javax.security.jacc.PolicyContext;

//$Id: LazyLoadPermissionCollectionTestCase.java 81036 2008-11-14 13:36:39Z dimitris@jboss.org $

/**
 *  Test portal usecase of
 *  b) Lazy load the PermissionCollection
 *  @author <a href="mailto:Anil.Saldhana@jboss.org">Anil Saldhana</a>
 *  @since  Jan 18, 2006 
 *  @version $Revision: 81036 $
 */
public class LazyLoadPermissionCollectionTestCase extends BasePortalJaccTestCase
{  
   
   public LazyLoadPermissionCollectionTestCase(String name)
   {
      super(name); 
   } 
   
   public void testLazyLoadingOfPermissionCollection() throws Exception
   { 
      PolicyConfiguration pc = getPolicyConfiguration("portal-context-1");
      
      //Add a Permission Collection to PolicyConfiguration
      /**
       * We have to add all possible permissions in the portal project to the PermissionCollection
       * as dummies, so that the dispatch to the implies method of the PermissionCollection
       * happens. In the case of LazyPermissionCollection, in the implies method, the permissions
       * are loaded based on the role and a check is made.
       */
      PermissionCollection permColl = new LazyPermissionCollection();
      permColl.add(getPortalObjectPermission("/")); //Add all possible permissions
      permColl.add(new DummyPortalPermission("/default", "view")); //Add a dummy permission
      pc.addToRole("employee", permColl);
      pc.addToRole("admin", permColl);
      pc.addToRole("janitor", permColl);
      pc.commit();
      
      Policy policy = Policy.getPolicy();
      policy.refresh();
      
      //Act like the Portal Container and check perm
      PolicyContext.setContextID("portal-context-1");
      checkAdminAccess( policy);
      checkEmployeeAccess( policy);  
      checkBadEmployeeAccess(policy);
      checkJanitorAccess(policy); 
   } 
   
}

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
package org.jboss.test.security.ejb;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.naming.InitialContext;

import org.jboss.security.AuthorizationManager;
import org.jboss.security.acl.EntitlementEntry;
import org.jboss.security.authorization.EntitlementHolder;
import org.jboss.security.authorization.Resource;
import org.jboss.security.authorization.ResourceKeys;
import org.jboss.security.identity.plugins.IdentityFactory;
import org.jboss.test.security.interfaces.ACLSession;
import org.jboss.test.security.resources.TestResource;

/**
 * <p>
 * Implementation of the {@code ACLSession} interface used in the ACL integration tests.
 * </p>
 * 
 * @author <a href="mailto:sguilhen@redhat.com">Stefan Guilhen</a>
 */
@Stateless
@Remote(ACLSession.class)
public class ACLSessionImpl implements ACLSession
{

   /*
    * (non-Javadoc)
    * 
    * @see org.jboss.test.security.interfaces.ACLSession#getEntitlementsForIdentity(java.lang.String)
    */
   public Map<Integer, String> getEntitlementsForIdentity(String identity)
   {
      Map<Integer, String> entitlementsMap = new HashMap<Integer, String>();

      try
      {
         // first retrieve the authorization manager for the acl-domain.
         InitialContext ctx = new InitialContext();
         AuthorizationManager manager = (AuthorizationManager) ctx.lookup("java:jaas/acl-domain/authorizationMgr");

         // create a resource 10 that has resource 11 as a child.
         TestResource resource10 = new TestResource(10);
         TestResource resource11 = new TestResource(11);
         Collection<Resource> childResources = new ArrayList<Resource>();
         childResources.add(resource11);
         resource10.getMap().put(ResourceKeys.CHILD_RESOURCES, childResources);
         resource11.getMap().put(ResourceKeys.PARENT_RESOURCE, resource10);

         // now call the getEntitlements method using created resource and identity objects.
         EntitlementHolder<EntitlementEntry> holder = manager.getEntitlements(EntitlementEntry.class, resource10,
               IdentityFactory.createIdentity(identity));

         // for each entitlement entry, put the resource id and associated permission in the map to be returned.
         for (EntitlementEntry entry : holder.getEntitled())
         {
            TestResource resource = (TestResource) entry.getResource();
            entitlementsMap.put(resource.getId(), entry.getPermission().toString());
         }
      }
      catch (Exception e)
      {
         throw new RuntimeException("Failed to obtain entitlements from authorization manager", e);
      }
      return entitlementsMap;
   }
}

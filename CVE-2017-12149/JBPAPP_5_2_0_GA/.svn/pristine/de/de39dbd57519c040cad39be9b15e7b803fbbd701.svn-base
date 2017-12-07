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
package org.jboss.test.security.interfaces;

import java.util.Map;

/**
 * <p>
 * Stateless session test bean interface used in the ACL integration tests.
 * </p>
 * 
 * @author <a href="mailto:sguilhen@redhat.com">Stefan Guilhen</a>
 */
public interface ACLSession
{

   /**
    * <p>
    * Calls the {@code AuthorizationManager#getEntitlements} method to retrieve the resources (and associated
    * permissions) available to the specified identity. It returns a map containing the resource id as key and the
    * permissions assigned to the identity as value (e.g. <1,"CREATE,READ,DELETE">).
    * </p>
    * 
    * @param identity the identity for which the entitlements are to be retrieved.
    * @return a {@code Map<Integer, String>} containing the ids of the resources available to the identity as keys and
    *         the permissions assigned to the identity as values.
    */
   public Map<Integer, String> getEntitlementsForIdentity(String identity);
}

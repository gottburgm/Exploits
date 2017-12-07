/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.system.server.profileservice.repository.clustered.local;

import org.jboss.system.server.profileservice.repository.clustered.metadata.RepositoryContentMetadata;

/**
 * Object responsible for maintaining a persistent copy of a node's
 * {@link RepositoryContentMetadata}.
 * 
 * @author Brian Stansberry
 * 
 * @version $Revision: $
 */
public interface ContentMetadataPersister
{   
   /**
    * Load content metadata from the persistent store.
    * 
    * @param storeName name of the store. Cannot be <code>null</code>
    * 
    * @return the metadata
    */
   RepositoryContentMetadata load(String storeName);

   /**
    * Store content metadata to the persistent store.
    * 
    * @param storeName name of the store. Cannot be <code>null</code> 
    * @param metadata the metadata. Cannot be <code>null</code>
    */
   void store(String storeName, RepositoryContentMetadata metadata);
}
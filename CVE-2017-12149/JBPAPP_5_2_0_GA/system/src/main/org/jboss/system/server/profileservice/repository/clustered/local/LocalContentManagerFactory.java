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

import java.net.URI;
import java.util.Collection;
import java.util.Map;

import org.jboss.profileservice.spi.ProfileKey;
import org.jboss.system.server.profileservice.repository.clustered.sync.SynchronizationActionContext;

/**
 * Factory for a {@link LocalContentManager} that understands a
 * particular type of URI.
 *
 * @author Brian Stansberry
 * 
 * @version $Revision: $
 */
public interface LocalContentManagerFactory<T extends SynchronizationActionContext>
{
   /**
    * Indicates whether this factory can create a {@link LocalContentManager}
    * that works with the given collection of URIs.
    * 
    * @param uris the URIs. Cannot be <code>null</code>
    * @return <code>true</code> if a persister can be created, <code>false</code>
    *         otherwise
    */
   boolean accepts(Collection<URI> uris);
   
   /**
    * Gets a {@link LocalContentManager} for the given set of URIs.
    * 
    * @param namedURIMap the URIs to be managed, keyed by a unique identifier
    * @param profileKey key of the {@link Profile} associated with the content
    * @param localNodeName cluster-wide unique name for this node
    * 
    * @return the content manager
    */
   LocalContentManager<T> getLocalContentManager(Map<String, URI> namedURIMap,
         ProfileKey profileKey, String localNodeName);
}

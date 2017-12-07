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
package org.jboss.system.server.profileservice.persistence.component;

import org.jboss.managed.api.ManagedComponent;
import org.jboss.system.server.profileservice.persistence.xml.PersistedComponent;

/**
 * @author <a href="mailto:emuckenh@redhat.com">Emanuel Muckenhuber</a>
 * @version $Revision$
 */
public interface ComponentMapper
{

   /**
    * Get the component mapper type.
    * 
    * @return the type
    */
   String getType();
   
   /**
    * Restore a component based on the persisted information.
    * 
    * @param attachment the attachment
    * @param component the persisted component
    */
   void restoreComponent(Object attachment, PersistedComponent component);

   /**
    * Add a component.
    * 
    * @param attachment the parent attachment
    * @param component the managed component
    * @return the persisted managed component
    */
   PersistedComponent addComponent(Object attachment, ManagedComponent component);
   
   /**
    * Update a component.
    * 
    * @param attachment the parent attachment
    * @param component the managed component
    * @return the persisted managed component
    */
   PersistedComponent updateComponent(Object attachment, ManagedComponent component);
   
   /**
    * Remove a component.
    * 
    * @param attachment the parent attachment
    * @param component the managed component
    * @return the persisted managed component
    */
   PersistedComponent removeComponent(Object attachment, ManagedComponent component);
   
}


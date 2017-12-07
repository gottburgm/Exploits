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
package org.jboss.system.server.profileservice.persistence.xml;

import java.util.Collection;

import org.jboss.metatype.api.types.MetaType;


/**
 * A persisted value visitor, extracting persisted managed objects.
 * 
 * @author <a href="mailto:emuckenh@redhat.com">Emanuel Muckenhuber</a>
 * @version $Revision$
 */
public interface PersistedValueVisitor
{

   /**
    * Get the meta type of the property.
    * 
    * @return the metaType.
    */
   MetaType getMetaType();
   
   /**
    * Get the persisted managed objects of this node.
    * 
    * @return the persisted managed objects.
    */
   Collection<PersistedManagedObject> getManagedObjects();
   
   /**
    * Add a persisted managed object.
    * 
    * @param mo the persisted managed object.
    */
   void addPersistedManagedObject(PersistedManagedObject mo);

   /**
    * Visit a persisted value.
    * 
    * @param the value
    */
   void visit(PersistedValue value);
 
}


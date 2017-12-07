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

import static org.jboss.system.server.profileservice.persistence.PersistenceConstants.MANAGED_OBJECT_ELEMENT_NAME;

import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlElement;

import org.w3c.dom.Element;

/**
 * The persisted generic value.
 * 
 * @author <a href="mailto:emuckenh@redhat.com">Emanuel Muckenhuber</a>
 * @version $Revision: 89042 $
 */
public class PersistedGenericValue extends AbstractPersisitedValue implements PersistedValue
{
   
   /** A managed-object. */
   private PersistedManagedObject managedObject;
   
   /** A generic element. */
   private Element generic;
   
   @XmlElement(name = MANAGED_OBJECT_ELEMENT_NAME, type = PersistedManagedObject.class)
   public PersistedManagedObject getManagedObject()
   {
      return managedObject;
   }
   
   public void setManagedObject(PersistedManagedObject managedObject)
   {
      this.managedObject = managedObject;
   }
   
   @XmlAnyElement
   public Element getGeneric()
   {
      return generic;
   }
   
   public void setGeneric(Element generic)
   {
      this.generic = generic;
   }
   
   protected void toString(StringBuilder builder)
   {
      if(getManagedObject() != null)
         builder.append(", managed-object = ").append(getManagedObject());
      if(getGeneric() != null)
         builder.append(", generic-value = ").append(getGeneric());
   }
   
   @Override
   public void visit(PersistedValueVisitor visitor)
   {
      if(this.managedObject != null)
         visitor.addPersistedManagedObject(this.managedObject);
   }
   
}

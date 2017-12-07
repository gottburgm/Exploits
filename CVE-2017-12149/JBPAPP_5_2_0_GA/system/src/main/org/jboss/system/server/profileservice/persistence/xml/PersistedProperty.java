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

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlType;

/**
 * The persisted ManagedProperty.
 * 
 * @author <a href="mailto:emuckenh@redhat.com">Emanuel Muckenhuber</a>
 * @version $Revision: 85730 $
 */
@XmlType(name = "persited-propertyType")
public class PersistedProperty extends AbstractElement
{
   
   /** The value */
   private PersistedValue value;
   
   public PersistedProperty()
   {
      //
   }
   
   public PersistedProperty(String name)
   {
      super(name);
   }
   
   public PersistedProperty(String name, String className)
   {
      super(name, className);
   }
   
   @XmlElements( value = {
         @XmlElement(name = "null", type = NullValue.class),
         @XmlElement(name = "simple", type = PersistedSimpleValue.class),
         @XmlElement(name = "enum", type = PersistedEnumValue.class),
         @XmlElement(name = "composite", type = PersistedCompositeValue.class),
         @XmlElement(name = "generic", type = PersistedGenericValue.class),
         @XmlElement(name = "collection", type = PersistedCollectionValue.class),
         @XmlElement(name = "properties", type = PersistedPropertiesValue.class),
         @XmlElement(name = "table", type = PersistedTableValue.class),
         @XmlElement(name = "array", type = PersistedArrayValue.class)
   })
   public PersistedValue getValue()
   {
      return value;
   }
   
   public void setValue(PersistedValue value)
   {
      this.value = value;
   }
   
   protected void toString(StringBuilder builder)
   {
      builder.append(", value = ").append(getValue());
   }
   
}

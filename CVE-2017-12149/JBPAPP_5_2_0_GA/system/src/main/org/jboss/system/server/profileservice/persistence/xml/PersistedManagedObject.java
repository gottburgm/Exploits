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

import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlNsForm;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.jboss.system.server.profileservice.persistence.PersistenceConstants;
import org.jboss.xb.annotations.JBossXmlSchema;

/**
 * A persisted xml representation of a ManagedObject.
 * 
 * @author <a href="mailto:emuckenh@redhat.com">Emanuel Muckenhuber</a>
 * @version $Revision: 88716 $
 */
@JBossXmlSchema(namespace = PersistenceConstants.COMPONENT_NAMESPACE_1_0, elementFormDefault = XmlNsForm.QUALIFIED)
@XmlRootElement(name = MANAGED_OBJECT_ELEMENT_NAME)
@XmlType(propOrder = {"originalName", "templateName", "properties"})
public class PersistedManagedObject extends AbstractElement
{
   
   /** The original name. */
   private String originalName;
   
   /** The template name. */
   private String templateName;
   
   /** The properties */
   private List<PersistedProperty> properties;
   
   /** The modification info. */
   // TODO this should be in PersistedComponent
   private ModificationInfo modificationInfo;
   
   public PersistedManagedObject()
   {
      //
   }
   
   public PersistedManagedObject(String name)
   {
      super(name);
   }
   
   public PersistedManagedObject(String name, String className)
   {
      super(name, className);
   }
   
   @XmlAttribute(name = "template-name")
   public String getTemplateName()
   {
      return templateName;
   }
   
   public void setTemplateName(String templateName)
   {
      this.templateName = templateName;
   }
   
   @XmlAttribute(name = "original-name")
   public String getOriginalName()
   {
      return originalName;
   }
   
   public void setOriginalName(String originalName)
   {
      this.originalName = originalName;
   }
   
   @XmlAttribute(name = "modification")
   public ModificationInfo getModificationInfo()
   {
      return this.modificationInfo;
   }
   
   public void setModificationInfo(ModificationInfo info)
   {
      this.modificationInfo = info;
   }
   
   @XmlElementWrapper(name="properties")
   @XmlElement(name = "property")
   public List<PersistedProperty> getProperties()
   {
      return this.properties;
   }
   
   public void setProperties(List<PersistedProperty> properties)
   {
      this.properties = properties;
   }
   
   protected void toString(StringBuilder builder)
   {
      builder.append(", template-name = ").append(getTemplateName());
   }

}

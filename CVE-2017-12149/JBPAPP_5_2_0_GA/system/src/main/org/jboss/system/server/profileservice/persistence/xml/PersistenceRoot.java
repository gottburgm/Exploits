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
package org.jboss.system.server.profileservice.persistence.xml;

import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlNsForm;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.jboss.system.server.profileservice.persistence.PersistenceConstants;
import org.jboss.xb.annotations.JBossXmlSchema;

/**
 * The attachment persistence xml root.
 * 
 * @author <a href="mailto:emuckenh@redhat.com">Emanuel Muckenhuber</a>
 * @version $Revision$
 */
@JBossXmlSchema(namespace = PersistenceConstants.COMPONENT_NAMESPACE_1_0,
      elementFormDefault = XmlNsForm.QUALIFIED)
@XmlRootElement(name = "root", namespace = PersistenceConstants.COMPONENT_NAMESPACE_1_0)
@XmlType(propOrder = {"components"})
public class PersistenceRoot
{

   /** The name */
   private String name;
   
   /** The attachment class name. */
   private String className;
   
   /** The components. */
   private List<PersistedComponent> components;
   
   @XmlAttribute(name = "name")
   public String getName()
   {
      return name;
   }
   
   public void setName(String name)
   {
      this.name = name;
   }
   
   @XmlAttribute(name = "class-name")
   public String getClassName()
   {
      return className;
   }
   
   public void setClassName(String className)
   {
      this.className = className;
   }
   
   @XmlElement(name = "component")
   public List<PersistedComponent> getComponents()
   {
      return components;
   }
   
   public void setComponents(List<PersistedComponent> components)
   {
      this.components = components;
   }
   
}


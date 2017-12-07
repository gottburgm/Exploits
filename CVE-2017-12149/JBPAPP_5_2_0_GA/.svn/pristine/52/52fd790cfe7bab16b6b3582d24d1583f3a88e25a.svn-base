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
package org.jboss.hibernate.deployers.metadata;

import java.io.Serializable;
import java.util.Set;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import org.jboss.beans.metadata.spi.BeanMetaData;
import org.jboss.beans.metadata.spi.builder.BeanMetaDataBuilder;
import org.jboss.hibernate.jmx.Hibernate;
import org.jboss.util.id.GUID;
import org.jboss.virtual.VirtualFile;

/**
 * Hibernate session factory metadata.
 * 
 * @author <a href="mailto:ales.justin@jboss.com">Ales Justin</a>
 */
@XmlRootElement(name="session-factory")
@XmlType(name="sessionFactoryType", propOrder={"properties", "depends"})
public class SessionFactoryMetaData implements Serializable
{
   private static final long serialVersionUID = 1;

   private String name;
   private String bean;
   private Set<BaseNamedElement> properties;
   private Set<BaseElement> depends;

   public SessionFactoryMetaData()
   {
   }

   @XmlTransient
   protected String getBeanName()
   {
      return bean != null ? bean : GUID.asString() + "$Hibernate";
   }

   @XmlTransient
   public BeanMetaData getBeanMetaData()
   {
      return getBeanMetaData(null);
   }

   /**
    * Create the BeanMetaData for Hibernate bean.
    *
    * @param root the root to scan from
    * @return bean meta data
    */
   public BeanMetaData getBeanMetaData(VirtualFile root)
   {
      BeanMetaDataBuilder builder = BeanMetaDataBuilder.createBuilder(getBeanName(), Hibernate.class.getName());
      if (root != null)
         builder.addConstructorParameter(VirtualFile.class.getName(), root);
      builder.addPropertyMetaData("sessionFactoryName", getName());
      if (properties != null && properties.isEmpty() == false)
      {
         builder.addPropertyMetaData("configurationElements", properties);
      }
      if (depends != null && depends.isEmpty() == false)
      {
         for (BaseElement bne : depends)
            builder.addDependency(bne.getValue());
      }
      return builder.getBeanMetaData();
   }

   public String getName()
   {
      return name;
   }

   @XmlAttribute(required = true)
   public void setName(String name)
   {
      this.name = name;
   }

   public String getBean()
   {
      return bean;
   }

   @XmlAttribute
   public void setBean(String bean)
   {
      this.bean = bean;
   }

   public Set<BaseNamedElement> getProperties()
   {
      return properties;
   }

   @XmlElement(name="property")
   public void setProperties(Set<BaseNamedElement> properties)
   {
      this.properties = properties;
   }

   public Set<BaseElement> getDepends()
   {
      return depends;
   }

   @XmlElement(name="depends")
   public void setDepends(Set<BaseElement> depends)
   {
      this.depends = depends;
   }
}
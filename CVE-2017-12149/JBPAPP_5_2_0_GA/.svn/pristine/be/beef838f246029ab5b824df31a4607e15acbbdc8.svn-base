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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlNsForm;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlTransient;

import org.jboss.beans.metadata.spi.BeanMetaData;
import org.jboss.beans.metadata.spi.BeanMetaDataFactory;
import org.jboss.xb.annotations.JBossXmlSchema;

/**
 * Hibernate metadata.
 * 
 * @author <a href="mailto:ales.justin@jboss.com">Ales Justin</a>
 */
@JBossXmlSchema(namespace="urn:jboss:hibernate-deployer:1.0", elementFormDefault=XmlNsForm.QUALIFIED, replacePropertyRefs=false)
@XmlRootElement(name="hibernate-configuration")
@XmlType(name="hibernateConfigurationType", propOrder={"sessionFactories"})
public class HibernateMetaData implements Serializable, BeanMetaDataFactory
{
   private static final long serialVersionUID = 2;

   private List<SessionFactoryMetaData> sessionFactories;

   public List<SessionFactoryMetaData> getSessionFactories()
   {
      return sessionFactories;
   }

   @XmlElement(name = "session-factory")
   public void setSessionFactories(List<SessionFactoryMetaData> sessionFactories)
   {
      this.sessionFactories = sessionFactories;
   }

   @XmlTransient
   public List<BeanMetaData> getBeans()
   {
      if (sessionFactories != null && sessionFactories.isEmpty() == false)
      {
         List<BeanMetaData> bmds = new ArrayList<BeanMetaData>();
         for (SessionFactoryMetaData sfmd : sessionFactories)
            bmds.add(sfmd.getBeanMetaData());
         return bmds;
      }
      else
         return Collections.emptyList();
   }
}


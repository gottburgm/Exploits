/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.resource.metadata.mcf;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.jboss.managed.api.annotation.ManagementProperty;
import org.jboss.managed.api.annotation.ManagementPropertyFactory;
import org.jboss.resource.deployers.management.ConnectionFactoryProperty;

/**
 * Provide a common base for non xa data source deployment meta data.
 * 
 * TODO: update to AS 5
 * See also http://docs.jboss.org/jbossas/jboss4guide/r2/html/ch7.chapt.html#ch7.jdbc.sect
 *
 * @author <a href="mailto:carlo.dewolf@jboss.com">Carlo de Wolf</a>
 * @author Scott.Stark@jboss.org
 * @version $Revision: 86409 $
 */
@XmlAccessorType(XmlAccessType.FIELD)
public abstract class NonXADataSourceDeploymentMetaData extends DataSourceDeploymentMetaData
{
   @XmlElement(name="driver-class", required=true)
   @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
   private String driverClass;
   
   @XmlElement(name="connection-url", required=true)
   @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
   private String connectionUrl;
   
   @XmlElement(name="connection-property")
   private List<DataSourceConnectionPropertyMetaData> connectionProperties = new ArrayList<DataSourceConnectionPropertyMetaData>();

   @ManagementProperty(name="connection-url",
         description="The DataSource connection URL",
         mandatory=true, includeInTemplate=true)
   public String getConnectionUrl()
   {
      return connectionUrl;
   }

   public void setConnectionUrl(String connectionUrl)
   {
      this.connectionUrl = connectionUrl;
   }

   @ManagementProperty(name="driver-class",
         description="The DataSource connection driver class name",
         mandatory=true, includeInTemplate=true)
   public String getDriverClass()
   {
      return driverClass;
   }

   public void setDriverClass(String driverClass)
   {
      this.driverClass = driverClass;
   }
   
   @ManagementProperty(name="connection-properties",
         description="The DataSource connection properties",
         managed=true, includeInTemplate=true)
   @ManagementPropertyFactory(ConnectionFactoryProperty.class)
   public List<DataSourceConnectionPropertyMetaData> getDataSourceConnectionProperties()
   {
      return this.connectionProperties;      
   }

   public void setDataSourceConnectionProperties(List<DataSourceConnectionPropertyMetaData> connectionProperties)
   {
      this.connectionProperties = connectionProperties;
   }
   
   @Override
   @ManagementProperty(name="config-property",
         description="The connection factory property info",
         managed=true, readOnly = true)
   @ManagementPropertyFactory(ConnectionFactoryProperty.class)
   public List<ManagedConnectionFactoryPropertyMetaData> getManagedConnectionFactoryProperties()
   {
      
      List<ManagedConnectionFactoryPropertyMetaData> properties = super.getManagedConnectionFactoryProperties();
      ManagedConnectionFactoryPropertyMetaData property = null;
      
      if(getConnectionUrl() != null)
      {
         property = new ManagedConnectionFactoryPropertyMetaData();
         property.setName("ConnectionURL");
         property.setValue(getConnectionUrl());
         properties.add(property);
         
      }
      
      if(getDriverClass() != null)
      {
         property = new ManagedConnectionFactoryPropertyMetaData();
         property.setName("DriverClass");
         property.setValue(getDriverClass());
         properties.add(property);         
      }
      
      StringBuffer propBuff = new StringBuffer();
      List<DataSourceConnectionPropertyMetaData> dsProps = getDataSourceConnectionProperties();

      if (dsProps != null)
      {
         for (DataSourceConnectionPropertyMetaData prop : dsProps)
         {
            propBuff.append(prop.getName() + "=" + prop.getValue() + "\n");      
         }
      }
      
      property = new ManagedConnectionFactoryPropertyMetaData();
      property.setName("ConnectionProperties");
      property.setValue(propBuff.toString());
      properties.add(property);

      return properties;
      
   }
}

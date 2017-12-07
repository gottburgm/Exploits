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
package org.jboss.resource.metadata.repository;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.resource.spi.ActivationSpec;

import org.jboss.aop.microcontainer.aspects.jmx.JMX;
import org.jboss.logging.Logger;
import org.jboss.resource.deployment.AdminObject;
import org.jboss.resource.metadata.ConnectorMetaData;
import org.jboss.resource.metadata.mcf.ManagedConnectionFactoryDeploymentGroup;
import org.jboss.resource.metadata.mcf.ManagedConnectionFactoryDeploymentMetaData;


/**
 * A JCAMetaDataRepository.
 * 
 * @author <a href="weston.price@jboss.org">Weston Price</a>
 * @version $Revision: 85945 $
 */
@JMX(exposedInterface=JCAMetaDataRepository.class,name="jboss.jca:service=JCAMetaDataRepository,name=DefaultJCAMetaDataRepository")
public class DefaultJCAMetaDataRepository implements JCAMetaDataRepository, Serializable
{
   private static final Logger log = Logger.getLogger(DefaultJCAMetaDataRepository.class);

   /** The serialVersionUID */
   private static final long serialVersionUID = 244233303934974673L;
   
   private static final String DEFAULT_FORMATTER = DefaultJCAEntryFormatter.class.getName();
      
   private String formatterClassName;
   private JCAMetaDataEntryFormatter formatter;
   
   private Map<JCAConnectorMetaDataKey, JCAConnectorMetaDataEntry> connectors = new ConcurrentHashMap<JCAConnectorMetaDataKey, JCAConnectorMetaDataEntry>();
      
   public void setFormatterClassName(String formatterClassName)
   {
      if(formatterClassName != null)
      {
         if(this.formatterClassName != null && !this.formatterClassName.equals(formatterClassName))
         {
            loadFormatter(formatterClassName);
            this.formatterClassName = formatter.getClass().getName();
         }
            
      }else
      {
         this.formatterClassName = "org.jboss.resource.metadata.repository.DefaultJCAEntryFormatter";
         loadFormatter(this.formatterClassName);
      }   
   }
   
   public String getFormatterClassName()
   {
      return this.formatterClassName;
      
   }
   
   public void create() throws Exception
   {
      if(formatter == null && formatterClassName != null)
      {
         loadFormatter(formatterClassName);
         
      }else if(formatter == null && formatterClassName == null)
      {
         loadFormatter(DEFAULT_FORMATTER);               
      }
      
   }
   
   public void addConnectorMetaData(String name, ConnectorMetaData cmd)
   {
      JCAConnectorMetaDataKey key = new JCAConnectorMetaDataKey(name);
      key.setName(name);

      JCAConnectorMetaDataEntry entry = connectors.get(key);
      
      if(entry != null)
      {            
         entry.setConnectorMetaData(cmd);                     
         log.debug("Updated ConnectorMetaData for: "+name);
      }
      else
      {
         entry = new JCAConnectorMetaDataEntry();
         entry.setConnectorMetaData(cmd);
         log.debug("Added ConnectorMetaData for: "+name);
         connectors.put(key, entry);
      }
      
   }

   public ConnectorMetaData getConnectorMetaData(String name)
   {
      JCAConnectorMetaDataKey entry = new JCAConnectorMetaDataKey(name);
      entry.setName(name);
      ConnectorMetaData md = getConnectorMetaData(entry);      
      return md;
   }

   private ConnectorMetaData getConnectorMetaData(JCAConnectorMetaDataKey key)
   {
      JCAConnectorMetaDataEntry entry = connectors.get(key);
      return entry.getConnectorMetaData();      
   }
   
   public void addActivationSpec(String rarName, ActivationSpec spec)
   {
      
      JCAConnectorMetaDataKey key = new JCAConnectorMetaDataKey(rarName);
      addDeployment(key, JCADeploymentType.ACTIVATION_SPEC, spec);
            
   }
   
   public void addAdminObject(String rarName, AdminObject adminObject)
   {
      JCAConnectorMetaDataKey key = new JCAConnectorMetaDataKey(rarName);
      addDeployment(key, JCADeploymentType.ADMIN_OBJECT, adminObject);
      
   }
   
   public void addManagedConnectionFactoryDeploymentGroup(ManagedConnectionFactoryDeploymentGroup group)
   {
      List<ManagedConnectionFactoryDeploymentMetaData> mcf = group.getDeployments();
      
      for (ManagedConnectionFactoryDeploymentMetaData deployment : mcf)
      {
         JCAConnectorMetaDataKey key = new JCAConnectorMetaDataKey(deployment.getRarName());
         addDeployment(key, JCADeploymentType.MCF, deployment);
      }
      
   }
   
   @SuppressWarnings("unchecked")
   public Object listDeploymentsForConnector(String rarName)
   {
      Object results = null;
      JCAConnectorMetaDataKey key = new JCAConnectorMetaDataKey(rarName);      
      JCAConnectorMetaDataEntry entry = connectors.get(key);
      
      if(entry != null)
      {
         List<JCADeploymentMetaDataEntry> deployments = entry.getDeployments();
         results = formatter.formatEntries(deployments);
         
      }
      else
      {
         results = "No entries for ConnectorMetaData " + rarName;
      }
      
      return results;
   }
   
   
   private void addDeployment(JCAConnectorMetaDataKey key, JCADeploymentType deploymentType, Object deployment)
   {
      
      JCAConnectorMetaDataEntry entry = connectors.get(key);
      
      if(entry == null)
      {
         entry = new JCAConnectorMetaDataEntry();
         entry.addDeployment(deploymentType,deployment);
         
         connectors.put(key, entry);         
      }
      
      else
      {
         entry.addDeployment(deploymentType, deployment);         
      }
   }
   
   public int getActivationSpecCount()
   {
      return getCount(JCADeploymentType.ACTIVATION_SPEC);
   }
   
   public int getAdminObjectCount()
   {
      return getCount(JCADeploymentType.ADMIN_OBJECT);
   }

   public int getConnectorMetaDataCount()
   {
      int count = 0;
      
      synchronized (connectors)
      {
         for(Iterator<JCAConnectorMetaDataEntry> iter = connectors.values().iterator(); iter.hasNext();)
         {
            JCAConnectorMetaDataEntry entry = iter.next();
            
            if(entry.getConnectorMetaData() != null)
            {
               count++;               
            }
            
         }
      }
      
      return count;
   }

   public int getManagedConnectionFactoryCount()
   {
      return getCount(JCADeploymentType.MCF);      
   }

   private int getCount(JCADeploymentType deploymentType)
   {
      int count = 0;
      
      synchronized (connectors)
      {
         for(Iterator<JCAConnectorMetaDataEntry> iter = connectors.values().iterator(); iter.hasNext();)
         {
            JCAConnectorMetaDataEntry entry = iter.next();
            
            List<JCADeploymentMetaDataEntry> deployments = entry.getDeployments();
            
            for (JCADeploymentMetaDataEntry deployment : deployments)
            {
               if(deployment.getDeploymentType().equals(deploymentType))
               {
                  count++;
               }
            }            
         }
      }
      
      return count;      
   }
   
   private void loadFormatter(String name)
   {
      
      if(name != null)
      {  
         try
         {
            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            Class clz = cl.loadClass(formatterClassName);
            formatter = (JCAMetaDataEntryFormatter)clz.newInstance();
            formatterClassName = name;
            
         }catch(Exception e)
         {
            log.trace("Could not formatter for classname " + formatterClassName + " using default.");
            formatter = new DefaultJCAEntryFormatter();
            formatterClassName = formatter.getClass().getName();
         }
         
      }
            
   }
}

/*
 * JBoss, Home of Professional Open Source
 * Copyright 2007, Red Hat Middleware LLC, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
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
package org.jboss.resource.deployers.management;

import java.io.File;
import java.io.FileWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;

import org.jboss.deployers.spi.management.DeploymentTemplate;
import org.jboss.deployers.spi.management.KnownComponentTypes;
import org.jboss.deployers.vfs.spi.client.VFSDeployment;
import org.jboss.logging.Logger;
import org.jboss.managed.api.DeploymentTemplateInfo;
import org.jboss.managed.api.ManagedObject;
import org.jboss.managed.api.ManagedProperty;
import org.jboss.managed.api.factory.ManagedObjectFactory;
import org.jboss.managed.plugins.factory.ManagedObjectFactoryBuilder;
import org.jboss.metatype.api.values.SimpleValue;
import org.jboss.resource.metadata.mcf.LocalDataSourceDeploymentMetaData;
import org.jboss.resource.metadata.mcf.ManagedConnectionFactoryDeploymentGroup;
import org.jboss.resource.metadata.mcf.ManagedConnectionFactoryDeploymentMetaData;
import org.jboss.resource.metadata.mcf.NoTxConnectionFactoryDeploymentMetaData;
import org.jboss.resource.metadata.mcf.NoTxDataSourceDeploymentMetaData;
import org.jboss.resource.metadata.mcf.TxConnectionFactoryDeploymentMetaData;
import org.jboss.resource.metadata.mcf.XADataSourceDeploymentMetaData;
import org.jboss.virtual.VFS;
import org.jboss.virtual.VirtualFile;

/**
 * The connection factory template implementation.
 * 
 * @author Scott.Stark@jboss.org
 * @author <a href="mailto:alex@jboss.org">Alexey Loubyansky</a>
 * @version $Revision: 86691 $
 */
public class DsDataSourceTemplate
   implements DeploymentTemplate
{
   /** The logger. */
   private static final Logger log = Logger.getLogger(DsDataSourceTemplate.class);

   /** The file suffix. */
   private static final String FILE_SUFFIX = "-ds.xml";
   
   /** The deployment template info. */
   private DeploymentTemplateInfo info;
  

   public String getDeploymentName(String deploymentBaseName)
   {
      if(deploymentBaseName == null)
         throw new IllegalArgumentException("Null base name.");
      
      if(deploymentBaseName.endsWith(FILE_SUFFIX) == false)
         deploymentBaseName = deploymentBaseName + FILE_SUFFIX;
      
      return deploymentBaseName;
   }

   public VirtualFile applyTemplate(VirtualFile root, String deploymentBaseName,
         DeploymentTemplateInfo values)
      throws Exception
   {
      return applyTemplate(values);
   }

   public VirtualFile applyTemplate(DeploymentTemplateInfo values) throws Exception
   {
      // Create a temp file
      File dsXml = File.createTempFile(getClass().getSimpleName(), FILE_SUFFIX);
      // Write template
      writeTemplate(dsXml, values);
      // Return virtual file
      return VFS.getRoot(dsXml.toURI());
   }

   public DeploymentTemplateInfo getInfo()
   {
      return info;
   }
   public void setInfo(DeploymentTemplateInfo info)
   {
      this.info = info;
   }

   public void updateTemplateDeployment(VFSDeployment ctx, DeploymentTemplateInfo values)
      throws Exception
   {
      // 
   }

   protected void writeTemplate(File dsXml, DeploymentTemplateInfo values)
      throws Exception
   {
      // Look for the CF type using the dsType ManagedProperty
      ManagedProperty dsTypeMP = values.getProperties().get("dsType");
      String cfType = null;
      if(dsTypeMP == null)
      {
         // Try casting this to a DsDataSourceTemplateInfo
         cfType = ((DsDataSourceTemplateInfo)values).getConnectionFactoryType();
      }
      else
      {
         SimpleValue dsTypeSV = (SimpleValue) dsTypeMP.getValue();
         cfType = dsTypeSV.getValue().toString();
      }

      ManagedConnectionFactoryDeploymentMetaData mcf;
      ManagedObjectFactory mof = ManagedObjectFactoryBuilder.create();
      ManagedObject cfMO;

      String rootElementName = "datasources";
      if("local-tx-datasource".equals(cfType))
      {
         mcf = new LocalDataSourceDeploymentMetaData();
         mof.setInstanceClassFactory(LocalDataSourceDeploymentMetaData.class, new LocalDSInstanceClassFactory(mof));
         cfMO = mof.initManagedObject(mcf,
            KnownComponentTypes.DataSourceTypes.LocalTx.getType().getType(),
            KnownComponentTypes.DataSourceTypes.LocalTx.getType().getSubtype());
      }
      else if("xa-datasource".equals(cfType))
      {
         mcf = new XADataSourceDeploymentMetaData();
         mof.setInstanceClassFactory(XADataSourceDeploymentMetaData.class, new XADSInstanceClassFactory(mof));
         cfMO = mof.initManagedObject(mcf,
            KnownComponentTypes.DataSourceTypes.XA.getType().getType(),
            KnownComponentTypes.DataSourceTypes.XA.getType().getSubtype());
      }
      else if("tx-connection-factory".equals(cfType))
      {
         rootElementName = "connection-factories";
         mcf = new TxConnectionFactoryDeploymentMetaData();
         mof.setInstanceClassFactory(TxConnectionFactoryDeploymentMetaData.class, new TxInstanceClassFactory(mof));
         cfMO = mof.initManagedObject(mcf,
            KnownComponentTypes.ConnectionFactoryTypes.XA.getType().getType(),
            KnownComponentTypes.ConnectionFactoryTypes.XA.getType().getSubtype());
      }
      else if("no-tx-connection-factory".equals(cfType))
      {
         rootElementName = "connection-factories";
         mcf = new NoTxConnectionFactoryDeploymentMetaData();
         mof.setInstanceClassFactory(NoTxConnectionFactoryDeploymentMetaData.class, new NoTxCFInstanceClassFactory(mof));
         cfMO = mof.initManagedObject(mcf,
            KnownComponentTypes.ConnectionFactoryTypes.NoTx.getType().getType(),
            KnownComponentTypes.ConnectionFactoryTypes.NoTx.getType().getSubtype());
      }
      else if("no-tx-datasource".equals(cfType))
      {
         mcf = new NoTxDataSourceDeploymentMetaData();
         mof.setInstanceClassFactory(NoTxDataSourceDeploymentMetaData.class, new NoTxInstanceClassFactory(mof));
         cfMO = mof.initManagedObject(mcf,
            KnownComponentTypes.DataSourceTypes.NoTx.getType().getType(),
            KnownComponentTypes.DataSourceTypes.NoTx.getType().getSubtype());
      }
      else
         throw new IllegalStateException("Unexpected value connection factory type: " + cfType);

      ManagedConnectionFactoryDeploymentGroup group = new ManagedConnectionFactoryDeploymentGroup();
      group.addManagedConnectionFactoryDeployment(mcf);
      boolean logTrace = log.isTraceEnabled();
      for(ManagedProperty tempProp : values.getProperties().values())
      {
         ManagedProperty dsProp = cfMO.getProperty(tempProp.getName());
         if(dsProp != null)
         {
            if(logTrace)
               log.trace("setting " + tempProp.getName() + "=" + tempProp.getValue());
            if(tempProp.getValue() != null)
               dsProp.setValue(tempProp.getValue());
         }
         else if(logTrace)
            log.trace("property not found: " + tempProp.getName());
      }

      Class[] classes = {ManagedConnectionFactoryDeploymentGroup.class};
      JAXBContext context = JAXBContext.newInstance(classes);      
      Marshaller marshaller = context.createMarshaller();
      marshaller.setProperty("jaxb.formatted.output", Boolean.TRUE);

      JAXBElement<ManagedConnectionFactoryDeploymentGroup> root =
            new JAXBElement<ManagedConnectionFactoryDeploymentGroup>(
                  new javax.xml.namespace.QName(rootElementName),
                  ManagedConnectionFactoryDeploymentGroup.class,
                  null, group
            );

      FileWriter fw = null;
      try
      {
         fw = new FileWriter(dsXml);
         marshaller.marshal(root, fw);
      }
      finally
      {
         if(fw != null)
            fw.close();
      }
   }
}

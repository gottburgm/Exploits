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
package org.jboss.resource.deployers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jboss.deployers.vfs.spi.deployer.MultipleObjectModelFactoryDeployer;
import org.jboss.deployers.vfs.spi.structure.VFSDeploymentUnit;
import org.jboss.resource.deployment.JBossRAObjectModelFactory;
import org.jboss.resource.deployment.ResourceAdapterObjectModelFactory;
import org.jboss.resource.metadata.ConnectorMetaData;
import org.jboss.resource.metadata.JBossRAMetaData;
import org.jboss.resource.metadata.RARDeploymentMetaData;
import org.jboss.resource.metadata.repository.JCAMetaDataRepository;
import org.jboss.virtual.VirtualFile;
import org.jboss.xb.binding.ObjectModelFactory;

/**
 * RARParserDeployer.
 *
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @author <a href="vicky.kak@jboss.com">Vicky Kak</a>
 * @author <a href="ales.justin@jboss.com">Ales Justin</a>
 * @version $Revision: 88350 $
 */
public class RARParserDeployer extends MultipleObjectModelFactoryDeployer<RARDeploymentMetaData>
{
   /** The metadata repsoitory */
   private JCAMetaDataRepository metaDataRepository;

   /** JEE specific RAR DD name */
   private static String jeeSpecRarDD = "ra.xml";

   /** Jboss specific RAR DD name */
   private static String jbossRarDD = "jboss-ra.xml";

   private static Map<String, Class<?>> getCustomMappings()
   {
      Map<String, Class<?>> mappings = new HashMap<String, Class<?>>();
      mappings.put(jeeSpecRarDD, ConnectorMetaData.class);
      mappings.put(jbossRarDD, JBossRAMetaData.class);
      return mappings;
   }

   /**
    * Create a new RARParserDeployer.
    */
   public RARParserDeployer()
   {
      super(RARDeploymentMetaData.class, getCustomMappings());
      // Enable MO creation of RARDeploymentMetaData
      setBuildManagedObject(true);
   }

   protected <U> ObjectModelFactory getObjectModelFactory(Class<U> expectedType, String fileName, U root)
   {
      if (ConnectorMetaData.class.equals(expectedType))
      {
         return new ResourceAdapterObjectModelFactory();
      }
      else if (JBossRAMetaData.class.equals(expectedType))
      {
         return new JBossRAObjectModelFactory();
      }
      else
      {
         throw new IllegalArgumentException("Cannot match arguments: expectedClass=" + expectedType + ", fileName=" + fileName);
      }
   }

   protected RARDeploymentMetaData mergeMetaData(VFSDeploymentUnit unit, Map<Class<?>, List<Object>> metadata) throws Exception
   {
      RARDeploymentMetaData deployment = new RARDeploymentMetaData();

      ConnectorMetaData cmd = getInstance(metadata, ConnectorMetaData.class);
      if (cmd != null)
         deployment.setConnectorMetaData(cmd);

      JBossRAMetaData jrmd = getInstance(metadata, JBossRAMetaData.class);
      if (jrmd != null)
         deployment.setRaXmlMetaData(jrmd);

      VFSDeploymentUnit parent = unit.getParent();
      String name = unit.getSimpleName();
      if (parent != null)
         name = parent.getSimpleName() + "#" + name;

      VirtualFile file = unit.getMetaDataFile(jeeSpecRarDD);
      deployment.getConnectorMetaData().setURL(file.toURL());
      metaDataRepository.addConnectorMetaData(name, deployment.getConnectorMetaData());

      return deployment;
   }

   public JCAMetaDataRepository getMetaDataRepository()
   {
      return metaDataRepository;
   }

   public void setMetaDataRepository(JCAMetaDataRepository metaDataRepository)
   {
      this.metaDataRepository = metaDataRepository;
   }
}

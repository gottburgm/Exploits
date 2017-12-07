/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.deployment;

import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.deployers.vfs.spi.deployer.SchemaResolverDeployer;
import org.jboss.metadata.web.jboss.JBossWebMetaData;
import org.jboss.metadata.web.spec.WebMetaData;

/**
 * An ObjectModelFactoryDeployer for translating jboss-web.xml descriptors into
 * WebMetaData instances.
 * 
 * @author Scott.Stark@jboss.org
 * @author adrian@jboss.org
 * @version $Revision: 85945 $
 */
public class JBossWebAppParsingDeployer extends SchemaResolverDeployer<JBossWebMetaData>
{
   /**
    * Create a new JBossWebAppParsingDeployer.
    */
   public JBossWebAppParsingDeployer()
   {
      super(JBossWebMetaData.class);
      setInput(WebMetaData.class);
      setName("jboss-web.xml");
   }

   /**
    * Get the virtual file path for the jboss-web descriptor in the
    * DeploymentContext.getMetaDataPath.
    * 
    * @return the current virtual file path for the web-app descriptor
    */
   public String getWebXmlPath()
   {
      return getName();
   }
   /**
    * Set the virtual file path for the jboss-web descriptor in the
    * DeploymentContext.getMetaDataLocation. The standard path is jboss-web.xml
    * to be found in the WEB-INF metdata path.
    * 
    * @param webXmlPath - new virtual file path for the web-app descriptor
    */
   public void setWebXmlPath(String webXmlPath)
   {
      setName(webXmlPath);
   }

   @Override
   protected void createMetaData(DeploymentUnit unit, String name, String suffix) throws DeploymentException
   {
      super.createMetaData(unit, name, suffix);
      // Merge the spec metadata
      WebMetaData specMetaData = unit.getAttachment(WebMetaData.class);
      JBossWebMetaData metaData = unit.getAttachment(JBossWebMetaData.class);
      if(specMetaData == null && metaData == null)
         return;

      // If there no JBossWebMetaData was created from a jboss-web.xml, create one
      if (metaData == null)
      {
         metaData = new JBossWebMetaData();
      }
      // Create a merged view
      JBossWebMetaData mergedMetaData = new JBossWebMetaData();
      mergedMetaData.merge(metaData, specMetaData);
      // Set the merged as the output
      unit.getTransientManagedObjects().addAttachment(JBossWebMetaData.class, mergedMetaData);
      // Keep the raw parsed metadata as well
      unit.addAttachment("Raw"+JBossWebMetaData.class.getName(), metaData, JBossWebMetaData.class);
   }

   /**
    * Make sure we always have a JBossWebMetaData object attached, even if there is no jboss-web.xml
    * in the deployment
    */
   @Override
   protected void createMetaData(DeploymentUnit unit, String name, String suffix, String key) throws DeploymentException
   {
      super.createMetaData(unit, name, suffix, key);
      
      WebMetaData wmd = unit.getTransientManagedObjects().getAttachment(WebMetaData.class);
      JBossWebMetaData result = unit.getTransientManagedObjects().getAttachment(getOutput());
      if (result == null && wmd != null)
      {
         result = new JBossWebMetaData();
         result.merge(null, wmd);
         unit.getTransientManagedObjects().addAttachment(key, result, getOutput());
      }
   }
}

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
package org.jboss.ejb.deployers;

import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.deployer.DeploymentStages;
import org.jboss.deployers.spi.deployer.helpers.AbstractDeployer;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.metadata.ejb.jboss.JBossMetaData;
import org.jboss.metadata.ejb.jboss.JBossMetaDataWrapper;

/**
 * Wrap the jboss.xml and standardjboss.xml JBossMetaData attachments
 * to create the combined JBossMetaData view
 * 
 * @author Scott.Stark@jboss.org
 * @version $Revision: 85945 $
 */
public class StandardJBossMetaDataDeployer extends AbstractDeployer
{
   public static final String RAW_ATTACHMENT_NAME = "raw."+JBossMetaData.class.getName();
   /**
    * Create a new StandardJBossMetaDataDeployer.
    */
   public StandardJBossMetaDataDeployer()
   {
      setStage(DeploymentStages.POST_CLASSLOADER);
      // jboss.xml meta data
      addInput(JBossMetaData.class);
      // The standardjboss.xml meta data
      addInput("standardjboss.xml");
      // Wrapper should come after any merged view
      addInput(MergedJBossMetaDataDeployer.EJB_MERGED_ATTACHMENT_NAME);
      // 
      setOutput(JBossMetaData.class);
      addOutput(RAW_ATTACHMENT_NAME);
   }

   /**
    * Override to wrap the jboss.xml metadata with a wrapper that delegates to
    * the standardjboss.xml metadata.
    */
   public void deploy(DeploymentUnit unit)
      throws DeploymentException
   {
      // Get the jboss.xml attachment
      JBossMetaData metaData = unit.getAttachment(JBossMetaData.class);
      // Get the standardjboss.xml attachment
      JBossMetaData stdMetaData = unit.getAttachment("standardjboss.xml", JBossMetaData.class);
      if(metaData == null || stdMetaData == null)
         return;

      JBossMetaDataWrapper wrapper = new JBossMetaDataWrapper(metaData, stdMetaData);
      // Set the wrapper as the output
      unit.getTransientManagedObjects().addAttachment(JBossMetaData.class, wrapper);
      // Keep the raw parsed metadata as well
      unit.addAttachment(RAW_ATTACHMENT_NAME, metaData, JBossMetaData.class);
   }
}

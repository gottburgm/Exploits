/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.ejb3.deployers;

import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.deployer.DeploymentStages;
import org.jboss.deployers.spi.deployer.helpers.AbstractDeployer;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.deployment.AnnotationMetaDataDeployer;
import org.jboss.metadata.client.jboss.JBossClientMetaData;
import org.jboss.metadata.client.spec.AnnotationMergedView;
import org.jboss.metadata.client.spec.ApplicationClient5MetaData;
import org.jboss.metadata.client.spec.ApplicationClientMetaData;


/**
 * A deployer that merges annotation metadata, xml metadata, and jboss metadata
 * into a merged JBossClientMetaData.
 * 
 * @author Scott.Stark@jboss.org
 * @version $Revision: 85945 $
 */
public class MergedJBossClientMetaDataDeployer extends AbstractDeployer
{
   public static final String CLIENT_MERGED_ATTACHMENT_NAME = "merged."+JBossClientMetaData.class.getName();

   /**
    * Create a new JBossEjbParsingDeployer.
    */
   public MergedJBossClientMetaDataDeployer()
   {
      setStage(DeploymentStages.POST_CLASSLOADER);
      // application.xml metadata
      addInput(ApplicationClientMetaData.class);
      // jboss-client.xml metadata
      addInput(JBossClientMetaData.class);
      // annotated metadata view
      addInput(AnnotationMetaDataDeployer.CLIENT_ANNOTATED_ATTACHMENT_NAME);
      // Output is the merge JBossMetaData view
      setOutput(JBossClientMetaData.class);
      // Specify a separate merged name to distiguish this output from JBossClientMetaData
      addOutput(CLIENT_MERGED_ATTACHMENT_NAME);
   }

   public void deploy(DeploymentUnit unit) throws DeploymentException
   {
      ApplicationClientMetaData specMetaData = unit.getAttachment(ApplicationClientMetaData.class);
      JBossClientMetaData metaData = unit.getAttachment(JBossClientMetaData.class);
      // Check for an annotated view
      String key = AnnotationMetaDataDeployer.CLIENT_ANNOTATED_ATTACHMENT_NAME;
      ApplicationClient5MetaData annotatedMetaData = unit.getAttachment(key, ApplicationClient5MetaData.class);
      if(specMetaData == null && metaData == null && annotatedMetaData == null)
         return;

      if(annotatedMetaData != null)
      {
         if(specMetaData != null)
         {
            ApplicationClient5MetaData specMerged = new ApplicationClient5MetaData();
            AnnotationMergedView.merge(specMerged, specMetaData, annotatedMetaData);
            specMetaData = specMerged;
         }
         else
            specMetaData = annotatedMetaData;
      }

      // Create a merged view
      JBossClientMetaData mergedMetaData = new JBossClientMetaData();
      mergedMetaData.merge(metaData, specMetaData, false);

      // Output the merged JBossMetaData
      unit.getTransientManagedObjects().addAttachment(JBossClientMetaData.class, mergedMetaData);
      unit.addAttachment(CLIENT_MERGED_ATTACHMENT_NAME, mergedMetaData, JBossClientMetaData.class);
   }
}

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
package org.jboss.web.deployers;

import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.deployer.DeploymentStages;
import org.jboss.deployers.spi.deployer.helpers.AbstractDeployer;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.deployment.AnnotationMetaDataDeployer;
import org.jboss.metadata.ear.jboss.JBossAppMetaData;
import org.jboss.metadata.web.spec.AnnotationMergedView;
import org.jboss.metadata.web.jboss.JBossWebMetaData;
import org.jboss.metadata.web.spec.Web25MetaData;
import org.jboss.metadata.web.spec.WebMetaData;
import org.jboss.metadata.javaee.spec.SecurityRolesMetaData;

/**
 * A deployer that merges annotation metadata, xml metadata, and jboss metadata
 * into a merged JBossWebMetaData. It also incorporates ear level overrides from
 * the top level JBossAppMetaData attachment.
 * 
 * @author Scott.Stark@jboss.org
 * @author adrian@jboss.org
 * @version $Revision: 85945 $
 */
public class MergedJBossWebMetaDataDeployer extends AbstractDeployer
{
   public static final String WEB_MERGED_ATTACHMENT_NAME = "merged."+JBossWebMetaData.class.getName();

   /**
    * Create a new MergedJBossWebMetaDataDeployer.
    */
   public MergedJBossWebMetaDataDeployer()
   {
      setStage(DeploymentStages.POST_CLASSLOADER);
      // web.xml metadata
      addInput(WebMetaData.class);
      // jboss.xml metadata
      addInput(JBossWebMetaData.class);
      // annotated metadata view
      addInput(AnnotationMetaDataDeployer.WEB_ANNOTATED_ATTACHMENT_NAME);
      // Output is the merge JBossWebMetaData view
      setOutput(JBossWebMetaData.class);
      // 
      addOutput(WEB_MERGED_ATTACHMENT_NAME);
   }

   public void deploy(DeploymentUnit unit) throws DeploymentException
   {
      WebMetaData specMetaData = unit.getAttachment(WebMetaData.class);
      JBossWebMetaData metaData = unit.getAttachment(JBossWebMetaData.class);
      if(specMetaData == null && metaData == null)
         return;

      // Check for an annotated view
      String key = AnnotationMetaDataDeployer.WEB_ANNOTATED_ATTACHMENT_NAME;
      Web25MetaData annotatedMetaData = unit.getAttachment(key, Web25MetaData.class);
      if(annotatedMetaData != null)
      {
         if(specMetaData != null)
         {
            Web25MetaData specMerged = new Web25MetaData();
            // TODO: JBMETA-7
            AnnotationMergedView.merge(specMerged, specMetaData, annotatedMetaData);
            specMetaData = specMerged;
         }
         else
            specMetaData = annotatedMetaData;
      }

      // Create a merged view
      JBossWebMetaData mergedMetaData = new JBossWebMetaData();
      mergedMetaData.merge(metaData, specMetaData);
      // Incorporate any ear level overrides
      DeploymentUnit topUnit = unit.getTopLevel();
      if(topUnit != null && topUnit.getAttachment(JBossAppMetaData.class) != null)
      {
         JBossAppMetaData earMetaData = topUnit.getAttachment(JBossAppMetaData.class);
         // Security domain
         String securityDomain = earMetaData.getSecurityDomain();
         if(securityDomain != null && mergedMetaData.getSecurityDomain() == null)
            mergedMetaData.setSecurityDomain(securityDomain);
         //Security Roles
         SecurityRolesMetaData earSecurityRolesMetaData = earMetaData.getSecurityRoles();
         if(earSecurityRolesMetaData != null)
         {
            SecurityRolesMetaData mergedSecurityRolesMetaData = mergedMetaData.getSecurityRoles(); 
            if(mergedSecurityRolesMetaData == null)
               mergedMetaData.setSecurityRoles(earSecurityRolesMetaData);
            
            //perform a merge to rebuild the principalVersusRolesMap
            if(mergedSecurityRolesMetaData != null )
            {
                mergedSecurityRolesMetaData.merge(mergedSecurityRolesMetaData, 
                     earSecurityRolesMetaData);
            }
        }
      }

      // Output the merged JBossWebMetaData
      unit.getTransientManagedObjects().addAttachment(JBossWebMetaData.class, mergedMetaData);
   }

}

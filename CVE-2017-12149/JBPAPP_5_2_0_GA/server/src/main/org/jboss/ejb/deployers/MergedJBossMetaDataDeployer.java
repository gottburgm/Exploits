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
import org.jboss.deployment.AnnotationMetaDataDeployer;
import org.jboss.metadata.ear.jboss.JBossAppMetaData;
import org.jboss.metadata.ejb.jboss.JBoss50MetaData;
import org.jboss.metadata.ejb.jboss.JBossAssemblyDescriptorMetaData;
import org.jboss.metadata.ejb.jboss.JBossMetaData;
import org.jboss.metadata.ejb.spec.EjbJarMetaData;
import org.jboss.metadata.javaee.spec.SecurityRolesMetaData;

/**
 * A deployer that merges annotation metadata, xml metadata, and jboss metadata
 * into a merged JBossMetaData. It also incorporates ear level overrides from
 * the top level JBossAppMetaData attachment.
 * 
 * @author Scott.Stark@jboss.org
 * @author adrian@jboss.org
 * @version $Revision: 85945 $
 */
public class MergedJBossMetaDataDeployer extends AbstractDeployer
{
   public static final String EJB_MERGED_ATTACHMENT_NAME = "merged."+JBossMetaData.class.getName();

   /**
    * Create a new JBossEjbParsingDeployer.
    */
   public MergedJBossMetaDataDeployer()
   {
      setStage(DeploymentStages.POST_CLASSLOADER);
      // ejb-jar.xml metadata
      addInput(EjbJarMetaData.class);
      // jboss.xml metadata
      addInput(JBossMetaData.class);
      // annotated metadata view
      addInput(AnnotationMetaDataDeployer.EJB_ANNOTATED_ATTACHMENT_NAME);
      // Output is the merge JBossMetaData view
      setOutput(JBossMetaData.class);
      // 
      addOutput(EJB_MERGED_ATTACHMENT_NAME);
   }

   public void deploy(DeploymentUnit unit) throws DeploymentException
   {
      EjbJarMetaData ejbJarMetaData = unit.getAttachment(EjbJarMetaData.class);
      JBossMetaData metaData = unit.getAttachment(JBossMetaData.class);
      // Check for an annotated view
      String key = AnnotationMetaDataDeployer.EJB_ANNOTATED_ATTACHMENT_NAME;
      JBossMetaData annotatedMetaData = unit.getAttachment(key, JBossMetaData.class);
      if(ejbJarMetaData == null && metaData == null && annotatedMetaData == null)
         return;

      JBossMetaData specMetaData = new JBoss50MetaData();
      if(ejbJarMetaData != null)
      {
         specMetaData.merge(null, ejbJarMetaData);
         if(annotatedMetaData != null)
         {
            JBossMetaData specMerged = new JBoss50MetaData();
            specMerged.merge(specMetaData, annotatedMetaData);
            specMetaData = specMerged;
         }
      }
      else
         specMetaData = annotatedMetaData;

      
      // Create a merged view
      JBossMetaData mergedMetaData = new JBossMetaData();
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
            JBossAssemblyDescriptorMetaData jadmd = mergedMetaData.getAssemblyDescriptor();
            if( jadmd == null)
            {
               jadmd = new JBossAssemblyDescriptorMetaData();
               mergedMetaData.setAssemblyDescriptor(jadmd); 
            }
            
            SecurityRolesMetaData mergedSecurityRolesMetaData = jadmd.getSecurityRoles(); 
            if(mergedSecurityRolesMetaData == null)
               jadmd.setSecurityRoles(earSecurityRolesMetaData);
            
            //perform a merge to rebuild the principalVersusRolesMap
            if(mergedSecurityRolesMetaData != null )
            {
                mergedSecurityRolesMetaData.merge(mergedSecurityRolesMetaData, 
                     earSecurityRolesMetaData);
            }
        }
      }

      // Output the merged JBossMetaData
      unit.getTransientManagedObjects().addAttachment(JBossMetaData.class, mergedMetaData);
      unit.addAttachment(EJB_MERGED_ATTACHMENT_NAME, mergedMetaData, JBossMetaData.class);
   }

}

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
package org.jboss.deployment.security;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.security.jacc.PolicyConfiguration;
import javax.security.jacc.PolicyContextException;

import org.jboss.deployers.vfs.spi.structure.VFSDeploymentUnit;
import org.jboss.ejb.EJBPermissionMapping;
import org.jboss.metadata.ejb.jboss.JBossEnterpriseBeansMetaData;
import org.jboss.metadata.ejb.jboss.JBossMetaData;
import org.jboss.metadata.ejb.jboss.JBossEnterpriseBeanMetaData;
import org.jboss.metadata.web.jboss.JBossWebMetaData;
import org.jboss.web.WebPermissionMapping;

//$Id: JaccPolicyUtil.java 85945 2009-03-16 19:45:12Z dimitris@jboss.org $

/**
 *  Static class with common methods used for jacc deployment processing
 *  
 *  TODO: Remove this class when the MC has the util methods
 *  @author <a href="mailto:Anil.Saldhana@jboss.org">Anil Saldhana</a>
 *  @author adrian@jboss.org
 *  @since  Dec 11, 2006 
 *  @version $Revision: 85945 $
 */
public class JaccPolicyUtil
{   
   public static String IGNORE_ME_NAME = "org.jboss.deployment.security.ignoreMe";
   
   /**
    * Given a deployment unit, get all the deployments underneath
    * that are valid Jacc deployments (ejbs,wars)
    * @param unit
    * @param ignoreSuffix (ignore "xml","beans", "deployer" etc)
    * @return
    */
   public static List<String> getJaccDeployments(VFSDeploymentUnit unit, 
         Collection<String> ignoreSuffix)
   {
      ArrayList<String> list = new ArrayList<String>();
      List<VFSDeploymentUnit> children = unit.getVFSChildren(); 
      for (VFSDeploymentUnit child: children)
      {
         String childName = child.getSimpleName();
         boolean tobeIgnored = false;
         //Go through the ignore list
         if (ignoreSuffix != null)
         {
            for(String ignoreStr: ignoreSuffix)
            {
               tobeIgnored = false;
               if(childName.endsWith(ignoreStr))
               {
                  tobeIgnored = true;
                  break;
               } 
            }
         }
         //Check if it is a "jar" file, then it must be ejb deployment to consider
         if(childName.endsWith("jar")  && !tobeIgnored  
               && !isEJBDeployment(child))
            continue;
         if(!tobeIgnored)  
           list.add(childName);  
      }
      return list;
   } 
   
   //TODO:Replace with ejb3 deployment logic
   public static boolean isEJBDeployment(VFSDeploymentUnit du)
   {
      // TODO: this is temporary EJB3 logic
      Boolean ignoreMe = du.getAttachment(IGNORE_ME_NAME, Boolean.class);
      if(ignoreMe != null && ignoreMe)
         return false;
      boolean ejbxml = du.getMetaDataFile("ejb-jar.xml") != null;
      boolean jbossxml = du.getMetaDataFile("jboss.xml") != null;
      return  ejbxml || jbossxml;
   } 
   
   /**
    * Create the JACC Permissions and add to the policy configuration passed
    * @param policyConfiguration
    * @param metadata
    * @throws PolicyContextException
    */
   public static void createPermissions(PolicyConfiguration policyConfiguration, Object metadata)
   throws PolicyContextException
   {
      if(metadata == null)
         throw new IllegalArgumentException("Meta Data is null");
      if(policyConfiguration == null)
         throw new IllegalArgumentException("Policy Configuration is null");
      
      if(metadata instanceof JBossWebMetaData)
      {
         JBossWebMetaData wmd = (JBossWebMetaData)metadata;  
         WebPermissionMapping.createPermissions(wmd, policyConfiguration); 
      }
      else if(metadata instanceof JBossEnterpriseBeanMetaData)
      {
         JBossEnterpriseBeanMetaData bmd = (JBossEnterpriseBeanMetaData)metadata;  
         EJBPermissionMapping.createPermissions(bmd, policyConfiguration); 
      } 
      else if(metadata instanceof JBossMetaData)
      {
         JBossMetaData jmd = (JBossMetaData)metadata;
         JBossEnterpriseBeansMetaData beans = jmd.getEnterpriseBeans();
         for(JBossEnterpriseBeanMetaData bmd : beans)
         {
            EJBPermissionMapping.createPermissions(bmd, policyConfiguration);
         } 
      }
      else
         throw new IllegalStateException("Unknown metadata");
   }
}

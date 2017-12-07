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

import org.jboss.aop.microcontainer.aspects.jmx.JMX;
import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.deployers.vfs.spi.deployer.SchemaResolverDeployer;
import org.jboss.metadata.ear.jboss.JBossAppMetaData;
import org.jboss.metadata.ear.spec.EarMetaData;

/**
 * An ObjectModelFactoryDeployer for translating jboss-app.xml descriptors into
 * JBossAppMetaData instances.
 * 
 * @author Scott.Stark@jboss.org
 * @author Anil.Saldhana@redhat.com
 * @author adrian@jboss.org
 * @version $Revision: 85945 $
 */
@JMX(name="jboss.j2ee:service=EARDeployer", exposedInterface=JBossAppParsingDeployerMBean.class)
public class JBossAppParsingDeployer extends SchemaResolverDeployer<JBossAppMetaData> implements JBossAppParsingDeployerMBean
{
   private boolean callByValue = false;
   
   private String unauthenticatedIdentity = null;

   /**
    * Create a new JBossAppParsingDeployer.
    */
   public JBossAppParsingDeployer()
   {
      super(JBossAppMetaData.class);
      //setInput(JBoss50Aporg.jboss.metadata.ear.spec.EarMetaDatapMetaData.class);
      setName("jboss-app.xml");
   }

   /**
    * Get the virtual file path for the application descriptor in the
    * DeploymentContext.getMetaDataPath.
    * 
    * @return the current virtual file path for the application descriptor
    */
   public String getAppXmlPath()
   {
      return getName();
   }
   /**
    * Set the virtual file path for the application descriptor in the
    * DeploymentContext.getMetaDataLocation. The standard path is jboss-app.xml
    * to be found in the META-INF metdata path.
    * 
    * @param appXmlPath - new virtual file path for the application descriptor
    */
   public void setAppXmlPath(String appXmlPath)
   {
      setName(appXmlPath);
   }
   
   /**
    * @return whether ear deployments should be call by value
    */
   public boolean isCallByValue()
   {
      return callByValue;
   }
   
   /**
    * @param callByValue whether ear deployments should be call by value
    */
   public void setCallByValue(boolean callByValue)
   {
      this.callByValue = callByValue;
   }
  
   /**
    * Obtain an unauthenticated identity
    * 
    * @return the unauthenticated identity
    */
   public String getUnauthenticatedIdentity()
   {
      return unauthenticatedIdentity;
   }

   /**
    * Specify an unauthenticated identity
    * @param unauthenticatedIdentity
    */
   public void setUnauthenticatedIdentity(String unauthenticatedIdentity)
   {
      this.unauthenticatedIdentity = unauthenticatedIdentity;
   }

   
   // FIXME This should all be in a seperate deployer
   @Override
   protected void createMetaData(DeploymentUnit unit, String name, String suffix) throws DeploymentException
   {
      super.createMetaData(unit, name, suffix);
      EarMetaData specMetaData = unit.getAttachment(EarMetaData.class);
      JBossAppMetaData metaData = unit.getAttachment(JBossAppMetaData.class);
      if(specMetaData == null && metaData == null)
         return;

      // If there no JBossMetaData was created from a jboss-app.xml, create one
      if (metaData == null)
         metaData = new JBossAppMetaData();

      // Create a merged view
      JBossAppMetaData mergedMetaData = new JBossAppMetaData();
      mergedMetaData.merge(metaData, specMetaData);
      // Set the merged as the output
      unit.getTransientManagedObjects().addAttachment(JBossAppMetaData.class, mergedMetaData);
      // Keep the raw parsed metadata as well
      unit.addAttachment("Raw"+JBossAppMetaData.class.getName(), metaData, JBossAppMetaData.class);
      // Pass the ear callByValue setting
      if (isCallByValue())
         unit.addAttachment("EAR.callByValue", Boolean.TRUE, Boolean.class);
      //Pass the unauthenticated identity
      if (this.unauthenticatedIdentity != null)
         unit.addAttachment("EAR.unauthenticatedIdentity", this.unauthenticatedIdentity, String.class);
   }
}

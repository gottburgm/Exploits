/*
* JBoss, Home of Professional Open Source
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
package org.jboss.ejb.deployers;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.deployer.helpers.AbstractSimpleRealDeployer;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.kernel.spi.deployment.KernelDeployment;
import org.jboss.metadata.ejb.jboss.JBossEnterpriseBeanMetaData;
import org.jboss.metadata.ejb.jboss.JBossEnterpriseBeansMetaData;
import org.jboss.metadata.ejb.jboss.JBossMessageDrivenBeanMetaData;
import org.jboss.metadata.ejb.jboss.JBossMetaData;
import org.jboss.system.metadata.ServiceMetaData;

/**
 * CreateDestinationDeployer.
 *
 * FIXME This should be a component deployer but the ejb deployment is currently componentised
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 85945 $
 */
public class CreateDestinationDeployer extends AbstractSimpleRealDeployer<JBossMetaData>
{
   /** The factories */
   private List<CreateDestination> factories = new CopyOnWriteArrayList<CreateDestination>();
   
   /**
    * Create a new CreateDestinationDeployer.
    */
   public CreateDestinationDeployer()
   {
      super(JBossMetaData.class);
      setOutput(JBossMetaData.class);
      setOutput(ServiceMetaData.class);
      setOutput(KernelDeployment.class);
   }

   /**
    * Add a create destination
    * 
    * @param factory the factory
    * @throws IllegalArgumentException for a null factory
    */
   public void addCreateDestination(CreateDestination factory)
   {
      if (factory == null)
         throw new IllegalArgumentException("Null factory");
      factories.add(factory);
   }

   /**
    * Remove a create destination
    * 
    * @param factory the factory
    * @throws IllegalArgumentException for a null factory
    */
   public void removeCreateDestination(CreateDestination factory)
   {
      if (factory == null)
         throw new IllegalArgumentException("Null factory");
      factories.add(factory);
   }
   
   public void deploy(DeploymentUnit unit, JBossMetaData deployment) throws DeploymentException
   {
      if (factories.isEmpty())
         return;
      
      JBossEnterpriseBeansMetaData beans = deployment.getEnterpriseBeans();
      if (beans != null && beans.isEmpty() == false)
      {
         ArrayList<JBossMessageDrivenBeanMetaData> deployed = new ArrayList<JBossMessageDrivenBeanMetaData>();
         for (JBossEnterpriseBeanMetaData bean : beans)
         {
            if (bean.isMessageDriven())
            {
               try
               {
                  JBossMessageDrivenBeanMetaData messageDriven = (JBossMessageDrivenBeanMetaData) bean;
                  if (isCreateDestination(unit, messageDriven))
                  {
                     deploy(unit, messageDriven);
                     deployed.add(messageDriven);
                  }
               }
               catch (Exception e)
               {
                  if (deployed.isEmpty() == false)
                  {
                     for (JBossMessageDrivenBeanMetaData messageDriven : deployed)
                     {
                        try
                        {
                           undeploy(unit, messageDriven);
                        }
                        catch (Exception t)
                        {
                           log.warn("Error undeploying destination: " + messageDriven.getName(), t);
                        }
                     }
                  }
                  throw DeploymentException.rethrowAsDeploymentException("Error deploying destination" + bean.getName(), e);
               }
            }
         }
      }
   }

   public void undeploy(DeploymentUnit unit, JBossMetaData deployment)
   {
      if (factories.isEmpty())
         return;
      
      JBossEnterpriseBeansMetaData beans = deployment.getEnterpriseBeans();
      if (beans != null && beans.isEmpty() == false)
      {
         for (JBossEnterpriseBeanMetaData bean : beans)
         {
            if (bean.isMessageDriven())
            {
               try
               {
                  JBossMessageDrivenBeanMetaData messageDriven = (JBossMessageDrivenBeanMetaData) bean;
                  if (isCreateDestination(unit, messageDriven))
                     undeploy(unit, messageDriven);
               }
               catch (Exception e)
               {
                  log.warn("Error undeploying destination" + bean.getName(), e);
               }
            }
         }
      }
   }

   /**
    * Deploy a message driven bean
    * 
    * @param unit the deployment unit
    * @param mdb the mdb
    * @throws DeploymentException for any error
    */
   protected void deploy(DeploymentUnit unit, JBossMessageDrivenBeanMetaData mdb) throws DeploymentException
   {
      for (CreateDestination createDestination : factories)
      {
         if (createDestination.getMatcher().isMatch(unit, mdb))
         {
            Object attachment = createDestination.getFactory().create(unit, mdb);
            if (attachment != null)
            {
               unit.addAttachment(getAttachmentName(unit, mdb), attachment);
               return;
            }
         }
      }
   }

   /**
    * Undeploy a message driven bean
    * 
    * @param unit the deployment unit
    * @param mdb the mdb
    * @throws DeploymentException for any error
    */
   protected void undeploy(DeploymentUnit unit, JBossMessageDrivenBeanMetaData mdb) throws DeploymentException
   {
      unit.removeAttachment(getAttachmentName(unit, mdb));
   }
   
   /**
    * Whether we should create a destination for this MDB
    * 
    * @param unit the deployment unit
    * @param mdb the message driven metadata
    * @return true to create a destination
    * @throws DeploymentException for any error
    */
   protected boolean isCreateDestination(DeploymentUnit unit, JBossMessageDrivenBeanMetaData mdb) throws DeploymentException
   {
      return mdb.isCreateDestination();
   }

   /**
    * Get the attachment name
    * 
    * @param unit the unit
    * @param mdb the mdb
    * @return the attachment name
    */
   protected String getAttachmentName(DeploymentUnit unit, JBossMessageDrivenBeanMetaData mdb)
   {
      return mdb.getName() + "##Create_Destination";
   }
}

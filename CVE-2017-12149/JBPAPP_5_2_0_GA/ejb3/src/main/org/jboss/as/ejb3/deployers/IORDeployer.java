/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.as.ejb3.deployers;

import org.jboss.beans.metadata.api.annotations.Inject;
import org.jboss.beans.metadata.spi.BeanMetaData;
import org.jboss.beans.metadata.spi.ValueMetaData;
import org.jboss.beans.metadata.spi.builder.BeanMetaDataBuilder;
import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.deployer.helpers.AbstractSimpleRealDeployer;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.ejb3.endpoint.deployers.EJBIdentifier;
import org.jboss.ejb3.iiop.IORFactory;
import org.jboss.ejb3.session.SessionContainer;
import org.jboss.metadata.ejb.jboss.JBossEnterpriseBeanMetaData;
import org.jboss.metadata.ejb.jboss.JBossSessionBeanMetaData;

/**
 * For each session bean that has a remote EJB 2.1 view deploy an IOR into CorbaNaming.
 * 
 * @author <a href="mailto:cdewolf@redhat.com">Carlo de Wolf</a>
 * @version $Revision: $
 */
public class IORDeployer extends AbstractSimpleRealDeployer<JBossEnterpriseBeanMetaData>
{
   private EJBIdentifier ejbIdentifier;

   public IORDeployer()
   {
      super(JBossEnterpriseBeanMetaData.class);
      
      setComponentsOnly(true);
      addOutput(BeanMetaData.class);
   }

   @Override
   public void deploy(DeploymentUnit unit, JBossEnterpriseBeanMetaData bmd) throws DeploymentException
   {
      if(!(bmd instanceof JBossSessionBeanMetaData))
         return;
      
      JBossSessionBeanMetaData sbmd = (JBossSessionBeanMetaData) bmd;
      
      String homeInterface = sbmd.getHome();
      if(homeInterface == null)
         return;
      
      String ejbName = sbmd.getEjbName();
      // TODO: how do we know it's in the parent?
      String ejbContainerBeanName = ejbIdentifier.identifyEJB(unit.getParent(), ejbName);
      String name = ejbContainerBeanName + "_IORFactory";
      BeanMetaDataBuilder builder = BeanMetaDataBuilder.createBuilder(name, IORFactory.class.getName());
      ValueMetaData value = builder.createInject(ejbContainerBeanName);
      builder.addConstructorParameter(SessionContainer.class.getName(), value);
      
      // just use the the parent and don't ask though questions
      unit.getParent().addAttachment(BeanMetaData.class.getName() + "." + name, builder.getBeanMetaData(), BeanMetaData.class);
   }
   
   @Inject
   public void setEJBIdentifier(EJBIdentifier ejbIdentifier)
   {
      this.ejbIdentifier = ejbIdentifier;
   }
}

/*
 * JBoss, Home of Professional Open Source
 * Copyright (c) 2010, JBoss Inc., and individual contributors as indicated
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
package org.jboss.as.integration.hornetq.deployers;

import org.hornetq.jms.server.config.JMSConfiguration;
import org.jboss.as.integration.hornetq.deployers.pojo.HornetQCoreDeployment;
import org.jboss.beans.metadata.spi.BeanMetaData;
import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.deployer.helpers.AbstractSimpleRealDeployer;
import org.jboss.deployers.structure.spi.DeploymentUnit;

/**
 * This deployer is called directly by the VFS Deployment Framework, as the Input object for this is a JMSConfiguration class.
 * 
 * In regular flow, {@code HornetQJMSParserDeployerr} will have parse hornetq-jms.xml configuration files and created {@code JMSConfiguration} outputs.
 * <br>
 * In an alternate flow, other deployers (e.g. TorqueBox) may instantiate a JMSConfiguration object directly and this deployer will take over after.
 * 
 * @author <a href="mailto:clebert.suconic@jboss.org">Clebert Suconic</a>
 */
public class HornetQJMSRealDeployer extends AbstractSimpleRealDeployer<JMSConfiguration>
{
   public HornetQJMSRealDeployer()
   {
      super(JMSConfiguration.class);

      // This is just to guarantee the order. The CoreDeployments should happen before
      addInput(HornetQCoreDeployment.class);

      addOutput(BeanMetaData.class);
   }
   
   @Override
   public void deploy(DeploymentUnit unit, JMSConfiguration mainConfig) throws DeploymentException
   {
      log.info("Deploying unit " + unit + " with config " + mainConfig);
      
      if (log.isTraceEnabled())
      {
         log.trace("Deploying unit " + unit + " with config " + mainConfig);
      }
      
      DeploymentFactory.getInstance().deployJMS(unit, mainConfig);
   }
   

}

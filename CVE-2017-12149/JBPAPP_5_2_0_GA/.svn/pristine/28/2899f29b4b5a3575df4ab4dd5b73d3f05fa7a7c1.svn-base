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

import org.hornetq.core.config.Configuration;
import org.hornetq.core.config.impl.ConfigurationImpl;
import org.jboss.as.integration.hornetq.deployers.pojo.HornetQCoreDeployment;
import org.jboss.beans.metadata.plugins.builder.BeanMetaDataBuilderFactory;
import org.jboss.beans.metadata.spi.BeanMetaData;
import org.jboss.beans.metadata.spi.builder.BeanMetaDataBuilder;
import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.deployer.helpers.AbstractSimpleRealDeployer;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.metadata.ejb.jboss.JBossMetaData;

/**
 * This deployer is called directly by the VFS Deployment Framework, as the Input object
 * for this is a Configuration class.
 * 
 * In regular flow, {@code HornetQConfigParserDeployer} will have parse HornetQ XML configuration files and created {@code Configuration} outputs.
 * <br>
 * In an alternate flow, other deployers (e.g. TorqueBox) may instantiate a Configuration object directly and this deployer will take over after.
 * <p>
 * The <strong>only resources deployed by this deployer</strong> are:
 * <ul>
 *   <li>core queues ({@link Configuration#getQueueConfigurations()}</li> 
 *   <li>address settings ({@link Configuration#getAddressesSettings()}</li> 
 *   <li>security roles ({@link Configuration#getSecurityRoles()}</li> 
 * </ul>
 * Other attributes of the Configuration are not deployed by these deployers.
 * 
 * @author <a href="mailto:clebert.suconic@jboss.org">Clebert Suconic</a>
 * 
 */
public class HornetQCoreConfigRealDeployer extends AbstractSimpleRealDeployer<Configuration>
{

    public HornetQCoreConfigRealDeployer()
    {
        super(Configuration.class);
        addOutput(BeanMetaData.class);
        
        // This is just to guarantee the order. JBossMetaData deployment should happen before
        addInput(JBossMetaData.class);
    }

    /** 
     * Actual deployment to HornetQ is done in {@link HornetQCoreDeployment#start()}
     */
    @Override
    public void deploy(DeploymentUnit unit, Configuration config) throws DeploymentException
    {
        if (log.isTraceEnabled())
        {
           log.trace("Deploying unit: " + unit + " with config " + config);
        }
        
        String name = config.getName();
        
        BeanMetaDataBuilder builder = BeanMetaDataBuilderFactory.createBuilder(name, HornetQCoreDeployment.class.getName());

        builder.addPropertyMetaData("jmxDomain", config.getJMXDomain());
        
        builder.addPropertyMetaData("name", name);

        builder.addPropertyMetaData("server", builder.createInject("HornetQServer"));
        
        builder.addPropertyMetaData("config", config);

        BeanMetaData bean = builder.getBeanMetaData();

        unit.addAttachment(name, bean);
    }

}

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

import org.hornetq.jms.server.JMSServerConfigParser;
import org.hornetq.jms.server.config.JMSConfiguration;
import org.hornetq.jms.server.impl.JMSServerConfigParserImpl;
import org.jboss.deployers.vfs.spi.deployer.AbstractVFSParsingDeployer;
import org.jboss.deployers.vfs.spi.structure.VFSDeploymentUnit;
import org.jboss.logging.Logger;
import org.jboss.virtual.VirtualFile;

import java.io.InputStream;


/**
 * This deployer will take a {@code hornetq-jms.xml} configuration file, parse it, create a {@code JMSConfiguration} object and
 * attach it to the deployment unit.
 * 
 * Another deployer, {@code HornetQJMSRealDeployer}, which takes JMSConfiguration as an input will then take over and deploy the
 * JMS resources.
 * 
 * @see HornetQJMSRealDeployer
 * 
 * @author <a href="mailto:clebert.suconic@jboss.org">Clebert Suconic</a>
 * 
 */
public class HornetQJMSParserDeployer extends AbstractVFSParsingDeployer<JMSConfiguration>
{

    private static final Logger log = Logger.getLogger(HornetQJMSParserDeployer.class);

    public static final String FILENAME = "hornetq-jms.xml";

    private final JMSServerConfigParser jmsConfigParser;

    public HornetQJMSParserDeployer()
    {
        super(JMSConfiguration.class);

        this.jmsConfigParser = new JMSServerConfigParserImpl();
        
        setSuffix(FILENAME);
    }
    
    @Override
    protected JMSConfiguration parse(VFSDeploymentUnit unit, VirtualFile file, JMSConfiguration root) throws Exception
    {

        InputStream deploymentStream = null;

        try
        {
            deploymentStream = file.openStream();
            JMSConfiguration config = jmsConfigParser.parseConfiguration(deploymentStream);
            if (log.isTraceEnabled())
            {
               log.trace("JMS config parser parsed config = " + config);
            }
            return config;
        } finally
        {
            try
            {
                deploymentStream.close();
            } catch (Exception ignored)
            {
            }
        }
    }
    
}

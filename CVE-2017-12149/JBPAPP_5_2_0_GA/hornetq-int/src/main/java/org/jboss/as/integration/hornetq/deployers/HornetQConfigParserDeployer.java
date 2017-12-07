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

import java.io.InputStream;

import org.hornetq.core.config.Configuration;
import org.hornetq.core.deployers.impl.FileConfigurationParser;
import org.jboss.deployers.vfs.spi.deployer.AbstractVFSParsingDeployer;
import org.jboss.deployers.vfs.spi.structure.VFSDeploymentUnit;
import org.jboss.logging.Logger;
import org.jboss.virtual.VirtualFile;

/**
 * This Deployer will take either {@code hornetq-configuration.xml} or {@code horneq-queues.xml}, parse it, create a Configuration object,
 * and attach the Configuration output into the deployment unit.
 * 
 * Another deployer, {@code HornetQCoreConfigRealDeployer}, which takes Configuration as an input will then take over and deploy the resources.

 * @see HornetQCoreConfigRealDeployer
 * 
 * @author <a href="mailto:clebert.suconic@jboss.org">Clebert Suconic</a>
 * 
 */
public class HornetQConfigParserDeployer extends AbstractVFSParsingDeployer<Configuration>
{

   private static final Logger log = Logger.getLogger(HornetQConfigParserDeployer.class);

   private final FileConfigurationParser parser;

   public HornetQConfigParserDeployer(final String name)
   {
      super(Configuration.class);

      this.parser = new FileConfigurationParser();
      
      this.setSuffix(name);
   }
   
   

   @Override
   protected Configuration parse(VFSDeploymentUnit unit, VirtualFile file, Configuration root) throws Exception
   {

      InputStream deploymentStream = null;

      try
      {
         deploymentStream = file.openStream();
         Configuration config = parser.parseMainConfig(deploymentStream);
        
         if (log.isTraceEnabled())
         {
            log.trace("Parser parsed Main Config = " + config);
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

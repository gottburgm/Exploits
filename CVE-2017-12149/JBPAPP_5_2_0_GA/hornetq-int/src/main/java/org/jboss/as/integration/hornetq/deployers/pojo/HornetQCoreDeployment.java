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
package org.jboss.as.integration.hornetq.deployers.pojo;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hornetq.api.core.SimpleString;
import org.hornetq.core.config.BridgeConfiguration;
import org.hornetq.core.config.Configuration;
import org.hornetq.core.config.CoreQueueConfiguration;
import org.hornetq.core.security.Role;
import org.hornetq.core.server.HornetQServer;
import org.hornetq.core.settings.impl.AddressSettings;
import org.jboss.as.integration.hornetq.deployers.HornetQJMSRealDeployer;
import org.jboss.beans.metadata.api.annotations.Start;
import org.jboss.beans.metadata.api.annotations.Stop;
import org.jboss.logging.Logger;

/**
 * 
 * Objects of this class are created by the MicroContainer through
 * BeanMetadataClass at {@link HornetQCoreRealDeployer}
 * 
 * @see HornetQJMSRealDeployer
 * */
public class HornetQCoreDeployment
{

   private static final Logger log = Logger.getLogger(HornetQCoreDeployment.class);

   private HornetQServer server;

   private String name;
   
   private String jmxDomain;

   private Configuration config;

   public Configuration getJmsConfig()
   {
      return config;
   }
	
    public String getJmxDomain() {
		return jmxDomain;
	}
	
	public void setJmxDomain(String jmxDomain) {
		this.jmxDomain = jmxDomain;
	}
   
   public void setConfig(Configuration config)
   {
      this.config = config;
   }

   public HornetQServer getServer()
   {
      return server;
   }

   public void setServer(HornetQServer server)
   {
      this.server = server;
   }

   public void setName(String name)
   {
      this.name = name;
   }

   public String getName()
   {
      return name;
   }

   /**
    * AS6 deployers will deploy HornetQ Core resources from hornetq-configuration and hornetq-queues.xml files.
    * The deployed resources are:
    * <ul>
    *   <li>core queues ({@link Configuration#getQueueConfigurations()}</li> 
    *   <li>address settings ({@link Configuration#getAddressesSettings()}</li> 
    *   <li>security roles ({@link Configuration#getSecurityRoles()}</li> 
    * </ul>
    * Other attributes of the Configuration are not deployed by these deployers.
    * 
    */
   @Start
   public void start()
   {
      if (log.isTraceEnabled())
      {
         log.trace(this.name + " is being started");
      }
      
      if (!jmxDomain.equals(server.getConfiguration().getJMXDomain()))
      {
    	  return;
      }

      for (Map.Entry<String, AddressSettings> entry : config.getAddressesSettings().entrySet())
      {
         server.getAddressSettingsRepository().addMatch(entry.getKey(), entry.getValue());
      }

      for (Map.Entry<String, Set<Role>> entry : config.getSecurityRoles().entrySet())
      {
         server.getSecurityRepository().addMatch(entry.getKey(), entry.getValue());
      }

      for (CoreQueueConfiguration coreQueue : config.getQueueConfigurations())
      {
         try
         {
            server.deployQueue(new SimpleString(coreQueue.getAddress()),
            				   new SimpleString(coreQueue.getName()),
            				   SimpleString.toSimpleString(coreQueue.getFilterString()),
            				   coreQueue.isDurable(),
            				   false);
         }
         catch (Exception e)
         {
            log.warn("Error on creating queue " + coreQueue.getName() + " address = " + coreQueue.getAddress(), e);
         }
      }

      for(BridgeConfiguration bridgeConfiguration : config.getBridgeConfigurations())
      {
         try
         {
            server.deployBridge(bridgeConfiguration);
         }
         catch (Exception e)
         {
            log.warn("Error deploying Bridge " + bridgeConfiguration.getName(), e);
         }
      }
   }

   @Stop
   public void stop()
   {
      if (log.isTraceEnabled())
      {
         log.trace(this.name + " is being stopped");
      }
      
      if (!jmxDomain.equals(server.getConfiguration().getJMXDomain()))
      {
    	  return;
      }

      for (String entry : config.getAddressesSettings().keySet())
      {
         server.getAddressSettingsRepository().removeMatch(entry);
      }

      for (String entry : config.getSecurityRoles().keySet())
      {
         server.getSecurityRepository().removeMatch(entry);
      }
      
      
      // Undeploy core queues means nothing on core queues, so no need to do anything here
   }

}

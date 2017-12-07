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

package org.jboss.test.cluster.web;

import org.jboss.cache.config.Configuration;
import org.jboss.cache.config.Configuration.CacheMode;
import org.jboss.cache.config.ConfigurationRegistry;

/**
 * Utility that analyzes the set of available cache configurations in
 * a ConfigurationRegistry and adds alternatives with different CacheMode
 * and buddy replication settings
 * 
 * @author Brian Stansberry
 */
public class TestConfigurationAdder
{
   public static final String DEFAULT_STD_SESSION_CFG = "standard-session-cache";
   public static final String DEFAULT_FIELD_SESSION_CFG = "field-granularity-session-cache";
   
   private ConfigurationRegistry configurationRegistry;
   private String standardSessionConfig = DEFAULT_STD_SESSION_CFG;
   private String fieldSessionConfig = DEFAULT_FIELD_SESSION_CFG;
   private boolean addAlternateCacheMode;
   private boolean addAlternateBuddyReplication;
   
   public void start() throws Exception
   {
      createAlternates(standardSessionConfig, configurationRegistry.getConfiguration(standardSessionConfig));      
      createAlternates(fieldSessionConfig, configurationRegistry.getConfiguration(fieldSessionConfig));      
   }

   private void createAlternates(String stdName, Configuration std) throws Exception
   {
      CacheMode altCacheMode = (std.getCacheMode() == CacheMode.REPL_ASYNC) ? CacheMode.REPL_SYNC : CacheMode.REPL_ASYNC;
      String altCacheModeTag = (altCacheMode == CacheMode.REPL_ASYNC ? "async" : "sync");
      boolean brEnabled = !std.getBuddyReplicationConfig().isEnabled();
      String altBrTag = (brEnabled ? "br-enabled" : "br-disabled");
      
      String altModeName = altCacheModeTag + "-" + stdName;
      if (configurationRegistry.getConfigurationNames().contains(altModeName) == false)
      {
         Configuration altMode = std.clone();
         altMode.setCacheMode(altCacheMode);
         altMode.setClusterName(altModeName);
         configurationRegistry.registerConfiguration(altModeName, altMode);
      }
      
      String altBrName = altBrTag + "-" + stdName;
      if (configurationRegistry.getConfigurationNames().contains(altBrName) == false)
      {
         Configuration altBr = std.clone();
         altBr.getBuddyReplicationConfig().setEnabled(brEnabled);
         altBr.setClusterName(altBrName);
         configurationRegistry.registerConfiguration(altBrName, altBr);
      }
      
      String altCombinedName = altBrTag + "-" + altCacheModeTag + "-" + stdName;
      if (configurationRegistry.getConfigurationNames().contains(altCombinedName) == false)
      {
         Configuration altCombined = std.clone();
         altCombined.setCacheMode(altCacheMode);
         altCombined.getBuddyReplicationConfig().setEnabled(brEnabled);
         altCombined.setClusterName(altCombinedName);
         configurationRegistry.registerConfiguration(altCombinedName, altCombined);
      }
   }


   public ConfigurationRegistry getConfigurationRegistry()
   {
      return configurationRegistry;
   }
   public void setConfigurationRegistry(ConfigurationRegistry configurationRegistry)
   {
      this.configurationRegistry = configurationRegistry;
   }
   public String getStandardSessionConfig()
   {
      return standardSessionConfig;
   }
   public void setStandardSessionConfig(String standardSessionConfig)
   {
      this.standardSessionConfig = standardSessionConfig;
   }
   public String getFieldSessionConfig()
   {
      return fieldSessionConfig;
   }
   public void setFieldSessionConfig(String fieldSessionConfig)
   {
      this.fieldSessionConfig = fieldSessionConfig;
   }
   public boolean isAddAlternateCacheMode()
   {
      return addAlternateCacheMode;
   }
   public void setAddAlternateCacheMode(boolean addAlternateCacheMode)
   {
      this.addAlternateCacheMode = addAlternateCacheMode;
   }
   public boolean isAddAlternateBuddyReplication()
   {
      return addAlternateBuddyReplication;
   }
   public void setAddAlternateBuddyReplication(boolean addAlternateBuddyReplication)
   {
      this.addAlternateBuddyReplication = addAlternateBuddyReplication;
   }
   
   

}

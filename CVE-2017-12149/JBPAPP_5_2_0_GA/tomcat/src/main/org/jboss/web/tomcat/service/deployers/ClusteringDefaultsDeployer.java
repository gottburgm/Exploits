/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
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

package org.jboss.web.tomcat.service.deployers;

import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.deployer.DeploymentStages;
import org.jboss.deployers.spi.deployer.helpers.AbstractDeployer;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.metadata.web.jboss.JBossWebMetaData;
import org.jboss.metadata.web.jboss.PassivationConfig;
import org.jboss.metadata.web.jboss.ReplicationConfig;
import org.jboss.metadata.web.jboss.ReplicationGranularity;
import org.jboss.metadata.web.jboss.ReplicationTrigger;
import org.jboss.metadata.web.jboss.SnapshotMode;

/**
 * Injects default clustering values into JBossWebMetaData.
 * 
 * TODO. A better approach is to use a jboss-web.xml equivalent to conf/web.xml 
 * and conf/standardjboss.xml as the source for defaults.
 * 
 * @author <a href="brian.stansberry@jboss.com">Brian Stansberry</a>
 * @version $Revision: 85945 $
 */
public class ClusteringDefaultsDeployer extends AbstractDeployer
{
   public static final int IGNORED = -1;
   
   private String cacheName;
   private String fieldGranularityCacheName;
   private SnapshotMode snapshotMode;
   private int snapshotInterval;
   private ReplicationGranularity replicationGranularity;
   private ReplicationTrigger replicationTrigger;
   private boolean replicationFieldBatchMode = true;
   private Boolean useJK;
   private int maxUnreplicatedInterval;
   private boolean useSessionPassivation;
   private int passivationMinIdleTime = IGNORED;
   private int passivationMaxIdleTime = IGNORED;
   private boolean useLocalCache = true;
   
   /**
    * Create a new CacheManagerDependencyDeployer. 
    */
   public ClusteringDefaultsDeployer()
   {
      setStage(DeploymentStages.POST_PARSE);
      setInput(JBossWebMetaData.class);
      setOutput(JBossWebMetaData.class);
   }

   public Boolean isUseJK()
   {
      return useJK;
   }

   public void setUseJK(Boolean useJK)
   {
      this.useJK = useJK;
   }

   public String getCacheName()
   {
      return cacheName;
   }

   public void setCacheName(String cacheName)
   {
      this.cacheName = cacheName;
   }

   public String getFieldGranularityCacheName()
   {
      return fieldGranularityCacheName;
   }

   public void setFieldGranularityCacheName(String fieldGranularityCacheName)
   {
      this.fieldGranularityCacheName = fieldGranularityCacheName;
   }

   public SnapshotMode getSnapshotMode()
   {
      return snapshotMode;
   }

   public void setSnapshotMode(SnapshotMode snapshotMode)
   {
      this.snapshotMode = snapshotMode;
   }

   /**
    * Get the snapshot interval.
    */
   public int getSnapshotInterval()
   {
      return snapshotInterval;
   }
   
   /**
    * Get the snapshot interval.
    */
   public void setSnapshotInterval(int snapshotInterval)
   {
      this.snapshotInterval = snapshotInterval;
   }

   public ReplicationGranularity getReplicationGranularity()
   {
      return replicationGranularity;
   }

   public void setReplicationGranularity(ReplicationGranularity granularity)
   {
      this.replicationGranularity = granularity;
   }

   public ReplicationTrigger getReplicationTrigger()
   {
      return replicationTrigger;
   }

   public void setReplicationTrigger(ReplicationTrigger trigger)
   {
      this.replicationTrigger = trigger;
   }

   public boolean isReplicationFieldBatchMode()
   {
      return replicationFieldBatchMode;
   }

   public void setReplicationFieldBatchMode(boolean fieldBatchMode)
   {
      this.replicationFieldBatchMode = fieldBatchMode;
   }

   public boolean isUseLocalCache()
   {
      return useLocalCache;
   }

   public void setUseLocalCache(boolean useLocalCache)
   {
      this.useLocalCache = useLocalCache;
   }

   public boolean isUseSessionPassivation()
   {
      return useSessionPassivation;
   }

   public void setUseSessionPassivation(boolean useSessionPassivation)
   {
      this.useSessionPassivation = useSessionPassivation;
   }

   public int getPassivationMinIdleTime()
   {
      return passivationMinIdleTime;
   }

   public void setPassivationMinIdleTime(int passivationMinIdleTime)
   {
      this.passivationMinIdleTime = passivationMinIdleTime;
   }

   public int getPassivationMaxIdleTime()
   {
      return passivationMaxIdleTime;
   }

   public void setPassivationMaxIdleTime(int passivationMaxIdleTime)
   {
      this.passivationMaxIdleTime = passivationMaxIdleTime;
   }

   public int getMaxUnreplicatedInterval()
   {
      return maxUnreplicatedInterval;
   }

   public void setMaxUnreplicatedInterval(int maxUnreplicatedInterval)
   {
      this.maxUnreplicatedInterval = maxUnreplicatedInterval;
   }

   /**
    * Injects the configured default property values into any
    * {@link JBossWebMetaData} attached to <code>unit</code> if the 
    * relevant property isn't already configured.
    */
   public void deploy(DeploymentUnit unit) throws DeploymentException
   {
      JBossWebMetaData metaData = unit.getAttachment(JBossWebMetaData.class);
      if( metaData != null && metaData.getDistributable() != null )
      {
         if (metaData.getDistributable() != null)
         {
            addReplicationConfigDefaults(metaData);
            
            addPassivationConfigDefaults(metaData);
         }
      }
   }

   /**
    * Inject default values in {@link PassivationConfig}
    * 
    * @param metaData
    */
   private void addPassivationConfigDefaults(JBossWebMetaData metaData)
   {
      PassivationConfig passCfg = metaData.getPassivationConfig();
      if (passCfg == null)
      {
         passCfg = new PassivationConfig();
         metaData.setPassivationConfig(passCfg);
      }
      
      if (passCfg.getUseSessionPassivation() == null)
         passCfg.setUseSessionPassivation(Boolean.valueOf(this.useSessionPassivation));
      if (passCfg.getPassivationMinIdleTime() == null)
         passCfg.setPassivationMinIdleTime(new Integer(this.passivationMinIdleTime));
      if (passCfg.getPassivationMinIdleTime() == null)
         passCfg.setPassivationMaxIdleTime(new Integer(this.passivationMaxIdleTime));
   }

   /**
    * Inject default values in {@link ReplicationConfig}
    * 
    * @param metaData
    */
   private void addReplicationConfigDefaults(JBossWebMetaData metaData)
   {
      ReplicationConfig repCfg = metaData.getReplicationConfig();
      if (repCfg == null)
      {
         repCfg = new ReplicationConfig();
         metaData.setReplicationConfig(repCfg);
      }
      
      if (repCfg.getUseJK() == null && useJK != null)
         repCfg.setUseJK(this.useJK);
      if (repCfg.getSnapshotMode() == null)
         repCfg.setSnapshotMode(this.snapshotMode);
      if (repCfg.getSnapshotInterval() == null)
         repCfg.setSnapshotInterval(new Integer(this.snapshotInterval));
      if (repCfg.getReplicationGranularity() == null)
         repCfg.setReplicationGranularity(this.replicationGranularity);
      if (repCfg.getReplicationTrigger() == null)
         repCfg.setReplicationTrigger(this.replicationTrigger);
      if (repCfg.getReplicationFieldBatchMode() == null)
         repCfg.setReplicationFieldBatchMode(Boolean.valueOf(this.replicationFieldBatchMode));

      if (repCfg.getCacheName() == null)
      {
         String cacheConfig = ReplicationGranularity.FIELD == repCfg.getReplicationGranularity() 
                                                           ? fieldGranularityCacheName : cacheName;
         repCfg.setCacheName(cacheConfig);
      }
      
      if (repCfg.getMaxUnreplicatedInterval() == null)
      {
         repCfg.setMaxUnreplicatedInterval(new Integer(maxUnreplicatedInterval));
      }
   }
   

}

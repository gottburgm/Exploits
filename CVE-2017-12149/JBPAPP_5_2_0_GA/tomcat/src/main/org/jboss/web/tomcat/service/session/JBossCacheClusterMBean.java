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
package org.jboss.web.tomcat.service.session;

import org.apache.catalina.Cluster;
import org.apache.catalina.LifecycleException;

/**
 * The MBean interface for the JBossCacheCluster.
 * 
 * @see org.jboss.web.tomcat.service.JBossCacheCluster
 * 
 * @author Brian Stansberry
 * @version $Revision: 85945 $
 */
public interface JBossCacheClusterMBean extends Cluster
{
   public abstract boolean isUseJK();

   public abstract void setUseJK(boolean useJK);

   public abstract boolean isUseLocalCache();

   public abstract void setUseLocalCache(boolean useLocalCache);

   public abstract String getManagerClassName();

   public abstract void setManagerClassName(String managerClassName);

   public abstract String getDefaultReplicationGranularity();

   public abstract void setDefaultReplicationGranularity(String defaultGran);

   public abstract String getDefaultReplicationTrigger();

   public abstract void setDefaultReplicationTrigger(String defaultTrigger);
   
   public boolean getDefaultReplicationFieldBatchMode();

   public void setDefaultReplicationFieldBatchMode(boolean replicationFieldBatchMode);

   public abstract int getSnapshotInterval();

   public abstract void setSnapshotInterval(int snapshotInterval);

   public abstract String getSnapshotMode();

   public abstract void setSnapshotMode(String snapshotMode);
   
   public abstract String getCacheObjectName();
   
   public abstract void setCacheObjectName(String objectName);

   public abstract void start() throws LifecycleException;

   /**
    * Does nothing.
    */
   public abstract void stop() throws LifecycleException;

}
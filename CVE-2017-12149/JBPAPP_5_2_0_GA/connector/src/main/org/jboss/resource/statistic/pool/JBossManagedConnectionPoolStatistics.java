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
package org.jboss.resource.statistic.pool;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.jboss.resource.statistic.JBossStatistics;

/**
 * A JBossManagedConnectionPoolStatistics.
 * 
 * @author <a href="weston.price@jboss.com">Weston Price</a>
 * @version $Revision: 92075 $
 */
public class JBossManagedConnectionPoolStatistics implements ManagedConnectionPoolStatistics
{
   
   /** The serialVersionUID */
   private static final long serialVersionUID = -2962342009092796221L;

   private String poolName;

   private long blockingTimeout;
   private long idleTimeout;
   private int min;
   private int max;
   private String criteria;
   private boolean noTxnSeperatePool;
   
   private final List subPools;

   private boolean prefill;
   
   public JBossManagedConnectionPoolStatistics(){
      
      subPools = new ArrayList();
   }

   public JBossManagedConnectionPoolStatistics(int subPoolSize){
      
      subPools = new ArrayList(subPoolSize);
   }

   public JBossManagedConnectionPoolStatistics(String poolName){      
      
      this();
      this.poolName = poolName;
      
   }
   
   public void addSubPool(final JBossSubPoolStatistics subPool){
      subPools.add(subPool);
   }
   
   public Collection getSubPools(){
      
      return Collections.unmodifiableCollection(subPools);

   }
   
   public long getBlockingTimeout()
   {
    
      return blockingTimeout;
   
   }
   
   public void setBlockingTimeout(long blockTime){
      
      this.blockingTimeout = blockTime;
            
   }
   
   public String getCriteria()
   {
      return this.criteria;
   }
   
   
   public long getIdleTimeout()
   {
      return this.idleTimeout;
   }

   public void setIdleTimeout(long idleTimeout){
      
      this.idleTimeout = idleTimeout;
      
   }
   public int getMax()
   {
      return this.max;
   }
   
   public void setMax(int max)
   {
      this.max = max;
   }
   public int getMin()
   {
      return this.min;
   }

   public void setMin(int min)
   {
     this.min = min;
     
   }   
   
   public boolean getNoTxnSeperatePool()
   {
      return this.noTxnSeperatePool;
   }
   
   public void setNoTxnSeperatePool(boolean noTxnSeperatePool)
   {
      this.noTxnSeperatePool = noTxnSeperatePool;
   }
   
   public JBossManagedConnectionPoolStatistics getStatistics()
   {
      
      return null;
   }

   public int getSubPoolCount()
   {
      return subPools.size();

   }

   public int getTotalConnectionsInUseCount()
   {
      int statValue = 0;
      
      for(Iterator iter = subPools.iterator(); iter.hasNext();){
         
         JBossSubPoolStatistics statGroup = (JBossSubPoolStatistics)iter.next();
         statValue += statGroup.getConnectionsInUse();
         
      }
      
      return statValue;
      
   }

   public int getTotalMaxConnectionsInUseCount()
   {
      
      int statValue = 0;
      
      for(Iterator iter = subPools.iterator(); iter.hasNext();){
         
         JBossSubPoolStatistics statGroup = (JBossSubPoolStatistics)iter.next();
         statValue += statGroup.getMaxConnectionsInUse();
         
      }
      
      return statValue;
     
   }

   public boolean getPrefill()
   {
      // TODO Auto-generated method stub
      return prefill;
   }

   public void setPrefill(boolean prefill)
   {
      this.prefill = prefill;
   }

   public String getName()
   {
      return this.poolName;
   }

   public void setName(String name)
   {
      this.poolName = name;
   }

   public void setCriteria(String criteria)
   {
     this.criteria = criteria;
      
   }

   public void addSubPool(JBossStatistics subPool)
   {
      subPools.add(subPool);
   }   
   
   public String toString()
   {
      StringBuffer poolStatBuff = new StringBuffer(); 
      poolStatBuff.append(" Sub Pool Statistics:");
      poolStatBuff.append("\n Sub Pool Count:"+subPools.size()); 
      poolStatBuff.append("\n\n---------------------------------------------------------------");
      for(Iterator iter = subPools.iterator(); iter.hasNext();)
      {		   
         JBossSubPoolStatistics statGroup = (JBossSubPoolStatistics)iter.next();
         poolStatBuff.append("\n Available Connections Count:" + statGroup.getAvailableConnections());
         poolStatBuff.append("\n Max Connections In Use Count:" + statGroup.getMaxConnectionsInUse());
         poolStatBuff.append("\n Connections Destroyed Count:" + statGroup.getConnectionsDestroyed());
         poolStatBuff.append("\n Connections In Use Count:" + statGroup.getConnectionsInUse());
         poolStatBuff.append("\n Total Block Time:" + statGroup.getTotalBlockTime());
         poolStatBuff.append("\n Average Block Time For Sub Pool:" + statGroup.getAverageBlockTime());
         poolStatBuff.append("\n Maximum Wait Time For Sub Pool:" + statGroup.getMaxWaitTime());
         poolStatBuff.append("\n Total Timed Out Connections:" + statGroup.getTotalTimedOut());
         poolStatBuff.append("\n\n ---------------------------------------------------------------");
      }
      return poolStatBuff.toString();
   }
}

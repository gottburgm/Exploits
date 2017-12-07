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

import java.util.Collection;

import org.jboss.resource.statistic.JBossStatistics;

/**
 * A ManagedConnectionPoolStatistics.
 * 
 * @author <a href="weston.price@jboss.com">Weston Price</a>
 * @version $Revision: 92075 $
 */
public interface ManagedConnectionPoolStatistics extends JBossStatistics
{
  
   /**
    * Get the name of the managed connection pool as a string.
    * 
    * @return the name of the managed connection pool.
    */
   public String getName();
   
   /**
    * Set the name of the managed connection pool
    * 
    * @param name the name of the managed connection pool.
    */
   public void setName(String name);
   
   /**
    * Get the number of sub pools.
    * 
    * @return the number of sub pools.
    */
   public int getSubPoolCount();

   /**
    * Get the total maximum connections used count
    * 
    * @return the total maximum connections used count.
    */
   public int getTotalMaxConnectionsInUseCount();

   /**
    * Get the total connections in use count.
    * 
    * @return the total connections in use count.
    */
   public int getTotalConnectionsInUseCount();

   
   /**
    * Get the minimum connections for all pools
    * 
    * @return the minimum connections for all pools
    */
   public int getMin();

   public void setMin(int min);
   public int getMax();
   public void setMax(int max);
   public long getBlockingTimeout();
   public void setBlockingTimeout(long blockTime);
   public long getIdleTimeout();
   public void setIdleTimeout(long idleTimeout);
   public String getCriteria();
   public void setCriteria(String criteria);
   public boolean getNoTxnSeperatePool();
   public void setNoTxnSeperatePool(boolean noTxnSeperatePool);
   public void setPrefill(boolean prefill);
   public boolean getPrefill();
   public JBossManagedConnectionPoolStatistics getStatistics();
   public void addSubPool(JBossStatistics subPool);
   public Collection getSubPools();
   
}

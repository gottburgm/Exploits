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
package org.jboss.resource.connectionmanager;

/**
 * Management interface for JBossManagedConnectionPool.
 * 
 * @author <a href="mailto:weston.price@jboss.com">Weston Price</a>
 * 
 * @version $Revision: 89476 $
 */
public interface JBossManagedConnectionPoolMBean extends org.jboss.system.ServiceMBean {

   //default object name
   public static final javax.management.ObjectName OBJECT_NAME = org.jboss.mx.util.ObjectNameFactory.create("jboss.jca:service=JBossManagedConnectionPool");

   /**
    * ManagedConnectionPool is a read only attribute returning the pool set up by this mbean.
    * @return the ManagedConnectionPool implementing the pool configured by this mbean.
    */
  org.jboss.resource.connectionmanager.ManagedConnectionPool getManagedConnectionPool() ;

   /**
    * ManagedConnectionFactoryName holds the ObjectName of the mbean that represents the ManagedConnectionFactory. Normally this can be an embedded mbean in a depends element rather than a separate mbean reference.
    * @return the ManagedConnectionFactoryName value.
    */
  javax.management.ObjectName getManagedConnectionFactoryName() ;

   /**
    * Set the ManagedConnectionFactoryName value.
    * @param newManagedConnectionFactoryName The new ManagedConnectionFactoryName value.
    */
  void setManagedConnectionFactoryName(javax.management.ObjectName newManagedConnectionFactoryName) ;

   /**
    * Get number of available free connections
    * @return number of available connections
    */
  long getAvailableConnectionCount() ;

  /**
    * Get the maximum number of connections that were in use at any point in time.
    * 
    * @return number of maximum connections in use.
    */
   long getMaxConnectionsInUseCount();

   /**
    * Get number of connections currently in use
    * @return number of connections currently in use
    */
  long getInUseConnectionCount() ;

   /**
    * The MinSize attribute indicates the minimum number of connections this pool should hold. These are not created until a Subject is known from a request for a connection. MinSize connections will be created for each sub-pool.
    * @return the MinSize value.
    */
  int getMinSize() ;

   /**
    * Set the MinSize value.
    * @param newMinSize The new MinSize value.
    */
  void setMinSize(int newMinSize) ;

   /**
    * The MaxSize attribute indicates the maximum number of connections for a pool. No more than MaxSize connections will be created in each sub-pool.
    * @return the MaxSize value.
    */
  int getMaxSize() ;

   /**
    * Set the MaxSize value.
    * @param newMaxSize The new MaxSize value.
    */
  void setMaxSize(int newMaxSize) ;

   /**
    * The BlockingTimeoutMillis attribute indicates the maximum time to block while waiting for a connection before throwing an exception. Note that this blocks only while waiting for a permit for a connection, and will never throw an exception if creating a new connection takes an inordinately long time.
    * @return the BlockingTimeout value.
    */
  int getBlockingTimeoutMillis() ;

   /**
    * Set the BlockingTimeout value.
    * @param newBlockingTimeout The new BlockingTimeout value.
    */
  void setBlockingTimeoutMillis(int newBlockingTimeout) ;

   /**
    * The IdleTimeoutMinutes attribute indicates the maximum time a connection may be idle before being closed. The actual maximum time depends also on the IdleRemover scan time, which is 1/2 the smallest IdleTimeout of any pool.
    * @return the IdleTimeoutMinutes value.
    */
  long getIdleTimeoutMinutes() ;

   /**
    * Set the IdleTimeoutMinutes value.
    * @param newIdleTimeoutMinutes The new IdleTimeoutMinutes value.
    */
  void setIdleTimeoutMinutes(long newIdleTimeoutMinutes) ;

   /**
    * The Criteria attribute indicates if Subject (from security domain) or app supplied parameters (such as from getConnection(user, pw)) are used to distinguish connections in the pool. Choices are ByContainerAndApplication (use both), ByContainer (use Subject), ByApplication (use app supplied params only), ByNothing (all connections are equivalent, usually if adapter supports reauthentication)
    * @return the Criteria value.
    */
  java.lang.String getCriteria() ;

   /**
    * Set the Criteria value.
    * @param newCriteria The new Criteria value.
    */
  void setCriteria(java.lang.String newCriteria) ;

   /**
    * Separate pools for transactional use
    * @return true when connections should have different pools for transactional and non-transaction use.
    */
  boolean getNoTxSeparatePools() ;

  void setNoTxSeparatePools(boolean value) ;

  /**
 * FIXME Comment this
 * 
 * @param prefill
 */
  void setPreFill(boolean prefill);
  
  /**
   * FIXME Comment this
   * 
   * @return
   */
  boolean getPreFill();
  
  boolean getStrictMin();
  
  void setStrictMin(boolean strictMin);
  
   /**
    * The <code>flush</code> method puts all currently checked out connections on a list to be destroyed when returned and disposes of all current pooled connections.
    */
  void flush() ;

  /**
   * Test if a connection can be obtained using default values
   * @return True if a connection was obtained; otherwise false
   */
  boolean testConnection();

   /**
    * Retrieve the connection count.
    * @return the connection count
    */
  int getConnectionCount() ;

   /**
    * Retrieve the connection created count.
    * @return the connection created count
    */
  int getConnectionCreatedCount() ;

   /**
    * Retrieve the destrooyed count.
    * @return the destroyed count
    */
  int getConnectionDestroyedCount() ;

  /**
    * Return raw statistics for all sub pools.
    * 
    * @return the statistics for all sub pools.
    */
   Object listStatistics();
   
   /**
    * Return statistics for all sub pools formatted for consumption.
    * 
    * @return the formatted statistics.
    */
   Object listFormattedSubPoolStatistics();
   
   /**
    * Return statistics for all sub pools formatted using the specified class name.
    * 
    * @param formatter the class name of the formatter to use.
    * 
    * @return the formatted statistics.
    */
   Object listFormattedSubPoolStatistics(String formatter);
   
   /**
    * Get the class name of the current statistics formatter
    * 
    * @return the name of the statistics formatter.
    */
   public String getStatisticsFormatter();
   
   /**
    * Set the class name of the statistics formatter
    * 
    * @param formatter the name of the statistics formatter to use. 
    * 
    */
   public void setStatisticsFormatter(String formatter);
  
   /**
    * Set the background validation in millis
    * 
    * @param backgroundValidationInterval the background interval in minutes
    */
   public void setBackGroundValidationMillis(long backgroundValidationInterval);
  
   /**
    * Get the background validation in millis
    * 
    * @return the background validation in millis
    */
   public long getBackGroundValidationMillis();
  

   /**
    * Get the jndi name of the pool
    * 
    * @return the jndi name of the pool.
    */
   public String  getPoolJndiName();
   
   /**
    * Set the jndi name of the pool.
    * 
    * @param poolName the jndi name of the pool.
    */
   public void setPoolJndiName(String poolName);
 
   /**
    * Whether or not we want to immeadiately create a new connection when 
    * an attempt to acquire a connection from the pool fails.
    * 
    * @return true of false depending upon whether fast fail is being used.
    * 
    */
   public boolean getUseFastFail();
   
   /**
    * Indicate whether or not we want to immeadiately create a new connection when 
    * an attempt to acquire a connection from the pool fails.
    * 
    * 
    * @param useFastFail whether or not we want to use fast fail semantics in a connection attempt.
    */
   public void setUseFastFail(boolean useFastFail);
   
   /**
    * This will list the statistics from the underlying native Connection of the ManagedConnection 
    * 
    * @return underlying connection statistics
    */
   public Object listUnderlyingNativeConnectionStatistics(); 
}

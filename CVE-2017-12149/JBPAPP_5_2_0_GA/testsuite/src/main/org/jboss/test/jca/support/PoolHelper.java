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
package org.jboss.test.jca.support;

import javax.management.Attribute;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.resource.spi.ManagedConnectionFactory;

import org.jboss.logging.Logger;
import org.jboss.resource.connectionmanager.BaseConnectionManager2;
import org.jboss.resource.connectionmanager.CachedConnectionManager;
import org.jboss.resource.connectionmanager.InternalManagedConnectionPool;
import org.jboss.resource.connectionmanager.JBossManagedConnectionPool;
import org.jboss.resource.connectionmanager.ManagedConnectionPool;

/**
 * A PoolHelper.
 * 
 * @author <a href="weston.price@jboss.com">Weston Price</a>
 * @version $Revision: 85731 $
 */
public class PoolHelper
{
   static Logger log = Logger.getLogger(PoolHelper.class);
   
   public static final String POOL_ATT_BLOCK_TIME = "BlockingTimeoutMillis";
   public static final String POOL_ATT_BACKGROUND_VAL_MILLIS = "BackGroundValidationMillis";
   public static final String POOL_ATT_PREFILL = "PreFill";
   public static final String POOL_ATT_MIN_CONN_COUNT = "MinSize";
   public static final String POOL_ATT_DESTROYED_COUNT = "ConnectionDestroyedCount";
   public static final String POOL_ATT_CONN_COUNT = "ConnectionCount";
   
   
   private MBeanServerConnection server;
   private ObjectName poolName;
   
   private static final int DEFAULT_MIN = 0;

   private static final int DEFAULT_MAX = 20;

   private static final int DEFAULT_BLOCK = 1000;

   private static final long DEFAULT_IDLE = 1000;

   private static final boolean DEFAULT_PREFILL = false;

   private static final PoolType DEFAULT_POOL_TYPE = PoolType.ONE_POOL;

   private PoolHelper(MBeanServerConnection server, ObjectName poolName)
   {
      this.server = server;
      this.poolName = poolName;
      
   }

   public static PoolHelper getInstance(MBeanServerConnection server) 
   {
      
      return new PoolHelper(server, null);
         
   }
   
   public static PoolHelper getInstance(MBeanServerConnection server, ObjectName poolName)
   {
      return new PoolHelper(server, poolName);
      
   }
   public static boolean comparePoolValues(MBeanServerConnection server, ObjectName poolName, String firstAtt, String secondAtt) throws Exception
   {
      
      Object first = getAttribute(server, poolName, firstAtt);
      Object second = getAttribute(server, poolName, secondAtt);
      return first.equals(second);
      
      
   }
   
   public Object getAttribute(String attribute) throws Exception
   {
      
      return getAttribute(poolName, attribute);
      
      
   }

   public Object getAttribute(ObjectName name, String attribute) throws Exception
   {
      
      return server.getAttribute(name, attribute);
      
   }
   
//   public static Integer getConnCount(MBeanServerConnection server, ObjectName poolName) throws Exception
//   {
//      
//      
//   }
   
   public static void sleepForValidation(long millis) throws Exception
   {
      Thread.sleep(millis);
      
   }
   
   public static Integer getConnectionCount(MBeanServerConnection server, ObjectName poolName) throws Exception
   {
      return (Integer)getAttribute(server, poolName, POOL_ATT_CONN_COUNT);
      
   }
   
   public Integer getDestroyed() throws Exception
   {
      return getDestroyed(server, poolName);
      
   }

   public static Integer getDestroyed(MBeanServerConnection server, ObjectName poolName) throws Exception
   {

      return (Integer)getAttribute(server, poolName, POOL_ATT_DESTROYED_COUNT);
      
   }
   
   public Integer getMinSize() throws Exception
   {
      return getMinSize(server, poolName);
      
   }
   
   public static Integer getMinSize(MBeanServerConnection server, ObjectName poolName) throws Exception
   {
      
      return (Integer)getAttribute(server, poolName, POOL_ATT_MIN_CONN_COUNT);
      
   }
   
   public Long getBackgroundValMillis() throws Exception
   {
      return getBackgroundValMillis(server, poolName);
      
   }
   
   public static Long getBackgroundValMillis(MBeanServerConnection server, ObjectName poolName) throws Exception
   {
      return (Long)getAttribute(server, poolName, POOL_ATT_BACKGROUND_VAL_MILLIS);
      
   }
   
   
   public static Integer getBlockingTimeout(MBeanServerConnection server, ObjectName poolName) throws Exception
   { 
      return (Integer)getAttribute(server, poolName, POOL_ATT_BLOCK_TIME);
     
   }
   
   public void setPoolAttributeAndFlush(String attName, Object attValue) throws Exception
   {
      setPoolAttributeAndFlush(server, poolName, attName, attValue);
      
   }

   public static void setPoolAttributeAndFlush(MBeanServerConnection server, ObjectName pool, String attName, Object attValue) throws Exception
   {
      
      setAttribute(server, pool, attName, attValue);
      flush(server, pool);
      
      
   }
   public static Object getAttribute(MBeanServerConnection server, ObjectName poolName, String attName) throws Exception
   {
      log.debug("Getting pool attribute " + attName);
      Object result = server.getAttribute(poolName, attName);      
      log.debug("Retrieved pool attribute " + attName + " with value " + result);
      
      return result;
   }

   public static void setAttribute(MBeanServerConnection server, ObjectName objectName, String attName, Object attValue) throws Exception
   {
     
      server.setAttribute(objectName, new Attribute(attName, attValue));
      
   }
   
   public Integer getConnectionCount() throws Exception
   {
     return getConnectionCount(server, poolName);
     
   }
   public static void flush(MBeanServerConnection server, ObjectName pool) throws Exception
   {
      server.invoke(pool, "flush", new Object[0], new String[0]);
      
   }
   public static InternalManagedConnectionPool.PoolParams getPoolParams()
   {

      InternalManagedConnectionPool.PoolParams params = new InternalManagedConnectionPool.PoolParams();
      params.minSize = DEFAULT_MIN;
      params.maxSize = DEFAULT_MAX;
      params.blockingTimeout = DEFAULT_BLOCK;
      params.idleTimeout = DEFAULT_IDLE;
      params.prefill = DEFAULT_PREFILL;
      return params;

   }

   public static InternalManagedConnectionPool.PoolParams getPoolParams(boolean prefill)
   {

      return getPoolParams(DEFAULT_MIN, DEFAULT_MAX, DEFAULT_BLOCK, DEFAULT_IDLE, prefill);
   }

   public static InternalManagedConnectionPool.PoolParams getPoolParams(long idleTimeout, boolean prefill)
   {

      return getPoolParams(DEFAULT_MIN, DEFAULT_MAX, DEFAULT_BLOCK, idleTimeout, prefill);
   }

   public static InternalManagedConnectionPool.PoolParams getPoolParams(int blockingTimeout, long idleTimeout,
         boolean prefill)
   {

      return getPoolParams(DEFAULT_MIN, DEFAULT_MAX, blockingTimeout, idleTimeout, prefill);
   }

   public static InternalManagedConnectionPool.PoolParams getPoolParams(int maxSize, int blockingTimeout,
         long idleTimeout, boolean prefill)
   {

      return getPoolParams(DEFAULT_MIN, maxSize, blockingTimeout, idleTimeout, prefill);
   }

   public static InternalManagedConnectionPool.PoolParams getPoolParams(int minSize, int maxSize, int blockingTimeout,
         long idleTimeout, boolean prefill)
   {

      InternalManagedConnectionPool.PoolParams params = getPoolParams();
      params.minSize = minSize;
      params.maxSize = maxSize;
      params.blockingTimeout = blockingTimeout;
      params.idleTimeout = idleTimeout;
      params.prefill = prefill;
      return params;

   }

   public static ManagedConnectionFactory getManagedConnectionFactory(String className) throws Exception
   {

      return getManagedConnectionFactory(Class.forName(className));
   }

   public static ManagedConnectionFactory getManagedConnectionFactory(Class clazz) throws Exception
   {

      return (ManagedConnectionFactory) clazz.newInstance();

   }

   public static ManagedConnectionPool getManagedConnectionPool(int minSize, int maxSize, int blockingTimeout, long idleTimeout, boolean prefill, ManagedConnectionFactory mcf, boolean noTxnSeperatePool, Logger log){
      
      InternalManagedConnectionPool.PoolParams pp = getPoolParams(minSize, maxSize, blockingTimeout, idleTimeout, prefill);
      return getManagedConnectionPool(DEFAULT_POOL_TYPE, mcf, noTxnSeperatePool, pp, log);
   }
   
   
   public static ManagedConnectionPool getManagedConnectionPool(PoolType type, ManagedConnectionFactory mcf,
         boolean noTxnSeperatePool, InternalManagedConnectionPool.PoolParams pp, Logger log)
   {

      ManagedConnectionPool mcp = null;

      if (type.equals(PoolType.ONE_POOL))
      {

         mcp = new JBossManagedConnectionPool.OnePool(mcf, pp, noTxnSeperatePool, null, log);

      }
      else if (type.equals(PoolType.CRI_POOL))
      {

         mcp = new JBossManagedConnectionPool.PoolByCri(mcf, pp, noTxnSeperatePool, null, log);

      }
      else if (type.equals(PoolType.SUB_POOL))
      {

         mcp = new JBossManagedConnectionPool.PoolBySubject(mcf, pp, noTxnSeperatePool, null, log);

      }
      else if (type.equals(PoolType.SUB_CRI_POOL))
      {

         mcp = new JBossManagedConnectionPool.PoolBySubjectAndCri(mcf, pp, noTxnSeperatePool, null, log);

      }

      return mcp;
   }

   public static BaseConnectionManager2 getCM()
   {

      return null;
   }

   public static CachedConnectionManager getCachedConnectionManager(){
      
      return new CachedConnectionManager();
      
   }
   
   public static class PoolType
   {

      private final int type;

      private PoolType(int type)
      {

         this.type = type;
      }

      public static final PoolType ONE_POOL = new PoolType(0);

      public static final PoolType CRI_POOL = new PoolType(1);

      public static final PoolType SUB_POOL = new PoolType(2);

      public static final PoolType SUB_CRI_POOL = new PoolType(3);

   }
}
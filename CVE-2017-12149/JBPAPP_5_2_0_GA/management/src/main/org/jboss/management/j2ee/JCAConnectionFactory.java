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
package org.jboss.management.j2ee;

import org.jboss.logging.Logger;
import org.jboss.management.j2ee.statistics.BoundedRangeStatisticImpl;
import org.jboss.management.j2ee.statistics.CountStatisticImpl;
import org.jboss.management.j2ee.statistics.JCAConnectionPoolStatsImpl;
import org.jboss.management.j2ee.statistics.RangeStatisticImpl;
import org.jboss.management.j2ee.statistics.TimeStatisticImpl;

import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import java.util.Hashtable;

/**
 * Root class of the JBoss JSR-77 implementation of JCAConnectionFactory.
 *
 * @author <a href="mailto:mclaugs@comcast.net">Scott McLaughlin</a>.
 * @author  <a href="mailto:thomas.diesler@jboss.org">Thomas Diesler</a>
 * @version $Revision: 81025 $
 */
public class JCAConnectionFactory extends J2EEManagedObject
   implements JCAConnectionFactoryMBean
{
   // Constants -----------------------------------------------------
   private static Logger log = Logger.getLogger(JCAConnectionFactory.class);

   // Attributes ----------------------------------------------------

   /**
    * The JBoss connection manager service name
    */
   private ObjectName cmServiceName;
   
   /**
    * The JBoss managed connection service name
    */
   private ObjectName mcfServiceName;
   private String jsr77MCFName;
   private JCAConnectionPoolStatsImpl poolStats;

   // Static --------------------------------------------------------

   public static ObjectName create(MBeanServer mbeanServer, String resName,
                                   ObjectName jsr77ParentName, ObjectName ccmServiceNameName,
                                   ObjectName mcfServiceName)
   {
      ObjectName jsr77Name = null;
      try
      {
         JCAConnectionFactory jcaFactory = new JCAConnectionFactory(resName,
                 jsr77ParentName, ccmServiceNameName, mcfServiceName);
         jsr77Name = jcaFactory.getObjectName();
         mbeanServer.registerMBean(jcaFactory, jsr77Name);
         log.debug("Created JSR-77 JCAConnectionFactory: " + resName);
         ObjectName jsr77MCFName = JCAManagedConnectionFactory.create(mbeanServer,
                 resName, jsr77Name);
         jcaFactory.setmanagedConnectionFactory(jsr77MCFName.getCanonicalName());
      }
      catch (Exception e)
      {
         log.debug("Could not create JSR-77 JCAConnectionFactory: " + resName, e);
      }
      return jsr77Name;
   }

   public static void destroy(MBeanServer mbeanServer, String resName)
   {
      try
      {
         // Remove the JCAConnectionFactory associated with resName
         String connName = J2EEDomain.getDomainName() + ":"
            + J2EEManagedObject.TYPE + "=" + J2EETypeConstants.JCAConnectionFactory
            + ",name=" + resName + ",*";
         J2EEManagedObject.removeObject(mbeanServer, connName);
      }
      catch (javax.management.InstanceNotFoundException infe)
      {
      }
      catch (Exception e)
      {
         log.debug("Could not destroy JSR-77 JCAConnectionFactory: " + resName, e);
      }
   }

   // Constructors --------------------------------------------------


   public JCAConnectionFactory(String resName, ObjectName jsr77ParentName,
                               ObjectName ccmServiceNameName, ObjectName mcfServiceName)
           throws MalformedObjectNameException, InvalidParentException
   {
      super(J2EETypeConstants.JCAConnectionFactory, resName, jsr77ParentName);
      this.cmServiceName = ccmServiceNameName;
      this.mcfServiceName = mcfServiceName;
   }

   // Public --------------------------------------------------------

   // javax.management.j2ee.JCAConnectionFactory implementation -----------------

   /**
    * @jmx:managed-attribute
    */
   public String getmanagedConnectionFactory()
   {
      return jsr77MCFName;
   }

   void setmanagedConnectionFactory(String jsr77MCFName)
   {
      this.jsr77MCFName = jsr77MCFName;
   }

   /**
    * @jmx:managed-operation
    */
   public JCAConnectionPoolStatsImpl getPoolStats(ObjectName poolServiceName)
   {
      TimeStatisticImpl waitTime = null;
      TimeStatisticImpl useTime = null;
      CountStatisticImpl closeCount = null;
      CountStatisticImpl createCount = null;
      BoundedRangeStatisticImpl freePoolSize = null;
      BoundedRangeStatisticImpl poolSize = null;
      RangeStatisticImpl waitingThreadCount = null;
      try
      {
         if (poolStats == null)
         {
            Integer max = (Integer) server.getAttribute(poolServiceName, "MaxSize");
            freePoolSize = new BoundedRangeStatisticImpl("FreePoolSize", "1",
                    "The free connection count", 0, max.longValue());
            poolSize = new BoundedRangeStatisticImpl("PoolSize", "1",
                    "The connection count", 0, max.longValue());
            poolStats = new JCAConnectionPoolStatsImpl(getobjectName(), jsr77MCFName,
                    waitTime, useTime, closeCount, createCount, freePoolSize, poolSize,
                    waitingThreadCount);
         }
         createCount = (CountStatisticImpl) poolStats.getCreateCount();
         closeCount = (CountStatisticImpl) poolStats.getCloseCount();
         freePoolSize = (BoundedRangeStatisticImpl) poolStats.getFreePoolSize();
         poolSize = (BoundedRangeStatisticImpl) poolStats.getPoolSize();

         // Update the stats
         Integer isize = (Integer) server.getAttribute(poolServiceName, "ConnectionCreatedCount");
         createCount.set(isize.longValue());
         isize = (Integer) server.getAttribute(poolServiceName, "ConnectionDestroyedCount");
         closeCount.set(isize.longValue());
         isize = (Integer) server.getAttribute(poolServiceName, "ConnectionCount");
         poolSize.set(isize.longValue());
         Long lsize = (Long) server.getAttribute(poolServiceName, "AvailableConnectionCount");
         freePoolSize.set(lsize.longValue());
      }
      catch (Exception e)
      {
         log.debug("Failed to update JCAConnectionPoolStats", e);
      }

      return poolStats;
   }

   // java.lang.Object overrides ------------------------------------

   public String toString()
   {
      return "JCAConnectionFactory { " + super.toString() + " } [ " +
              " ]";
   }

   // Package protected ---------------------------------------------

   // Protected -----------------------------------------------------

   /**
    * @return A hashtable with the JCAResource and J2EEServer
    */
   protected Hashtable getParentKeys(ObjectName parentName)
   {
      Hashtable keys = new Hashtable();
      Hashtable nameProps = parentName.getKeyPropertyList();
      String factoryName = (String) nameProps.get("name");
      String serverName = (String) nameProps.get(J2EETypeConstants.J2EEServer);
      keys.put(J2EETypeConstants.J2EEServer, serverName);
      keys.put(J2EETypeConstants.JCAResource, factoryName);
      return keys;
   }

}

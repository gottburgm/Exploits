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
import org.jboss.management.j2ee.statistics.CountStatisticImpl;
import org.jboss.management.j2ee.statistics.JTAStatsImpl;

import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.j2ee.statistics.Stats;

/**
 * The JBoss JSR-77.3.30 implementation of the JTAResource model
 *
 * @author  <a href="mailto:andreas@jboss.org">Andreas Schaefer</a>
 * @author  <a href="mailto:scott.stark@jboss.org">Scott Stark</a>
 * @author  <a href="mailto:thomas.diesler@jboss.org">Thomas Diesler</a>
 * @version $Revision: 81025 $
 */
public class JTAResource  extends J2EEResource
   implements JTAResourceMBean
{
   // Constants -----------------------------------------------------
   private static Logger log = Logger.getLogger(JTAResource.class);

   // Attributes ----------------------------------------------------

   private ObjectName jtaServiceName;
   private JTAStatsImpl stats;

   // Static --------------------------------------------------------

   public static ObjectName create(MBeanServer mbeanServer, String resName,
                                   ObjectName jtaServiceName)
   {
      ObjectName j2eeServerName = J2EEDomain.getDomainServerName(mbeanServer);
      ObjectName jsr77Name = null;
      try
      {
         JTAResource jtaRes = new JTAResource(resName, j2eeServerName, jtaServiceName);
         jsr77Name = jtaRes.getObjectName();
         mbeanServer.registerMBean(jtaRes, jsr77Name);
         log.debug("Created JSR-77 JTAResource: " + resName);
      }
      catch (Exception e)
      {
         log.debug("Could not create JSR-77 JTAResource: " + resName, e);
      }
      return jsr77Name;
   }

   public static void destroy(MBeanServer mbeanServer, String resName)
   {
      try
      {
         J2EEManagedObject.removeObject(mbeanServer,
                 J2EEDomain.getDomainName() + ":" +
                 J2EEManagedObject.TYPE + "=" + J2EETypeConstants.JTAResource + "," +
                 "name=" + resName + "," +
                 "*");
      }
      catch (Exception e)
      {
         log.error("Could not destroy JSR-77 JTAResource: " + resName, e);
      }
   }

   // Constructors --------------------------------------------------

   /**
    * @param resName Name of the JTAResource
    * @throws InvalidParameterException If list of nodes or ports was null or empty
    */
   public JTAResource(String resName, ObjectName j2eeServerName, ObjectName jtaServiceName)
           throws
           MalformedObjectNameException,
           InvalidParentException
   {
      super(J2EETypeConstants.JTAResource, resName, j2eeServerName);
      if (log.isDebugEnabled())
         log.debug("Service name: " + jtaServiceName);
      this.jtaServiceName = jtaServiceName;
      stats = new JTAStatsImpl();
   }

   // Begin StatisticsProvider interface methods

   /**
    * Obtain the Stats from the StatisticsProvider.
    *
    * @return An EJBStats subclass
    * @jmx:managed-attribute
    */
   public Stats getstats()
   {
      try
      {
         CountStatisticImpl readyCount = (CountStatisticImpl) stats.getActiveCount();
         Long count = (Long) server.getAttribute(jtaServiceName, "TransactionCount");
         readyCount.set(count.longValue());
         CountStatisticImpl commitCount = (CountStatisticImpl) stats.getCommittedCount();
         count = (Long) server.getAttribute(jtaServiceName, "CommitCount");
         commitCount.set(count.longValue());
         CountStatisticImpl rollbackCount = (CountStatisticImpl) stats.getRolledbackCount();
         count = (Long) server.getAttribute(jtaServiceName, "RollbackCount");
         rollbackCount.set(count.longValue());
      }
      catch (Exception e)
      {
         log.debug("Failed to retrieve stats", e);
      }
      return stats;
   }

   public void resetStats()
   {
      stats.reset();
   }

   // End StatisticsProvider interface methods

   // javax.managment.j2ee.EventProvider implementation -------------

   public String[] getEventTypes()
   {
      return StateManagement.stateTypes;
   }

   public String getEventType(int pIndex)
   {
      if (pIndex >= 0 && pIndex < StateManagement.stateTypes.length)
      {
         return StateManagement.stateTypes[pIndex];
      }
      else
      {
         return null;
      }
   }

   // java.lang.Object overrides ------------------------------------

   public String toString()
   {
      return "JTAResource { " + super.toString() + " } [ " +
              " ]";
   }

}

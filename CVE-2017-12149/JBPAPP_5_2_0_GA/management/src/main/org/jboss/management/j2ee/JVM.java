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

import org.jboss.management.j2ee.statistics.JVMStatsImpl;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.j2ee.statistics.Stats;
import java.util.Hashtable;

/**
 * The JBoss JSR-77.3.4 JVM model implementation
 *
 * @author  <a href="mailto:andreas@jboss.org">Andreas Schaefer</a>
 * @author  <a href="mailto:scott.stark@jboss.org">Scott Stark</a>
 * @author  <a href="mailto:thomas.diesler@jboss.org">Thomas Diesler</a>
 * @version $Revision: 81025 $
 */
public class JVM extends J2EEManagedObject
   implements JVMMBean
{
   // Constants -----------------------------------------------------

   // Attributes ----------------------------------------------------

   private String javaVendor;
   private String javaVersion;
   private String node;
   private JVMStatsImpl stats;

   // Static --------------------------------------------------------

   // Constructors --------------------------------------------------

   /**
    */
   public JVM(String name, ObjectName j2eeServer, String javaVersion,
              String javaVendor, String node)
           throws MalformedObjectNameException,
           InvalidParentException
   {
      super(J2EETypeConstants.JVM, name, j2eeServer);
      this.javaVendor = javaVendor;
      this.javaVersion = javaVersion;
      this.node = node;
      this.stats = new JVMStatsImpl();
   }

   // Public --------------------------------------------------------

   /**
    * @jmx:managed-attribute
    */
   public String getjavaVendor()
   {
      return javaVendor;
   }

   /**
    * @jmx:managed-attribute
    */
   public String getjavaVersion()
   {
      return javaVersion;
   }

   /**
    * @jmx:managed-attribute
    */
   public String getnode()
   {
      return node;
   }

   // Begin StatisticsProvider interface methods
   /**
    * Obtain the Stats from the StatisticsProvider
    *
    * @return
    * @jmx:managed-attribute
    */
   public Stats getstats()
   {
      // Refresh the stats
      stats.getUpTime();
      stats.getHeapSize();
      return stats;
   }

   /**
    * Reset all statistics in the StatisticsProvider
    *
    * @jmx:managed-operation
    */
   public void resetStats()
   {
      stats.reset();
   }
   // End StatisticsProvider interface methods

   // java.lang.Object overrides ------------------------------------

   public String toString()
   {
      StringBuffer tmp = new StringBuffer("JVM");
      tmp.append('[');
      tmp.append("JavaVendor: ");
      tmp.append(javaVendor);
      tmp.append(", JavaVersion: ");
      tmp.append(javaVersion);
      tmp.append(", JavaVersion: ");
      tmp.append(javaVendor);
      tmp.append(", Stats: ");
      tmp.append(stats);
      tmp.append(']');
      return tmp.toString();
   }

   // Protected -----------------------------------------------------

   /**
    * @return A hashtable with the J2EE Server as parent
    */
   protected Hashtable getParentKeys(ObjectName pParent)
   {
      Hashtable lReturn = new Hashtable();
      Hashtable lProperties = pParent.getKeyPropertyList();
      lReturn.put(J2EETypeConstants.J2EEServer, lProperties.get("name"));

      return lReturn;
   }

}

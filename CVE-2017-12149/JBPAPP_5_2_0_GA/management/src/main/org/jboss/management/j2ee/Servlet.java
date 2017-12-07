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
import org.jboss.management.j2ee.statistics.ServletStatsImpl;
import org.jboss.management.j2ee.statistics.TimeStatisticImpl;

import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.j2ee.statistics.Stats;
import java.util.Hashtable;

/**
 * The JBoss JSR-77.3.17 Servlet model implementation
 *
 * @author  <a href="mailto:andreas@jboss.org">Andreas Schaefer</a>
 * @author  <a href="mailto:scott.stark@jboss.org">Scott Stark</a>
 * @author  <a href="mailto:thomas.diesler@jboss.org">Thomas Diesler</a>
 * @version $Revision: 81025 $
 */
public class Servlet extends J2EEManagedObject
   implements ServletMBean
{

   // Attributes ----------------------------------------------------
   private static Logger log = Logger.getLogger(Servlet.class);

   private ObjectName servletServiceName;
   private ServletStatsImpl stats;

   // Static --------------------------------------------------------
   /**
    * Create a JSR77 Servlet submodel.
    *
    * @param mbeanServer      the MBeanServer to use for mbean creation
    * @param webModuleName    the name of the JSR77 web module mbean
    * @param webContainerName the name of the JBoss web container mbean
    * @param servletName      the name of the servlet
    * @return the ObjectName of the JSR77 Servlet mbean
    */
   public static ObjectName create(MBeanServer mbeanServer, ObjectName webModuleName,
                                   ObjectName webContainerName, ObjectName servletServiceName)
   {
      try
      {
         Servlet servlet = new Servlet(servletServiceName, webModuleName, webContainerName);
         ObjectName jsr77Name = servlet.getObjectName();
         mbeanServer.registerMBean(servlet, jsr77Name);
         log.debug("Created JSR-77 Servlet: " + jsr77Name);
         return jsr77Name;
      }
      catch (Exception e)
      {
         log.debug("Could not create JSR-77 Servlet: " + servletServiceName, e);
         return null;
      }
   }

   public static void destroy(MBeanServer mbeanServer, ObjectName jsr77Name)
   {
      try
      {
         mbeanServer.unregisterMBean(jsr77Name);
         log.debug("Destroyed JSR-77 Servlet: " + jsr77Name);
      }
      catch (javax.management.InstanceNotFoundException ignore)
      {
      }
      catch (Exception e)
      {
         log.debug("Could not destroy JSR-77 Servlet: " + jsr77Name, e);
      }
   }

   // Constructors --------------------------------------------------

   /**
    * @param pName Name of the Servlet
    * @throws InvalidParameterException If list of nodes or ports was null or empty
    */
   public Servlet(ObjectName servletServiceName, ObjectName webModuleName,
                  ObjectName webContainerName)
           throws MalformedObjectNameException,
           InvalidParentException
   {
      super(J2EETypeConstants.Servlet, servletServiceName.getKeyProperty("name"), webModuleName);
      this.servletServiceName = servletServiceName;
      this.stats = new ServletStatsImpl();
   }

   /**
    * StatisticsProvider access to stats.
    *
    * @return A ServletStats implementation
    * @jmx:managed-attribute
    */
   public Stats getstats()
   {
      try
      {
         TimeStatisticImpl serviceTime = (TimeStatisticImpl) stats.getServiceTime();
         Integer count = (Integer) server.getAttribute(servletServiceName, "requestCount");
         Long totalTime = (Long) server.getAttribute(servletServiceName, "processingTime");
         Long minTime = (Long) server.getAttribute(servletServiceName, "minTime");
         Long maxTime = (Long) server.getAttribute(servletServiceName, "maxTime");
         serviceTime.set(count.longValue(), minTime.longValue(),
                 maxTime.longValue(), totalTime.longValue());
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
   // java.lang.Object overrides --------------------------------------

   public String toString()
   {
      return "Servlet { " + super.toString() + " } []";
   }

   // Package protected ---------------------------------------------

   // Protected -----------------------------------------------------

   /**
    * @return A hashtable with the Web-Module, J2EE-Application and J2EE-Server as parent
    */
   protected Hashtable getParentKeys(ObjectName pParent)
   {
      Hashtable lReturn = new Hashtable();
      Hashtable lProperties = pParent.getKeyPropertyList();
      lReturn.put(J2EETypeConstants.WebModule, lProperties.get("name"));
      // J2EE-Application and J2EE-Server is already parent of J2EE-Application therefore lookup
      // the name by the J2EE-Server type
      lReturn.put(J2EETypeConstants.J2EEApplication, lProperties.get(J2EETypeConstants.J2EEApplication));
      lReturn.put(J2EETypeConstants.J2EEServer, lProperties.get(J2EETypeConstants.J2EEServer));

      return lReturn;
   }

}

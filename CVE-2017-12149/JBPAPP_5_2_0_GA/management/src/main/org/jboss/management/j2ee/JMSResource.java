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
import org.jboss.management.j2ee.statistics.JMSStatsImpl;

import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.j2ee.statistics.Stats;
import java.util.Iterator;
import java.util.Map;

/**
 * Root class of the JBoss JSR-77 implementation of the JMSResource model
 *
 * @author <a href="mailto:mclaugs@comcast.net">Scott McLaughlin</a>.
 * @author  <a href="mailto:thomas.diesler@jboss.org">Thomas Diesler</a>
 * @version $Revision: 81025 $
 */
public class JMSResource extends J2EEResource
   implements JMSResourceMBean
{
   // Constants -----------------------------------------------------
   private static Logger log = Logger.getLogger(JMSResource.class);

   // Attributes ----------------------------------------------------

   private ObjectName jmsServiceName;
   private JMSStatsImpl stats;

   // Static --------------------------------------------------------

   public static ObjectName create(MBeanServer mbeanServer, String resName,
                                   ObjectName jmsServiceName)
   {
      ObjectName j2eeServerName = J2EEDomain.getDomainServerName(mbeanServer);
      ObjectName jsr77Name = null;
      try
      {
         JMSResource jmsRes = new JMSResource(resName, j2eeServerName, jmsServiceName);
         jsr77Name = jmsRes.getObjectName();
         mbeanServer.registerMBean(jmsRes, jsr77Name);
         log.debug("Created JSR-77 JMSResource: " + resName);
      }
      catch (Exception e)
      {
         log.debug("Could not create JSR-77 JMSResource: " + resName, e);
      }
      return jsr77Name;
   }

   public static void destroy(MBeanServer pServer, String pName)
   {
      try
      {
         J2EEManagedObject.removeObject(pServer,
                 J2EEDomain.getDomainName() + ":" +
                 J2EEManagedObject.TYPE + "=" + J2EETypeConstants.JMSResource + "," +
                 "name=" + pName + "," +
                 "*");
      }
      catch (Exception e)
      {
         log.error("Could not destroy JSR-77 JMSResource Resource", e);
      }
   }

   // -------------------------------------------------------------------------
   // Constructors
   // -------------------------------------------------------------------------

   /**
    * @param pName Name of the JMSResource
    * @throws InvalidParameterException If list of nodes or ports was null or empty
    */
   public JMSResource(String resName, ObjectName j2eeServerName,
                      ObjectName jmsServiceName)
           throws MalformedObjectNameException,
           InvalidParentException
   {
      super(J2EETypeConstants.JMSResource, resName, j2eeServerName);
      this.jmsServiceName = jmsServiceName;
      stats = new JMSStatsImpl(null);
   }

   // Begin StatisticsProvider interface methods

   /**
    * Obtain the Stats from the StatisticsProvider.
    *
    * @return An JMSStats subclass
    * @jmx:managed-attribute
    */
   public Stats getstats()
   {
      try
      {
         // Obtain the current clients Map<ConnectionToken, ClientConsumer>
         Map clients = (Map) server.getAttribute(jmsServiceName, "Clients");
         Iterator iter = clients.keySet().iterator();
      }
      catch (Exception e)
      {
         log.debug("Failed to obtain stats", e);
      }
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
      return "JMSResource { " + super.toString() + " } []";
   }
}

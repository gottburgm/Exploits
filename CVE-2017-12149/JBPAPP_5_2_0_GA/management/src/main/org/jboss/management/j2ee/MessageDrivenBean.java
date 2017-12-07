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
import org.jboss.management.j2ee.statistics.MessageDrivenBeanStatsImpl;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.j2ee.statistics.Stats;

/**
 * The JBoss JSR-77.3.11 implementation of the MessageDrivenBean model
 *
 * @author  <a href="mailto:andreas@jboss.org">Andreas Schaefer</a>
 * @author  <a href="mailto:thomas.diesler@jboss.org">Thomas Diesler</a>
 * @version $Revision: 81025 $
 */
public class MessageDrivenBean extends EJB
   implements MessageDrivenBeanMBean
{

   // Attributes ----------------------------------------------------
   private static Logger log = Logger.getLogger(MessageDrivenBean.class);

   private MessageDrivenBeanStatsImpl stats;

   // Static --------------------------------------------------------

   // Constructors --------------------------------------------------

   public MessageDrivenBean(String name, ObjectName ejbModuleName,
                            ObjectName ejbContainerName)
           throws MalformedObjectNameException,
           InvalidParentException
   {
      this(name, ejbModuleName, ejbContainerName, null);
   }
   /**
    * Create a MessageDrivenBean model
    *
    * @param name             the ejb-name from the deployment 
    * @param ejbModuleName    the JSR-77 EJBModule name for this bean
    * @param ejbContainerName the JMX name of the JBoss ejb container MBean
    * @param localJndiName the jndi name of the local home binding is one exists,
    *    null otherwise.
    * @throws MalformedObjectNameException
    * @throws InvalidParentException
    */
   public MessageDrivenBean(String name, ObjectName ejbModuleName,
                            ObjectName ejbContainerName, String localJndiName)
           throws MalformedObjectNameException,
           InvalidParentException
   {
      super(J2EETypeConstants.MessageDrivenBean, name, ejbModuleName,
         ejbContainerName, null, localJndiName);
      stats = new MessageDrivenBeanStatsImpl();
   }

   // Begin StatisticsProvider interface methods
   public Stats getstats()
   {
      try
      {
         updateCommonStats(stats);

         ObjectName containerName = getContainerName();
         CountStatisticImpl msgCount = (CountStatisticImpl) stats.getMessageCount();
         Long count = (Long) server.getAttribute(containerName, "MessageCount");
         msgCount.set(count.longValue());
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

   // Object overrides ---------------------------------------------------

   public String toString()
   {
      return "MessageDrivenBean[ " + super.toString() + " ]";
   }
}

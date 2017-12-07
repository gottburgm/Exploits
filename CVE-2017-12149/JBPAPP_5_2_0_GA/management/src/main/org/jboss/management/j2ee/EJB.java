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

import org.jboss.invocation.InvocationStatistics;
import org.jboss.logging.Logger;
import org.jboss.management.j2ee.statistics.CountStatisticImpl;
import org.jboss.management.j2ee.statistics.EJBStatsImpl;
import org.jboss.management.j2ee.statistics.TimeStatisticImpl;

import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.j2ee.statistics.Stats;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

/**
 * Root class of the JBoss JSR-77.3.10 EJB model
 *
 * @author  <a href="mailto:andreas@jboss.org">Andreas Schaefer</a>
 * @author  <a href="mailto:scott.stark@jboss.org">Scott Stark</a>
 * @author  <a href="mailto:thomas.diesler@jboss.org">Thomas Diesler</a>
 * @version $Revision: 81025 $
 */
public abstract class EJB extends J2EEManagedObject
   implements EJBMBean
{
   // Constants -----------------------------------------------------
   public static final int ENTITY_BEAN = 0;
   public static final int STATEFUL_SESSION_BEAN = 1;
   public static final int STATELESS_SESSION_BEAN = 2;
   public static final int MESSAGE_DRIVEN_BEAN = 3;

   // Attributes ----------------------------------------------------
   
   /** The logger */
   private static Logger log = Logger.getLogger(EJB.class);

   /**
    * The ObjectName of the ejb container MBean
    */
   protected ObjectName ejbContainerName;
   
   protected String jndiName;
   protected String localJndiName;

   // Static --------------------------------------------------------

   /**
    * Create a JSR77 EJB submodel.
    *
    * @param mbeanServer      the MBeanServer to use for mbean creation
    * @param ejbModuleName    the name of the JSR77 EJBModule mbean
    * @param ejbContainerName the name of the JBoss ejb container mbean
    * @param ejbType          an EJB.XXX_BEAN type constant value
    * @param ejbName          the bean ejb-name
    * @param jndiName         the jndi name of the remote home binding if one exists, or null
    * @param localJndiName    the jndi name of the local home binding if one exists, or null
    * @return the ObjectName of the JSR77 EJB mbean
    */
   public static ObjectName create(MBeanServer mbeanServer, ObjectName ejbModuleName,
      ObjectName ejbContainerName, int ejbType, String ejbName,
      String jndiName, String localJndiName)
   {
      try
      {
         // Now create the EJB mbean
         EJB ejb = null;
         switch (ejbType)
         {
            case ENTITY_BEAN:
               ejb = new EntityBean(ejbName, ejbModuleName, ejbContainerName,
                 jndiName, localJndiName);
               break;
            case STATEFUL_SESSION_BEAN:
               ejb = new StatefulSessionBean(ejbName, ejbModuleName,
                  ejbContainerName, jndiName, localJndiName);
               break;
            case STATELESS_SESSION_BEAN:
               ejb = new StatelessSessionBean(ejbName, ejbModuleName,
                  ejbContainerName, jndiName, localJndiName);
               break;
            case MESSAGE_DRIVEN_BEAN:
               ejb = new MessageDrivenBean(ejbName, ejbModuleName,
                  ejbContainerName, localJndiName);
               break;
         }

         ObjectName jsr77Name = ejb.getObjectName();
         mbeanServer.registerMBean(ejb, jsr77Name);
         log.debug("Created JSR-77 EJB: " + jsr77Name);
         return jsr77Name;
      }
      catch (Exception e)
      {
         log.debug("Could not create JSR-77 EJB: " + ejbName, e);
         return null;
      }
   }

   public static void destroy(MBeanServer mbeanServer, ObjectName jsr77Name)
   {
      try
      {
         // Now remove the EJB
         mbeanServer.unregisterMBean(jsr77Name);
         log.debug("Destroyed JSR-77 EJB: " + jsr77Name);
      }
      catch (javax.management.InstanceNotFoundException ignore)
      {
      }
      catch (Exception e)
      {
         log.debug("Could not destroy JSR-77 EJB: " + jsr77Name, e);
      }
   }

   // Constructors --------------------------------------------------

   /**
    * Create a EJB model
    *
    * @param ejbType          the EJB.EJB_TYPES string
    * @param ejbName          the ejb-name from the deployment
    * @param ejbModuleName    the JSR-77 EJBModule name for this bean
    * @param ejbContainerName the JMX name of the JBoss ejb container MBean
    * @throws MalformedObjectNameException
    * @throws InvalidParentException
    */
   public EJB(String ejbType, String ejbName, ObjectName ejbModuleName,
              ObjectName ejbContainerName)
           throws MalformedObjectNameException,
           InvalidParentException
   {
      this(ejbType, ejbName, ejbModuleName, ejbContainerName, null, null);
   }
   
   /**
    * Create a EJB model
    *
    * @param ejbType          the EJB.EJB_TYPES string
    * @param ejbName          the ejb-name from the deployment
    * @param ejbModuleName    the JSR-77 EJBModule name for this bean
    * @param ejbContainerName the JMX name of the JBoss ejb container MBean
    * @param jndiName the jndi name of the remote home binding is one exists,
    *    null if there is no remote home.
    * @param localJndiName the jndi name of the local home binding is one exists,
    *    null if there is no local home.
    * @throws MalformedObjectNameException
    * @throws InvalidParentException
    */
   public EJB(String ejbType, String ejbName, ObjectName ejbModuleName,
              ObjectName ejbContainerName, String jndiName, String localJndiName)
           throws MalformedObjectNameException,
           InvalidParentException
   {
      super(ejbType, ejbName, ejbModuleName);
      this.ejbContainerName = ejbContainerName;
      this.jndiName = jndiName;
      this.localJndiName = localJndiName;
   }

   // Begin StatisticsProvider interface methods

   /**
    * Obtain the Stats from the StatisticsProvider.
    *
    * @return An EJBStats subclass
    * @jmx:managed-attribute
    */
   public abstract Stats getstats();

   /**
    * Reset all statistics in the StatisticsProvider
    *
    * @jmx:managed-operation
    */
   public abstract void resetStats();
   // End StatisticsProvider interface methods

   public String getJndiName()
   {
      return this.jndiName;
   }
   
   public String getLocalJndiName()
   {
      return this.localJndiName;
   }

   // java.lang.Object overrides --------------------------------------

   public String toString()
   {
      return "EJB { " + super.toString() + " } []";
   }
   // Package protected ---------------------------------------------

   // Protected -----------------------------------------------------

   /**
    * Obtain the Stats from the StatisticsProvider. This method simply
    * updates the statistics common to all EJBs:
    * CreateCount
    * RemoveCount
    * InvocationTimes
    * <p/>
    * It should be invoked to update these common statistics.
    */
   protected void updateCommonStats(EJBStatsImpl stats)
   {
      try
      {
         ObjectName containerName = getContainerName();
         CountStatisticImpl createCount = (CountStatisticImpl) stats.getCreateCount();
         Long creates = (Long) server.getAttribute(containerName, "CreateCount");
         createCount.set(creates.longValue());
         CountStatisticImpl removeCount = (CountStatisticImpl) stats.getRemoveCount();
         Long removes = (Long) server.getAttribute(containerName, "RemoveCount");
         removeCount.set(removes.longValue());

         // Now build a TimeStatistics for every
         InvocationStatistics times = (InvocationStatistics) server.getAttribute(containerName, "InvokeStats");
         HashMap timesMap = new HashMap(times.getStats());
         Iterator iter = timesMap.entrySet().iterator();
         while (iter.hasNext())
         {
            Map.Entry entry = (Map.Entry) iter.next();
            Method m = (Method) entry.getKey();
            InvocationStatistics.TimeStatistic stat = (InvocationStatistics.TimeStatistic) entry.getValue();
            TimeStatisticImpl tstat = new TimeStatisticImpl(m.getName(), StatisticsConstants.MILLISECOND,
                    "The timing information for the given method");
            tstat.set(stat.count, stat.minTime, stat.maxTime, stat.totalTime);
            stats.addStatistic(m.getName(), tstat);
         }
      }
      catch (Exception e)
      {
         log.debug("Failed to retrieve stats", e);
      }
   }

   /**
    * @return the JMX name of the EJB container
    */
   protected ObjectName getContainerName()
   {
      return this.ejbContainerName;
   }

   /**
    * @return the JMX name of the EJB container cache
    */
   protected ObjectName getContainerCacheName()
   {
      ObjectName cacheName = null;
      try
      {
         Hashtable props = ejbContainerName.getKeyPropertyList();
         props.put("plugin", "cache");
         cacheName = new ObjectName(ejbContainerName.getDomain(), props);
      }
      catch (MalformedObjectNameException e)
      {
      }
      return cacheName;
   }

   /**
    * @return the JMX name of the EJB container pool
    */
   protected ObjectName getContainerPoolName()
   {
      ObjectName poolName = null;
      try
      {
         Hashtable props = ejbContainerName.getKeyPropertyList();
         props.put("plugin", "pool");
         poolName = new ObjectName(ejbContainerName.getDomain(), props);
      }
      catch (MalformedObjectNameException e)
      {
      }
      return poolName;
   }

   /**
    * @return A hashtable with the EJB-Module, J2EE-Application and J2EE-Server as parent
    */
   protected Hashtable getParentKeys(ObjectName pParent)
   {
      Hashtable lReturn = new Hashtable();
      Hashtable lProperties = pParent.getKeyPropertyList();
      lReturn.put(J2EETypeConstants.EJBModule, lProperties.get("name"));
      // J2EE-Application and J2EE-Server is already parent of J2EE-Application therefore lookup
      // the name by the J2EE-Server type
      lReturn.put(J2EETypeConstants.J2EEApplication, lProperties.get(J2EETypeConstants.J2EEApplication));
      lReturn.put(J2EETypeConstants.J2EEServer, lProperties.get(J2EETypeConstants.J2EEServer));
      return lReturn;
   }

   // Private -------------------------------------------------------

   // Inner classes -------------------------------------------------

}

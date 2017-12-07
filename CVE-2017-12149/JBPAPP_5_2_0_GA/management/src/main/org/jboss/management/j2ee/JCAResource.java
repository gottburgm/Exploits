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
import org.jboss.management.j2ee.statistics.JCAConnectionPoolStatsImpl;
import org.jboss.management.j2ee.statistics.JCAStatsImpl;

import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.j2ee.statistics.Stats;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;

/**
 * The JBoss JSR-77.3.22 JCAResource model implementation
 *
 * @author <a href="mailto:mclaugs@comcast.com">Scott McLaughlin</a>.
 * @author  <a href="mailto:scott.stark@jboss.org">Scott Stark</a>
 * @author  <a href="mailto:thomas.diesler@jboss.org">Thomas Diesler</a>
 * @version $Revision: 81025 $
 */
public class JCAResource extends J2EEResource
   implements JCAResourceMBean
{
   // Constants -----------------------------------------------------
   private static Logger log = Logger.getLogger(JCAResource.class);

   // Attributes ----------------------------------------------------

   // list of object names as strings
   private List connectionFactories = new ArrayList();

   private ObjectName cmServiceName;
   private ObjectName poolServiceName;
   private JCAStatsImpl stats;

   // Static --------------------------------------------------------

   /**
    * Create a JCAResource
    *
    * @param mbeanServer
    * @param resName
    * @param jsr77RAParentName
    * @param cmServiceName
    * @param mcfServiceName
    * @param poolServiceName
    * @return
    */
   public static ObjectName create(MBeanServer mbeanServer, String resName,
                                   ObjectName jsr77RAParentName, ObjectName cmServiceName,
                                   ObjectName mcfServiceName, ObjectName poolServiceName)
   {
      ObjectName jsr77Name = null;
      try
      {
         JCAResource jcaRes = new JCAResource(resName, jsr77RAParentName,
                 cmServiceName, poolServiceName);
         jsr77Name = jcaRes.getObjectName();
         mbeanServer.registerMBean(jcaRes, jsr77Name);
         log.debug("Created JSR-77 JCAResource: " + resName);

         // Create a JCAConnectionFactory and JCAManagedConnectionFactory
         ObjectName jcaFactoryName = JCAConnectionFactory.create(mbeanServer,
                 resName, jsr77Name, cmServiceName, mcfServiceName);
      }
      catch (Exception e)
      {
         log.debug("Could not create JSR-77 JCAResource: " + resName, e);
      }
      return jsr77Name;
   }

   public static void destroy(MBeanServer mbeanServer, String resName)
   {
      try
      {
         // Find the Object to be destroyed
         ObjectName search = new ObjectName(J2EEDomain.getDomainName() + ":" +
                 J2EEManagedObject.TYPE + "=" + J2EETypeConstants.JCAResource + "," +
                 "name=" + resName + "," +
                 "*");
         Set resNames = mbeanServer.queryNames(search, null);
         if( resNames.isEmpty() == false )
         {
            ObjectName jcaRes = (ObjectName) resNames.iterator().next();
            // Now check if the JCAResource does not contains another Connection Factory
            String[] factories = (String[]) mbeanServer.getAttribute(jcaRes,
                    "connectionFactories");
            for(int n = 0; n < factories.length; n ++)
            {
               // Remove the JCAConnectionFactory
               ObjectName cf = new ObjectName(factories[n]);
               mbeanServer.unregisterMBean(cf);
               /* Remove the JCAManagedConnectionFactory using a name of the
               form *:J2EEServer=*,j2eeType=JCAManagedConnectionFactory,name=resName
               */
               Hashtable props = cf.getKeyPropertyList();
               props.put("j2eeType", J2EETypeConstants.JCAManagedConnectionFactory);
               props.remove(J2EETypeConstants.JCAResource);
               ObjectName mcf = new ObjectName(cf.getDomain(), props);
               mbeanServer.unregisterMBean(mcf);
            }
            mbeanServer.unregisterMBean(jcaRes);
         }
      }
      catch (Exception e)
      {
         log.debug("Could not destroy JSR-77 JCAResource", e);
      }
   }

   // Constructors --------------------------------------------------

   /**
    * @param resName
    * @param jsr77ParentName
    * @param cmServiceName
    * @param poolServiceName
    * @throws MalformedObjectNameException
    * @throws InvalidParentException
    */
   public JCAResource(String resName, ObjectName jsr77ParentName,
                      ObjectName cmServiceName, ObjectName poolServiceName)
           throws MalformedObjectNameException, InvalidParentException
   {
      super(J2EETypeConstants.JCAResource, resName, jsr77ParentName);
      this.cmServiceName = cmServiceName;
      this.poolServiceName = poolServiceName;
   }

   // Public --------------------------------------------------------

   // javax.management.j2ee.JCAResource implementation ---------------------

   /**
    * @jmx:managed-attribute
    */
   public String[] getconnectionFactories()
   {
      return (String[]) connectionFactories.toArray(new String[connectionFactories.size()]);
   }

   /**
    * @jmx:managed-operation
    */
   public String getconnectionFactory(int i)
   {
      if (i >= 0 && i < connectionFactories.size())
      {
         return (String) connectionFactories.get(i);
      }
      else
      {
         return null;
      }
   }

   // J2EEManagedObjectMBean implementation -------------------------

   public void addChild(ObjectName pChild)
   {
      String lType = J2EEManagedObject.getType(pChild);
      if (J2EETypeConstants.JCAConnectionFactory.equals(lType))
      {
         connectionFactories.add(pChild.getCanonicalName());
      }
   }

   public void removeChild(ObjectName pChild)
   {
      String lType = J2EEManagedObject.getType(pChild);
      if (J2EETypeConstants.JCAConnectionFactory.equals(lType))
      {
         connectionFactories.remove(pChild.getCanonicalName());
      }
   }

   // Begin StatisticsProvider interface methods

   /**
    * Obtain the Stats from the StatisticsProvider.
    *
    * @return An JCAStats implementation
    * @jmx:managed-attribute
    */
   public Stats getstats()
   {
      try
      {
         ObjectName jsr77CFName = newObjectName(getconnectionFactory(0));
         Object[] params = {poolServiceName};
         String[] sig = {ObjectName.class.getName()};
         JCAConnectionPoolStatsImpl cfStats = (JCAConnectionPoolStatsImpl)
                 server.invoke(jsr77CFName, "getPoolStats", params, sig);
         JCAConnectionPoolStatsImpl[] poolStats = {cfStats};
         stats = new JCAStatsImpl(null, poolStats);
      }
      catch (Exception e)
      {
         log.debug("Failed to create JCAStats", e);
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
      if (stats != null)
         stats.reset();
   }
   // End StatisticsProvider interface methods

   // java.lang.Object overrides ------------------------------------

   public String toString()
   {
      return "JCAResource { " + super.toString() + " } [ " +
              "Connection Factories: " + connectionFactories +
              " ]";
   }

   /**
    * @return A hashtable with the J2EEServer and ResourceAdapter
    */
   protected Hashtable getParentKeys(ObjectName parentName)
   {
      Hashtable keys = new Hashtable();
      Hashtable nameProps = parentName.getKeyPropertyList();
      String adapterName = (String) nameProps.get("name");
      String serverName = (String) nameProps.get(J2EETypeConstants.J2EEServer);
      keys.put(J2EETypeConstants.J2EEServer, serverName);
      keys.put(J2EETypeConstants.ResourceAdapter, adapterName);
      return keys;
   }
}

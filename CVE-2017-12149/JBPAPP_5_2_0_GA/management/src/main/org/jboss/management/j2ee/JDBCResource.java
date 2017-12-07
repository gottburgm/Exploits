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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.management.JMException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.jboss.logging.Logger;
import org.jboss.system.ServiceMBean;

/**
 * Root class of the JBoss JSR-77 implementation of
 * {@link javax.management.j2ee.JDBCResource JDBCResource}.
 *
 * @author  <a href="mailto:andreas@jboss.org">Andreas Schaefer</a>
 * @author  <a href="mailto:thomas.diesler@jboss.org">Thomas Diesler</a>
 * @author  <a href="mailto:dimitris@jboss.org">Dimitris Andreadis</a>
 * @version $Revision: 81025 $
 * 
 * @todo This resource should not implement state manageable because it
 * has no MBean/Service associated but codes stays.
 */
public class JDBCResource extends J2EEResource
   implements JDBCResourceMBean
{
   // Constants -----------------------------------------------------
   private static Logger log = Logger.getLogger(JDBCResource.class);

   // Attributes ----------------------------------------------------

   private StateManagement mState;
   private ObjectName mService;

   // list of object names as strings
   private List mDataSources = new ArrayList();

   // Static --------------------------------------------------------

   public static ObjectName create(MBeanServer mbeanServer, String resName)
   {
      ObjectName lServer = null;
      try
      {
         lServer = (ObjectName) mbeanServer.queryNames(new ObjectName(J2EEDomain.getDomainName() + ":" +
                 J2EEManagedObject.TYPE + "=" + J2EETypeConstants.J2EEServer + "," +
                 "*"),
                 null).iterator().next();
      }
      catch (Exception e)
      {
         log.error("Could not find parent J2EEServer", e);
         return null;
      }
      try
      {
         JDBCResource jdbcRes = new JDBCResource(resName, lServer);
         ObjectName jsr77Name = jdbcRes.getObjectName();
         mbeanServer.registerMBean(jdbcRes, jsr77Name);
         log.debug("Created JSR-77 JDBC Manager: " + resName);
         
         return jsr77Name;
      }
      catch (Exception e)
      {
         log.error("Could not create JSR-77 JDBC Manager", e);
         return null;
      }
   }

   public static void destroy(MBeanServer mbeanServer, String resName)
   {
      try
      {
         // Find the Object to be destroyed
         ObjectName lSearch = new ObjectName(J2EEDomain.getDomainName() + ":" +
                 J2EEManagedObject.TYPE + "=" + J2EETypeConstants.JDBCResource + "," +
                 "name=" + resName + "," +
                 "*");
         Set lNames = mbeanServer.queryNames(lSearch,
                 null);
         if (!lNames.isEmpty())
         {
            ObjectName lJDBCResource = (ObjectName) lNames.iterator().next();
            // Now check if the JDBCResource Manager does not contains another DataSources
            String[] lDataSources = (String[]) mbeanServer.getAttribute(lJDBCResource,
                    "jdbcDataSources");
            if (lDataSources.length == 0)
            {
               // Remove it because it does not reference any JDBC DataSources
               mbeanServer.unregisterMBean(lJDBCResource);
            }
         }
      }
      catch (Exception e)
      {
         log.error("Could not destroy JSR-77 JDBC Manager", e);
      }
   }

   // Constructors --------------------------------------------------

   /**
    * @param pName Name of the JDBCResource
    */
   public JDBCResource(String resName, ObjectName pServer)
           throws
           MalformedObjectNameException,
           InvalidParentException
   {
      super(J2EETypeConstants.JDBCResource, resName, pServer);
      mState = new StateManagement(this);
   }

   // Public --------------------------------------------------------

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

   // javax.management.j2ee.StateManageable implementation ----------

   public long getStartTime()
   {
      return mState.getStartTime();
   }

   public int getState()
   {
      return mState.getState();
   }
   public String getStateString()
   {
      return mState.getStateString();
   }

   public void mejbStart()
   {
      // No component behind therefore just do it as it is started
      mState.setState(ServiceMBean.STARTING + 2);
      mState.setState(ServiceMBean.STARTED + 2);
   }

   public void mejbStartRecursive()
   {
      mState.setState(ServiceMBean.STOPPING + 2);
      Iterator i = mDataSources.iterator();
      String lDataSource = null;
      while (i.hasNext())
      {
         lDataSource = (String) i.next();
         try
         {
            getServer().invoke(newObjectName(lDataSource),
                    "mejbStart",
                    new Object[]{},
                    new String[]{});
         }
         catch (JMException jme)
         {
            getLog().error("Could not start JSR-77 JDBC-DataSource: " + lDataSource, jme);
         }
      }
      mState.setState(ServiceMBean.STOPPED + 2);
   }

   public void mejbStop()
   {
      // No component behind therefore just do it as it is started
      mState.setState(3);
      Iterator i = mDataSources.iterator();
      while (i.hasNext())
      {
         String lDataSource = (String) i.next();
         try
         {
            getServer().invoke(newObjectName(lDataSource),
                    "mejbStop",
                    new Object[]{},
                    new String[]{});
         }
         catch (JMException jme)
         {
            getLog().error("Could not stop JSR-77 JDBC-DataSource: " + lDataSource, jme);
         }
      }
      // No component behind therefore just do it as it is started
      mState.setState(2);
   }

   /**
    * @todo Listener cannot be used right now because there is no MBean associated
    * to it and therefore no state management possible but currently it stays
    * StateManageable to save the code.
    */
   public void postCreation()
   {
      sendNotification(NotificationConstants.OBJECT_DELETED, "JDBC Resource Resource deleted");
   }

   /**
    * @todo Listener cannot be used right now because there is no MBean associated
    * to it and therefore no state management possible but currently it stays
    * StateManageable to save the code.
    */
   public void preDestruction()
   {
      sendNotification(NotificationConstants.OBJECT_CREATED, "JDBC Resource Resource created");
   }

   // javax.management.j2ee.JDBCResource implementation ---------------------

   /**
    * @jmx:managed-attribute
    */
   public String[] getjdbcDataSources()
   {
      return (String[]) mDataSources.toArray(new String[mDataSources.size()]);
   }

   /**
    * @jmx:managed-operation
    */
   public String getjdbcDataSource(int pIndex)
   {
      if (pIndex >= 0 && pIndex < mDataSources.size())
      {
         return (String) mDataSources.get(pIndex);
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
      if (J2EETypeConstants.JDBCDataSource.equals(lType))
      {
         mDataSources.add(pChild.getCanonicalName());
      }
   }

   public void removeChild(ObjectName pChild)
   {
      String lType = J2EEManagedObject.getType(pChild);
      if (J2EETypeConstants.JDBCDataSource.equals(lType))
      {
         mDataSources.remove(pChild.getCanonicalName());
      }
   }

   // java.lang.Object overrides ------------------------------------

   public String toString()
   {
      return "JDBCResource { " + super.toString() + " } [ " +
              "Datasources: " + mDataSources +
              " ]";
   }

   // Package protected ---------------------------------------------

   // Protected -----------------------------------------------------

   // Private -------------------------------------------------------

   // Inner classes -------------------------------------------------
}

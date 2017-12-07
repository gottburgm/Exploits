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

import javax.management.JMException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import java.util.Hashtable;
import java.util.Set;

/**
 * Root class of the JBoss JSR-77 implementation of
 * {@link org.jboss.management.j2ee.JDBCDataSource JDBCDataSource}.
 *
 * @author  <a href="mailto:andreas@jboss.org">Andreas Schaefer</a>
 * @version $Revision: 81025 $
 */
public class JDBCDataSource extends J2EEManagedObject
   implements JDBCDataSourceMBean
{
   // Constants -----------------------------------------------------
   private static Logger log = Logger.getLogger(JDBCDataSource.class);

   // Attributes ----------------------------------------------------

   private StateManagement mState;
   private ObjectName mService;
   private ObjectName mJdbcDriver;

   // Static --------------------------------------------------------

   public static ObjectName create(MBeanServer pServer, String pName, ObjectName pService)
   {
      ObjectName lServer = null;
      try
      {
         lServer = (ObjectName) pServer.queryNames(new ObjectName(J2EEDomain.getDomainName() + ":" +
                 J2EEManagedObject.TYPE + "=" + J2EETypeConstants.J2EEServer + "," +
                 "*"),
                 null).iterator().next();
      }
      catch (Exception e)
      {
         log.error("Could not locate JSR-77 Server: " + pName, e);
         // Return because without the JDBC manager go on does not work
         return null;
      }
      // First create its parent the JDBC resource
      ObjectName lJDBC = null;
      try
      {
         // Check if the JDBC Manager exists and if not create one
         Set lNames = pServer.queryNames(new ObjectName(J2EEDomain.getDomainName() + ":" +
                 J2EEManagedObject.TYPE + "=" + J2EETypeConstants.JDBCResource + "," +
                 "*"),
                 null);
         if (lNames.isEmpty())
         {
            // Now create the JDBC Manager
            lJDBC = JDBCResource.create(pServer, "JDBC");
         }
         else
         {
            lJDBC = (ObjectName) lNames.iterator().next();
         }
      }
      catch (Exception e)
      {
         log.error("Could not create JSR-77 JDBC Manager", e);
         // Return because without the JDBC manager go on does not work
         return null;
      }

      try
      {
         //AS ToDo: Replace any ':' by '~' do avoid ObjectName conflicts for now
         //AS FixMe: look for a solution
         pName = pName.replace(':', '~');
         // Now create the JNDI Representant
         return pServer.createMBean("org.jboss.management.j2ee.JDBCDataSource",
                 null,
                 new Object[]{
                    pName,
                    lJDBC,
                    pService
                 },
                 new String[]{
                    String.class.getName(),
                    ObjectName.class.getName(),
                    ObjectName.class.getName()
                 }).getObjectName();
      }
      catch (Exception e)
      {
         log.error("Could not create JSR-77 JDBC DataSource: " + pName, e);
         return null;
      }
   }

   public static void destroy(MBeanServer pServer, String pName)
   {
      try
      {
         J2EEManagedObject.removeObject(pServer,
                 J2EEDomain.getDomainName() + ":" +
                 J2EEManagedObject.TYPE + "=" + J2EETypeConstants.JDBCDataSource + "," +
                 "name=" + pName + "," +
                 "*");
         // Now let us try to destroy the JDBC Manager
         JDBCResource.destroy(pServer, "JDBC");
      }
      catch (Exception e)
      {
         log.error("Could not destroy JSR-77 JDBC DataSource: " + pName, e);
      }
   }

   // Constructors --------------------------------------------------

   /**
    * @param pName Name of the JDBCDataSource
    * @throws MalformedObjectNameException
    * @throws InvalidParentException
    */
   public JDBCDataSource(String pName, ObjectName pServer, ObjectName pService)
           throws
           MalformedObjectNameException,
           InvalidParentException
   {
      super(J2EETypeConstants.JDBCDataSource, pName, pServer);
      mService = pService;
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
      try
      {
         getServer().invoke(mService,
                 "start",
                 new Object[]{},
                 new String[]{});
      }
      catch (Exception e)
      {
         getLog().error("start failed", e);
      }
   }

   public void mejbStartRecursive()
   {
      mejbStart();
   }

   public void mejbStop()
   {
      try
      {
         getServer().invoke(mService,
                 "stop",
                 new Object[]{},
                 new String[]{});
      }
      catch (Exception e)
      {
         getLog().error("Stop of JDBCDataSource failed", e);
      }
   }

   public void postCreation()
   {
      try
      {
         getServer().addNotificationListener(mService, mState, null, null);
      }
      catch (JMException jme)
      {
         getLog().debug("Could not add listener at target service", jme);
      }
      sendNotification(NotificationConstants.OBJECT_CREATED, "JDBC DataSource Resource deleted");
   }

   public void preDestruction()
   {
      sendNotification(NotificationConstants.OBJECT_DELETED, "JDBC DataSource Resource deleted");
      // Remove the listener of the target MBean
      try
      {
         getServer().removeNotificationListener(mService, mState);
      }
      catch (JMException jme)
      {
         // When the service is not available anymore then just ignore the exception
      }
   }

   // javax.management.j2ee.JDBCDataSource implementation -----------------

   public ObjectName getJdbcDriver()
   {
      return mJdbcDriver;
   }

   // java.lang.Object overrides ------------------------------------

   public String toString()
   {
      return "JDBCDatasource { " + super.toString() + " } [ " +
              " ]";
   }

   // Package protected ---------------------------------------------

   // Protected -----------------------------------------------------

   /**
    * @return A hashtable with the JDBC-Resource and J2EE-Server as parent
    */
   protected Hashtable getParentKeys(ObjectName pParent)
   {
      Hashtable lReturn = new Hashtable();
      Hashtable lProperties = pParent.getKeyPropertyList();
      lReturn.put(J2EETypeConstants.JDBCResource, lProperties.get("name"));
      // J2EE-Server is already parent of J2EE-Application therefore lookup
      // the name by the J2EE-Server type
      lReturn.put(J2EETypeConstants.J2EEServer, lProperties.get(J2EETypeConstants.J2EEServer));

      return lReturn;
   }

   // Private -------------------------------------------------------

   // Inner classes -------------------------------------------------
}

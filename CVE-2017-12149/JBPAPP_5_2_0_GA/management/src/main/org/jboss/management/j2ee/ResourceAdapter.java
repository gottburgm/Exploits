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

import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import java.util.Hashtable;

/**
 * Root class of the JBoss JSR-77 implementation of ResourceAdapter.
 *
 * @author <a href="mailto:mclaugs@comcast.net">Scott McLaughlin</a>.
 * @version $Revision: 81025 $
 */
public class ResourceAdapter extends J2EEManagedObject
   implements ResourceAdapterMBean
{
   // Constants -----------------------------------------------------

   // Attributes ----------------------------------------------------
   private static Logger log = Logger.getLogger(ResourceAdapter.class);

   /**
    * The JBoss RAR service MBean name
    */
   private ObjectName rarServiceName;
   
   /**
    * The JSR77 JCAResource associated with this adapter
    */
   private ObjectName jcaResourceName;

   // Static --------------------------------------------------------

   public static ObjectName create(MBeanServer mbeanServer, String displayName,
                                   ObjectName jsr77ParentName, ObjectName rarServiceName)
   {
      ObjectName jsr77Name = null;
      try
      {
         ResourceAdapter adapter = new ResourceAdapter(displayName, jsr77ParentName,
                 rarServiceName);
         jsr77Name = adapter.getObjectName();
         mbeanServer.registerMBean(adapter, jsr77Name);
         log.debug("Created JSR-77 ResourceAdapter: " + displayName);
      }
      catch (Exception e)
      {
         log.debug("Could not create JSR-77 ResourceAdapter: " + displayName, e);
      }
      return jsr77Name;
   }

   public static void destroy(MBeanServer mbeanServer, String displayName)
   {
      try
      {
         J2EEManagedObject.removeObject(mbeanServer,
                 J2EEDomain.getDomainName() + ":" +
                 J2EEManagedObject.TYPE + "=" + J2EETypeConstants.ResourceAdapter + "," +
                 "name=" + displayName + "," +
                 "*");
      }
      catch (Exception e)
      {
         log.error("Could not destroy JSR-77 ResourceAdapter: " + displayName, e);
      }
   }

   // Constructors --------------------------------------------------

   /**
    * @param displayName     The ra.xml/connector/display-name value
    * @param jsr77ParentName ObjectName of the ResourceAdaptorModule
    */
   public ResourceAdapter(String displayName, ObjectName jsr77ParentName,
                          ObjectName rarServiceName)
           throws MalformedObjectNameException,
           InvalidParentException
   {
      super(J2EETypeConstants.ResourceAdapter, displayName, jsr77ParentName);
      this.rarServiceName = rarServiceName;
   }

   /**
    * @jmx:managed-attribute
    */
   public ObjectName getJBossServiceName()
   {
      return rarServiceName;
   }

   /**
    * @jmx:managed-attribute
    */
   public ObjectName getJcaResource()
   {
      return jcaResourceName;
   }
   /**
    * @jmx:managed-attribute
    */
   public ObjectName getjcaResource()
   {
      return jcaResourceName;
   }

   // java.lang.Object overrides --------------------------------------

   public String toString()
   {
      return "ResourceAdapter { " + super.toString() + " } []";
   }

   public void addChild(ObjectName j2eeName)
   {
      String j2eeType = J2EEManagedObject.getType(j2eeName);
      if (J2EETypeConstants.JCAResource.equals(j2eeType))
      {
         jcaResourceName = j2eeName;
      }
   }

   public void removeChild(ObjectName j2eeName)
   {
      String j2eeType = J2EEManagedObject.getType(j2eeName);
      if (J2EETypeConstants.JCAResource.equals(j2eeType))
      {
         jcaResourceName = null;
      }
   }

   // Package protected ---------------------------------------------

   // Protected -----------------------------------------------------

   /**
    * @return A hashtable with the Resource-Adapter-Module, J2EE-Application and J2EE-Server as parent
    */
   protected Hashtable getParentKeys(ObjectName pParent)
   {
      Hashtable lReturn = new Hashtable();
      Hashtable lProperties = pParent.getKeyPropertyList();
      lReturn.put(J2EETypeConstants.ResourceAdapterModule, lProperties.get("name"));
      // J2EE-Application and J2EE-Server is already parent of J2EE-Application therefore lookup
      // the name by the J2EE-Server type
      lReturn.put(J2EETypeConstants.J2EEApplication, lProperties.get(J2EETypeConstants.J2EEApplication));
      lReturn.put(J2EETypeConstants.J2EEServer, lProperties.get(J2EETypeConstants.J2EEServer));

      return lReturn;
   }

   // Private -------------------------------------------------------

   // Inner classes -------------------------------------------------

}

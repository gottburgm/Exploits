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
 * Root class of the JBoss JSR-77 implementation of JCAManagedConnectionFactory.
 *
 * @author <a href="mailto:mclaugs@comcast.net">Scott McLaughlin</a>.
 * @version $Revision: 81025 $
 */
public class JCAManagedConnectionFactory extends J2EEManagedObject
   implements JCAManagedConnectionFactoryMBean
{
   // Constants -----------------------------------------------------
   private static Logger log = Logger.getLogger(JCAManagedConnectionFactory.class);

   // Attributes ----------------------------------------------------

   // Static --------------------------------------------------------

   public static ObjectName create(MBeanServer mbeanServer, String resName,
                                   ObjectName jsr77ParentName)
   {
      ObjectName jsr77Name = null;
      try
      {
         JCAManagedConnectionFactory mcf = new JCAManagedConnectionFactory(resName,
                 jsr77ParentName);
         jsr77Name = mcf.getObjectName();
         mbeanServer.registerMBean(mcf, jsr77Name);
      }
      catch (Exception e)
      {
         log.debug("Could not create JSR-77 JCAManagedConnectionFactory: " + resName, e);
      }
      return jsr77Name;
   }

   public static void destroy(MBeanServer mbeanServer, String resName)
   {
      try
      {
         // Remove the JCAManagedConnectionFactory associated with resName
         String mcfName = J2EEDomain.getDomainName() + ":" +
                 J2EEManagedObject.TYPE + "=" + J2EETypeConstants.JCAManagedConnectionFactory
                 + ",name=" + resName + ",*";
         J2EEManagedObject.removeObject(mbeanServer, mcfName);
      }
      catch (Exception e)
      {
         log.debug("Could not destroy JSR-77 JCAManagedConnectionFactory: " + resName, e);
      }
   }

   // Constructors --------------------------------------------------

   /**
    **/
   public JCAManagedConnectionFactory(String resName, ObjectName jsr77ParentName)
           throws MalformedObjectNameException, InvalidParentException
   {
      super(J2EETypeConstants.JCAManagedConnectionFactory, resName, jsr77ParentName);
   }

   // Public --------------------------------------------------------

   // java.lang.Object overrides ------------------------------------

   public String toString()
   {
      return "JCAManagedConnectionFactory { " + super.toString() + " } [ " +
              " ]";
   }

   /**
    * @return A hashtable with the JCAResource and J2EEServer
    */
   protected Hashtable getParentKeys(ObjectName parentName)
   {
      Hashtable keys = new Hashtable();
      Hashtable nameProps = parentName.getKeyPropertyList();
      String serverName = (String) nameProps.get(J2EETypeConstants.J2EEServer);
      keys.put(J2EETypeConstants.J2EEServer, serverName);
      return keys;
   }

}

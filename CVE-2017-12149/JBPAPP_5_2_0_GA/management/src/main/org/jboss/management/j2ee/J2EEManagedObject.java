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

import java.security.InvalidParameterException;
import java.util.Hashtable;
import java.util.Set;

import javax.management.InstanceNotFoundException;
import javax.management.JMException;
import javax.management.MBeanRegistration;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.Notification;
import javax.management.ObjectName;

import org.jboss.logging.Logger;
import org.jboss.management.j2ee.statistics.StatisticsProvider;
import org.jboss.mx.util.JBossNotificationBroadcasterSupport;
import org.jboss.mx.util.ObjectNameConverter;

/**
 * Root class of the JBoss JSR-77 implementation of J2EEManagedObject.
 *
 * @author  <a href="mailto:andreas@jboss.org">Andreas Schaefer</a>
 * @author  <a href="mailto:scott.stark@jboss.org">Scott Stark</a>
 * @author  <a href="mailto:thomas.diesler@jboss.org">Thomas Diesler</a>
 * @author  <a href="mailto:dimitris@jboss.org">Dimitris Andreadis</a>
 * @version $Revision: 81025 $
 */
public abstract class J2EEManagedObject extends JBossNotificationBroadcasterSupport
   implements J2EEManagedObjectMBean, MBeanRegistration
{
   // Constants -----------------------------------------------------
   public static final String TYPE = "j2eeType";
   public static final String NAME = "name";

   // Attributes ----------------------------------------------------
   private static Logger log = Logger.getLogger(J2EEManagedObject.class);

   private ObjectName parentName = null;
   private ObjectName name = null;
   
   protected MBeanServer server;

   // Static --------------------------------------------------------

   /**
    * Retrieves the type out of an JSR-77 object name
    *
    * @param pName Object Name to check if null then
    *              it will be treated like NO type found
    * @return The type of the given Object Name or an EMPTY
    *         string if either Object Name null or type not found
    */
   protected static String getType(String pName)
   {
      String lType = null;
      if (pName != null)
      {
         ObjectName oname = newObjectName(pName);
         lType = (String) oname.getKeyPropertyList().get(TYPE);
      }
      // Return an empty string if type not found
      return lType == null ? "" : lType;
   }

   /**
    * Retrieves the type out of an JSR-77 object name
    *
    * @param pName Object Name to check if null then
    *              it will be treated like NO type found
    * @return The type of the given Object Name or an EMPTY
    *         string if either Object Name null or type not found
    */
   protected static String getType(ObjectName pName)
   {
      String lType = null;
      if (pName != null)
      {
         lType = (String) pName.getKeyPropertyList().get(TYPE);
      }
      // Return an empty string if type not found
      return lType == null ? "" : lType;
   }

   /**
    * Return the ObjectName that is represented by the given string.
    *
    * @param pName a object name
    */
   protected static ObjectName newObjectName(String pName)
   {
      try
      {
         return new ObjectName(pName);
      }
      catch (MalformedObjectNameException e)
      {
         throw new IllegalArgumentException("Invalid object name: " + pName);
      }
   }

   protected static ObjectName removeObject(MBeanServer pServer, String pSearchCriteria)
           throws JMException
   {
      ObjectName lSearch = ObjectNameConverter.convert(pSearchCriteria);
      log.debug("removeObject(), search for: " + pSearchCriteria + ", search criteria: " + lSearch);
      Set lNames = pServer.queryNames(lSearch, null);
      if (!lNames.isEmpty())
      {
         ObjectName lName = (ObjectName) lNames.iterator().next();
         pServer.unregisterMBean(lName);
         return lName;
      }
      return null;
   }

   protected static ObjectName removeObject(MBeanServer pServer, String pName, String pSearchCriteria)
           throws JMException
   {
      String lEncryptedName = ObjectNameConverter.convertCharacters(pName, true);
      ObjectName lSearch = new ObjectName(pSearchCriteria + "," + NAME + "=" + lEncryptedName);
      log.debug("removeObject(), name: " + pName + ", encrypted name: " + lEncryptedName + ", search criteria: " + lSearch);
      Set lNames = pServer.queryNames(lSearch, null);
      if (!lNames.isEmpty())
      {
         ObjectName lName = (ObjectName) lNames.iterator().next();
         pServer.unregisterMBean(lName);
         return lName;
      }
      return null;
   }

   // Constructors --------------------------------------------------

   /**
    * Constructor for the root J2EEDomain object
    *
    * @param domainName domain portion to use for the JMX ObjectName
    * @param j2eeType   JSR77 j2ee-type of the resource being created
    * @param resName    Name of the managed resource
    * @throws InvalidParameterException If the given Domain Name, Type or Name is null
    */
   public J2EEManagedObject(String domainName, String j2eeType, String resName)
           throws MalformedObjectNameException
   {
      log = Logger.getLogger(getClass());
      if (domainName == null)
      {
         throw new InvalidParameterException("Domain Name must be set");
      }
      Hashtable lProperties = new Hashtable();
      lProperties.put(TYPE, j2eeType);
      lProperties.put(NAME, resName);
      name = ObjectNameConverter.convert(domainName, lProperties);
      log.debug("ctor, name: " + name);
   }

   /**
    * Constructor for any Managed Object except the root J2EEMangement.
    *
    * @param j2eeType        JSR77 j2ee-type of the resource being created
    * @param resName         name of the resource
    * @param jsr77ParentName Object Name of the parent of this Managed Object
    *                        which must be defined
    * @throws InvalidParameterException If the given Type, Name or Parent is null
    */
   public J2EEManagedObject(String j2eeType, String resName, ObjectName jsr77ParentName)
           throws MalformedObjectNameException,
           InvalidParentException
   {
      log = Logger.getLogger(getClass());
      Hashtable lProperties = getParentKeys(jsr77ParentName);
      lProperties.put(TYPE, j2eeType);
      lProperties.put(NAME, resName);
      name = ObjectNameConverter.convert(J2EEDomain.getDomainName(), lProperties);
      setparent(jsr77ParentName.getCanonicalName());
   }

   // Public --------------------------------------------------------

   public Logger getLog()
   {
      return log;
   }
   
   public MBeanServer getServer()
   {
      return server;
   }

   public ObjectName getObjectName()
   {
      return name;
   }
   
   // J2EEManagedObjectMBean implementation ----------------------------------------------

   /**
    * @jmx:managed-attribute
    */
   public String getobjectName()
   {
      return name.getCanonicalName();
   }

   /**
    * @jmx:managed-attribute
    */
   public String getparent()
   {
      String parent = null;
      
      if (parentName != null)
         parent = parentName.getCanonicalName();
      
      return parent;
   }

   /**
    * @jmx:managed-attribute
    */
   public void setparent(String pParent) throws InvalidParentException
   {
      if (pParent == null)
      {
         throw new InvalidParameterException("Parent must be set");
      }
      parentName = newObjectName(pParent);
   }

   /**
    * @jmx:managed-operation
    */
   public void addChild(ObjectName pChild)
   {
   }

   /**
    * @jmx:managed-operation
    */
   public void removeChild(ObjectName pChild)
   {
   }

   /**
    * @jmx:managed-attribute
    */
   public boolean isstateManageable()
   {
      return this instanceof StateManageable;
   }

   /**
    * @jmx:managed-attribute
    */
   public boolean isstatisticsProvider()
   {
      return this instanceof StatisticsProvider;
   }

   /**
    * @jmx:managed-attribute
    */
   public boolean iseventProvider()
   {
      return this instanceof EventProvider;
   }

   // MBeanRegistration implementation ------------------------------
   
   public ObjectName preRegister(MBeanServer server, ObjectName name)
   {
      this.server = server;
      return name;
   }

   /**
    * Last steps to be done after MBean is registered on MBeanServer. This
    * method is made final because it contains vital steps mandatory to all
    * J2EEManagedObjects. To perform your own Post-Creation steps please
    * override {@link #postCreation postCreation()} method.
    */
   public final void postRegister(Boolean registrationDone)
   {
      // This try-catch block is here because of debugging purposes because
      // runtime exception in JMX client is a awful thing to figure out
      try
      {
         log.debug("postRegister(), parent: " + parentName);
         if (registrationDone.booleanValue())
         {
            // Let the subclass handle post creation steps
            postCreation();
            if (parentName != null)
            {
               try
               {
                  // Notify the parent about its new child
                  if (parentName.getKeyProperty("name").compareTo("null") != 0)
                  {
                     getServer().invoke(parentName,
                             "addChild",
                             new Object[]{name},
                             new String[]{ObjectName.class.getName()});
                  }
                  else
                  {
                     ObjectName j2eeServerName = J2EEDomain.getDomainServerName(server);
                     server.invoke(j2eeServerName,
                             "addChild",
                             new Object[]{name},
                             new String[]{ObjectName.class.getName()});
                  }
               }
               catch (JMException e)
               {
                  log.debug("Failed to add child", e);
                  registrationDone = Boolean.FALSE;
               }
            }
         }
      }
      catch (RuntimeException re)
      {
         log.debug("postRegister() caught this exception", re);
         throw re;
      }
   }

   /**
    * Last steps to be done before MBean is unregistered on MBeanServer. This
    * method is made final because it contains vital steps mandatory to all
    * J2EEManagedObjects. To perform your own Pre-Destruction steps please
    * override {@link #preDestruction preDestruction()} method.
    */
   public final void preDeregister()
           throws Exception
   {
      log.debug("preDeregister(), parent: " + parentName);
      // Only remove child if it is a child (root has not parent)
      if (parentName != null)
      {
         try
         {
            // Notify the parent about the removal of its child
            server.invoke(parentName,
                    "removeChild",
                    new Object[]{name},
                    new String[]{ObjectName.class.getName()});
         }
         catch (InstanceNotFoundException infe)
         {
         }
         preDestruction();
      }
   }
   public void postDeregister()
   {
      server = null;
   }

   /**
    * An overload of the super sendNotification that only takes the event
    * type and msg. The source will be set to the managed object name, the
    * sequence will be the getNextNotificationSequenceNumber() value, and the
    * timestamp System.currentTimeMillis().
    *
    * @param type the notification event type
    * @param info the notification event msg info
    */
   public void sendNotification(String type, String info)
   {
      Notification msg = new Notification(type, this.getObjectName(),
              this.getNextNotificationSequenceNumber(),
              System.currentTimeMillis(),
              info);
      super.sendNotification(msg);
   }

   // Object overrides ---------------------------------------------------

   public String toString()
   {
      return "J2EEManagedObject [ name: " + name + ", parent: " + parentName + " ];";
   }

   // Package protected ---------------------------------------------

   // Protected -----------------------------------------------------

   protected void postCreation()
   {
   }

   protected void preDestruction()
   {
   }

   /**
    * This method can be overwritten by any subclass which must
    * return &lt;parent-j2eeType&gt; indicating its parents. By
    * default it returns an empty hashtable instance.
    *
    * @param pParent The direct parent of this class
    * @return An empty hashtable
    */
   protected Hashtable getParentKeys(ObjectName pParent)
   {
      return new Hashtable();
   }

   /**
    * The <code>getNextNotificationSequenceNumber</code> method returns 
    * the next sequence number for use in notifications.
    *
    * @return a <code>long</code> value
    */
   protected long getNextNotificationSequenceNumber()
   {
      return super.nextNotificationSequenceNumber();
   }

   // Private -------------------------------------------------------

   // Inner classes -------------------------------------------------
}

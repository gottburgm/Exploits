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
import org.w3c.dom.Document;

import javax.management.Attribute;
import javax.management.JMException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.io.InputStream;

/**
 * Root class of the JBoss JSR-77 implementation of JavaMailResource.
 *
 * @author  <a href="mailto:andreas@jboss.org">Andreas Schaefer</a>
 * @author  <a href="mailto:scott.stark@jboss.org">Scott Stark</a>
 * @author  <a href="mailto:thomas.diesler@jboss.org">Thomas Diesler</a>
 * @version $Revision: 81025 $
 */
public class JavaMailResource extends J2EEResource
   implements JavaMailResourceMBean
{
   // Constants -----------------------------------------------------
   private static Logger log = Logger.getLogger(JavaMailResource.class);

   // Attributes ----------------------------------------------------

   private StateManagement mState;
   private ObjectName mailServiceName;

   // Static --------------------------------------------------------

   public static ObjectName create(MBeanServer mbeanServer, String resName,
                                   ObjectName mailServiceName)
   {
      ObjectName j2eeServerName = J2EEDomain.getDomainServerName(mbeanServer);
      ObjectName jsr77Name = null;
      try
      {
         JavaMailResource mailRes = new JavaMailResource(resName, j2eeServerName, mailServiceName);
         jsr77Name = mailRes.getObjectName();
         mbeanServer.registerMBean(mailRes, jsr77Name);
         log.debug("Created JSR-77 JavaMailResource: " + resName);
      }
      catch (Exception e)
      {
         log.debug("Could not create JSR-77 JavaMailResource: " + resName, e);
      }
      return jsr77Name;
   }

   public static void destroy(MBeanServer mbeanServer, String resName)
   {
      try
      {
         J2EEManagedObject.removeObject(mbeanServer,
                 J2EEDomain.getDomainName() + ":" +
                 J2EEManagedObject.TYPE + "=" + J2EETypeConstants.JavaMailResource + "," +
                 "name=" + resName + "," +
                 "*");
      }
      catch (Exception e)
      {
         log.debug("Could not destroy JSR-77 JNDIResource: " + resName, e);
      }
   }

   // Constructors --------------------------------------------------

   /**
    * @param resName Name of the JavaMailResource
    */
   public JavaMailResource(String resName, ObjectName j2eeServerName,
                           ObjectName mailServiceName)
           throws MalformedObjectNameException,
           InvalidParentException
   {
      super(J2EETypeConstants.JavaMailResource, resName, j2eeServerName);
      this.mailServiceName = mailServiceName;
      mState = new StateManagement(this);
   }

   // Public --------------------------------------------------------

   /**
    * @jmx:managed-attribute
    */
   public String getuserName()
           throws Exception
   {
      return (String) server.getAttribute(mailServiceName, "User");
   }

   /**
    * @jmx:managed-attribute
    */
   public void setuserName(String pName)
           throws Exception
   {
      server.setAttribute(mailServiceName, new Attribute("User", pName));
   }

   /**
    * @jmx:managed-attribute
    */
   public void setpassword(String pPassword)
           throws Exception
   {
      server.setAttribute(mailServiceName, new Attribute("Password", pPassword));
   }

   /**
    * @jmx:managed-attribute
    */
   public String getjndiName()
           throws Exception
   {
      return (String) server.getAttribute(mailServiceName, "JNDIName");
   }

   /**
    * @jmx:managed-attribute
    */
   public void setjndiName(String pName)
           throws Exception
   {
      server.setAttribute(mailServiceName, new Attribute("JNDIName", pName));
   }

   /**
    * @jmx:managed-attribute
    */
   public String getconfiguration()
           throws Exception
   {
      return server.getAttribute(mailServiceName, "Configuration") + "";
   }

   /**
    * @jmx:managed-attribute
    */
   public void setconfiguration(String pConfigurationElement)
           throws Exception
   {
      if (pConfigurationElement == null || pConfigurationElement.length() == 0)
      {
         pConfigurationElement = "<configuration/>";
      }
      DocumentBuilder lParser = DocumentBuilderFactory.newInstance().newDocumentBuilder();
      InputStream lInput = new ByteArrayInputStream(pConfigurationElement.getBytes());
      Document lDocument = lParser.parse(lInput);
      server.setAttribute(mailServiceName, new Attribute("Configuration", lDocument.getDocumentElement()));
   }

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
         server.invoke(mailServiceName,
                 "start",
                 new Object[]{},
                 new String[]{});
      }
      catch (Exception e)
      {
         log.error("start failed", e);
      }
   }

   public void mejbStartRecursive()
   {
      // No recursive start here
      try
      {
         mejbStart();
      }
      catch (Exception e)
      {
         log.error("start failed", e);
      }
   }

   public void mejbStop()
   {
      try
      {
         server.invoke(mailServiceName,
                 "stop",
                 new Object[]{},
                 new String[]{});
      }
      catch (Exception e)
      {
         log.error("Stop of JavaMailResource failed", e);
      }
   }

   public void postCreation()
   {
      try
      {
         server.addNotificationListener(mailServiceName, mState, null, null);
      }
      catch (JMException e)
      {
         log.debug("Failed to add notification listener", e);
      }
      sendNotification(NotificationConstants.OBJECT_CREATED, "Java Mail Resource created");
   }

   public void preDestruction()
   {
      sendNotification(NotificationConstants.OBJECT_DELETED, "Java Mail Resource deleted");
      // Remove the listener of the target MBean
      try
      {
         server.removeNotificationListener(mailServiceName, mState);
      }
      catch (JMException jme)
      {
         // When the service is not available anymore then just ignore the exception
      }
   }

   // java.lang.Object overrides ------------------------------------

   public String toString()
   {
      return "JavaMailResource { " + super.toString() + " } [ " +
              " ]";
   }
}

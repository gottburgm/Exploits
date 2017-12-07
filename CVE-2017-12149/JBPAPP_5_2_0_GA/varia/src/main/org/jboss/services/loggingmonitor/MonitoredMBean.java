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
package org.jboss.services.loggingmonitor;

import javax.management.JMException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.apache.log4j.Logger;
import org.jboss.mx.util.JMXExceptionDecoder;
import org.jboss.mx.util.MBeanServerLocator;

/**
 * This class encapsulates the information necessary for monitoring an MBean.
 * 
 * @author <a href="mailto:jimmy.wilson@acxiom.com">James Wilson</a>
 * @author <a href="mailto:dimitris@jboss.org">Dimitris Andreadis</a>
 * @version $Revision: 81038 $
 */
class MonitoredMBean
{
   private MBeanServer mbeanServer;
   private ObjectName  objectName;
   private String[]    attributes;
   private Logger      logger; //apache logger

   /**
    * Constructor.
    *
    * @param objectName this monitored MBean's object name.
    * @param attributes the attributes of this MBean to monitor.
    * @param logger the logger for this monitored MBean.
    * @param throws MalformedObjectNameException if the specified object name
    *                                            is invalid.
    */
   public MonitoredMBean(String objectName, String[] attributes, Logger logger) throws MalformedObjectNameException
   {
      this.objectName = new ObjectName(objectName);
      this.attributes = attributes;
      this.logger = logger;
      this.mbeanServer = MBeanServerLocator.locateJBoss();
   }

   /**
    * Retrieves the object name of this monitored MBean.
    *
    * @return this monitored MBean's object name.
    */
   public ObjectName getObjectName()
   {
      return objectName;
   }

   /**
    * Retrieves the attributes of this monitored MBean.
    *
    * @return this monitored MBean's attributes.
    */
   public String[] getAttributes()
   {
      return attributes;
   }

   /**
    * Retrieves the logger for this monitored MBean.
    *
    * @return this monitored MBean's logger.
    */
   public Logger getLogger()
   {
      return logger;
   }

   /**
    * Logs the format message for this monitored MBean.
    */
   public void logFormat()
   {
      logger.info(buildFormatMessage());
   }

   /**
    * Logs the attributes of this MBean that are being monitored.
    *
    * @throws JMException if the retrieval of a MBean attribute causes such an
    *                     exception.
    */
   public void logAttributes() throws Exception
   {
      try
      {
         StringBuffer message = new StringBuffer();

         for (int j = 0; j < attributes.length; ++j)
         {
            Object attributeValue = mbeanServer.getAttribute(objectName, attributes[j]);

            message.append(attributeValue);

            if (j < (attributes.length - 1))
            {
               message.append(",");
            }
         }

         logger.info(message);
      }
      catch (Exception e)
      {
         JMXExceptionDecoder.rethrow(e);
      }
   }

   /**
    * Builds the format message for this monitored MBean.
    *
    * @return this monitored MBean's format message.
    */
   private String buildFormatMessage()
   {
      StringBuffer message = new StringBuffer(objectName.toString());
      message.append(" monitor format: (");

      for (int i = 0; i < attributes.length; ++i)
      {
         message.append(attributes[i]);

         if (i < (attributes.length - 1))
         {
            message.append(',');
         }
      }
      message.append(")");

      return message.toString();
   }
}

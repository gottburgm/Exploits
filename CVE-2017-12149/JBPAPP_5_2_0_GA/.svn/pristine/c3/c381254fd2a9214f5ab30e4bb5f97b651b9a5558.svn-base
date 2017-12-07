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
package org.jboss.logging;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.management.ObjectName;

import org.jboss.mx.util.ObjectNameFactory;
import org.jboss.system.ServiceMBean;

/**
 * MBean interface.
 */
public interface Log4jServiceMBean extends ServiceMBean
{
   /** The default object name */
   ObjectName OBJECT_NAME = ObjectNameFactory.create("jboss.system:type=Log4jService,service=Logging");

   /** Notification type used to indicate a log4j reconfiguration */
   String RECONFIGURE_NOTIFICATION_TYPE = "jboss.logging.log4j.reconfigure";
   
   /** Name of system property used to control the logging threshold for the server.log file */
   String JBOSS_SERVER_LOG_THRESHOLD_PROPERTY = "jboss.server.log.threshold";
   
   // Attributes ----------------------------------------------------
   
   /**
    * The catch <tt>System.out</tt> flag.
    * @param flag True to enable, false to disable.
    */
   void setCatchSystemOut(boolean flag);
   boolean getCatchSystemOut();
   
   /**
    * The catch <tt>System.err</tt> flag.
    * @param flag True to enable, false to disable.
    */
   void setCatchSystemErr(boolean flag);
   boolean getCatchSystemErr();

   /**
    * The org.apache.log4j.helpers.LogLog.setQuietMode flag
    * @return True if enabled, false if disabled.
    */
   void setLog4jQuietMode(boolean flag);
   boolean getLog4jQuietMode();
   
   /**
    * The refresh period.
    */
   void setRefreshPeriod(int refreshPeriod);   
   int getRefreshPeriod();

   /**
    * The Log4j configuration URL.
    */
   void setConfigurationURL(URL url);
   URL getConfigurationURL();
   
   /**
    * The value to assign to system property {@link #JBOSS_SERVER_LOG_THRESHOLD_PROPERTY}
    * if it is not already set. This system property in turn controls
    * the logging threshold for the server.log file.
    * <p>
    * If the system property is already set when this service is created,
    * this value is ignored.
    * </p>
    */
   void setDefaultJBossServerLogThreshold(String level);
   String getDefaultJBossServerLogThreshold();
   
   // Operations ----------------------------------------------------
   
   /**
    * Sets the level for a logger of the give name.
    * <p>Values are trimmed before used.
    * 
    * @param name The name of the logger to change level
    * @param levelName The name of the level to change the logger to.
    */
   void setLoggerLevel(String name, String levelName);

   /**
    * Sets the levels of each logger specified by the given comma seperated list of logger names.
    * @see #setLoggerLevel
    * 
    * @param list A comma seperated list of logger names.
    * @param levelName The name of the level to change the logger to.
    */
   void setLoggerLevels(String list, String levelName);

   /**
    * Gets the level of the logger of the give name.
    * 
    * @param name The name of the logger to inspect.
    */
   String getLoggerLevel(String name);

   /**
    * Force the logging system to reconfigure.
    */
   void reconfigure() throws IOException;

   /**
    * Hack to reconfigure and change the URL. This is needed until
    * we have a JMX HTML Adapter that can use PropertyEditor to coerce.
    * 
    * @param url The new configuration url
    */
   void reconfigure(String url) throws IOException, MalformedURLException;

}

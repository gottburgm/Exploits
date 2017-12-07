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

import javax.management.MalformedObjectNameException;

import org.jboss.system.ServiceMBean;
import org.w3c.dom.Element;

/**
 * This is the management interface of the LoggingMonitor service which provides
 * the ability to create monitoring logs for various MBeans and their
 * attributes.
 * 
 * @author <a href="mailto:jimmy.wilson@acxiom.com">James Wilson</a>
 * @author <a href="mailto:dimitris@jboss.org">Dimitris Andreadis</a>
 * @version $Revision: 81038 $
 */
public interface LoggingMonitorMBean extends ServiceMBean
{
   /**
    * The name of the file to which this monitor's information
    * will be logged.
    */
   void setFilename(String filename);   
   String getFilename();

   /**
    * Flag to indicate whether or not this monitor's log file should have
    * information appended to it, if it already exists. (default is true)
    */
   void setAppendToFile(boolean appendToFile);   
   boolean getAppendToFile();

   /**
    * This monitor's log file rollover period. Valid values are
    * MONTH, WEEK, DAY, HALFDAY, HOUR, and MINUTE (case insensitive).
    * (default is DAY)
    */
   void setRolloverPeriod(String rolloverPeriod);   
   String getRolloverPeriod();

   /**
    * The period to delay between monitoring snapshots.
    * (non-zero positive value)
    */
   void setMonitorPeriod(long monitorPeriod);   
   long getMonitorPeriod();

   /**
    * The PatternLayout for logging entries
    * (default is "%d %-5p [%c] %m%n")
    */
   void setPatternLayout(String patternLayout);
   String getPatternLayout();
   
   /**
    * This monitor's log file rollover format as determined by the
    * <code>RolloverPeriod</code> attribute.
    */
    String getRolloverFormat();
    
   /**
    * Sets the monitored objects configuration.
    *
    * @param monitoredObjects the objects to be monitored specified in the
    * following format:
    * <pre>
    * &lt;attribute name="MonitoredObjects"&gt;
    *   &lt;configuration&gt;
    *      &lt;monitoredmbean name="[object name]" logger="[logger name]"&gt;
    *         &lt;attribute&gt;[attribute name]&lt;/attribute&gt;
    *         &lt;attribute&gt;[attribute name]&lt;/attribute&gt;
    *         ...
    *      &lt;/monitoredmbean&gt;
    *   &lt;/configuration&gt;
    * &lt;/attribute&gt;
    * </pre>
    *
    * @throws MalformedObjectNameException if the monitored objects
    *                                      configuration contains an invalid
    *                                      object name.
    */
   void setMonitoredObjects(Element monitoredObjects) throws MalformedObjectNameException;

}

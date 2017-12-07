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

import java.util.Timer;
import java.util.TimerTask;

import javax.management.MalformedObjectNameException;

import org.apache.log4j.Appender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.jboss.logging.appender.DailyRollingFileAppender;
import org.jboss.system.ServiceMBeanSupport;
import org.w3c.dom.DOMException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

/**
 * This class implements the LoggingMonitor service which provides the ability
 * to create monitoring logs for various MBeans and their attributes.
 * 
 * @author <a href="mailto:jimmy.wilson@acxiom.com">James Wilson</a>
 * @author <a href="mailto:dimitris@jboss.org">Dimitris Andreadis</a>
 * @version $Revision: 81038 $
 */
public class LoggingMonitor extends ServiceMBeanSupport
   implements LoggingMonitorMBean
{
   // Static --------------------------------------------------------
   
   public final static String MONITORED_MBEAN_ELEMENT = "monitoredmbean";
   public final static String MBEAN_NAME_ATTRIBUTE    = "name";
   public final static String MBEAN_LOGGER_ATTRIBUTE  = "logger";   
   public final static String MBEAN_ATTRIBUTE_ELEMENT = "attribute";
   
   public final static String DEFAULT_PATTERN_LAYOUT  = "%d %-5p [%c] %m%n";
   
   // Private data --------------------------------------------------
   
   private String           filename;
   private boolean          appendToFile;
   private RolloverPeriod   rolloverPeriod;
   private MonitoredMBean[] monitoredObjects;
   private long             monitorPeriod;
   private String           patternLayout;
   
   private Appender         appender;
   private Timer            timer;

   // Constructors -------------------------------------------------
    
   /**
    * Default constructor.
    */
   public LoggingMonitor()
   {
      appendToFile = true;
      rolloverPeriod = new RolloverPeriod("DAY");
      patternLayout = DEFAULT_PATTERN_LAYOUT;
   }

   // Attributes ----------------------------------------------------

   /**
    * @jmx.managed-attribute
    */
   public void setFilename(String filename)
   {
      if (filename == null || filename.length() == 0)
      {
         throw new IllegalArgumentException("Logging monitor's filename can not be null or empty");
      }
      this.filename = filename;
   }
   
   /**
    * @jmx.managed-attribute
    */   
   public String getFilename()
   {
      return filename;
   }

   /**
    * @jmx.managed-attribute
    */   
   public void setAppendToFile(boolean appendToFile)
   {
      this.appendToFile = appendToFile;
   }

   /**
    * @jmx.managed-attribute
    */   
   public boolean getAppendToFile()
   {
      return appendToFile;
   }

   /**
    * @jmx.managed-attribute
    */
   public void setRolloverPeriod(String rolloverPeriod)
   {
      this.rolloverPeriod = new RolloverPeriod(rolloverPeriod);
   }   
   
   /**
    * @jmx.managed-attribute
    */
   public String getRolloverPeriod()
   {
      return rolloverPeriod.toString();
   }

   /**
    * @jmx.managed-attribute
    */
   public void setMonitorPeriod(long monitorPeriod)
   {
      if (monitorPeriod < 1)
      {
         throw new IllegalArgumentException("Logging monitor's monitor period must be a positive, non-zero value");
      }
      this.monitorPeriod = monitorPeriod;
   }
   
   /**
    * @jmx.managed-attribute
    */
   public long getMonitorPeriod()
   {
      return monitorPeriod;
   }   
   
   /**
    * @jmx.managed-attribute
    */
   public void setPatternLayout(String patternLayout)
   {
      this.patternLayout = patternLayout;
   }
   
   /**
    * @jmx.managed-attribute
    */
   public String getPatternLayout()
   {
      return patternLayout;
   }
   
   /**
    * @jmx.managed-attribute
    */    
   public String getRolloverFormat()
   {
      return rolloverPeriod.getRolloverFormat();
   }   
   
   /**
    * @jmx.managed-attribute
    */
   public void setMonitoredObjects(Element monitoredObjects) throws MalformedObjectNameException
   {
      NodeList monitoredMBeans = monitoredObjects.getElementsByTagName(MONITORED_MBEAN_ELEMENT);

      int mbeanCount = monitoredMBeans.getLength();
      if (mbeanCount < 1)
      {
         throw createMissingElementException(MONITORED_MBEAN_ELEMENT);
      }

      this.monitoredObjects = new MonitoredMBean[mbeanCount];
      for (int i = 0; i < mbeanCount; ++i)
      {
         Node monitoredMBean = monitoredMBeans.item(i);
         this.monitoredObjects[i] = toMonitoredMBean((Element) monitoredMBean);
      }
   }
   
   // ServiceMBeanSupport overrides ---------------------------------

   protected void startService()
   {
      if (monitoredObjects == null)
      {
         throw new IllegalStateException("'MonitoredObjects' attribute not configured");
      }
      DailyRollingFileAppender appender = new DailyRollingFileAppender();
      appender.setFile(filename);
      appender.setAppend(appendToFile);
      appender.setDatePattern(rolloverPeriod.getRolloverFormat());
      appender.setLayout(new PatternLayout(patternLayout));
      appender.setThreshold(Level.INFO);
      appender.activateOptions();
      this.appender = appender;

      for (int i = 0; i < monitoredObjects.length; ++i)
      {
         monitoredObjects[i].getLogger().addAppender(appender);
      }

      // use the ServiceMBeanSupport logger for reporting errors
      TimerTask task = new LoggingMonitorTimerTask(monitoredObjects, log);

      timer = new Timer();
      timer.schedule(task, 0, monitorPeriod);

      log.debug("Logging monitor started logging to " + filename);
   }

   protected void stopService()
   {
      timer.cancel();

      for (int i = 0; i < monitoredObjects.length; ++i)
      {
         monitoredObjects[i].getLogger().removeAllAppenders();
      }

      appender.close();

      log.debug("Logging monitor stopped logging to " + filename);
   }

   // Private -------------------------------------------------------
    
   /**
    * Converts the specified XML DOM element to a monitored MBean.
    *
    * @param element the XML DOM element to be converted.
    * @return a monitored MBean represented by the specified XML DOM element.
    * @throws MalformedObjectNameException if the specified XML DOM element
    *                                      does not contain a valid object
    *                                      name.
    */
   private MonitoredMBean toMonitoredMBean(Element element) throws MalformedObjectNameException
   {
      String objectName = element.getAttribute(MBEAN_NAME_ATTRIBUTE);

      if ("".equals(objectName))
      {
         throw createAttributeNotFoundException(MBEAN_NAME_ATTRIBUTE);
      }

      String loggerName = element.getAttribute(MBEAN_LOGGER_ATTRIBUTE);
      if ("".equals(loggerName))
      {
         throw createAttributeNotFoundException(MBEAN_LOGGER_ATTRIBUTE);
      }

      Logger logger = Logger.getLogger(loggerName.toLowerCase());
      logger.setAdditivity(false);
      logger.setLevel(Level.INFO);

      String[] attributes = getMonitoredAttributes(element);

      return new MonitoredMBean(objectName, attributes, logger);
   }
    
   /**
    * Retrieves the attributes of the MBean to monitor.
    *
    * @param monitoredMBean a MBean, represented as a XML DOM element, for
    *                       which to retrieve the attributes to monitor.
    * @return the attributes of the MBean to monitor.
    */
   private String[] getMonitoredAttributes(Element monitoredMBean)
   {
      NodeList monitoredAttributes = monitoredMBean.getElementsByTagName(MBEAN_ATTRIBUTE_ELEMENT);

      int monitoredAttributesCount = monitoredAttributes.getLength();
      if (monitoredAttributesCount < 1)
      {
         throw createMissingElementException(MBEAN_ATTRIBUTE_ELEMENT);
      }

      String[] attributes = new String[monitoredAttributesCount];
      for (int i = 0; i < monitoredAttributesCount; ++i)
      {
         Node node = monitoredAttributes.item(i);
         Node attribute = node.getFirstChild();

         if (attribute.getNodeType() != Node.TEXT_NODE)
         {
            throw new DOMException(DOMException.HIERARCHY_REQUEST_ERR,
               "Unexpected node type inside <attribute> for monitored MBean.");
         }
         attributes[i] = (((Text) attribute).getData()).trim();
      }
      return attributes;
   }
   
   /**
    * Creates a <code>DOMException</code> relating that at least one occurrence
    * of the specified element was not found as expected.
    *
    * @param element the expected element.
    * @return a <code>DOMException</code> relating that at least one occurrence
    *         of the specified element was not found as expected.
    */
   private DOMException createMissingElementException(String element)
   {
      return new DOMException(DOMException.NOT_FOUND_ERR,
            "At least one <" + element + "> element is expected");
   }
   
   /**
    * Creates a <code>DOMException</code> relating that the specified attribute
    * was not found as expected.
    *
    * @param attribute the expected attribute.
    * @return a <code>DOMException</code> relating that the specified attribute
    *         was not found as expected.
    */
   private DOMException createAttributeNotFoundException(String attribute)
   {
       return new DOMException(DOMException.NOT_FOUND_ERR,
             "Missing expected '" + attribute + "' attribute of a <monitoredmbean> element");
   }
   
}

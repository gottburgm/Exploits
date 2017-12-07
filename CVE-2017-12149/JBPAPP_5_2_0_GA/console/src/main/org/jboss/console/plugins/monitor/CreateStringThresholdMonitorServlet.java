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
package org.jboss.console.plugins.monitor;

import java.io.IOException;
import java.util.ArrayList;

import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jboss.logging.Logger;
import org.jboss.monitor.StringThresholdMonitor;
import org.jboss.mx.util.MBeanServerLocator;

/**
 * Created by IntelliJ IDEA.
 * User: wburke
 * Date: Nov 25, 2003
 * Time: 5:53:01 PM
 * To change this template use Options | File Templates.
 */
public class CreateStringThresholdMonitorServlet extends javax.servlet.http.HttpServlet
{
   private static final Logger log = Logger.getLogger(CreateStringThresholdMonitorServlet.class);
   
   protected void doGet(HttpServletRequest req, HttpServletResponse resp)
           throws ServletException, IOException
   {
      doit(req, resp);
   }

   protected void doPost(HttpServletRequest req, HttpServletResponse resp)
           throws ServletException, IOException
   {
      doit(req, resp);
   }

   protected void error(String msg, HttpServletRequest req, HttpServletResponse resp)
           throws ServletException, IOException
   {
      req.setAttribute("error", "Error: " + msg);
      //this.getServletContext().getRequestDispatcher("/createThresholdMonitor.jsp").forward(req, resp);
      req.getRequestDispatcher("/createStringThresholdMonitor.jsp").forward(req, resp);
      return;
   }

   protected void doit(HttpServletRequest req, HttpServletResponse resp)
           throws ServletException, IOException
   {

      String monitorName = req.getParameter("monitorName").trim();
      log.debug(monitorName);
      String objectName = req.getParameter("objectName").trim();
      log.debug(objectName);
      MBeanServer mbeanServer = MBeanServerLocator.locateJBoss();
      ObjectName oname = null;
      try
      {
         oname = new ObjectName(objectName);
      }
      catch (MalformedObjectNameException e)
      {
         error("Malformed ObjectName ", req, resp);
         return;
      }
      String attribute = req.getParameter("attribute").trim();
      log.debug(attribute);
      Object val = null;
      try
      {
         val = mbeanServer.getAttribute(oname, attribute);
      }
      catch (Exception e)
      {
         error("Unable to pull attribute value from MBean, does the attribute exist? ", req, resp);
         return;
      }
      String threshold = req.getParameter("threshold").trim();
      log.debug(threshold);
      boolean enabled = req.getParameter("enabled") != null;
      log.debug("Enabled: " + enabled);
      boolean persisted = req.getParameter("persisted") != null;
      log.debug("Persisted: " + persisted);
      String period = req.getParameter("period").trim();
      log.debug(period);
      long timePeriod = 0;
      try
      {
         timePeriod = Long.parseLong(period);
      }
      catch (NumberFormatException e)
      {
         error("Illegal format for watch period.", req, resp);
         return;
      }
      boolean equality = req.getParameter("equality") != null;
      String[] alerts = req.getParameterValues("alerts");
      if (alerts == null)
      {
         error("you must select at least one alert listener", req, resp);
         return;
      }

      if (persisted)
      {

         try
         {
            Object[] args = {"monitors", monitorName, "-service.xml"};
            String[] signature = {"java.lang.String", "java.lang.String", "java.lang.String"};
            Object rtn = mbeanServer.invoke(new ObjectName("jboss.admin:service=DeploymentFileRepository"), "isStored", args, signature);
            if (((Boolean)rtn).booleanValue())
            {
               error("Monitor with this name already exists", req, resp);
               return;
            }
         }
         catch (Exception ex)
         {
            error("Failed to determine if monitor with that name already exists: " + ex.toString(), req, resp);
            return;
         }
         StringBuffer xml = new StringBuffer("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
         xml.append("<server>\n");

         xml.append("<mbean code=\"org.jboss.monitor.StringThresholdMonitor\"\n");
         xml.append("       name=\"jboss.monitor:service=" + monitorName.replace(' ', '_') + "\">\n");
         xml.append("  <attribute name=\"MonitorName\">" + monitorName + "</attribute>\n");
         try
         {
         if (mbeanServer.isInstanceOf(oname, "org.jboss.system.ServiceMBean"))
         {
            xml.append("  <depends optional-attribute-name=\"ObservedObject\">" + objectName + "</depends>\n");
         }
         else
         {
            xml.append("  <attribute name=\"ObservedObject\">" + objectName + "</attribute>\n");
         }
         }
         catch (Exception ex)
         {
            error("failed creating service: " + ex.toString(), req, resp);
            return;
         }
         xml.append("  <attribute name=\"ObservedAttribute\">" + attribute + "</attribute>\n");
         xml.append("  <depends-list optional-attribute-name=\"AlertListeners\">\n");
         for (int i = 0; i < alerts.length; i++)
         {
            xml.append("      <depends-list-element>");
            xml.append(alerts[i].trim());
            xml.append("      </depends-list-element>\n");
         }
         xml.append("  </depends-list>\n");
         xml.append("  <attribute name=\"Threshold\">" + threshold + "</attribute>\n");
         xml.append("  <attribute name=\"Period\">" + timePeriod + "</attribute>\n");
         xml.append("  <attribute name=\"EqualityTriggersAlert\">" + equality + "</attribute>\n");
         xml.append("  <attribute name=\"Enabled\">" + enabled + "</attribute>\n");
         xml.append("</mbean>\n</server>");

         try
         {
            Object[] args = {"monitors", monitorName, "-service.xml", xml.toString(), Boolean.TRUE};
            String[] signature = {"java.lang.String", "java.lang.String", "java.lang.String", "java.lang.String", "boolean"};
            mbeanServer.invoke(new ObjectName("jboss.admin:service=DeploymentFileRepository"), "store", args, signature);
         }
         catch (Exception ex)
         {
            error("Failed to create persisted file: " + ex.toString(), req, resp);
            return;
         }
      }
      else
      {
         try
         {
            StringThresholdMonitor monitor = new StringThresholdMonitor();

            monitor.setMonitorName(monitorName);
            monitor.setObservedObject(oname);
            monitor.setObservedAttribute(attribute);
            monitor.setThreshold(threshold);
            monitor.setEnabled(new Boolean(enabled).booleanValue());
            monitor.setPeriod(timePeriod);
            monitor.setEqualityTriggersAlert(equality);
            ArrayList list = new ArrayList();
            for (int i = 0; i < alerts.length; i++)
            {
               list.add(new ObjectName(alerts[i]));
            }
            monitor.setAlertListeners(list);
            mbeanServer.registerMBean(monitor, new ObjectName("jboss.monitor:name=" + monitorName));
            monitor.create();
            monitor.start();
         }
         catch (Exception ex)
         {
            error("Failed to create non-persisted monitor: " + ex.toString(), req, resp);
         }
      }
      req.getRequestDispatcher("/createStringThresholdMonitorSummary.jsp").forward(req, resp);

   }

}

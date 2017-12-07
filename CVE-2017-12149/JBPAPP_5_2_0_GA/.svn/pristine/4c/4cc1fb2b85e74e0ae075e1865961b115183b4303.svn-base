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

import javax.management.Attribute;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jboss.logging.Logger;
import org.jboss.mx.util.MBeanServerLocator;

/**
 * Created by IntelliJ IDEA.
 * User: wburke
 * Date: Nov 25, 2003
 * Time: 5:53:01 PM
 * To change this template use Options | File Templates.
 */
public class ManageStringThresholdMonitorServlet extends javax.servlet.http.HttpServlet
{
   private static final Logger log = Logger.getLogger(ManageStringThresholdMonitorServlet.class);
   
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
      req.getRequestDispatcher("/manageThresholdMonitor.jsp").forward(req, resp);
      return;
   }

   protected void doit(HttpServletRequest req, HttpServletResponse resp)
           throws ServletException, IOException
   {
      String action = req.getParameter("action");
      if (action == null)
      {
         error("unknown action: ", req, resp);
         return;
      }
      String monitorName = req.getParameter("monitorName").trim();
      MBeanServer mbeanServer = MBeanServerLocator.locateJBoss();
      ObjectName monitorObjectName, oname = null;
      String attribute = null;
      try
      {
         monitorObjectName = new ObjectName(req.getParameter("monitorObjectName"));
      }
      catch (Exception ex)
      {
         error("Malformed Monitor ObjectName: " + req.getParameter("monitorObjectName"), req, resp);
         return;
      }
      if (action.trim().equals("Clear Alert"))
      {
         try
         {
            log.debug("Clearing Alert for monitor: " + monitorObjectName.toString());
            String[] signature = {};
            Object[] args = {};
            mbeanServer.invoke(monitorObjectName, "clearAlert", args, signature);
            req.setAttribute("error", "Alert cleared");
            req.getRequestDispatcher("/manageStringThresholdMonitor.jsp").forward(req, resp);
            return;
         }
         catch (Exception ex)
         {
            error("Failed to Clear Alert: " + ex.toString(), req, resp);
            return;
         }
      }
      else if (action.trim().equals("Remove Monitor"))
      {
         try
         {
            log.debug("removing monitor: " + monitorObjectName.toString());
            Object[] args = {"monitors", monitorName, "-service.xml"};
            String[] signature = {"java.lang.String", "java.lang.String", "java.lang.String"};
            mbeanServer.invoke(new ObjectName("jboss.admin:service=DeploymentFileRepository"), "remove", args, signature);
            req.getRequestDispatcher("/ServerInfo.jsp").forward(req, resp);
         }
         catch (Exception ex)
         {
            error("Failed to Remove Monitor: " + ex.toString(), req, resp);
         }
         return;
      }
      try
      {
         monitorObjectName = new ObjectName(req.getParameter("monitorObjectName"));
         oname = (ObjectName) mbeanServer.getAttribute(monitorObjectName, "ObservedObject");
         attribute = (String) mbeanServer.getAttribute(monitorObjectName, "ObservedAttribute");
      }
      catch (Exception ex)
      {
         error("Malformed Monitor ObjectName: " + req.getParameter("monitorObjectName"), req, resp);
         return;
      }
      String threshold = req.getParameter("threshold").trim();
      log.debug(threshold);
      boolean enabled = req.getParameter("enabled") != null;
      log.debug("Enabled: " + enabled);
      boolean persisted = req.getParameter("persisted") != null;
      boolean equality = req.getParameter("equality") != null;
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
      String[] alerts = req.getParameterValues("alerts");
      if (alerts == null)
      {
         error("you must select at least one alert listener", req, resp);
         return;
      }
      try
      {
         mbeanServer.setAttribute(monitorObjectName, new Attribute("Threshold", threshold));
         mbeanServer.setAttribute(monitorObjectName, new Attribute("Enabled", new Boolean(enabled)));
         mbeanServer.setAttribute(monitorObjectName, new Attribute("Period", new Long(timePeriod)));
         mbeanServer.setAttribute(monitorObjectName, new Attribute("EqualityTriggersAlert", new Boolean(equality)));

         ArrayList list = new ArrayList();
         for (int i = 0; i < alerts.length; i++)
         {
            list.add(new ObjectName(alerts[i]));
         }
         mbeanServer.setAttribute(monitorObjectName, new Attribute("AlertListeners", list));
      }
      catch (Exception ex)
      {
         error("Failed to update mbean monitor: " + ex.toString(), req, resp);
         return;
      }
      if (persisted)
      {

         try
         {
            Object[] args = {"monitors", monitorName, "-service.xml"};
            String[] signature = {"java.lang.String", "java.lang.String", "java.lang.String"};
            Object rtn = mbeanServer.invoke(new ObjectName("jboss.admin:service=DeploymentFileRepository"), "isStored", args, signature);
            if (!((Boolean) rtn).booleanValue())
            {
               error("Monitor with this name doesn't exist in repository", req, resp);
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
               xml.append("  <depends optional-attribute-name=\"ObservedObject\">" + oname + "</depends>\n");
            }
            else
            {
               xml.append("  <attribute name=\"ObservedObject\">" + oname + "</attribute>\n");
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
      req.setAttribute("error", "Update complete!");
      req.getRequestDispatcher("/manageThresholdMonitor.jsp").forward(req, resp);

   }

}

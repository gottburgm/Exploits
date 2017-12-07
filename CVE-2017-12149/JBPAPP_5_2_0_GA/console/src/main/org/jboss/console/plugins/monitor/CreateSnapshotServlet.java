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

import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jboss.logging.Logger;
import org.jboss.monitor.SnapshotRecordingMonitor;
import org.jboss.mx.util.MBeanServerLocator;

/**
 * Created by IntelliJ IDEA.
 * User: wburke
 * Date: Nov 25, 2003
 * Time: 5:53:01 PM
 * To change this template use Options | File Templates.
 */
public class CreateSnapshotServlet extends javax.servlet.http.HttpServlet
{
   private static final Logger log = Logger.getLogger(CreateSnapshotServlet.class);
   
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
      req.getRequestDispatcher("/createSnapshot.jsp").forward(req, resp);
      return;
   }

   protected void doit(HttpServletRequest req, HttpServletResponse resp)
           throws ServletException, IOException
   {
      if (req.getParameter("submit")==null)
        return;
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
      try
      {
         SnapshotRecordingMonitor monitor = new SnapshotRecordingMonitor();

         monitor.setMonitorName(monitorName);
         monitor.setObservedObject(oname);
         monitor.setObservedAttribute(attribute);
         monitor.setPeriod(timePeriod);
         ObjectName sname = new ObjectName("jboss.snapshot:name=" + monitorName);
         mbeanServer.registerMBean(monitor, sname);
         resp.sendRedirect("/web-console/manageSnapshot.jsp?monitorObjectName=" + java.net.URLEncoder.encode(sname.toString()));
      }
      catch (Exception ex)
      {
         error("Failed to create non-persisted monitor: " + ex.toString(), req, resp);
      }


   }

}

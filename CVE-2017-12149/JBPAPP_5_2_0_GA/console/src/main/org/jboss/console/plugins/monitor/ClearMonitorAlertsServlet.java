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

import org.jboss.monitor.ThresholdMonitor;
import org.jboss.mx.util.MBeanServerLocator;
import org.jboss.mx.util.InstanceOfQueryExp;

import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;
import java.util.Iterator;

/**
 * Created by IntelliJ IDEA.
 * User: wburke
 * Date: Nov 25, 2003
 * Time: 5:53:01 PM
 * To change this template use Options | File Templates.
 */
public class ClearMonitorAlertsServlet extends javax.servlet.http.HttpServlet
{
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

   protected void doit(HttpServletRequest req, HttpServletResponse resp)
           throws ServletException, IOException
   {
      try
      {
         MBeanServer mbeanServer = MBeanServerLocator.locateJBoss();
         InstanceOfQueryExp queryExp = null;
         queryExp = new InstanceOfQueryExp("org.jboss.monitor.JBossMonitorMBean");
         Set monitors = mbeanServer.queryNames(null, queryExp);
         Iterator mbeans = monitors.iterator();
         while (mbeans.hasNext())
         {
            ObjectName moname = (ObjectName) mbeans.next();
            Object[] nullArgs = {};
            String[] nullSig = {};
            boolean alerted = ((Boolean) mbeanServer.invoke(moname, "alerted", nullArgs, nullSig)).booleanValue();
            if (alerted)
            {
               mbeanServer.invoke(moname, "clearAlert", nullArgs, nullSig);
            }
         }
      }
      catch (Exception ex)
      {
         req.setAttribute("error", "Error clearing alerts: " + ex.toString());
      }
      req.getRequestDispatcher("/listMonitors.jsp").forward(req, resp);
   }

}

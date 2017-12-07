/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
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
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jboss.logging.Logger;
import org.jboss.mx.util.MBeanServerLocator;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.DefaultTableXYDataset;
import org.jfree.data.xy.XYSeries;

/**
 * @author Bill Burke
 * @version $Revision: 68694 $
 */
public class ManageSnapshotServlet extends javax.servlet.http.HttpServlet
{
   static final long serialVersionUID = 128303790912009915L;

   private static final Logger log = Logger.getLogger(ManageSnapshotServlet.class);
   
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
      req.getRequestDispatcher("/manageSnapshot.jsp").forward(req, resp);
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
      action = action.trim();
      MBeanServer mbeanServer = MBeanServerLocator.locateJBoss();
      ObjectName monitorObjectName;
      String attribute = null;
      try
      {
         monitorObjectName = new ObjectName(req.getParameter("monitorObjectName"));
         attribute = (String)mbeanServer.getAttribute(monitorObjectName, "ObservedAttribute");
      }
      catch (Exception ex)
      {
         error("Malformed Monitor ObjectName: " + req.getParameter("monitorObjectName"), req, resp);
         return;
      }
      if (action.equals("Start Snapshot"))
      {
         Object[] nullArgs = {};
         String[] nullSig = {};
         try
         {
            mbeanServer.invoke(monitorObjectName, "startSnapshot", nullArgs, nullSig);
         }
         catch (Exception ex)
         {
            error("Problem invoking startSnapshot: " + ex.toString(), req, resp);
            return;
         }
         req.getRequestDispatcher("/manageSnapshot.jsp").forward(req, resp);
         return;
      }
      else if (action.equals("Stop Snapshot"))
      {
         Object[] nullArgs = {};
         String[] nullSig = {};
         try
         {
            mbeanServer.invoke(monitorObjectName, "endSnapshot", nullArgs, nullSig);
         }
         catch (Exception ex)
         {
            error("Problem invoking endSnapshot: " + ex.toString(), req, resp);
            return;
         }
         req.getRequestDispatcher("/manageSnapshot.jsp").forward(req, resp);
         return;
      }
      else if (action.equals("Clear Dataset"))
      {
         Object[] nullArgs = {};
         String[] nullSig = {};
         try
         {
            mbeanServer.invoke(monitorObjectName, "clearData", nullArgs, nullSig);
         }
         catch (Exception ex)
         {
            error("Problem invoking clearData: " + ex.toString(), req, resp);
            return;
         }
         req.setAttribute("error", "Dataset Cleared!");
         req.getRequestDispatcher("/manageSnapshot.jsp").forward(req, resp);
         return;
      }
      else if (action.equals("Remove Snapshot"))
      {
         try
         {
            log.debug("removing snapshot: " + monitorObjectName.toString());
            mbeanServer.unregisterMBean(monitorObjectName);
            req.getRequestDispatcher("/ServerInfo.jsp").forward(req, resp);
         }
         catch (Exception ex)
         {
            error("Failed to Remove Monitor: " + ex.toString(), req, resp);
         }
         return;
      }
      else if (action.equals("Show Dataset"))
      {
         ArrayList data = null;
         long start, end = 0;
         try
         {
            data = (ArrayList)mbeanServer.getAttribute(monitorObjectName, "Data");
            start = ((Long)mbeanServer.getAttribute(monitorObjectName, "StartTime")).longValue();
            end = ((Long)mbeanServer.getAttribute(monitorObjectName, "EndTime")).longValue();
         }
         catch (Exception ex)
         {
            error("Problem invoking getData: " + ex.toString(), req, resp);
            return;
         }
         resp.setContentType("text/html");
         PrintWriter writer = resp.getWriter();
         writer.println("<html>");
         writer.println("<body>");
         writer.println("<b>Start Time:</b> " + start + "ms<br>");
         writer.println("<b>End Time:</b> " + end + "ms<br>");
         writer.println("<b>Total Time:</b> " + (end - start) + "ms<br>");
         writer.println("<br><table border=\"0\">");
         for (int i = 0; i < data.size(); i++)
         {
            writer.println("<tr><td>" + data.get(i) + "</td></tr");
         }
         writer.println("</table></body></html>");
         return;
      }
      else if (action.equals("Graph Dataset"))
      {
         ArrayList data = null;
         long start, end = 0;
         try
         {
            data = (ArrayList)mbeanServer.getAttribute(monitorObjectName, "Data");
            start = ((Long)mbeanServer.getAttribute(monitorObjectName, "StartTime")).longValue();
            end = ((Long)mbeanServer.getAttribute(monitorObjectName, "EndTime")).longValue();
         }
         catch (Exception ex)
         {
            error("Problem invoking getData: " + ex.toString(), req, resp);
            return;
         }
         XYSeries set = new XYSeries(attribute, false, false);
         for (int i = 0; i < data.size(); i++)
         {
            set.add(new Integer(i), (Number)data.get(i));
         }
         DefaultTableXYDataset dataset = new DefaultTableXYDataset(false);
         dataset.addSeries(set);
         JFreeChart chart = ChartFactory.createXYLineChart(
                 "JMX Attribute: " + attribute, "count", attribute, dataset,
                 PlotOrientation.VERTICAL,
                 true,
                 true,
                 false
         );
         resp.setContentType("image/png");
         OutputStream out = resp.getOutputStream();
         ChartUtilities.writeChartAsPNG(out, chart, 400, 300);
         out.close();
         return;
      }
      error("Unknown Action", req, resp);
      return;
   }

}

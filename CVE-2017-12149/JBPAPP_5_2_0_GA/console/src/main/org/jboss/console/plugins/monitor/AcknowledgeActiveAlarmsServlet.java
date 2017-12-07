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
import javax.management.ObjectName;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jboss.monitor.services.ActiveAlarmTableMBean;
import org.jboss.mx.util.MBeanServerLocator;

/**
 * @author  <a href="mailto:dimitris@jboss.org">Dimitris Andreadis</a>.
 * @version $Revision: 81010 $
 */
public class AcknowledgeActiveAlarmsServlet extends HttpServlet
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
         MBeanServer server = MBeanServerLocator.locateJBoss();
         ObjectName target = ActiveAlarmTableMBean.OBJECT_NAME;

         // use the alarm table serverId for 'system' 
         String system = (String)server.getAttribute(target, "ServerId");
         // use the user.name environment property for user
         String user = System.getProperty("user.name");
         
         String alarmId = req.getParameter("alarmId").trim();
         
         if (alarmId.equals("*"))
         {
            server.invoke(
                  target,
                  "acknowledgeAll",
                  new Object[] { user, system },
                  new String[] { "java.lang.String", "java.lang.String" });
         }
         else
         {
            server.invoke(
                  target,
                  "acknowledge",
                  new Object[] { alarmId, user, system },
                  new String[] { "java.lang.String", "java.lang.String", "java.lang.String" });
         }
      }
      catch (Exception ex)
      {
         req.setAttribute("error", "Error acknowledging alarms: " + ex.toString());
      }
      req.getRequestDispatcher("/listActiveAlarmTable.jsp").forward(req, resp);
   }

}

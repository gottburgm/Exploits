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
package org.jboss.test.timer.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.naming.InitialContext;

import org.jboss.test.timer.interfaces.TimerSLSBHome;
import org.jboss.test.timer.interfaces.TimerSLSB;
import org.jboss.logging.Logger;

/** A servlet that creates an ejb timer in its init method to test that timer
 * restoration on receipt of the server startup event does not try to create
 * a duplicate timer from this one.
 * 
 * @author Scott.Stark@jboss.org
 * @version $Revision: 107174 $
 */
public class InitTimerServlet extends HttpServlet
{
   private static Logger log = Logger.getLogger(InitTimerServlet.class);
   private static final String TIMER_NAME = "InitTimerServlet";

   /**
    * Start an ejb timer from within the init method.
    * 
    * @param servletConfig
    * @throws ServletException
    */ 
   public void init(ServletConfig servletConfig) throws ServletException
   {
      super.init(servletConfig);
      log.info("init, creating ejb timer");
      // 
      try
      {
         InitialContext ctx = new InitialContext();
         TimerSLSBHome home = (TimerSLSBHome) ctx.lookup("java:/comp/env/ejb/TimerSLSBHome");
         TimerSLSB bean = home.create();
         bean.startTimer(TIMER_NAME,60000);
      }
      catch(Exception e)
      {
         throw new ServletException(e);
      }
   }

   protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException
   {
      try
      {
         InitialContext ctx = new InitialContext();
         TimerSLSBHome home = (TimerSLSBHome) ctx.lookup("java:/comp/env/ejb/TimerSLSBHome");
         TimerSLSB bean = home.create();
         int timeoutCount = bean.getTimeoutCount(TIMER_NAME);
         Date nextTimeout = bean.getNextTimeout(TIMER_NAME);
         long timeRemaining = bean.getTimeRemaining(TIMER_NAME);
         PrintWriter pw = response.getWriter();
         pw.println("<html><head><title>InitTimerServlet</title></head><body>");
         pw.println("<h1>Timer Info</h1>");
         pw.println("TimeoutCount:"+timeoutCount);
         pw.println("<br>NextTimeout:"+nextTimeout);
         pw.println("<br>TimeRemaining:"+timeRemaining);
         pw.println("</body></html>");
      }
      catch(Exception e)
      {
         throw new ServletException(e);
      }
   }
}

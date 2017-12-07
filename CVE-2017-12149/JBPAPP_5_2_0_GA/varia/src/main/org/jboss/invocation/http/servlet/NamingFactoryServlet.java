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
package org.jboss.invocation.http.servlet;

import java.io.IOException;
import java.io.ObjectOutputStream;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.management.ObjectName;
import javax.management.MalformedObjectNameException;
import javax.management.MBeanServer;

import org.jboss.invocation.MarshalledValue;
import org.jboss.logging.Logger;
import org.jboss.mx.util.MBeanServerLocator;

/** Create a Naming interface proxy that uses HTTP to communicate with the
 * JBoss JNDI naming service. Any request to this servlet receives a 
 * serialized object stream containing a MarshalledValue with the Naming proxy
 * as its content. The proxy is obtained from the MBean named by the
 * namingProxyMBean init-param.
 *
 * @author Scott.Stark@jboss.org
 * @version $Revision: 81038 $
 */
public class NamingFactoryServlet extends HttpServlet
{
   /** A serialized MarshalledValue */
   private static String RESPONSE_CONTENT_TYPE =
      "application/x-java-serialized-object; class=org.jboss.invocation.MarshalledValue";
   private Logger log;

   /** The Naming proxy instance obtained from the MBean */
   private Object namingProxy;
   /** The JMX ObjectName that provides the Naming proxy for the servlet */
   private ObjectName namingProxyMBean;
   /** The name of the attribute of namingProxyMBean used to obtain the proxy */
   private String proxyAttribute;

   /** Initializes the servlet.
    */
   public void init(ServletConfig config) throws ServletException
   {
      super.init(config);
      String category = getClass().getName() + '.' + config.getServletName();
      log = Logger.getLogger(category);

      // Get the name of the MBean that provides the Naming proxy
      String name = config.getInitParameter("namingProxyMBean");
      if( name == null )
         throw new ServletException("An namingProxyMBean must be specified");
      proxyAttribute = config.getInitParameter("proxyAttribute");
      if( proxyAttribute == null )
         proxyAttribute = "Proxy";

      try
      {
         namingProxyMBean = new ObjectName(name);
      }
      catch (MalformedObjectNameException e)
      {
         throw new ServletException("Failed to create object name: "+name, e);
      }
   }

   /** Destroys the servlet.
    */
   public void destroy()
   {
   }

   /** Returns a short description of the servlet.
    */
   public String getServletInfo()
   {
      return "A factory servlet for Naming proxies";
   }

   /** Return a Naming service proxy for any GET/POST made against this servlet
    * @param response servlet response
    */
   protected void processRequest(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException
   {
      boolean trace = log.isTraceEnabled();
      if( trace )
         log.trace("processRequest");
      // Lazy load of the proxy
      lookupNamingProxy();
      try
      {
         response.setContentType(RESPONSE_CONTENT_TYPE);
         MarshalledValue mv = new MarshalledValue(namingProxy);
         if( trace )
            log.trace("Serialized Naming proxy, size="+mv.size());
         //response.setContentLength(mv.size());
         ServletOutputStream sos = response.getOutputStream();
         ObjectOutputStream oos = new ObjectOutputStream(sos);
         oos.writeObject(mv);
         oos.flush();
         oos.close();
      }
      catch(Throwable t)
      {
         log.debug("Invoke failed", t);
         // Marshall the exception
         response.resetBuffer();
         MarshalledValue mv = new MarshalledValue(t);
         ServletOutputStream sos = response.getOutputStream();
         ObjectOutputStream oos = new ObjectOutputStream(sos);
         oos.writeObject(mv);
         oos.close();
      }

   }

   /** Handles the HTTP <code>GET</code> method.
    * @param request servlet request
    * @param response servlet response
    */
   protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException
   {
      processRequest(request, response);
   }
   
   /** Handles the HTTP <code>POST</code> method.
    * @param request servlet request
    * @param response servlet response
    */
   protected void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException
   {
      processRequest(request, response);
   }

   /** If the namingProxy has not been loaded, query the namingProxyMBean for
    * its proxyAttribute.
    * @throws ServletException
    */
   private synchronized void lookupNamingProxy()
      throws ServletException
   {
      if( namingProxy != null )
         return;

      MBeanServer mbeanServer = MBeanServerLocator.locateJBoss();
      try
      {
         namingProxy = mbeanServer.getAttribute(namingProxyMBean, proxyAttribute);
      }
      catch(Exception e)
      {
         String msg = "Failed to obtain proxy from: "+namingProxyMBean
            + " via attribute:" + proxyAttribute;
         log.debug(msg, e);
         throw new ServletException(msg, e);
      }

   }
}

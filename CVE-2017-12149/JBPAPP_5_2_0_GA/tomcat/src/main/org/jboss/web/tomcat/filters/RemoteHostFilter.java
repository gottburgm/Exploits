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
package org.jboss.web.tomcat.filters;

import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jboss.logging.Logger;

/**
 * Accept or deny a request based on the IP address of the client who made the
 * request.  JDK 1.4 or higher is required.
 * <p/>
 * This filter is configured by setting the "allow" and/or "deny" properties to
 * a comma-delimited list of regular expressions (in the syntax supported by the
 * java.util.regex package) to which the client IP address will be compared.
 * <p/>
 * <filter>
 * <filter-name>RemoteHostFilter</filter-name>
 * <filter-class>org.jboss.remotehostfilter.RemoteHostFilter</filter-class>
 * <init-param>
 * <param-name>deny</param-name>
 * <param-value>128.0.*,192.4.5.7</param-value>
 * </init-param>
 * <init-param>
 * <param-name>allow</param-name>
 * <param-value>192.4.5.6,127.0.0.*</param-value>
 * </init-param>
 * </filter>
 * <p/>
 * Evaluation proceeds as follows:
 * <p/>
 * If there are any deny expressions configured, the IP will be compared to each
 * expression. If a match is found, this request will be rejected with a
 * "Forbidden" HTTP response.
 * If there are any allow expressions configured, the IP will be compared to
 * each such expression. If a match is NOT found, this request will be rejected
 * with a "Forbidden" HTTP response.
 * Otherwise, the request will be rejected with a "Forbidden" HTTP response.
 *
 * @author Stan Silvert
 * @version $Revision: 81037 $
 */
public class RemoteHostFilter implements Filter
{
   private static final Logger log = Logger.getLogger(RemoteHostFilter.class);
   /** The list of hosts explicitly allowed */
   private String[] allow;
   /** The list of hosts explicitly denied */
   private String[] deny;
   /** The trace level log falg */
   private boolean trace;

   private FilterConfig filterConfig = null;

   public RemoteHostFilter()
   {
      trace = log.isTraceEnabled();
   }

   /**
    * @param request The servlet request we are processing
    * @param response  The servlet response we are creating
    * @param chain   The filter chain we are processing
    * @throws IOException      if an input/output error occurs
    * @throws ServletException if a servlet error occurs
    */
   public void doFilter(ServletRequest request,
                        ServletResponse response,
                        FilterChain chain)
      throws IOException, ServletException
   {
      String clientAddr = request.getRemoteAddr();
      if( trace )
         log.trace("Client addres is: " + clientAddr);

      if (hasMatch(clientAddr, deny))
      {
         handleInvalidAccess(request, response, clientAddr);
         return;
      }

      if ((allow.length > 0) && !hasMatch(clientAddr, allow))
      {
         handleInvalidAccess(request, response, clientAddr);
         return;
      }

      chain.doFilter(request, response);
   }

   private void handleInvalidAccess(ServletRequest request,
                                    ServletResponse response,
                                    String clientAddr) throws IOException
   {
      HttpServletRequest httpRequest = (HttpServletRequest) request;
      HttpServletResponse httpResponse = (HttpServletResponse) response;
      String url = httpRequest.getRequestURL().toString();
      if( trace )
         log.trace("Invalid access attempt to " + url + " from " + clientAddr);
      httpResponse.sendError(HttpServletResponse.SC_FORBIDDEN);
   }

   private boolean hasMatch(String clientAddr, String[] regExps)
   {
      for (int i = 0; i < regExps.length; i++)
      {
         if (clientAddr.matches(regExps[i]))
            return true;
      }

      return false;
   }

   /**
    * Destroy method for this filter
    */
   public void destroy()
   {
      this.filterConfig = null;
      this.allow = null;
      this.deny = null;
   }


   /**
    * Init method for this filter
    */
   public void init(FilterConfig filterConfig)
   {
      this.filterConfig = filterConfig;
      this.allow = extractRegExps(filterConfig.getInitParameter("allow"));
      this.deny = extractRegExps(filterConfig.getInitParameter("deny"));
   }

   private String[] extractRegExps(String initParam)
   {
      if (initParam == null)
      {
         return new String[0];
      }
      else
      {
         return initParam.split(",");
      }
   }

   /**
    * Return a String representation of this object.
    */
   public String toString()
   {
      if (filterConfig == null) return ("ClientAddrFilter()");
      StringBuffer sb = new StringBuffer("ClientAddrFilter(");
      sb.append(filterConfig);
      sb.append(")");
      return sb.toString();
   }

}

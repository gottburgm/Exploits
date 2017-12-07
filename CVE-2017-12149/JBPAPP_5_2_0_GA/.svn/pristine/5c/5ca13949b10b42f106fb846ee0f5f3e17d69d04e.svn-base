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
package org.jboss.web.tomcat.security;

import java.io.IOException;
import java.security.Principal;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.jboss.logging.Logger;

/**
 * A filter to keep track of principals in the sessions.
 * 
 * @author mmoyses@redhat.com
 * @version $Revison: 1 $
 */
public class PrincipalSessionAttributeFilter implements Filter
{

   private static Logger log = Logger.getLogger(PrincipalSessionAttributeFilter.class);

   private boolean trace = log.isTraceEnabled();

   private static final String JBOSS_PRINCIPAL = "org.jboss.web.tomcat.security.principal";

   /**
    * @see Filter#init(javax.servlet.FilterConfig)
    */
   public void init(FilterConfig filterConfig) throws ServletException
   {
   }

   /**
    * @see Filter#destroy()
    */
   public void destroy()
   {
   }

   /**
    * @see Filter#doFilter(javax.servlet.ServletRequest,
    *      javax.servlet.ServletResponse, javax.servlet.FilterChain)
    */
   public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
         ServletException
   {
      HttpSession session = ((HttpServletRequest) request).getSession();
      Principal principal = (Principal) session.getAttribute(JBOSS_PRINCIPAL);
      if (principal == null)
      {
         principal = ((HttpServletRequest) request).getUserPrincipal();
         session.setAttribute(JBOSS_PRINCIPAL, principal);
         if (trace)
            log.trace("Placing principal in session");
      }
      chain.doFilter(request, response);
   }

}

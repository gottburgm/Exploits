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
package org.jboss.test.web.servlets;

import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletResponse;
import javax.security.auth.Subject;
import javax.naming.NamingException;
import javax.naming.InitialContext;

import org.jboss.security.SubjectSecurityManager;

/**
 * @author Scott.Stark@jboss.org
 * @version $Revision: 81036 $
 */
public class SubjectFilter implements Filter
{
   public void init(FilterConfig filterConfig) throws ServletException
   {

   }

   public void doFilter(ServletRequest request, ServletResponse response,
      FilterChain filterChain)
      throws IOException, ServletException
   {
      HttpServletResponse httpResponse = (HttpServletResponse) response;
      try
      {
         Subject userSubject = getActiveSubject(httpResponse);
         if (userSubject == null)
            throw new ServletException("Active subject was null");
      }
      catch (NamingException e)
      {
         throw new ServletException("Failed to lookup active subject", e);
      }
      filterChain.doFilter(request, response);
   }

   public void destroy()
   {
   }

   protected Subject getActiveSubject(HttpServletResponse httpResponse)
      throws NamingException
   {
      InitialContext ctx = new InitialContext();
      SubjectSecurityManager mgr = (SubjectSecurityManager) ctx.lookup("java:comp/env/security/securityMgr");
      Subject s0 = mgr.getActiveSubject();
      httpResponse.addHeader("X-SubjectFilter-SubjectSecurityManager", s0.toString());
      Subject s1 = (Subject) ctx.lookup("java:comp/env/security/subject");
      httpResponse.addHeader("X-SubjectFilter-ENC", s1.toString());
      return s1;
   }
}

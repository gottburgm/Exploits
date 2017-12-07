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
import java.util.ArrayList;
import java.util.Enumeration;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

import org.jboss.logging.Logger;

/** A servlet filter that simply adds all header specified in its config
to replies the filter is mapped to. An example would be to set the cache
 control max age:

   <filter>
      <filter-name>CacheControlFilter</filter-name>
      <filter-class>filter.ReplyHeaderFilter</filter-class>
      <init-param>
         <param-name>Cache-Control</param-name>
         <param-value>max-age=3600</param-value>
      </init-param>
   </filter>
   
 <filter-mapping>
    <filter-name>CacheControlFilter</filter-name>
    <url-pattern>/images/*</url-pattern>
 </filter-mapping>
 <filter-mapping>
    <filter-name>CacheControlFilter</filter-name>
    <url-pattern>*.js</url-pattern>
 </filter-mapping>


 @author Scott.Stark@jboss.org
 @version $Revison:$
 */
public class ReplyHeaderFilter implements Filter
{
   static Logger log = Logger.getLogger(ReplyHeaderFilter.class);
   private String[][] replyHeaders = {{}};

   public void init(FilterConfig config)
   {
      Enumeration names = config.getInitParameterNames();
      ArrayList tmp = new ArrayList();
      while( names.hasMoreElements() )
      {
         String name = (String) names.nextElement();
         String value = config.getInitParameter(name);
         log.debug("Adding header name: "+name+"='"+value+"'");
         String[] pair = {name, value};
         tmp.add(pair);
      }
      replyHeaders = new String[tmp.size()][2];
      tmp.toArray(replyHeaders);
   }

   public void doFilter(ServletRequest request, ServletResponse response,
      FilterChain chain)
      throws IOException, ServletException
   {
      // Apply the headers
      HttpServletResponse httpResponse = (HttpServletResponse) response;
      for(int n = 0; n < replyHeaders.length; n ++)
      {
         String name = replyHeaders[n][0];
         String value = replyHeaders[n][1];
         httpResponse.addHeader(name, value);
      }
      chain.doFilter(request, response);
   }

   public void destroy()
   {
   }
}

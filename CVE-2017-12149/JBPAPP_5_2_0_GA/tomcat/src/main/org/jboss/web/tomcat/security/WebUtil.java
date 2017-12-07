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
package org.jboss.web.tomcat.security;

import java.util.Arrays;
import java.util.Enumeration;
import javax.servlet.http.HttpServletRequest;

/**
 *  Provides utility static methods for the web security integration
 *  @author <a href="mailto:Anil.Saldhana@jboss.org">Anil Saldhana</a>
 *  @version $Revision: 112732 $
 *  @since  Aug 22, 2006
 *
 *  Code in here should be kept in sync with
 *     jbosssx/src/main/java/org/jboss/security/authorization/resources/WebResource.java 
 */
public class WebUtil
{
   /** System Property setting to configure the web audit 
    *  off = turn it off
    *  headers = audit the headers
    *  cookies = audit the cookie
    *  parameters = audit the parameters
    *  attributes = audit the attributes
    *  headers,cookies,parameters = audit the headers,cookie and parameters
    *  headers,cookies = audit the headers and cookies
    *  and so on 
    *  
    *  Note: If this flag is not set in the system property, then we get no
    *  audit data for the web request
    * */
   public static final String WEB_AUDIT_FLAG = "org.jboss.security.web.audit";
   private static String auditFlag = " ";
   static
   {
      auditFlag =  System.getProperty(WEB_AUDIT_FLAG, " ").toLowerCase();
   }

   /**
    * Obtain debug information from the servlet request object
    * @param httpRequest
    * @return
    */
   public static String deriveUsefulInfo(HttpServletRequest httpRequest)
   {
      StringBuilder sb = new StringBuilder();
      sb.append("[").append(httpRequest.getContextPath());
       //Append cookies
       if(auditFlag.contains("cookies"))
       {
           sb.append(":cookies=").append(Arrays.toString(httpRequest.getCookies()));
       }
      //Append Header information
      if(auditFlag.contains("headers"))
      {
         sb.append(":headers=");
         Enumeration<?> en = httpRequest.getHeaderNames();
         for(;en.hasMoreElements();)
         {
            String headerName = (String)en.nextElement();
            sb.append(headerName).append("=");
            //Ensure HTTP Basic Password is not logged
            if(headerName.contains("authorization") == false)
               sb.append(httpRequest.getHeader(headerName)).append(",");
         }
         sb.append("]");
      }
      //Append Request parameter information
     if(auditFlag.contains("parameters"))
      {
         sb.append("[parameters=");
         Enumeration<?> enparam = httpRequest.getParameterNames();
         for(;enparam.hasMoreElements();)
         {
            String paramName = (String)enparam.nextElement();
            sb.append(paramName).append("=");
            if (paramName.equalsIgnoreCase("j_password"))
            {
               sb.append("***");
            }
            else
            {
               String[] paramValues = httpRequest.getParameterValues(paramName);
               int len = paramValues != null ? paramValues.length : 0;
               for(int i = 0 ; i < len ; i++)
                  sb.append(paramValues[i]).append("::");
            }
            sb.append(",");
         }
      }
      //Append Request attribute information      
      if(auditFlag.contains("attributes"))
      {
         sb.append("][attributes=");
         Enumeration<?> enu = httpRequest.getAttributeNames();
         for(;enu.hasMoreElements();)
         {
            String attrName = (String)enu.nextElement();
            sb.append(attrName).append("=");
            sb.append(httpRequest.getAttribute(attrName)).append(",");
         }
      }
      sb.append("]");
      return sb.toString();
   } 
}

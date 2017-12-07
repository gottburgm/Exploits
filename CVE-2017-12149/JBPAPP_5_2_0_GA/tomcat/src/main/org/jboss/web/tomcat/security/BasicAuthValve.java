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

import java.io.IOException;
import javax.servlet.ServletException;

import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.valves.ValveBase;
import org.jboss.logging.Logger;

/** A valve that provides information on the jaas login exception seen in the
 SecurityAssociation exception data. The useExceptionAsMsg flag indicates if
 the exception message should be set as the http response message. The
 exceptionHeader attribute if set is the header name that should be populated
 with the exception message.
   
 @author Scott.Stark@jboss.org
 @version $Revision: 81037 $
 */
public class BasicAuthValve
   extends ValveBase
{
   private static Logger log = Logger.getLogger(BasicAuthValve.class);
   private static boolean trace = log.isTraceEnabled();

   /** Should the exception message be used as the request status message */
   private boolean useExceptionAsMsg = false;
   /** A flag indicating if the auth exception thread local should be cleared */
   private boolean clearAuthException = true;
   /** The name of the reply header to use to return the exception message */
   private String exceptionHeader = null;

   public boolean isUseExceptionAsMsg()
   {
      return useExceptionAsMsg;
   }
   public void setUseExceptionAsMsg(boolean useExceptionAsMsg)
   {
      this.useExceptionAsMsg = useExceptionAsMsg;
   }

   public String getExceptionHeader()
   {
      return exceptionHeader;
   }
   public void setExceptionHeader(String exceptionHeader)
   {
      this.exceptionHeader = exceptionHeader;
   }

   public void invoke(Request request, Response response)
      throws IOException, ServletException
   {
      getNext().invoke(request, response);
      // Check the SecurityAssociation context exception
      Throwable t = SecurityAssociationActions.getAuthException();
      int status = response.getStatus();
      
      if( trace )
         log.trace("Status: "+status+"SecurityAssociation.exception: ", t);
      if( status >= 400 && t != null )
      {
         String msg = t.getMessage();
         // Set the response msg
         if( useExceptionAsMsg )
         {
            if( response.getCoyoteResponse() != null )
               response.getCoyoteResponse().setMessage(msg);
         }
         // Set the response exception header
         if( exceptionHeader != null )
            response.setHeader(exceptionHeader, msg);
         // Clear the exception thread local
         if( clearAuthException )
         {
            try
            {
               SecurityAssociationActions.clearAuthException();
            }
            catch(Throwable e)
            {
               log.warn("Unable to clear auth exception ", e);
            }
         }
      }
   }
}
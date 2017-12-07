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
package org.jboss.web.tomcat.service.request;

import java.io.IOException;

import javax.servlet.ServletException;

import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.valves.ValveBase;
import org.jboss.servlet.http.HttpEvent;

/** JBAS-7311: Caches the active Catalina request/response
 * @author Anil.Saldhana@redhat.com
 * @since Oct 5, 2009
 */
public class ActiveRequestResponseCacheValve extends ValveBase
{
   /** Maintain the Catalina Request for programmatic web login */
   public static ThreadLocal<Request> activeRequest = new ThreadLocal<Request>();
   /** Maintain the Catalina Response for programmatic web login */
   public static ThreadLocal<Response> activeResponse = new ThreadLocal<Response>();
  
   @Override
   public void invoke(Request request, Response response) throws IOException, ServletException
   {
      internalProcess(request, response, null); 
   }

   @Override
   public void event(Request request, Response response, HttpEvent event) throws IOException, ServletException
   {
      internalProcess(request, response, event);
   } 
   
   /**
    * Set the active request and response on the threadlocals
    * @param request
    * @param response
    * @param event
    * @throws IOException
    * @throws ServletException
    */
   private void internalProcess(Request request, Response response, HttpEvent event) throws IOException, ServletException
   {
      //Set the active request and response objects
      activeRequest.set(request);
      activeResponse.set(response);
      
      try
      {
         if(event == null)
         {
            getNext().invoke(request, response);
         }
         else
         {
            getNext().event(request, response, event);
         }
      }
      finally
      {
         activeRequest.set(null);
         activeResponse.set(null);
      }  
   }
}
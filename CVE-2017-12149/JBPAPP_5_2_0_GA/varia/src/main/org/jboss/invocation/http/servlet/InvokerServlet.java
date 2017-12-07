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
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.security.PrivilegedAction;
import java.security.Principal;
import java.security.AccessController;
import javax.management.MalformedObjectNameException;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jboss.invocation.InvocationException;
import org.jboss.invocation.MarshalledInvocation;
import org.jboss.invocation.MarshalledValue;
import org.jboss.logging.Logger;
import org.jboss.mx.util.MBeanServerLocator;
import org.jboss.mx.util.JMXExceptionDecoder;
import org.jboss.system.Registry;
import org.jboss.security.SecurityAssociation;

/** This servlet accepts a post containing a MarshalledInvocation, extracts
 the Invocation object, and then routes the invocation via JMX to either:
 1. the MBean specified via the invokerName ini parameter
 2. the MBean whose object name hash is specified by the invocation.getObjectName()
 value. This name's hash must have been entered into the Registry.

 The method signature of the invoker must be Object invoke(org.jboss.invocation.Invocation).

 @see org.jboss.system.Registry
 @see org.jboss.invocation.Invocation

 * @author  Scott.Stark@jboss.org
 * @version $Revision: 81038 $
 */
public class InvokerServlet extends HttpServlet
{
   private static Logger log = Logger.getLogger(InvokerServlet.class);
   /** A serialized MarshalledInvocation */
   private static String REQUEST_CONTENT_TYPE =
      "application/x-java-serialized-object; class=org.jboss.invocation.MarshalledInvocation";
   /** A serialized MarshalledValue */
   private static String RESPONSE_CONTENT_TYPE =
      "application/x-java-serialized-object; class=org.jboss.invocation.MarshalledValue";
   private MBeanServer mbeanServer;
   private ObjectName localInvokerName;

   /** Initializes the servlet.
    */
   public void init(ServletConfig config) throws ServletException
   {
      super.init(config);
      try
      {
         // See if the servlet is bound to a particular invoker
         String name = config.getInitParameter("invokerName");
         if( name != null )
         {
            localInvokerName = new ObjectName(name);
            log.debug("localInvokerName="+localInvokerName);
         }
      }
      catch(MalformedObjectNameException e)
      {
         throw new ServletException("Failed to build invokerName", e);
      }

      // Lookup the MBeanServer
      mbeanServer = MBeanServerLocator.locateJBoss();
      if( mbeanServer == null )
         throw new ServletException("Failed to locate the MBeanServer");
   }

   /** Destroys the servlet.
    */
   public void destroy()
   {

   }

   /** Read a MarshalledInvocation and dispatch it to the target JMX object
    invoke(Invocation) object.

    @param request servlet request
    @param response servlet response
    */
   protected void processRequest(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException
   {
      boolean trace = log.isTraceEnabled();
      if( trace )
      {
         log.trace("processRequest, ContentLength: "+request.getContentLength());
         log.trace("processRequest, ContentType: "+request.getContentType());
      }

      Boolean returnValueAsAttribute = (Boolean) request.getAttribute("returnValueAsAttribute");
      try
      {
         response.setContentType(RESPONSE_CONTENT_TYPE);
         // See if the request already has the MarshalledInvocation
         MarshalledInvocation mi = (MarshalledInvocation) request.getAttribute("MarshalledInvocation");
         if( mi == null )
         {
            // Get the invocation from the post
            ServletInputStream sis = request.getInputStream();
            ObjectInputStream ois = new ObjectInputStream(sis);
            mi = (MarshalledInvocation) ois.readObject();
            ois.close();
         }
         /* If the invocation carries no auth context, look to to the auth
         context of this servlet as seen in the SecurityAssocation. This allows
         the web app authentication to transparently be used as the call
         authentication.
         */
         if (mi.getPrincipal() == null && mi.getCredential() == null)
         {
            mi.setPrincipal(GetPrincipalAction.getPrincipal());
            mi.setCredential(GetCredentialAction.getCredential());
         }
         Object[] params = {mi};
         String[] sig = {"org.jboss.invocation.Invocation"};
         ObjectName invokerName = localInvokerName;
         // If there is no associated invoker, get the name from the invocation
         if( invokerName == null )
         {
            Integer nameHash = (Integer) mi.getObjectName();
            invokerName = (ObjectName) Registry.lookup(nameHash);
            if( invokerName == null )
               throw new ServletException("Failed to find invoker name for hash("+nameHash+")");
         }
         // Forward the invocation onto the JMX invoker
         Object value = mbeanServer.invoke(invokerName, "invoke", params, sig);
         if( returnValueAsAttribute == null || returnValueAsAttribute.booleanValue() == false )
         {
            MarshalledValue mv = new MarshalledValue(value);
            ServletOutputStream sos = response.getOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(sos);
            oos.writeObject(mv);
            oos.close();
         }
         else
         {
            request.setAttribute("returnValue", value);
         }
      }
      catch(Throwable t)
      {
         t = JMXExceptionDecoder.decode(t);
         // Unwrap any reflection InvocationTargetExceptions
         if( t instanceof InvocationTargetException )
         {
            InvocationTargetException ite = (InvocationTargetException) t;
            t = ite.getTargetException();
         }
         /* Wrap the exception in an InvocationException to distinguish
            between application and transport exceptions
         */
         InvocationException appException = new InvocationException(t);
         log.debug("Invoke threw exception", t);
         // Marshall the exception
         if( returnValueAsAttribute == null || returnValueAsAttribute.booleanValue() == false )
         {
            response.resetBuffer();
            MarshalledValue mv = new MarshalledValue(appException);
            ServletOutputStream sos = response.getOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(sos);
            oos.writeObject(mv);
            oos.close();
         }
         else
         {
            request.setAttribute("returnValue", appException);
         }
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

   /** Returns a short description of the servlet.
    */
   public String getServletInfo()
   {
      return "An HTTP to JMX invocation servlet";
   }

   private static class GetPrincipalAction implements PrivilegedAction
   {
      static PrivilegedAction ACTION = new GetPrincipalAction();
      public Object run()
      {
         Principal principal = SecurityAssociation.getPrincipal();
         return principal;
      }
      static Principal getPrincipal()
      {
         Principal principal = (Principal) AccessController.doPrivileged(ACTION);
         return principal;
      }
   }

   private static class GetCredentialAction implements PrivilegedAction
   {
      static PrivilegedAction ACTION = new GetCredentialAction();
      public Object run()
      {
         Object credential = SecurityAssociation.getCredential();
         return credential;
      }
      static Object getCredential()
      {
         Object credential = AccessController.doPrivileged(ACTION);
         return credential;
      }
   }
}

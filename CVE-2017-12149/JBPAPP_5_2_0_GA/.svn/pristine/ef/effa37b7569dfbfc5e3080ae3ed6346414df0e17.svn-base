/*
 * JBoss, Home of Professional Open Source
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
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
package org.jboss.profileservice.web;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Map;
import java.util.Set;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jboss.deployers.spi.management.ManagementView;
import org.jboss.managed.api.DeploymentState;
import org.jboss.managed.api.ManagedComponent;
import org.jboss.managed.api.ManagedDeployment;
import org.jboss.profileservice.spi.ProfileService;

/**
 * @author Scott.Stark@jboss.org
 * @version $Revision:$
 */
public class DebugServlet extends HttpServlet
{
   private static final String ProfileServiceJNDIName = "java:ProfileService";
   private static final long serialVersionUID = 1;

   private ManagementView mgtView;
   
   enum Operations {
      listDeploymentTree,
      listOperations
   };

   
   @Override
   public void init(ServletConfig config) throws ServletException
   {
      super.init(config);
      if(mgtView == null)
      {
         try
         {
            final InitialContext ctx = new InitialContext();
            final ProfileService profileService = ProfileService.class.cast(ctx.lookup(ProfileServiceJNDIName));
            mgtView = profileService.getViewManager();
         }
         catch (NamingException e)
         {
            throw new ServletException("Failed to lookup ManagementView", e);
         }
      }
   }

   @Override
   protected void doGet(HttpServletRequest req, HttpServletResponse resp)
         throws ServletException, IOException
   {
      if(mgtView == null)
      {
         throw new ServletException("The ManagementView has not been injected");
      }
      String op = req.getParameter("op");
      if(op == null)
         op = "listOperations";
      Operations theOP = Enum.valueOf(Operations.class, "listDeploymentTree");
      String opResult = "";
      super.log("op="+op);
      switch(theOP)
      {
         case listDeploymentTree:
            opResult = listDeploymentTree();
            break;
         case listOperations:
            opResult = listOperations();
            break;
         default:
            throw new ServletException("Unknown op: "+op+", valid ops: "+Operations.values());
      }
      
      PrintWriter pw = resp.getWriter();
      pw.write("<h1>");
      pw.write(theOP+" Results");
      pw.write("</h1>");
      pw.write("<pre>");
      pw.write(opResult);
      pw.write("</pre>");
   }

   protected String listDeploymentTree()
      throws ServletException
   {
      StringBuilder tmp = new StringBuilder();
      try
      {
         mgtView.load();
         Set<String> names = mgtView.getDeploymentNames();
         for(String name : names)
         {
            final ManagedDeployment md = mgtView.getDeployment(name);
            final DeploymentState state = md.getDeploymentState();
            tmp.append("ManagedDeployment: ");
            tmp.append(md.getName());
            tmp.append("; state: ").append(state);
            tmp.append('\n');
            if(state == DeploymentState.STOPPED || state == DeploymentState.FAILED)
            {
               // In case there are debug information
               final Exception e = md.getAttachment(Exception.class);
               if(e != null)
               {
                  tmp.append("Cause: ").append(getStackTrace(e));
                  tmp.append('\n');
               }
            }
            Map<String, ManagedComponent> mcs = md.getComponents();
            if(mcs != null)
            {
               for(ManagedComponent mc : mcs.values())
               {
                  tmp.append("+++ ManagedComponent(name=");
                  tmp.append(mc.getName());
                  tmp.append(", type=(");
                  tmp.append(mc.getType());
                  tmp.append("), compName=");
                  tmp.append(mc.getComponentName());
                  tmp.append(", attachment: ");
                  tmp.append(mc.getAttachmentName());
                  tmp.append("\n++++++ properties: ");
                  tmp.append(mc.getPropertyNames());
                  tmp.append('\n');
               }
            }
            tmp.append('\n');
         }
      }
      catch(Exception e)
      {
         throw new ServletException("listDeploymentTree failure", e);
      }
      return tmp.toString();
   }
   
   protected String listOperations()
   {
      StringBuilder tmp = new StringBuilder();
      tmp.append("Available operations: ");
      tmp.append("<ul>");
      for(Operations op : Operations.values())
      {
         tmp.append("<li>");
         tmp.append(op.name());
         tmp.append("</li>\n");
      }
      return tmp.toString();
   }
   
   static String getStackTrace(Throwable aThrowable) {
      final Writer result = new StringWriter();
      final PrintWriter printWriter = new PrintWriter(result);
      aThrowable.printStackTrace(printWriter);
      return result.toString();
    }
   
}

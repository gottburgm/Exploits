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
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;

import javax.naming.InitialContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jboss.security.AuthorizationManager;
import org.jboss.security.acl.EntitlementEntry;
import org.jboss.security.authorization.EntitlementHolder;
import org.jboss.security.authorization.Resource;
import org.jboss.security.authorization.ResourceKeys;
import org.jboss.security.identity.Identity;
import org.jboss.security.identity.plugins.SimpleIdentity;
import org.jboss.test.security.resources.TestResource;

/**
 * <p>
 * A servlet implementation that looks up the authorization manager and call the getEntitlements method to obtain the
 * resources (and associated permissions) that are available to the identity specified in the request.
 * </p>
 * 
 * @author <a href="mailto:sguilhen@redhat.com">Stefan Guilhen</a>
 */
public class ACLServlet extends HttpServlet
{

   private static final long serialVersionUID = 511576053104979345L;

   /*
    * (non-Javadoc)
    * 
    * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest,
    *      javax.servlet.http.HttpServletResponse)
    */
   @Override
   protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
   {
      this.processRequest(request, response);
   }

   /*
    * (non-Javadoc)
    * 
    * @see javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest,
    *      javax.servlet.http.HttpServletResponse)
    */
   @Override
   protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
   {
      this.processRequest(request, response);
   }

   /**
    * <p>
    * This method calls the {@code AuthorizationManager#getEntitlements} method using the identity retrieved from the
    * request and then writes the result in the response. Each line written to the response corresponds to one
    * {@code EntitlementEntry} and has the following format: resource_id:permissions.
    * </p>
    * 
    * @param request the {@code HttpServletRequest} that contains the identity for which the entitlements are to be
    *            retrieved.
    * @param response the {@code HttpServletResponse} object used to write the results of calling
    *            {@code getEntitlements} with the identity retrieved from the request.
    * @throws ServletException if an error occurs while processing the request.
    * @throws IOException if an input or output error is detected when the servlet handles the request.
    */
   protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException,
         IOException
   {
      try
      {
         // first retrieve the authorization manager for the acl-domain.
         InitialContext ctx = new InitialContext();
         AuthorizationManager manager = (AuthorizationManager) ctx.lookup("java:jaas/acl-domain/authorizationMgr");

         // create a resource 1 that has resource 2 as a child.
         TestResource resource1 = new TestResource(1);
         TestResource resource2 = new TestResource(2);
         Collection<Resource> childResources = new ArrayList<Resource>();
         childResources.add(resource2);
         resource1.getMap().put(ResourceKeys.CHILD_RESOURCES, childResources);
         resource2.getMap().put(ResourceKeys.PARENT_RESOURCE, resource1);

         // retrieve the identity name from the request.
         String name = request.getParameter("identity");
         Identity identity = new SimpleIdentity(name);

         // now call the getEntitlements method using created resource and identity objects.
         EntitlementHolder<EntitlementEntry> holder = manager.getEntitlements(EntitlementEntry.class, resource1,
               identity);

         // write the results in the response (resource id : permissions)
         response.setContentType("text/html");
         PrintWriter writer = response.getWriter();
         for (EntitlementEntry entry : holder.getEntitled())
         {
            TestResource resource = (TestResource) entry.getResource();
            writer.println(resource.getId() + ":" + entry.getPermission());
         }
         writer.close();
      }
      catch (Exception e)
      {
         throw new ServletException("Failed to obtain entitlements from authorization manager", e);
      }
   }
}

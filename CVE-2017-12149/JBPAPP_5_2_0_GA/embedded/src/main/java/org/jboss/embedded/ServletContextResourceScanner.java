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
package org.jboss.embedded;

import java.net.URL;
import java.util.StringTokenizer;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.jboss.deployers.spi.DeploymentException;

/**
 * This listener takes in the init parameter "jboss.embedded.resources"
 * which is a comma delimited list of ServletContext resources that you want
 * deployed within the WAR file.  i.e. "/WEB-INF/lib/mybeans.jar"
 *
 * When the WAR is initialized, those referenced resources will be deployed.
 * When the WAR is destroyed, those referenced resources are undeployed from the
 * embedded kernel.
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @version $Revision: 85945 $
 */
public class ServletContextResourceScanner implements ServletContextListener
{
   private DeploymentGroup group = Bootstrap.getInstance().createDeploymentGroup();

   public void contextInitialized(ServletContextEvent servletContextEvent)
   {
      ServletContext servletContext = servletContextEvent.getServletContext();
      String resources = servletContext.getInitParameter("jboss.embedded.resources");

      if (resources == null) throw new RuntimeException("You must specify a resources parameter to the ServletContextResourceScanner");

      StringTokenizer tokenizer = new StringTokenizer(resources, ",");
      while (tokenizer.hasMoreTokens())
      {
         String token = tokenizer.nextToken().trim();
         try
         {
            URL resource = servletContext.getResource(token);
            group.add(resource);
         }
         catch (Exception e)
         {
            throw new RuntimeException("Unable to add servlet resource to deloyer: " + token, e);
         }
      }
      try
      {
         group.process();
      }
      catch (DeploymentException e)
      {
         throw new RuntimeException("Failed to deploy servlet resources: " + resources, e);
      }
   }

   public void contextDestroyed(ServletContextEvent servletContextEvent)
   {
      try
      {
         group.undeploy();
      }
      catch (DeploymentException e)
      {
         throw new RuntimeException(e);
      }
   }
}

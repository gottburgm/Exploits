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
import java.util.Set;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.jboss.deployers.spi.DeploymentException;
import org.jboss.logging.Logger;

/**
 * Deploy all jars in WEB-INF/lib.  These jars are undeployed at WAR destruction
 *
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @version $Revision: 85945 $
 */
public class WebInfLibScanner implements ServletContextListener
{
   private static final Logger log = Logger.getLogger(WebInfLibScanner.class);

   private DeploymentGroup group = Bootstrap.getInstance().createDeploymentGroup();

   public void contextInitialized(ServletContextEvent servletContextEvent)
   {
      scan(servletContextEvent, "/WEB-INF/lib");
   }

   protected void scan(ServletContextEvent servletContextEvent, String path)
   {
      try
      {
         ServletContext servletContext = servletContextEvent.getServletContext();
         Set libJars = servletContext.getResourcePaths(path);
         for (Object jar : libJars)
         {
            URL archive = servletContext.getResource((String) jar);
            group.add(archive);
         }
         group.process();
      }
      catch (Exception e)
      {
         try
         {
            group.undeploy();
         }
         catch (DeploymentException e1)
         {
            log.warn("failed to undeploy on error");
         }
         log.error("failed to deploy from ServletListenerDeployer", e);
         throw new RuntimeException(e);
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

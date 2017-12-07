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
package org.jboss.embedded.tomcat;

import java.io.File;
import java.lang.reflect.Method;

import org.apache.catalina.Lifecycle;
import org.apache.catalina.LifecycleEvent;
import org.apache.catalina.LifecycleListener;
import org.apache.catalina.core.StandardContext;
import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.embedded.Bootstrap;
import org.jboss.embedded.DeploymentGroup;

/**
 * Searches for WAR's WEB-INF directory and deploys that directory.
 * A JBoss DeploymentContext is created and the StandardContext is added as an attachment
 *
 * This listener assumes an exploded deployment, in other words, that WEB-INF/ is
 * a directory in a file system.
 *
 * @author <a href="bill@jboss.com">Bill Burke</a>
 * @author adrian@jboss.org
 * @version $Revision: 85945 $
 */
public class WebinfScanner implements LifecycleListener
{
   private DeploymentGroup group;

   public void lifecycleEvent(LifecycleEvent event)
   {
      if (event.getType().equals(Lifecycle.START_EVENT))
      {
         if (!(event.getSource() instanceof StandardContext))
            return;

         StandardContext container = (StandardContext)event.getSource();
         container.getLoader().getClassLoader();
         Method getBasePath = getBasePathMethod();
         String basePath;
         try
         {
            basePath = (String)getBasePath.invoke(container);
         }
         catch (Exception e)
         {
            throw new RuntimeException(e);
         }

         File webInf = new File(basePath, "WEB-INF");
         File webInfLib = new File(webInf, "lib");
         File webInfClasses = new File(webInf, "classes");

         group = Bootstrap.getInstance().createDeploymentGroup();
         try
         {
            if (webInfLib.exists())
            {
               group.add(webInfLib.toURL());
            }
            if (webInfClasses.exists())
            {
               group.add(webInfClasses.toURL());
            }
            for (DeploymentUnit deployment : group.getDeploymentUnits())
            {
               deployment.getTransientManagedObjects().addAttachment(StandardContext.class, container);
            }
            group.process();
         }
         catch (Exception e)
         {
            throw new RuntimeException(e);
         }
      }
      else if (event.getType().equals(Lifecycle.STOP_EVENT))
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

   private static Method basePathMethod;

   private static synchronized Method getBasePathMethod()
   {
      if (basePathMethod != null) return basePathMethod;
      try
      {
         basePathMethod = StandardContext.class.getDeclaredMethod("getBasePath");
      }
      catch (NoSuchMethodException e)
      {
         throw new RuntimeException(e);
      }
      basePathMethod.setAccessible(true);
      return basePathMethod;
   }
}

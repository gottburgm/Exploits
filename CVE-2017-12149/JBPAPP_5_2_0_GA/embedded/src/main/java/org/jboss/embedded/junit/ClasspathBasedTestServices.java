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
package org.jboss.embedded.junit;

import org.jboss.test.JBossTestServices;
import org.jboss.embedded.Bootstrap;
import org.jboss.embedded.adapters.JMXKernel;

import javax.management.MBeanServerConnection;
import java.net.URL;

/**
 * comment
 *
 * @author <a href="bill@jboss.com">Bill Burke</a>
 * @version $Revision: 85945 $
 */
public class ClasspathBasedTestServices extends JBossTestServices
{
   private boolean top;
   private Bootstrap bootstrap;

   public ClasspathBasedTestServices(String className)
   {
      super(className);
   }

   public ClasspathBasedTestServices(Class clazz)
   {
      super(clazz);
   }


   @Override
   public void setUp() throws Exception
   {
      bootstrap = Bootstrap.getInstance();
      if (!bootstrap.isStarted())
      {
         bootstrap.bootstrap();
         top = true;
      }
      super.setUp();
   }

   @Override
   public void tearDown() throws Exception
   {
      super.tearDown();
      if (top) bootstrap.shutdown();
   }


   @Override
   public MBeanServerConnection getServer() throws Exception
   {
      if (server == null)
      {
         JMXKernel jmxKernel = (JMXKernel)bootstrap.getKernel().getRegistry().getEntry("JMXKernel").getTarget();
         server = jmxKernel.getMbeanServer();
      }
      return server;
   }

   @Override
   public void deploy(String name) throws Exception
   {
      URL url = getDeployURL(name);
      bootstrap.deploy(url);
   }

   @Override
   public void redeploy(String name) throws Exception
   {
      URL url = getDeployURL(name);
      bootstrap.deploy(url);
   }

   @Override
   public void undeploy(String name) throws Exception
   {
      URL url = getDeployURL(name);
      bootstrap.undeploy(url);
   }


}

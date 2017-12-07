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
package org.jboss.web.tomcat.service.management;

import org.jboss.managed.api.annotation.ManagementComponent;
import org.jboss.managed.api.annotation.ManagementObject;
import org.jboss.managed.api.annotation.ManagementProperties;
import org.jboss.managed.api.annotation.ManagementProperty;
import org.jboss.managed.api.annotation.ViewUse;

/**
 * @author Scott.Stark@jboss.org
 * @version $Revision: 86269 $
 */
@ManagementObject(
      componentType=@ManagementComponent(type = "MBean", subtype = "Web"),
      isRuntime=true,
      properties=ManagementProperties.EXPLICIT)
public class ThreadPool extends BaseBean
{
   IThreadPool mbeanProxy;

   @ManagementProperty(use=ViewUse.STATISTIC)
   public int getCurrentThreadCount()
   {
      initProxy();
      return mbeanProxy.getcurrentThreadCount();
   }

   @ManagementProperty(use=ViewUse.STATISTIC)
   public int getCurrentThreadsBusy()
   {
      initProxy();
      return mbeanProxy.getcurrentThreadsBusy();
   }

   @ManagementProperty(use=ViewUse.STATISTIC)
   public int getMaxThreads()
   {
      initProxy();
      return mbeanProxy.getmaxThreads();
   }

   @ManagementProperty(use=ViewUse.STATISTIC)
   public int getThreadPriority()
   {
      initProxy();
      return mbeanProxy.getthreadPriority();
   }

   protected void initProxy()
   {
      if(mbeanProxy == null)
      {
         // Set the name property
         String name = "http-" + getAddress() +"-"+getPort();
         super.getNameProps().put("name", name);
         mbeanProxy = super.initProxy(IThreadPool.class);
      }
   }
}

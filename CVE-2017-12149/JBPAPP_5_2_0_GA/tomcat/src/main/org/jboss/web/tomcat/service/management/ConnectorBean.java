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

import org.apache.coyote.RequestInfo;
import org.jboss.managed.api.annotation.ManagementComponent;
import org.jboss.managed.api.annotation.ManagementObject;
import org.jboss.managed.api.annotation.ManagementOperation;
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
public class ConnectorBean extends BaseBean
{
   private IConnector mbeanProxy;


   @ManagementProperty(use=ViewUse.STATISTIC)
   public long getBytesReceived()
      throws Exception
   {
      initProxy();
      return mbeanProxy.getbytesReceived();
   }

   @ManagementProperty(use=ViewUse.STATISTIC)
   public long getBytesSent()
   throws Exception
   {
      initProxy();
      long sent = mbeanProxy.getbytesSent();
      return sent;
   }

   @ManagementProperty(use=ViewUse.STATISTIC)
   public int getErrorCount()
   throws Exception
   {
      initProxy();
      return mbeanProxy.geterrorCount();
   }

   @ManagementProperty(use=ViewUse.STATISTIC)
   public long getMaxTime()
   throws Exception
   {
      initProxy();
      long maxTime = mbeanProxy.getmaxTime();
      return maxTime;
   }

   @ManagementProperty(use=ViewUse.STATISTIC)
   public long getProcessingTime()
   throws Exception
   {
      initProxy();
      return mbeanProxy.getprocessingTime();
   }

   @ManagementProperty(use=ViewUse.STATISTIC)
   public int getRequestCount()
   throws Exception
   {
      initProxy();
      int count = mbeanProxy.getrequestCount();
      return count;
   }

   @ManagementProperty(use=ViewUse.STATISTIC, ignored=true)
   public RequestInfo[] getRequestProcessors()
   throws Exception
   {
      initProxy();
      return mbeanProxy.getrequestProcessors();
   }

   @ManagementOperation()
   public void resetCounters()
   throws Exception
   {
      initProxy();
      mbeanProxy.resetCounters();
   }

   
   protected void initProxy()
   {
      if(mbeanProxy == null)
      {
         // Set the name property
         String name = "http-" + getAddress() +"-"+getPort();
         super.getNameProps().put("name", name);
         mbeanProxy = super.initProxy(IConnector.class);
      }
   }
   
}

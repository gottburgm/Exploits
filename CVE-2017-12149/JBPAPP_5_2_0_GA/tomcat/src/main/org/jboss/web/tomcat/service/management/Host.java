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

/**
 * Host managed objects
 * 
 * @author Scott.Stark@jboss.org
 * @version $Revision: 85945 $
 */
@ManagementObject(
      componentType=@ManagementComponent(type = "MBean", subtype = "Web"),
      isRuntime=true,
      properties=ManagementProperties.EXPLICIT)
public class Host extends BaseBean
{
   private IHost mbeanProxy;

   @ManagementProperty(description="The Host aliases")
   public String[] getaliases()
   {
      return mbeanProxy.getaliases();
   }

   @ManagementProperty(description="The MBean Names of the Valves associated with this Host")
   public String[] getvalveNames()
   {
      return mbeanProxy.getvalveNames();
   }

   @ManagementProperty(description="Unpack WARs property")
   public boolean getunpackWARs()
   {
      return mbeanProxy.getunpackWARs();
   }

   protected void initProxy()
   {
      if(mbeanProxy == null)
      {
         // host=host1.x.com,type=Host
         mbeanProxy = super.initProxy(IHost.class);
      }
   }
}

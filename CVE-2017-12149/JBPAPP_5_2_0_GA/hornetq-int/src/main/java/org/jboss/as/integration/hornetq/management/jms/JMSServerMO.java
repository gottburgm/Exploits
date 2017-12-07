/*
 * JBoss, Home of Professional Open Source
 * Copyright (c) 2010, JBoss Inc., and individual contributors as indicated
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
package org.jboss.as.integration.hornetq.management.jms;

import org.hornetq.api.core.management.HornetQServerControl;
import org.hornetq.api.jms.management.JMSServerControl;
import org.hornetq.core.server.HornetQServer;
import org.jboss.managed.api.annotation.*;

import java.lang.reflect.Method;

/**
 * @author <a href="mailto:andy.taylor@jboss.org">Andy Taylor</a>
 *         Created Mar 26, 2010
 */
@ManagementObject(componentType = @ManagementComponent(type = "JMSManage", subtype = "ServerManage"),
    properties = ManagementProperties.EXPLICIT, isRuntime = true)
public class JMSServerMO extends JMSManageMO
{
   public JMSServerMO(HornetQServer server)
   {
      super(server);   
   }

   @ManagementOperation(name = "getVersion", description = "returns the servers version")
   public String getVersion()
   {
      JMSServerControl jmsServerControl = getJmsServerControl();
      if(jmsServerControl == null)
      {
         return null;
      }
      else
      {
         return jmsServerControl.getVersion();
      }
   }

   @ManagementOperation(name = "getVersion", description = "returns the servers status")
   public boolean isStarted()
   {
      JMSServerControl jmsServerControl = getJmsServerControl();
      return jmsServerControl != null && jmsServerControl.isStarted();
   }

   @ManagementOperation(name = "invokeManagerOperation", description = "invokes a queues method",
       params = {
           @ManagementParameter(name = "name", description = "the name"),
           @ManagementParameter(name = "method", description = "the method"),
           @ManagementParameter(name = "params", description = "the method params")})
   public Object invokeManagerOperation(String name, String method, String[] params, String[] type) throws Exception
   {
      Class[] classes = getClassTypes(type);
      Method m = null;
      try
      {
         JMSServerControl jmsServerControl = getJmsServerControl();
         m = jmsServerControl.getClass().getMethod(method, classes);
         return m.invoke(jmsServerControl, getParams(params, classes));
      }
      catch (NoSuchMethodException e)
      {
         HornetQServerControl hornetQServerControl = getHornetQServerControl();
         m = hornetQServerControl.getClass().getMethod(method, classes);
         return m.invoke(hornetQServerControl, getParams(params, classes));
      }

   }
}

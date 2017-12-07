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
import org.hornetq.api.core.management.ResourceNames;
import org.hornetq.api.jms.management.JMSServerControl;
import org.hornetq.core.server.HornetQServer;
import org.hornetq.core.server.management.ManagementService;

/**
 * @author <a href="mailto:andy.taylor@jboss.org">Andy Taylor</a>
 *         Created: 17-Mar-2010
 */
public class JMSManageMO
{
   private JMSServerControl jmsServerControl;

   private ManagementService managementService;

   private HornetQServerControl hornetQServerControl;

   private HornetQServer server;


   public JMSManageMO(HornetQServer server)
   {
      this.server = server;
   }

   protected String coomaSeparatedString(Object[] objects)
   {
      StringBuffer sb = new StringBuffer("");
      for (Object object : objects)
      {
         if (sb.length() > 0)
         {
            sb.append(",");
         }
         sb.append(object.toString());
      }
      return sb.toString();
   }


   protected Object[] getParams(String[] params, Class[] classes)
   {
      Object[] objects = new Object[params.length];
      for (int i = 0, objectsLength = objects.length; i < objectsLength; i++)
      {
         if ("null".equalsIgnoreCase(params[i]))
         {
            objects[i] = null;
         }
         else if (classes[i] == String.class)
         {
            objects[i] = params[i];
         }
         else if (classes[i] == Double.class)
         {
            objects[i] = Double.valueOf(params[i]);
         }
         else if (classes[i] == double.class)
         {
            objects[i] = Double.valueOf(params[i]);
         }
         else if (classes[i] == Long.class)
         {
            objects[i] = Long.valueOf(params[i]);
         }
         else if (classes[i] == long.class)
         {
            objects[i] = Long.valueOf(params[i]);
         }
         else if (classes[i] == Integer.class)
         {
            objects[i] = Integer.valueOf(params[i]);
         }
         else if (classes[i] == int.class)
         {
            objects[i] = Integer.valueOf(params[i]);
         }
         else if (classes[i] == Float.class)
         {
            objects[i] = Float.valueOf(params[i]);
         }
         else if (classes[i] == float.class)
         {
            objects[i] = Float.valueOf(params[i]);
         }
      }
      return objects;
   }

   protected Class[] getClassTypes(String[] type) throws Exception
   {
      Class[] classes = new Class[type.length];
      for (int i = 0, typeLength = type.length; i < typeLength; i++)
      {
         classes[i] = Class.forName(type[i]);
      }
      return classes;
   }

   public JMSServerControl getJmsServerControl()
   {
      if(jmsServerControl == null)
      {
         jmsServerControl = (JMSServerControl) getManagementService().getResource(ResourceNames.JMS_SERVER);
      }
      return jmsServerControl;
   }

   public ManagementService getManagementService()
   {
      if(managementService == null)
      {
         managementService = server.getManagementService();
      }
      return managementService;
   }

   public HornetQServerControl getHornetQServerControl()
   {
      if(hornetQServerControl == null)
      {
         hornetQServerControl = (HornetQServerControl) getManagementService().getResource(ResourceNames.CORE_SERVER);
      }
      return hornetQServerControl;
   }
}

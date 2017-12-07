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

import java.util.Hashtable;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.apache.coyote.RequestGroupInfo;
import org.jboss.managed.api.annotation.ManagementObjectID;
import org.jboss.managed.api.annotation.ManagementProperty;
import org.jboss.mx.util.MBeanProxyExt;

/**
 * @author Scott.Stark@jboss.org
 * @version $Revision: 85945 $
 */
public class BaseBean
{
   private String domain;
   private Hashtable nameProps;
   private ObjectName mbeanName;
   private MBeanServer server;
   private int port;
   private String address;

   
   public int getPort()
   {
      return port;
   }

   public void setPort(int port)
   {
      this.port = port;
   }

   public String getAddress()
   {
      return address;
   }

   public void setAddress(String address)
   {
      this.address = address;
   }
   public String getDomain()
   {
      return domain;
   }
   public void setDomain(String domain)
   {
      this.domain = domain;
   }
   public Hashtable getNameProps()
   {
      return nameProps;
   }
   public void setNameProps(Hashtable nameProps)
   {
      this.nameProps = nameProps;
   }
   public ObjectName getMbeanName()
   {
      return mbeanName;
   }
   public void setMbeanName(ObjectName mbeanName)
   {
      this.mbeanName = mbeanName;
   }
   @ManagementProperty()
   @ManagementObjectID
   public String getMbeanNameAsString()
   {
      initMBeanName();
      return mbeanName.getCanonicalName();
   }
   public MBeanServer getServer()
   {
      return server;
   }
   public void setServer(MBeanServer server)
   {
      this.server = server;
   }

   protected void initMBeanName()
   {
      try
      {
         mbeanName = new ObjectName(domain, nameProps);
      }
      catch(Exception e)
      {
         throw new IllegalStateException(e);
      }
   }
   protected <T> T initProxy(Class<T> clazz)
   {
      T mbeanProxy = null;
      try
      {
         mbeanName = new ObjectName(domain, nameProps);
         mbeanProxy = (T) MBeanProxyExt.create(clazz, mbeanName, server);
      }
      catch(Exception e)
      {
         throw new IllegalStateException(e);
      }
      return mbeanProxy;
   }
}

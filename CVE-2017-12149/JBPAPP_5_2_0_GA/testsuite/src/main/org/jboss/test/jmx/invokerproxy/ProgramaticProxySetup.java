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
package org.jboss.test.jmx.invokerproxy;

import java.util.Date;
import java.util.Hashtable;
import javax.management.ObjectName;
import javax.management.MBeanServer;

import org.jboss.invocation.jrmp.server.JRMPProxyFactory;

/** A POJO XMBean service that uses the JRMPProxyFactory programatically to
 * expose an IProxy interface to this services echoDate method.
 * 
 * @author Scott.Stark@jboss.org
 * @version $Revision: 81036 $
 */
public class ProgramaticProxySetup
{
   private String jndiName;
   private MBeanServer server;
   private ObjectName serviceName;
   private ObjectName invokerName;
   private JRMPProxyFactory proxyFactory;

   public void setMBeanServer(MBeanServer server)
   {
      this.server = server;
   }

   public void setObjectName(ObjectName serviceName)
   {
      this.serviceName = serviceName;
   }

   public ObjectName getInvokerName()
   {
      return invokerName;
   }
   public void setInvokerName(ObjectName invokerName)
   {
      this.invokerName = invokerName;
   }

   public String getJndiName()
   {
      return jndiName;
   }
   public void setJndiName(String jndiName)
   {
      this.jndiName = jndiName;
   }

   public void start() throws Exception
   {
      proxyFactory = new JRMPProxyFactory();
      proxyFactory.setInvokerName(invokerName);
      proxyFactory.setTargetName(serviceName);
      proxyFactory.setJndiName(jndiName);
      proxyFactory.setExportedInterface(IProxy.class);
      proxyFactory.setInvokeTargetMethod(true);
      Hashtable props = serviceName.getKeyPropertyList();
      props.put("proxyFactory", "JRMPProxyFactory");
      ObjectName proxyFactoryName = new ObjectName(serviceName.getDomain(), props);
      server.registerMBean(proxyFactory, proxyFactoryName);
      proxyFactory.start();
      Object proxy = proxyFactory.getProxy();
      System.out.println("Created IProxy: "+proxy);
   }

   public void stop()
   {
      proxyFactory.stop();
      try
      {
         Hashtable props = serviceName.getKeyPropertyList();
         props.put("proxyFactory", "JRMPProxyFactory");
         ObjectName proxyFactoryName = new ObjectName(serviceName.getDomain(), props);
         server.unregisterMBean(proxyFactoryName);
      }
      catch(Exception e)
      {
         e.printStackTrace();
      }
   }

   public String echoDate(String prefix)
   {
      String date = prefix + " - " + new Date();
      return date;
   }
}

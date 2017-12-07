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
package org.jboss.test.classloader.scoping.naming.service;

import javax.naming.InitialContext;
import javax.naming.Context;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.Attribute;

import org.jboss.mx.util.MBeanServerLocator;

/** A service that binds custom serializable objects into jndi for use by
 * a web app to test the behavior of jndi lookups across class loading
 * scopes.
 * 
 * @author Scott.Stark@jboss.org
 * @version $Revision: 81036 $
 */
public class BindingService
{
   private String[] names = {"Name0", "Name1", "Name2"};
   private Boolean origCallByValue;

   public String[] getNames()
   {
      return names;
   }
   public void setNames(String[] names)
   {
      this.names = names;
   }

   public void start() throws Exception
   {
      // Put the NamingService into call by value mode
      MBeanServer server = MBeanServerLocator.locateJBoss();
      ObjectName namingService = new ObjectName("jboss:service=Naming");
      origCallByValue = (Boolean) server.getAttribute(namingService, "CallByValue");
      Attribute callByValue = new Attribute("CallByValue", Boolean.TRUE);
      server.setAttribute(namingService, callByValue);
      System.out.println("NamingService.CallByValue set to true");

      InitialContext ctx = new InitialContext();
      Context testCtx = ctx.createSubcontext("shared-context");
      System.out.println("Created shared-context");
      testCtx.bind("KeyCount", new Integer(names.length));
      System.out.println("Bound KeyCount");
      for(int n = 0; n < names.length; n ++)
      {
         String key = "Key#" + n;
         BindValue value = new BindValue();
         value.setValue("Value#"+n);
         testCtx.bind(key, value);
         System.out.println("Bound "+key);
      }
   }

   public void stop() throws Exception
   {
      MBeanServer server = MBeanServerLocator.locateJBoss();
      ObjectName namingService = new ObjectName("jboss:service=Naming");
      Attribute callByValue = new Attribute("CallByValue", origCallByValue);
      server.setAttribute(namingService, callByValue);
      System.out.println("NamingService.CallByValue restored to: "+origCallByValue);

      InitialContext ctx = new InitialContext();
      Context testCtx = (Context) ctx.lookup("shared-context");
      testCtx.unbind("KeyCount");
      System.out.println("Unbound KeyCount");
      for(int n = 0; n < names.length; n ++)
      {
         String key = "Key#" + n;
         testCtx.unbind(key);
         System.out.println("Unbound "+key);
      }
      ctx.unbind("shared-context");
      System.out.println("Destroyed shared-context");
   }
}

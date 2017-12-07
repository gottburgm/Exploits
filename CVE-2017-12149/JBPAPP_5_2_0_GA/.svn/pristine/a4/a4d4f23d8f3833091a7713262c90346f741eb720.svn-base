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
package org.jboss.test.cluster.haservice;

import java.util.List;
import java.util.Stack;

import javax.management.MalformedObjectNameException;
import javax.management.Notification;
import javax.management.ObjectName;

import org.jboss.ha.framework.interfaces.HAService;
import org.jboss.ha.framework.server.HAServiceImpl;
import org.jboss.ha.jmx.HAServiceMBeanSupport;

/**
 * 
 * @author   Ivelin Ivanov <ivelin@apache.org>
 *
 */
public class HAServiceMBeanSupportTester extends HAServiceMBeanSupport
{
   public static final String SERVICE_NAME = "jboss.examples:name=HAServiceMBeanSupportTester";
   
   public final Stack<Object> invocationStack = new Stack<Object>();

   public boolean isSingletonStarted = false;

   public boolean shouldSendNotificationRemoteFail = false;

   /**
    * @see org.jboss.ha.jmx.HAServiceMBeanSupport#createHAService()
    */
   @Override
   protected HAService<Notification> createHAService()
   {
      return new HAServiceImpl<Notification>(this, this)
      {
         @Override
         protected void registerRPCHandler()
         {
            HAServiceMBeanSupportTester.this.invocationStack.push("registerRPCHandler");
         }

         @Override
         protected void unregisterRPCHandler()
         {
            HAServiceMBeanSupportTester.this.invocationStack.push("unregisterRPCHandler");
         }

         @Override
         protected void registerDRMListener() throws Exception
         {
            HAServiceMBeanSupportTester.this.invocationStack.push("registerDRMListener");
         }

         @Override
         protected void unregisterDRMListener() throws Exception
         {
            HAServiceMBeanSupportTester.this.invocationStack.push("unregisterDRMListener");
         }
      };
   }

   @Override
   protected void setupPartition() throws Exception
   {
      this.invocationStack.push("setupPartition");
   }

   @Override
   protected void sendNotificationRemote(Notification notification)
      throws Exception
   {
      if (this.shouldSendNotificationRemoteFail)
      {
         throw new Exception("simulated exception");
      }
      this.invocationStack.push("sendNotificationRemote");
      this.invocationStack.push(notification);
   }

   @Override
   protected void sendNotificationToLocalListeners(Notification notification)
   {
      this.invocationStack.push("sendNotificationToLocalListeners");
      this.invocationStack.push(notification);
   }

   @Override
   @SuppressWarnings("unchecked")
   public List callMethodOnPartition(String methodName, Object[] args, Class[] types) throws Exception
   {
      this.invocationStack.push("callMethodOnCluster:" + methodName);
      return null;
   }

   @Override
   public ObjectName getServiceName()
   {
      ObjectName oname = null;
      try
      {
         oname = new ObjectName(SERVICE_NAME);
      }
      catch (MalformedObjectNameException e)
      {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }
      return oname;
   }
}

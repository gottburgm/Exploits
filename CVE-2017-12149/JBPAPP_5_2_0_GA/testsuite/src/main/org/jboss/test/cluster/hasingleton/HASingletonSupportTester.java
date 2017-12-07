/*
 * JBoss, Home of Professional Open Source
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
package org.jboss.test.cluster.hasingleton;

import java.util.List;
import java.util.Stack;

import javax.management.Notification;

import org.jboss.ha.singleton.HASingletonSupport;

/**
 * 
 * @author   Ivelin Ivanov <ivelin@apache.org>
 *
 */
public class HASingletonSupportTester extends HASingletonSupport
{
   public final Stack<String> invocationStack = new Stack<String>();

   public boolean isDRMMasterReplica = false;
   public boolean isSingletonStarted = false;

   /**
    * @see org.jboss.ha.singleton.HASingletonSupport#createHAService()
    */
   @Override
   protected HASingletonTester createHAService()
   {
      return new HASingletonTester(this);
   }
   
   @Override
   protected void setupPartition() throws Exception
   {
      this.invocationStack.push("setupPartition");
   }

   @Override
   @SuppressWarnings("unchecked")
   public List callMethodOnPartition(String methodName, Object[] args, Class[] types) throws Exception
   {
      this.invocationStack.push("callMethodOnCluster:" + methodName);
      return null;
   }

   @Override
   public void startSingleton()
   {
      this.invocationStack.push("startSingleton");
   }

   @Override
   public void stopSingleton()
   {
      this.invocationStack.push("stopSingleton");
   }

   @Override
   public void sendNotification(Notification notification)
   {
   }
   
   public HASingletonTester getDelegate()
   {
      return (HASingletonTester) this.getHAService();
   }
}

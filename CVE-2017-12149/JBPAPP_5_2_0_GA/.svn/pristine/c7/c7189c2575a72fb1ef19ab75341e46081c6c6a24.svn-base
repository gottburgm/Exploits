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
package org.jboss.test.cluster.hasingleton;

import java.util.List;

import javax.management.Notification;

import org.jboss.ha.framework.server.HASingletonImpl;

/**
 * @author Paul Ferraro
 *
 */
public class HASingletonTester extends HASingletonImpl<Notification>
{
   private final HASingletonSupportTester tester;

   public HASingletonTester(HASingletonSupportTester tester)
   {
      super(tester, tester, tester);
      
      this.tester = tester;
      
      this.setHAPartition(new MockHAPartition());
      this.setServiceHAName("HASingletonSupportTester");
   }

   @Override
   protected void registerRPCHandler()
   {
      this.tester.invocationStack.push("registerRPCHandler");
   }

   @Override
   protected void unregisterRPCHandler()
   {
      this.tester.invocationStack.push("unregisterRPCHandler");
   }

   @Override
   protected void registerDRMListener() throws Exception
   {
      this.tester.invocationStack.push("registerDRMListener");
   }

   @Override
   protected void unregisterDRMListener() throws Exception
   {
      this.tester.invocationStack.push("unregisterDRMListener");
   }
   
   @Override
   protected void makeThisNodeMaster()
   {
      this.tester.invocationStack.push("makeThisNodeMaster");
      super.makeThisNodeMaster();
   }
   
   @Override
   protected void restartMaster()
   {
      this.tester.invocationStack.push("restartMaster");
      super.restartMaster();
   }

   @Override
   protected boolean isDRMMasterReplica()
   {
      this.tester.invocationStack.push("isDRMMasterReplica");
      return this.tester.isDRMMasterReplica;
   }
   
   @Override
   public void stopIfMaster()
   {
      super.stopIfMaster();
   }

   @Override
   public void partitionTopologyChanged(List<?> newReplicants, int newViewID, boolean merge)
   {
      super.partitionTopologyChanged(newReplicants, newViewID, merge);
   }
}
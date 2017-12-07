/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
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

package org.jboss.test.cluster.defaultcfg.test;

import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.resetToStrict;
import static org.easymock.EasyMock.verify;

import java.util.List;
import java.util.Vector;

import org.jboss.ha.framework.interfaces.ClusterNode;
import org.jboss.ha.framework.interfaces.HAPartition;
import org.jboss.ha.framework.server.lock.YieldingGloballyExclusiveClusterLockSupport;
import org.jboss.ha.framework.server.lock.LocalLockHandler;
import org.jboss.ha.framework.server.lock.RemoteLockResponse;
import org.jboss.ha.framework.server.lock.AbstractClusterLockSupport.RpcTarget;
import org.jboss.test.cluster.lock.ClusteredLockManagerTestBase;

/**
 * Unit test of ExclusiveClusterLockManager
 * 
 * @author Brian Stansberry
 *
 */
public class YieldingGloballyExclusiveClusterLockSupportUnitTestCase extends ClusteredLockManagerTestBase<YieldingGloballyExclusiveClusterLockSupport>
{
   /**
    * Create a new ClusteredLockManagerImplUnitTestCase.
    * 
    * @param name
    */
   public YieldingGloballyExclusiveClusterLockSupportUnitTestCase(String name)
   {
      super(name);
   }

   @Override
   protected YieldingGloballyExclusiveClusterLockSupport createClusteredLockManager(String serviceHAName, 
         HAPartition partition, LocalLockHandler handler)
   {
      return new YieldingGloballyExclusiveClusterLockSupport(serviceHAName, partition, handler);
   }

   public void testBasicRemoteLock() throws Exception
   { 
      TesteeSet<YieldingGloballyExclusiveClusterLockSupport> testeeSet = getTesteeSet(node1, 1, 2);
      YieldingGloballyExclusiveClusterLockSupport testee = testeeSet.impl;
      LocalLockHandler handler = testee.getLocalHandler();
      RpcTarget target = testeeSet.target;
      
      ClusterNode caller = testee.getCurrentView().get(0);
      assertFalse(node1.equals(caller));
      
      resetToStrict(handler);      
      replay(handler);
      
      RemoteLockResponse rsp = target.remoteLock("test", caller, 1000);
      
      assertEquals(RemoteLockResponse.Flag.OK, rsp.flag);
      assertNull(rsp.holder);
      
      verify(handler);
      
      // Do it again; should still work
      resetToStrict(handler);      
      replay(handler);
      
      rsp = target.remoteLock("test", caller, 1000);
      
      assertEquals(RemoteLockResponse.Flag.OK, rsp.flag);
      assertNull(rsp.holder);
      
      verify(handler);
   }
   
   public void testContestedRemoteLock() throws Exception
   { 
      TesteeSet<YieldingGloballyExclusiveClusterLockSupport> testeeSet = getTesteeSet(node1, 1, 3);
      YieldingGloballyExclusiveClusterLockSupport testee = testeeSet.impl;
      LocalLockHandler handler = testee.getLocalHandler();
      RpcTarget target = testeeSet.target;
      
      ClusterNode caller1 = testee.getCurrentView().get(0);
      assertFalse(node1.equals(caller1));
      
      ClusterNode caller2 = testee.getCurrentView().get(2);
      assertFalse(node1.equals(caller2));
      
      resetToStrict(handler);      
      replay(handler);
      
      RemoteLockResponse rsp = target.remoteLock("test", caller1, 1000);
      
      assertEquals(RemoteLockResponse.Flag.OK, rsp.flag);
      assertNull(rsp.holder);
      
      verify(handler);
      
      // A call from a different caller should still work as
      // w/ supportLockOnly==false we only reject if WE hold the lock
      resetToStrict(handler);
      replay(handler);
      
      rsp = target.remoteLock("test", caller2, 1000);
      
      assertEquals(RemoteLockResponse.Flag.OK, rsp.flag);
      assertNull(rsp.holder);
      
      verify(handler);
      
   }
   
   /**
    * Test that if a member holds a lock but is then removed from the
    * view, another remote member can obtain the lock.
    * 
    * @throws Exception
    */
   public void testDeadMemberCleanupAllowsRemoteLock() throws Exception
   { 
      TesteeSet<YieldingGloballyExclusiveClusterLockSupport> testeeSet = getTesteeSet(node1, 1, 3);
      YieldingGloballyExclusiveClusterLockSupport testee = testeeSet.impl;
      LocalLockHandler handler = testee.getLocalHandler();
      RpcTarget target = testeeSet.target;
      
      List<ClusterNode> members = testee.getCurrentView();
      ClusterNode caller1 = members.get(0);
      assertFalse(node1.equals(caller1));
      
      ClusterNode caller2 = members.get(2);
      assertFalse(node1.equals(caller2));
      
      resetToStrict(handler);      
      replay(handler);
      
      RemoteLockResponse rsp = target.remoteLock("test", caller1, 1000);
      
      assertEquals(RemoteLockResponse.Flag.OK, rsp.flag);
      assertNull(rsp.holder);
      
      verify(handler);
      
      // Change the view
      Vector<ClusterNode> dead = new Vector<ClusterNode>();
      dead.add(caller1);
      
      Vector<ClusterNode> all = new Vector<ClusterNode>(members);
      all.remove(caller1);
      
      resetToStrict(handler);
      replay(handler);
      
      testee.membershipChanged(dead, new Vector<ClusterNode>(), all);
      
      verify(handler);
      
      // A call from a different caller should work 
      resetToStrict(handler);
      replay(handler);
      
      rsp = target.remoteLock("test", caller2, 1000);
      
      assertEquals(RemoteLockResponse.Flag.OK, rsp.flag);
      assertNull(rsp.holder);
      
      verify(handler);
   }

}

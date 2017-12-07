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

import static org.easymock.EasyMock.anyLong;
import static org.easymock.EasyMock.aryEq;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.makeThreadSafe;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.resetToNice;
import static org.easymock.EasyMock.resetToStrict;
import static org.easymock.EasyMock.verify;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.jboss.ha.framework.interfaces.ClusterNode;
import org.jboss.ha.framework.interfaces.HAPartition;
import org.jboss.ha.framework.server.lock.AbstractClusterLockSupport;
import org.jboss.ha.framework.server.lock.LocalLockHandler;
import org.jboss.ha.framework.server.lock.NonGloballyExclusiveClusterLockSupport;
import org.jboss.ha.framework.server.lock.RemoteLockResponse;
import org.jboss.ha.framework.server.lock.TimeoutException;
import org.jboss.ha.framework.server.lock.AbstractClusterLockSupport.RpcTarget;
import org.jboss.test.cluster.lock.ClusteredLockManagerTestBase;

/**
 * Unit test of ClusteredLockManagerImpl
 * 
 * @author Brian Stansberry
 *
 */
public class ReadWriteClusteredLockManagerUnitTestCase extends ClusteredLockManagerTestBase<NonGloballyExclusiveClusterLockSupport>
{
   /**
    * Create a new ClusteredLockManagerImplUnitTestCase.
    * 
    * @param name
    */
   public ReadWriteClusteredLockManagerUnitTestCase(String name)
   {
      super(name);
   }

   @Override
   protected NonGloballyExclusiveClusterLockSupport createClusteredLockManager(String serviceHAName, 
         HAPartition partition, LocalLockHandler handler)
   {
      return new NonGloballyExclusiveClusterLockSupport(serviceHAName, partition, handler);
   }
   
   public void testBasicRemoteLock() throws Exception
   { 
      TesteeSet<NonGloballyExclusiveClusterLockSupport> testeeSet = getTesteeSet(node1, 1, 2);
      NonGloballyExclusiveClusterLockSupport testee = testeeSet.impl;
      LocalLockHandler handler = testee.getLocalHandler();
      RpcTarget target = testeeSet.target;
      
      ClusterNode caller = testee.getCurrentView().get(0);
      assertFalse(node1.equals(caller));
      
      resetToStrict(handler);      
      handler.lockFromCluster("test", caller, 1000);
      replay(handler);
      
      RemoteLockResponse rsp = target.remoteLock("test", caller, 1000);
      
      assertEquals(RemoteLockResponse.Flag.OK, rsp.flag);
      assertNull(rsp.holder);
      
      verify(handler); 
      
      // Do it again; should fail as another thread from caller already
      // acquired the lock
      resetToStrict(handler); // fail if we call the local handler
      replay(handler);
      
      rsp = target.remoteLock("test", caller, 1000);
      
      assertEquals(RemoteLockResponse.Flag.REJECT, rsp.flag);
      assertEquals(caller, rsp.holder);
      
      verify(handler);   
   }
   
   public void testContestedRemoteLock() throws Exception
   { 
      TesteeSet<NonGloballyExclusiveClusterLockSupport> testeeSet = getTesteeSet(node1, 1, 3);
      NonGloballyExclusiveClusterLockSupport testee = testeeSet.impl;
      LocalLockHandler handler = testee.getLocalHandler();
      RpcTarget target = testeeSet.target;
      
      ClusterNode caller1 = testee.getCurrentView().get(0);
      assertFalse(node1.equals(caller1));
      
      ClusterNode caller2 = testee.getCurrentView().get(2);
      assertFalse(node1.equals(caller2));
      
      resetToStrict(handler);        
      handler.lockFromCluster("test", caller1, 1000);    
      replay(handler);
      
      RemoteLockResponse rsp = target.remoteLock("test", caller1, 1000);
      
      assertEquals(RemoteLockResponse.Flag.OK, rsp.flag);
      assertNull(rsp.holder);
      
      verify(handler);
      
      // A call from a different caller should be rejected without need
      // to go to the LocalLockHandler
      resetToStrict(handler);
      replay(handler);
      
      rsp = target.remoteLock("test", caller2, 1000);
      
      assertEquals(RemoteLockResponse.Flag.REJECT, rsp.flag);
      assertEquals(caller1, rsp.holder);
      
      verify(handler);      
   }
   
   /**
    * Test for handling concurrent calls to remoteLock when the lock
    * is in UNLOCKED state. Calls should get passed to the local lock handler,
    * which allows one to succeed and the other to throw a TimeoutException;
    * testee should react correctly.
    * 
    * FIXME We are using a MockObject for the LocalLockHandler impl, and with
    * that approach we can't really get concurrent calls to it. Effect is 
    * sometimes the thread that acquires the lock has already done so before
    * the other thread even invokes remoteLock, defeating the purpose of this
    * test and turning it into a variant of testContestedRemoteLock. Need to redo
    * this test with a true multithreaded local lock handler, updating the latches
    * such that both threads are in BlockingAnswer.answer at the same time.
    * 
    * @throws Exception
    */   
   public void testConcurrentRemoteLock() throws Exception
   { 
      TesteeSet<NonGloballyExclusiveClusterLockSupport> testeeSet = getTesteeSet(node1, 1, 3);
      NonGloballyExclusiveClusterLockSupport testee = testeeSet.impl;
      LocalLockHandler handler = testee.getLocalHandler();
      final RpcTarget target = testeeSet.target;
      
      ClusterNode caller1 = testee.getCurrentView().get(0);
      assertFalse(node1.equals(caller1));
      
      ClusterNode caller2 = testee.getCurrentView().get(2);
      assertFalse(node1.equals(caller2));

      
      resetToStrict(handler);   
      makeThreadSafe(handler, true);
      // When caller 1 invokes, block before giving response 
      CountDownLatch answerStartLatch = new CountDownLatch(1);
      CountDownLatch answerDoneLatch = new CountDownLatch(1);
      BlockingAnswer<Boolean> caller1Answer = new BlockingAnswer<Boolean>(Boolean.TRUE, answerStartLatch, null, answerDoneLatch);
      BlockingAnswer<Boolean> caller2Answer = new BlockingAnswer<Boolean>(new TimeoutException(caller1), answerDoneLatch, 0, null, null);
      handler.lockFromCluster("test", caller1, 1000);
      expectLastCall().andAnswer(caller1Answer); 
      handler.lockFromCluster("test", caller2, 1000); 

      
      // There is a race where t1 may have already marked the lock as LOCKED in 
      // which case t2 will not call handler.lockFromCluster("test", caller2, 1000);
      // See FIXME in method javadoc. So, we use times(0, 1) to specify no
      // calls are OK
      expectLastCall().andAnswer(caller2Answer).times(0, 1);    
      replay(handler);
      
      CountDownLatch startLatch1 = new CountDownLatch(1);
      CountDownLatch startLatch2 = new CountDownLatch(1);
      CountDownLatch finishedLatch = new CountDownLatch(2);
      
      RemoteLockCaller winner = new RemoteLockCaller(target, caller1, startLatch1, null, finishedLatch);
      RemoteLockCaller loser = new RemoteLockCaller(target, caller2, startLatch2, null, finishedLatch);
      
      Thread t1 = new Thread(winner);
      t1.setDaemon(true);
      Thread t2 = new Thread(loser);
      t2.setDaemon(true);
      
      try
      {
         t1.start();         
         assertTrue(startLatch1.await(1, TimeUnit.SECONDS));
         // t1 should now be blocking in caller1Answer
         
         t2.start();         
         assertTrue(startLatch2.await(1, TimeUnit.SECONDS));
         // t2 should now be blocking due to t1
         
         // release t1
         answerStartLatch.countDown();
         
         // wait for both to complete
         assertTrue(finishedLatch.await(1, TimeUnit.SECONDS));
         
         verify(handler);
         
         rethrow("winner had an exception", winner.getException());
         rethrow("loser had an exception", loser.getException());
         
         RemoteLockResponse rsp = winner.getResult();         
         assertEquals(RemoteLockResponse.Flag.OK, rsp.flag);
         assertNull(rsp.holder);
         
         rsp = loser.getResult();     
         if (rsp.flag != RemoteLockResponse.Flag.REJECT)
         {
            assertEquals(RemoteLockResponse.Flag.FAIL, rsp.flag);
         }
         assertEquals(caller1, rsp.holder);
      }
      finally
      {
         if (t1.isAlive())
            t1.interrupt();
         if (t2.isAlive())
            t2.interrupt();
      }
   }
   
   public void testRemoteLockFailsAgainstLocalLock() throws Exception
   { 
      TesteeSet<NonGloballyExclusiveClusterLockSupport> testeeSet = getTesteeSet(node1, 1, 2);
      NonGloballyExclusiveClusterLockSupport testee = testeeSet.impl;
      LocalLockHandler handler = testee.getLocalHandler();
      RpcTarget target = testeeSet.target;
      
      ClusterNode caller1 = testee.getCurrentView().get(0);
      assertFalse(node1.equals(caller1));
      
      resetToStrict(handler);   
      // We throw TimeoutException to indicate "node1" holds the lock
      handler.lockFromCluster("test", caller1, 1000);
      expectLastCall().andThrow(new TimeoutException(node1));    
      replay(handler);
      
      RemoteLockResponse rsp = target.remoteLock("test", caller1, 1000);
      
      assertEquals(RemoteLockResponse.Flag.FAIL, rsp.flag);
      assertEquals(node1, rsp.holder);
      
      verify(handler);  
      
      // A second attempt should succeed if the local lock is released
      
      resetToStrict(handler);     
      // We return normally to indicate success
      handler.lockFromCluster("test", caller1, 1000);    
      replay(handler);
      
      rsp = target.remoteLock("test", caller1, 1000);
      
      assertEquals(RemoteLockResponse.Flag.OK, rsp.flag);
      assertNull(rsp.holder);
      
      verify(handler);  
   }
   
   public void testBasicClusterLockFailsAgainstLocalLock() throws Exception
   {
      basicClusterLockFailsAgainstLocalLockTest(2);
   }
   
   public void testStandaloneClusterLockFailsAgainstLocalLock() throws Exception
   {
      basicClusterLockFailsAgainstLocalLockTest(2);
   }
   
   private void basicClusterLockFailsAgainstLocalLockTest(int viewSize) throws Exception
   { 
      int viewPos = viewSize == 1 ? 0 : 1;
      TesteeSet<NonGloballyExclusiveClusterLockSupport> testeeSet = getTesteeSet(node1, viewPos, viewSize);
      NonGloballyExclusiveClusterLockSupport testee = testeeSet.impl;
      HAPartition partition = testee.getPartition();
      LocalLockHandler handler = testee.getLocalHandler();
      
      resetToNice(partition);
      resetToStrict(handler);
      
      ArrayList<RemoteLockResponse> rspList = new ArrayList<RemoteLockResponse>();
      for (int i = 0; i < viewSize - 1; i++)
      {
         rspList.add(new RemoteLockResponse(null, RemoteLockResponse.Flag.OK));
      }
      
      expect(partition.callMethodOnCluster(eq("test"), 
                                           eq("remoteLock"), 
                                           eqLockParams(node1, 2000000), 
                                           aryEq(AbstractClusterLockSupport.REMOTE_LOCK_TYPES), 
                                           eq(true))).andReturn(rspList).atLeastOnce();
      
      handler.lockFromCluster(eq("test"), eq(node1), anyLong());
      expectLastCall().andThrow(new TimeoutException(node1)).atLeastOnce();

      
      expect(partition.callMethodOnCluster(eq("test"), 
                                           eq("releaseRemoteLock"), 
                                           aryEq(new Object[]{"test", node1}), 
                                           aryEq(AbstractClusterLockSupport.RELEASE_REMOTE_LOCK_TYPES), 
                                           eq(true))).andReturn(new ArrayList<Object>()).atLeastOnce();
      replay(partition);
      replay(handler);
      
      assertFalse(testee.lock("test", 10));
      
      verify(partition);
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
      TesteeSet<NonGloballyExclusiveClusterLockSupport> testeeSet = getTesteeSet(node1, 1, 3);
      NonGloballyExclusiveClusterLockSupport testee = testeeSet.impl;
      LocalLockHandler handler = testee.getLocalHandler();
      RpcTarget target = testeeSet.target;
      
      List<ClusterNode> members = testee.getCurrentView();
      ClusterNode caller1 = members.get(0);
      assertFalse(node1.equals(caller1));
      
      ClusterNode caller2 = members.get(2);
      assertFalse(node1.equals(caller2));
      
      resetToStrict(handler);             
      handler.lockFromCluster("test", caller1, 1000); 
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
      expect(handler.getLockHolder("test")).andReturn(caller1);
      handler.unlockFromCluster("test", caller1);
      replay(handler);
      
      testee.membershipChanged(dead, new Vector<ClusterNode>(), all);
      
      verify(handler);
      
      // A call from a different caller should work 
      resetToStrict(handler);             
      handler.lockFromCluster("test", caller2, 1000);
      replay(handler);
      
      rsp = target.remoteLock("test", caller2, 1000);
      
      assertEquals(RemoteLockResponse.Flag.OK, rsp.flag);
      assertNull(rsp.holder);
      
      verify(handler);
   }
   
   /**
    * Remote node acquires a lock; different remote node tries to release which is ignored.
    * 
    * @throws Exception
    */
   public void testSpuriousLockReleaseIgnored2() throws Exception
   {
      TesteeSet<NonGloballyExclusiveClusterLockSupport> testeeSet = getTesteeSet(node1, 1, 3);
      NonGloballyExclusiveClusterLockSupport testee = testeeSet.impl;
      HAPartition partition = testee.getPartition();
      LocalLockHandler handler = testee.getLocalHandler();
      RpcTarget target = testeeSet.target;
      
      ClusterNode caller1 = testee.getCurrentView().get(0);
      ClusterNode caller2 = testee.getCurrentView().get(2);
      
      resetToStrict(partition);
      resetToStrict(handler);
      
      handler.lockFromCluster(eq("test"), eq(caller1), anyLong());
      
      expect(handler.getLockHolder("test")).andReturn(caller1);
      
      replay(partition);
      replay(handler);
      
      RemoteLockResponse rsp = target.remoteLock("test", caller1, 1);
      assertEquals(RemoteLockResponse.Flag.OK, rsp.flag);
      
      target.releaseRemoteLock("test", caller2);
      
      verify(partition);
      verify(handler);
   }

}

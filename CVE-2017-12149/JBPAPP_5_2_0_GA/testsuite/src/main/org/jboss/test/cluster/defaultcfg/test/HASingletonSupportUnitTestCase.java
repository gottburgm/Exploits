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
package org.jboss.test.cluster.defaultcfg.test;
 
import java.util.ArrayList;

import junit.framework.TestCase;

import org.jboss.test.cluster.hasingleton.HASingletonSupportTester;

/**
 * Tests of the HASingletonSupport class.
 *
 * @author   Ivelin Ivanov <ivelin@jboss.org>
 * @author   Brian Stansberry
 *
 */
public class HASingletonSupportUnitTestCase extends TestCase
{

   private HASingletonSupportTester singletonSupportTester = null;

   public HASingletonSupportUnitTestCase(String testCaseName)
   {
      super(testCaseName);
   }


   @Override
   public void setUp()
   {
      this.singletonSupportTester = new HASingletonSupportTester();
      this.singletonSupportTester.setRestartOnMerge(true);
   }
   
   @Override
   public void tearDown()
   {
      this.singletonSupportTester = null;
   }
   
   public void testStartService() throws Exception
   {
      this.singletonSupportTester.start();

      // test that the correct start sequence was followed correctly
      assertEquals("method not invoked as expected", this.singletonSupportTester.invocationStack.pop(), "registerDRMListener");
      assertEquals("method not invoked as expected", this.singletonSupportTester.invocationStack.pop(), "registerRPCHandler");
      assertEquals("method not invoked as expected", this.singletonSupportTester.invocationStack.pop(), "setupPartition");
   }

   public void testStopService() throws Exception
   {
      this.singletonSupportTester.start();
      this.singletonSupportTester.stop();

      assertEquals("method not invoked as expected", this.singletonSupportTester.invocationStack.pop(), "unregisterRPCHandler");
      assertEquals("method not invoked as expected", this.singletonSupportTester.invocationStack.pop(), "unregisterDRMListener");
   }
   
   public void testBecomeMasterNode() throws Exception
   {
       this.becomeMasterNodeTest(false);
   }
   
   private void becomeMasterNodeTest(boolean merge) throws Exception
   {
      this.singletonSupportTester.start();
      
      // register DRM Listener is expected to call back
      this.singletonSupportTester.isDRMMasterReplica = true;
      this.singletonSupportTester.getDelegate().partitionTopologyChanged(new ArrayList<Integer>(2), 1, merge);

      // test whether it was elected
      assertTrue("expected to become master", this.singletonSupportTester.isMasterNode());
      
      // test whether the election sequence was followed correctly
      assertEquals("method not invoked as expected", this.singletonSupportTester.invocationStack.pop(), "startSingleton");
      //assertEquals("method not invoked as expected", singletonSupportTester.invocationStack.pop(), "callMethodOnCluster:_stopOldMaster");
      assertEquals("method not invoked as expected", this.singletonSupportTester.invocationStack.pop(), "makeThisNodeMaster");
   }
   
   public void testBecomeSlaveNodeWithAnotherMaster() throws Exception
   {
      this.becomeSlaveNodeWithAnotherMasterTest(false);
   }
   
   private void becomeSlaveNodeWithAnotherMasterTest(boolean merge) throws Exception
   {
      this.singletonSupportTester.start();
      
      boolean savedIsMasterNode = this.singletonSupportTester.isMasterNode();
      
      // register DRM Listener is expected to call back
      this.singletonSupportTester.isDRMMasterReplica = false;
      this.singletonSupportTester.getDelegate().partitionTopologyChanged(new ArrayList<Integer>(2), 1, merge);
      
      // this call back should not change the master/slave status
      assertEquals("expected to be still in old master/slave state", this.singletonSupportTester.isMasterNode(), savedIsMasterNode );
      
      // the new master is expected to call back
      this.singletonSupportTester.getDelegate().stopIfMaster();
      
      if (savedIsMasterNode)
      {
         assertEquals("this node was the old master, but method not invoked as expected", this.singletonSupportTester.invocationStack.pop(), "stopSingleton");
      }
         
      // now it should be slave
      assertTrue("expected to be slave", !this.singletonSupportTester.isMasterNode());
                  
   }

   public void testStopOnlyNode() throws Exception
   {
      this.singletonSupportTester.start();
      
      // register DRM Listener is expected to call back
      this.singletonSupportTester.isDRMMasterReplica = true;
      this.singletonSupportTester.getDelegate().partitionTopologyChanged(new ArrayList<Integer>(2), 1, false);

      // test whether it was elected for master
      assertTrue("expected to become master", this.singletonSupportTester.isMasterNode());
      
      this.singletonSupportTester.stop();
      
      // register DRM Listener is expected to call back
      this.singletonSupportTester.isDRMMasterReplica = false;
      // since the only node (this one) in the partition is now removed, the replicants list should be empty
      this.singletonSupportTester.getDelegate().partitionTopologyChanged(new ArrayList<Integer>(0), 1, false);
      
      assertTrue("expected to have made a call to _stopOldMaster(), thus become slave", !this.singletonSupportTester.isMasterNode() );
      
      assertEquals("method not invoked as expected", this.singletonSupportTester.invocationStack.pop(), "stopSingleton");
         
   }
   
   public void testStartServiceWithRestartOff() throws Exception
   {
      this.singletonSupportTester.setRestartOnMerge(false);
      this.testStartService();
   }

   public void testStopServiceWithRestartOff() throws Exception
   {
      this.singletonSupportTester.setRestartOnMerge(false);
      this.testStopService();
   }
   
   public void testBecomeMasterNodeWithRestartOff() throws Exception
   {
      this.singletonSupportTester.setRestartOnMerge(false);
      this.becomeMasterNodeTest(false);
   }
   
   public void testBecomeSlaveNodeWithAnotherMasterWithRestartOff() throws Exception
   {
      this.singletonSupportTester.setRestartOnMerge(false);
      this.becomeSlaveNodeWithAnotherMasterTest(false);
   }
   
   public void testBecomeMasterNodeDuringMerge() throws Exception
   {
      this.becomeMasterNodeTest(true);
   }
   
   public void testMasterRestartDuringMerge() throws Exception
   {
      // Just run the BecomeMaster test to get ourself set up as master
      this.becomeMasterNodeTest(false);
      
      // Drain off any un-popped events
      this.singletonSupportTester.invocationStack.clear();
      
      this.singletonSupportTester.getDelegate().partitionTopologyChanged(new ArrayList<Integer>(3), 2, true);
      
      // test whether it's still master
      assertTrue("expected to remain master", this.singletonSupportTester.isMasterNode());
      
      // test whether the election sequence was followed correctly
      assertEquals("method not invoked as expected", "startSingleton", this.singletonSupportTester.invocationStack.pop());
      assertEquals("method not invoked as expected", "stopSingleton", this.singletonSupportTester.invocationStack.pop());
      assertEquals("method not invoked as expected", "restartMaster", this.singletonSupportTester.invocationStack.pop());
   }
   
   public void testBecomeSlaveNodeWithAnotherMasterDuringMerge() throws Exception
   {
      // Just run the BecomeMaster test to get ourself set up as master
      this.becomeMasterNodeTest(false);
      
      // Drain off any un-popped events
      this.singletonSupportTester.invocationStack.clear();
      
      this.singletonSupportTester.isDRMMasterReplica = false;
      
      this.singletonSupportTester.getDelegate().partitionTopologyChanged(new ArrayList<Integer>(3), 2, true);
      
      // now it should be slave
      assertFalse("expected to be slave", this.singletonSupportTester.isMasterNode());
      
      assertEquals("this node was the old master, but method not invoked as expected", this.singletonSupportTester.invocationStack.pop(), "stopSingleton");
   }

   
   public void testMasterRestartDuringMergeWithRestartOff() throws Exception
   {
      this.singletonSupportTester.setRestartOnMerge(false);
      
      // Just run the BecomeMaster test to get ourself set up as master
      this.testBecomeMasterNode();
      
      // Drain off any un-popped events
      this.singletonSupportTester.invocationStack.clear();
      
      this.singletonSupportTester.getDelegate().partitionTopologyChanged(new ArrayList<Integer>(3), 2, true);
      
       // test whether it's still master
      assertTrue("expected to remain master", this.singletonSupportTester.isMasterNode());
      
      // test whether the election sequence was followed correctly
      assertEquals("method not invoked as expected", "isDRMMasterReplica", this.singletonSupportTester.invocationStack.pop());
      assertEquals("method not invoked as expected", 0, this.singletonSupportTester.invocationStack.size());
   }
}

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
package org.jboss.test.cluster.defaultcfg.test;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;

import junit.framework.Test;

import org.jboss.cache.config.Configuration;
import org.jboss.ha.cachemanager.CacheManager;
import org.jboss.ha.cachemanager.DependencyInjectedConfigurationRegistry;
import org.jboss.ha.framework.interfaces.ClusterNode;
import org.jboss.ha.framework.interfaces.DistributedReplicantManager;
import org.jboss.ha.framework.interfaces.DistributedReplicantManager.ReplicantListener;
import org.jboss.ha.framework.server.ClusterNodeImpl;
import org.jboss.ha.framework.server.ClusterPartition;
import org.jboss.ha.framework.server.DistributedReplicantManagerImpl;
import org.jboss.ha.framework.server.DistributedStateImpl;
import org.jboss.ha.framework.server.HAPartitionCacheHandlerImpl;
import org.jboss.ha.framework.server.JChannelFactory;
import org.jboss.logging.Logger;
import org.jboss.test.JBossClusteredTestCase;
import org.jboss.test.cluster.hapartition.drm.MockHAPartition;
import org.jgroups.stack.GossipRouter;
import org.jgroups.stack.IpAddress;

import EDU.oswego.cs.dl.util.concurrent.Semaphore;

/** Tests of the DistributedReplicantManagerImpl
 *
 * @author  Scott.Stark@jboss.org
 * @author  Brian.Stansberry@jboss.com
 * @author  <a href="mailto:galder.zamarreno@jboss.com">Galder Zamarreno</a>
 * @version $Revision: 104634 $
 */
public class DRMTestCase extends JBossClusteredTestCase
{  
   private static final String SERVICEA = "serviceA";
   private static final String SERVICEB = "serviceB";
   
   /**
    * Thread that will first register a DRM ReplicantLister that synchronizes
    * on the test class' lock object, and then calls DRM add or remove,
    * causing the thread to block if the lock object's monitor is held.
    */
   static class BlockingListenerThread extends Thread 
      implements DistributedReplicantManager.ReplicantListener
   {
      private DistributedReplicantManagerImpl drm;
      private String nodeName;
      private boolean add;
      private boolean blocking;
      private Exception ex;
      
      BlockingListenerThread(DistributedReplicantManagerImpl drm,
                             boolean add,
                             String nodeName)
      {
         this.drm = drm;
         this.add =add;
         this.nodeName = nodeName;
         drm.registerListener("TEST", this);         
      }

      public void replicantsChanged(String key, List newReplicants, int newReplicantsViewId, boolean merge)
      {
         blocking = true;
         synchronized(lock)
         {
            blocking = false;
         }
      }
      
      public void run()
      {
         try
         {
            if (add)
            {
               if (nodeName == null)
                  drm.add("TEST", "local-replicant");
               else
                  drm._add("TEST", nodeName, "remote-replicant");
            }
            else 
            {
               if (nodeName == null)
                  drm.remove("TEST");
               else
                  drm._remove("TEST", nodeName);
            }
         }
         catch (Exception e)
         {
            ex = e;
         }
      }
      
      public boolean isBlocking()
      {
         return blocking;
      }
      
      public Exception getException()
      {
         return ex;
      }
      
   }
   
   /**
    * Thread that registers and then unregisters a DRM ReplicantListener.
    */
   static class RegistrationThread extends Thread
   {
      private DistributedReplicantManager drm;
      private boolean registered = false;
      private boolean unregistered = true;
      
      RegistrationThread(DistributedReplicantManager drm)
      {
         this.drm = drm;
      }
      
      public void run()
      {
         NullListener listener = new NullListener();
         drm.registerListener("DEADLOCK", listener);
         registered = true;
         drm.unregisterListener("DEADLOCK", listener);
         unregistered = true;
      }
      
      public boolean isRegistered()
      {
         return registered;
      }
      
      public boolean isUnregistered()
      {
         return unregistered;
      }
      
   }
   
   /**
    * A DRM ReplicantListener that does nothing.
    */
   static class NullListener
      implements DistributedReplicantManager.ReplicantListener
   {
      public void replicantsChanged(String key, List newReplicants, 
                                    int newReplicantsViewId, boolean merge)
      {
         // no-op
      }
   }
   
   /**
    * DRM ReplicantListener that mimics the HASingletonDeployer service
    * by deploying/undeploying a service if it's notified that by that DRM
    * that it is the master replica for its key.
    */
   static class MockHASingletonDeployer
      implements DistributedReplicantManager.ReplicantListener
   {
      DistributedReplicantManager drm;
      MockDeployer deployer;
      String key;
      boolean master = false;
      NullListener deploymentListener = new NullListener();
      Exception ex;
      Logger log;
      Object mutex = new Object();
      
      MockHASingletonDeployer(MockDeployer deployer, String key, Logger log)
      {
         this.drm = deployer.getDRM();
         this.deployer = deployer;
         this.key = key;
         this.log = log;
      }

      public void replicantsChanged(String key, 
                                    List newReplicants, 
                                    int newReplicantsViewId, 
                                    boolean merge)
      {
         if (this.key.equals(key))
         {
            synchronized(mutex)
            {
               boolean nowMaster = drm.isMasterReplica(key);
               
               try
               {
                  if (!master && nowMaster) {
                     log.debug(Thread.currentThread().getName() + 
                               " Deploying " + key);
                     deployer.deploy(key + "A", key, deploymentListener);
                  }
                  else if (master && !nowMaster) {
                     log.debug(Thread.currentThread().getName() + 
                               " undeploying " + key);
                     deployer.undeploy(key + "A", deploymentListener);
                  }
                  else 
                  {
                     log.debug(Thread.currentThread().getName() + 
                               " -- no status change in " + key + 
                               " -- master = " + master);   
                  }
                  master = nowMaster;
               }
               catch (Exception e)
               {
                  e.printStackTrace();
                  if (ex == null)
                     ex = e;
               }
            }
         }         
      }
      
      public Exception getException()
      {
         return ex;
      }
      
   }
   
   /**
    * Thread the repeatedly deploys and undeploys a MockHASingletonDeployer.
    */
   static class DeployerThread extends Thread
   {
      Semaphore semaphore;
      MockDeployer deployer;
      DistributedReplicantManager.ReplicantListener listener;
      String key;
      Exception ex;
      int count = -1;
      Logger log;
      
      DeployerThread(MockDeployer deployer, 
                     String key, 
                     DistributedReplicantManager.ReplicantListener listener,
                     Semaphore semaphore,
                     Logger log)
      {
         super("Deployer " + key);
         this.deployer = deployer;
         this.listener = listener;
         this.key = key;
         this.semaphore = semaphore;
         this.log = log;
      }
      
      public void run()
      {
         boolean acquired = false;
         try
         {
            acquired = semaphore.attempt(60000);
            if (!acquired)
               throw new Exception("Cannot acquire semaphore");
            SecureRandom random = new SecureRandom();
            for (count = 0; count < LOOP_COUNT; count++)
            {
               deployer.deploy(key, "JGroups", listener);

               sleepThread(random.nextInt(50));
               deployer.undeploy(key, listener);
            }
         }
         catch (Exception e)
         {
            e.printStackTrace();
            ex = e;
         }
         finally
         {
            if (acquired)
               semaphore.release();
         }
      }
      
      public Exception getException()
      {
         return ex;
      }
      
      public int getCount()
      {
         return count;
      }
   }
   
   /**
    * Thread that mimics the JGroups up-handler thread that calls into the DRM.
    * Repeatedly and randomly calls adds or removes a replicant for a set
    * of keys. 
    */
   static class JGroupsThread extends Thread
   {
      Semaphore semaphore;
      DistributedReplicantManagerImpl drm;
      String[] keys;
      String nodeName;
      Exception ex;
      int count = -1;
      int weightFactor;
      
      JGroupsThread(DistributedReplicantManagerImpl drm, 
                    String[] keys,
                    String nodeName,
                    Semaphore semaphore)
      {
         super("JGroups");
         this.drm = drm;
         this.keys = keys;
         this.semaphore = semaphore;
         this.nodeName = nodeName;
         this.weightFactor = (int) 2.5 * keys.length;
      }
      
      public void run()
      {
         boolean acquired = false;
         try
         {
            acquired = semaphore.attempt(60000);
            if (!acquired)
               throw new Exception("Cannot acquire semaphore");
            boolean[] added = new boolean[keys.length];
            SecureRandom random = new SecureRandom();
            
            for (count = 0; count < weightFactor * LOOP_COUNT; count++)
            {
               int pos = random.nextInt(keys.length);
               if (added[pos])
               {
                  drm._remove(keys[pos], nodeName);
                  added[pos] = false;
               }
               else
               {
                  drm._add(keys[pos], nodeName, "");
                  added[pos] = true;
               }
               sleepThread(random.nextInt(30));
            }
         }
         catch (Exception e)
         {
            e.printStackTrace();
            ex = e;
         }
         finally
         {
            if (acquired)
               semaphore.release();
         }
      }
      
      public Exception getException()
      {
         return ex;
      }
      
      public int getCount()
      {
         return (count / weightFactor);
      }
      
   }
   
   /**
    * Mocks the deployer of a service that registers/unregisters DRM listeners 
    * and replicants. Only allows a single thread of execution, a la the
    * org.jboss.system.ServiceController.
    */
   static class MockDeployer
   {
      DistributedReplicantManager drm;
      
      MockDeployer(DistributedReplicantManager drm)
      {
         this.drm = drm;
      }
      
      void deploy(String key, String replicant, 
                  DistributedReplicantManager.ReplicantListener listener)
            throws Exception 
      {
         synchronized(this)
         {
            drm.registerListener(key, listener);
            drm.add(key, replicant);
            sleepThread(10);
         }
      }
      
      void undeploy(String key, 
                    DistributedReplicantManager.ReplicantListener listener)
         throws Exception 
      {
         synchronized(this)
         {
            drm.remove(key);
            drm.unregisterListener(key, listener);
            sleepThread(10);
         }
      }
      
      DistributedReplicantManager getDRM()
      {
         return drm;
      }
   }
   
   /** ReplicantListener that caches the list of replicants */
   static class CachingListener implements ReplicantListener
   {
      List replicants = null;
      boolean clean = true;
      
      public void replicantsChanged(String key, List newReplicants, 
                                    int newReplicantsViewId, boolean merge)
      {
         this.replicants = newReplicants;
         if (clean && newReplicants != null)
         {
            int last = Integer.MIN_VALUE;
            for (Iterator iter = newReplicants.iterator(); iter.hasNext(); )
            {
               int cur = ((Integer) iter.next()).intValue();
               if (last >= cur)
               {
                  clean = false;
                  break;
               }
               
               last = cur;
            }
         }
      }
      
   }

   private static Object lock = new Object();
   private static int LOOP_COUNT = 30;
   
   public static Test suite() throws Exception
   {
      Test t1 = getDeploySetup(DRMTestCase.class, "drm-tests.sar");
      return t1;
   }

   public DRMTestCase(String name)
   {
      super(name);
   }
   
   /**
    * Tests the functionality of isMasterReplica(), also testing merge
    * handling.  This test creates and manipulates two HAPartition instances in memory 
    * so it doesn't require a server deployment.  The partition instances communicate via
    * a GossipRouter.  The router is stopped and restarted to simulate a merge condition.
    * 
    * TODO move this test out of the testsuite and into the cluster module
    *      itself, since it doesn't rely on the container.
    * 
    * @throws Exception
    */
   public void testIsMasterReplica() throws Exception
   {
      GossipRouter router = null;
      ClusterPartition partition1 = null;
      ClusterPartition partition2 = null;
      boolean partition1Started = false;
      boolean partition2Started = false;
      
      log.debug("+++ testIsMasterReplica()");

      try
      {  
         String partitionName = "DRMTestCasePartition";
         String muxFile = "cluster/drm/drm-test-stacks.xml";
         String stackName = "tunnel1";
         
         log.info("DRMTestCase.testIsMasterReplica() - starting GossipRouter");
         // router characteristics here must match the definition in the stack configuration
         router = new GossipRouter(12001, "127.0.0.1");
         router.start();
         Thread.sleep(10000);
         
         JChannelFactory factory1 = new JChannelFactory();
         factory1.setMultiplexerConfig(muxFile);
         factory1.setNamingServicePort(1099);
         factory1.setNodeName("node1");
         factory1.setExposeChannels(false);
         factory1.setExposeProtocols(false);
         factory1.create();
         factory1.start();

         Configuration cacheConfig1 = new Configuration();
         cacheConfig1.setMultiplexerStack(stackName);
         cacheConfig1.setCacheMode("REPL_SYNC");
         
         DependencyInjectedConfigurationRegistry registry1 = new DependencyInjectedConfigurationRegistry();
         registry1.registerConfiguration("config1", cacheConfig1);         
         
         CacheManager cacheManager1 = new CacheManager(registry1, factory1);
         cacheManager1.start();
         
         HAPartitionCacheHandlerImpl cacheHandler1 = new HAPartitionCacheHandlerImpl();
         cacheHandler1.setCacheManager(cacheManager1);
         cacheHandler1.setCacheConfigName("config1");
         
         DistributedStateImpl ds1 = new DistributedStateImpl();
         ds1.setCacheHandler(cacheHandler1);
         
         partition1 = new ClusterPartition();
         partition1.setPartitionName(partitionName);
         partition1.setCacheHandler(cacheHandler1);
         partition1.setStateTransferTimeout(30000);
         partition1.setMethodCallTimeout(60000);
         partition1.setDistributedStateImpl(ds1);
         partition1.setBindIntoJndi(false);
         
         partition1.create();         
         partition1.start();

         DistributedReplicantManager drm1 = partition1.getDistributedReplicantManager();

         Thread.sleep(10000);
         
         // Use a different stack name with the same config to avoid singleton conflicts
         stackName = "tunnel2";
         
         JChannelFactory factory2 = new JChannelFactory();
         factory2.setMultiplexerConfig(muxFile);
         factory2.setNamingServicePort(1099);
         factory2.setNodeName("node2");
         factory2.setExposeChannels(false);
         factory2.setExposeProtocols(false);
         factory2.create();
         factory2.start();
         
         Configuration cacheConfig2 = new Configuration();
         cacheConfig2.setMultiplexerStack(stackName);
         cacheConfig2.setCacheMode("REPL_SYNC");
         
         DependencyInjectedConfigurationRegistry registry2 = new DependencyInjectedConfigurationRegistry();
         registry2.registerConfiguration("config2", cacheConfig2);         
         
         CacheManager cacheManager2 = new CacheManager(registry2, factory2);
         cacheManager2.start();
         
         HAPartitionCacheHandlerImpl cacheHandler2 = new HAPartitionCacheHandlerImpl();
         cacheHandler2.setCacheManager(cacheManager2);
         cacheHandler2.setCacheConfigName("config2");
         
         DistributedStateImpl ds2 = new DistributedStateImpl();
         ds2.setCacheHandler(cacheHandler2);
         
         partition2 = new ClusterPartition();
         partition2.setPartitionName(partitionName);
         partition2.setCacheHandler(cacheHandler2);
         partition2.setStateTransferTimeout(30000);
         partition2.setMethodCallTimeout(60000);
         partition2.setDistributedStateImpl(ds2);
         partition2.setBindIntoJndi(false);
         
         partition2.create();         
         partition2.start();

         DistributedReplicantManager drm2 = partition2.getDistributedReplicantManager();
         
         Thread.sleep(10000);
         
         // confirm that each partition contains two nodes   
         assertEquals("Partition1 should contain two nodes; ", 2, partition1.getCurrentView().size());
         assertEquals("Partition2 should contain two nodes; ", 2, partition2.getCurrentView().size());
         
         drm1.add(SERVICEA, "valueA1");
         drm2.add(SERVICEA, "valueA2");
         drm2.add(SERVICEB, "valueB2");
         
         // test that only one node is the master replica for serviceA
         assertTrue("ServiceA must have a master replica", 
                 drm1.isMasterReplica(SERVICEA) || drm2.isMasterReplica(SERVICEA));
         assertTrue("ServiceA must have a single master replica", 
                 drm1.isMasterReplica(SERVICEA) != drm2.isMasterReplica(SERVICEA));
 
         // ServiceB should only be a master replica on partition2
         assertFalse("ServiceB should not be a master replica on partition1", 
                 drm1.isMasterReplica(SERVICEB));
         assertTrue("ServiceB must have a master replica on partition2", 
                 drm2.isMasterReplica(SERVICEB));
         
         // confirm that each partition contains correct DRM replicants for services A and B  
         assertEquals("Partition1 should contain two DRM replicants for serviceA; ", 
                 2, drm1.lookupReplicants(SERVICEA).size());
         assertEquals("Partition2 should contain two DRM replicants for serviceA; ", 
                 2, drm2.lookupReplicants(SERVICEA).size());
         assertEquals("Partition1 should contain one DRM replicant for serviceB; ", 
                 1, drm1.lookupReplicants(SERVICEB).size());
         assertEquals("Partition2 should contain one DRM replicant for serviceB; ", 
                 1, drm2.lookupReplicants(SERVICEB).size());

         // simulate a split of the partition
         log.info("DRMTestCase.testIsMasterReplica() - stopping GossipRouter");
         router.stop();
         sleepThread(15000);
         
         // confirm that each partition contains one node   
         assertEquals("Partition1 should contain one node after split; ", 
                 1, partition1.getCurrentView().size());
         assertEquals("Partition2 should contain one node after split; ", 
                 1, partition2.getCurrentView().size());
        
         // confirm that each node is a master replica for serviceA after the split
         assertTrue("ServiceA should be a master replica on partition1 after split", 
                 drm1.isMasterReplica(SERVICEA));
         assertTrue("ServiceA should be a master replica on partition2 after split", 
                 drm2.isMasterReplica(SERVICEA));
         
         // ServiceB should still only be a master replica on partition2 after split
         assertFalse("ServiceB should not be a master replica on partition1 after split", 
                 drm1.isMasterReplica(SERVICEB));
         assertTrue("ServiceB must have a master replica on partition2 after split", 
                 drm2.isMasterReplica(SERVICEB));
         
         // Remove ServiceA replicant from partition1         
         drm1.remove(SERVICEA);
         
         // test that this node is not the master replica         
         assertFalse("partition1 is not master replica after dropping ServiceA replicant", 
                 drm1.isMasterReplica(SERVICEA));
         
         //Restore the local replicant         
         drm1.add(SERVICEA, "valueA1a");
         
         // simulate a merge
         log.info("DRMTestCase.testIsMasterReplica() - restarting GossipRouter");
         router.start();
         // it seems to take more than 15 seconds for the merge to take effect
         sleepThread(30000);
         
         assertTrue(router.isStarted());

         // confirm that each partition contains two nodes again
         assertEquals("Partition1 should contain two nodes after merge; ", 
               2, partition1.getCurrentView().size());
         assertEquals("Partition2 should contain two nodes after merge; ", 
                 2, partition2.getCurrentView().size());
         
         // test that only one node is the master replica for serviceA after merge
         assertTrue("ServiceA must have a master replica after merge", 
                 drm1.isMasterReplica(SERVICEA) || drm2.isMasterReplica(SERVICEA));
         assertTrue("ServiceA must have a single master replica after merge", 
                 drm1.isMasterReplica(SERVICEA) != drm2.isMasterReplica(SERVICEA));
 
         // ServiceB should only be a master replica on partition2 after merge
         assertFalse("ServiceB should not be a master replica on partition1 after merge", 
                 drm1.isMasterReplica(SERVICEB));
         assertTrue("ServiceB must have a master replica on partition2 after merge", 
                 drm2.isMasterReplica(SERVICEB));
         
         // confirm that each partition contains correct DRM replicants for services A and B after merge 
         assertEquals("Partition1 should contain two DRM replicants for serviceA after merge; ", 
                 2, drm1.lookupReplicants(SERVICEA).size());
         assertEquals("Partition2 should contain two DRM replicants for serviceA after merge; ", 
                 2, drm2.lookupReplicants(SERVICEA).size());
         assertEquals("Partition1 should contain one DRM replicant for serviceB after merge; ", 
                 1, drm1.lookupReplicants(SERVICEB).size());
         assertEquals("Partition2 should contain one DRM replicant for serviceB after merge; ", 
                 1, drm2.lookupReplicants(SERVICEB).size());
         
         partition1.stop();
         partition2.stop();
      }
      finally
      {
         log.info("DRMTestCase.testIsMasterReplica() - cleaning up resources");
         if (partition1Started)
            partition1.stop();
         if (partition2Started)
            partition2.stop();
         if (router != null)
            router.stop();
      }
   }
   
   /**
    * Tests that one thread blocking in DRM.notifyKeyListeners() does not
    * prevent other threads registering/unregistering listeners. JBAS-2539
    * 
    * TODO move this test out of the testsuite and into the cluster module
    *      itself, since it doesn't rely on the container.
    * 
    * @throws Exception
    */
   public void testKeyListenerDeadlock() throws Exception
   {
      log.debug("+++ testKeyListenerDeadlock()");
      
      MBeanServer mbeanServer = 
         MBeanServerFactory.createMBeanServer("mockPartition");
      try {
         ClusterNode localAddress = new ClusterNodeImpl(new IpAddress("127.0.0.1", 12345));
         MockHAPartition partition = new MockHAPartition(localAddress);
      
         DistributedReplicantManagerImpl drm = new DistributedReplicantManagerImpl(partition);

         drm.createService();
         
         // Create a fake view for the MockHAPartition
         
         Vector<ClusterNode> remoteAddresses = new Vector<ClusterNode>();
         for (int i = 1; i < 5; i++)
            remoteAddresses.add(new ClusterNodeImpl(new IpAddress("127.0.0.1", 12340 + i)));
         
         Vector<ClusterNode> allNodes = new Vector<ClusterNode>(remoteAddresses);
         allNodes.add(localAddress);
         partition.setCurrentViewClusterNodes(allNodes);
         
         drm.startService();
         
         BlockingListenerThread blt = 
            new BlockingListenerThread(drm, true, null);
         
         // Hold the lock monitor so the test thread can't acquire it
         // This keeps the blocking thread alive.
         synchronized(lock) {
            // Spawn a thread that will change a key and then block on the
            // notification back to itself
            blt.start();

            sleepThread(50);
            
            assertTrue("Test thread is alive", blt.isAlive());            
            assertTrue("Test thread is blocking", blt.isBlocking());
            
            RegistrationThread rt = new RegistrationThread(drm);
            rt.start();

            sleepThread(50);
            
            assertTrue("No deadlock on listener registration", rt.isRegistered());
            
            assertTrue("No deadlock on listener unregistration", rt.isUnregistered());
            
            assertNull("No exception in deadlock tester", blt.getException());
            
            assertTrue("Test thread is still blocking", blt.isBlocking());
            assertTrue("Test thread is still alive", blt.isAlive());
         }
         
         drm.unregisterListener("TEST", blt);
         
         sleepThread(50);
         
         // Test going through remove
         blt = new BlockingListenerThread(drm, false, null);
         
         // Hold the lock monitor so the test thread can't acquire it
         // This keeps the blocking thread alive.
         synchronized(lock) {
            // Spawn a thread that will change a key and then block on the
            // notification back to itself
            blt.start();

            sleepThread(50);
            
            assertTrue("Test thread is alive", blt.isAlive());            
            assertTrue("Test thread is blocking", blt.isBlocking());
            
            RegistrationThread rt = new RegistrationThread(drm);
            rt.start();

            sleepThread(50);
            
            assertTrue("No deadlock on listener registration", rt.isRegistered());
            
            assertTrue("No deadlock on listener unregistration", rt.isUnregistered());
            
            assertNull("No exception in deadlock tester", blt.getException());
            
            assertTrue("Test thread is still blocking", blt.isBlocking());
            assertTrue("Test thread is still alive", blt.isAlive());
         }
      }
      finally {
         MBeanServerFactory.releaseMBeanServer(mbeanServer);
      }
   }
   
   
   /**
    * Tests that remotely-originated calls don't block.
    * 
    * TODO move this test out of the testsuite and into the cluster module
    *      itself, since it doesn't rely on the container.
    * 
    * @throws Exception
    */
   public void testRemoteCallBlocking() throws Exception
   {
      log.debug("+++ testRemoteCallBlocking()");
      
      MBeanServer mbeanServer = 
         MBeanServerFactory.createMBeanServer("mockPartition");
      try {
         ClusterNode localAddress = new ClusterNodeImpl(new IpAddress("127.0.0.1", 12345));
         MockHAPartition partition = new MockHAPartition(localAddress);
      
         DistributedReplicantManagerImpl drm = new DistributedReplicantManagerImpl(partition);

         drm.createService();
         
         // Create a fake view for the MockHAPartition
         
         Vector<ClusterNode> remoteAddresses = new Vector<ClusterNode>();
         for (int i = 1; i < 5; i++)
            remoteAddresses.add(new ClusterNodeImpl(new IpAddress("127.0.0.1", 12340 + i)));
         
         Vector<ClusterNode> allNodes = new Vector<ClusterNode>(remoteAddresses);
         allNodes.add(localAddress);
         partition.setCurrentViewClusterNodes(allNodes);
         
         drm.startService();
         
         String sender = ((ClusterNode)remoteAddresses.get(0)).getName();
         BlockingListenerThread blt = 
            new BlockingListenerThread(drm, true, sender);
         
         // Hold the lock monitor so the test thread can't acquire it
         // This keeps the blocking thread alive.
         synchronized(lock) {
            // Spawn a thread that will change a key and then block on the
            // notification back to itself
            blt.start();

            sleepThread(50);
            
            assertFalse("JGroups thread is not alive", blt.isAlive());            
            assertTrue("Async handler thread is blocking", blt.isBlocking());
            
            assertNull("No exception in JGroups thread", blt.getException());
         }
         
         drm.unregisterListener("TEST", blt);
         
         sleepThread(50);
         
         // Test going through remove
         blt = new BlockingListenerThread(drm, false, sender);
         
         // Hold the lock monitor so the test thread can't acquire it
         // This keeps the blocking thread alive.
         synchronized(lock) {
            // Spawn a thread that will change a key and then block on the
            // notification back to itself
            blt.start();

            sleepThread(50);
            
            assertFalse("JGroups thread is not alive", blt.isAlive());            
            assertTrue("Async handler thread is blocking", blt.isBlocking());
            
            assertNull("No exception in JGroups thread", blt.getException());
         }
      }
      finally {
         MBeanServerFactory.releaseMBeanServer(mbeanServer);
      }
   }
   
   /**
    * Tests that one thread blocking in DRM.notifyKeyListeners() does not
    * prevent other threads that use different keys adding/removing 
    * replicants. JBAS-2169
    * 
    * TODO move this test out of the testsuite and into the cluster module
    *      itself, since it doesn't rely on the container.
    * 
    * @throws Exception
    */
   public void testNonConflictingAddRemoveDeadlock() throws Exception
   {

      log.debug("+++ testNonConflictingAddRemoveDeadlock()");
      
      addRemoveDeadlockTest(false);
   }
   
   /**
    * Tests that one thread blocking in DRM.notifyKeyListeners() does not
    * prevent other threads that use the same keys adding/removing 
    * replicants. JBAS-1151
    * 
    * NOTE: This test basically demonstrates a small race condition that can
    * happen with the way HASingletonSupport's startService() method is
    * implemented (actually HAServiceMBeanSupport, but relevant in the case
    * of subclass HASingletonSupport, and in particular in its use in the
    * HASingletonDeployer service).  However, since the test doesn't actually
    * use the relevant code, but rather uses mock objects that work the same
    * way, this test is disabled -- its purpose has been achieved.  JIRA issue 
    * JBAS-1151 tracks the real problem; when it's resolved we'll create a test 
    * case against the real code that proves that fact.
    * 
    * TODO move this test out of the testsuite and into the cluster module
    *      itself, since it doesn't rely on the container.
    * 
    * @throws Exception
    */
   public void badtestConflictingAddRemoveDeadlock() throws Exception
   {
      log.debug("+++ testConflictingAddRemoveDeadlock()");
      
      addRemoveDeadlockTest(true);
   }  
   
   private void addRemoveDeadlockTest(boolean conflicting) throws Exception
   {  
      String[] keys = { "A", "B", "C", "D", "E" };
      int count = keys.length;
      
      MBeanServer mbeanServer = 
         MBeanServerFactory.createMBeanServer("mockPartition");
      try {
         ClusterNode localAddress = new ClusterNodeImpl(new IpAddress("127.0.0.1", 12345));
         MockHAPartition partition = new MockHAPartition(localAddress);
      
         DistributedReplicantManagerImpl drm = new DistributedReplicantManagerImpl(partition);

         drm.createService();
         
         // Create a fake view for the MockHAPartition
         
         Vector<ClusterNode> remoteAddresses = new Vector<ClusterNode>();
         ClusterNode remote = new ClusterNodeImpl(new IpAddress("127.0.0.1", 12341));
         remoteAddresses.add(remote);
         
         Vector<ClusterNode> allNodes = new Vector<ClusterNode>(remoteAddresses);
         allNodes.add(localAddress);
         partition.setCurrentViewClusterNodes(allNodes);
         
         drm.startService();
         
         MockDeployer deployer = new MockDeployer(drm);
         
         if (!conflicting)
         {
            // Register a MockHASingletonDeployer, but since we're in
            // non-conflicting mode, the DeployerThreads won't deal with it
            MockHASingletonDeployer listener = 
                  new MockHASingletonDeployer(deployer, "HASingleton", log);
            
            drm.registerListener("HASingleton", listener);
            drm.add("HASingleton", "HASingleton");            
         }
         
         // Create a semaphore to gate the threads and acquire all its permits
         Semaphore semaphore = new Semaphore(count + 1);
         for (int i = 0; i <= count; i++)
            semaphore.acquire();
         
         DeployerThread[] deployers = new DeployerThread[keys.length];
         for (int i = 0; i < count; i++)
         {
            DistributedReplicantManager.ReplicantListener listener = null;
            if (conflicting)
            {
               listener = new MockHASingletonDeployer(deployer, keys[i], log);
            }
            else
            {
               listener = new NullListener();
            }
            deployers[i] = new DeployerThread(deployer, keys[i], listener, semaphore, log);
            deployers[i].start();
         }
         
         String[] jgKeys = keys;
         if (!conflicting)
         {
            // The JGroups thread also deals with the MockHASingletonDeployer
            // key that the DeployerThreads don't
            jgKeys = new String[keys.length + 1];
            System.arraycopy(keys, 0, jgKeys, 0, keys.length);
            jgKeys[keys.length] = "HASingleton";            
         }
         JGroupsThread jgThread = new JGroupsThread(drm, jgKeys, remote.getName(), semaphore);
         jgThread.start();
         
         // Launch the threads
         semaphore.release(count + 1);
         
         boolean reacquired = false;
         try
         {
            // Give the threads 5 secs to acquire the semaphore
            long maxElapsed = System.currentTimeMillis() + 5000;
            for (int i = 0; i < keys.length; i++)
            {
               if (deployers[i].getCount() < 0)
               {
                  assertTrue("Thread " + keys[i] + " started in time",
                              maxElapsed - System.currentTimeMillis() > 0);
                  sleepThread(10);
                  i--; // try again
               }   
            }
            
            while (jgThread.getCount() < 0)
            {
               assertTrue("jgThread started in time",
                           maxElapsed - System.currentTimeMillis() > 0);
               sleepThread(10);               
            }
            // Reaquire all the permits, thus showing the threads didn't deadlock
            
            // Give them 500 ms per loop
            maxElapsed = System.currentTimeMillis() + (500 * LOOP_COUNT);
            for (int i = 0; i <= count; i++)
            {
               long waitTime = maxElapsed - System.currentTimeMillis();
               assertTrue("Acquired thread " + i, semaphore.attempt(waitTime));
            }
            
            reacquired = true;
            
            // Ensure there were no exceptions
            for (int i = 0; i < keys.length; i++)
            {
               assertEquals("Thread " + keys[i] + " finished", LOOP_COUNT, deployers[i].getCount());
               assertNull("Thread " + keys[i] + " saw no exceptions", deployers[i].getException());
            }
            assertEquals("JGroups Thread finished", LOOP_COUNT, jgThread.getCount());
            assertNull("JGroups Thread saw no exceptions", jgThread.getException());
         }
         finally
         {

            if (!reacquired)
            {
               for (int i = 0; i < keys.length; i++)
               {
                  if (deployers[i].getException() != null)
                  {
                     System.out.println("Exception in deployer " + i);
                     deployers[i].getException().printStackTrace(System.out);
                  }
                  else
                  {
                     System.out.println("Thread " + i + " completed " + deployers[i].getCount());
                  }
               }
               if (jgThread.getException() != null)
               {
                  System.out.println("Exception in jgThread");
                  jgThread.getException().printStackTrace(System.out);
               }
               else
               {
                  System.out.println("jgThread completed " + jgThread.getCount());
               }
            }
            
            // Be sure the threads are dead
            if (jgThread.isAlive())
            {
               jgThread.interrupt();
               sleepThread(5);   
               printStackTrace(jgThread.getName(), jgThread.getException());
            }
            for (int i = 0; i < keys.length; i++)
            {
               if (deployers[i].isAlive())
               {
                  deployers[i].interrupt();
                  sleepThread(5);
                  printStackTrace(deployers[i].getName(), deployers[i].getException());
               }
            }
               
         }
      }
      finally {
         MBeanServerFactory.releaseMBeanServer(mbeanServer);
      }
   }
   
   /**
    * testReplicantOrder
    * 
    * @throws Exception
    * @deprecated Test method based in deprecated DRM.lookupReplicantsNodeNames()
    */
   @Deprecated public void testReplicantOrder() throws Exception
   {
      MBeanServer mbeanServer =
         MBeanServerFactory.createMBeanServer("mockPartitionA");
      try {
         
         //  Create a fake view for the MockHAPartition
         ClusterNode[] nodes = new ClusterNode[5];
         String[] names = new String[nodes.length];
         Integer[] replicants = new Integer[nodes.length];
         Vector<ClusterNode> allNodes = new Vector<ClusterNode>();
         for (int i = 0; i < nodes.length; i++)
         {
            nodes[i] = new ClusterNodeImpl(new IpAddress("127.0.0.1", 12340 + i));
            allNodes.add(nodes[i]);
            names[i] = nodes[i].getName();
            replicants[i] = new Integer(i);
         }
         
         MockHAPartition partition = new MockHAPartition(nodes[2]);
         partition.setCurrentViewClusterNodes(allNodes);
         
         DistributedReplicantManagerImpl drm = new DistributedReplicantManagerImpl(partition);
         drm.createService();
         drm.startService();
         
         CachingListener listener = new CachingListener();
         drm.registerListener("TEST", listener);
         
         SecureRandom random = new SecureRandom();
         boolean[] added = new boolean[nodes.length];
         List lookup = null;
         for (int i = 0; i < 10; i++)
         {
            int node = random.nextInt(nodes.length);
            if (added[node])
            {
               if (node == 2)
                  drm.remove("TEST");
               else
                  drm._remove("TEST", nodes[node].getName());
               added[node] = false;
            }
            else
            {     
               if (node == 2)
                  drm.add("TEST", replicants[node]);
               else
                  drm._add("TEST", nodes[node].getName(), replicants[node]);   
               added[node] = true;
            }
            
            // Confirm the proper order of the replicant node names
            lookup = maskListClass(drm.lookupReplicantsNodeNames("TEST"));
            confirmReplicantList(lookup, names, added);
            
            // Confirm the proper order of the replicants via lookupReplicants
            lookup = maskListClass(drm.lookupReplicants("TEST"));
            confirmReplicantList(lookup, replicants, added);
            
            // Confirm the listener got the same list
//            assertEquals("Listener received a correct list", lookup, 
//                         maskListClass(listener.replicants));
         }
         
         // Let the asynchronous notification thread catch up
         sleep(25);
         
         // Confirm all lists presented to the listener were properly ordered
         assertTrue("Listener saw no misordered lists", listener.clean);
         
      }
      finally {
         MBeanServerFactory.releaseMBeanServer(mbeanServer);
      }
   }
   
   public void testReplicantOrderWithLookupReplicantsNodes() throws Exception
   {
      MBeanServer mbeanServer =
         MBeanServerFactory.createMBeanServer("mockPartitionA");
      try {
         
         //  Create a fake view for the MockHAPartition
         ClusterNode[] nodes = new ClusterNode[5];
//         String[] names = new String[nodes.length];
         Integer[] replicants = new Integer[nodes.length];
         Vector<ClusterNode> allNodes = new Vector<ClusterNode>();
         for (int i = 0; i < nodes.length; i++)
         {
            nodes[i] = new ClusterNodeImpl(new IpAddress("127.0.0.1", 12340 + i));
            allNodes.add(nodes[i]);
//            names[i] = nodes[i].getName();
            replicants[i] = new Integer(i);
         }
         
         MockHAPartition partition = new MockHAPartition(nodes[2]);
         partition.setCurrentViewClusterNodes(allNodes);
         
         DistributedReplicantManagerImpl drm = new DistributedReplicantManagerImpl(partition);
         drm.createService();
         drm.startService();
         
         CachingListener listener = new CachingListener();
         drm.registerListener("TEST", listener);
         
         SecureRandom random = new SecureRandom();
         boolean[] added = new boolean[nodes.length];
         List lookup = null;
         List<ClusterNode> lookupReplicantsNodes = null;
         for (int i = 0; i < 10; i++)
         {
            int node = random.nextInt(nodes.length);
            if (added[node])
            {
               if (node == 2)
                  drm.remove("TEST");
               else
                  drm._remove("TEST", nodes[node].getName());
               added[node] = false;
            }
            else
            {     
               if (node == 2)
                  drm.add("TEST", replicants[node]);
               else
                  drm._add("TEST", nodes[node].getName(), replicants[node]);   
               added[node] = true;
            }
            
            // Confirm the proper order of the replicant node names
            lookupReplicantsNodes = maskListClass(drm.lookupReplicantsNodes("TEST"));
            confirmReplicantList(lookupReplicantsNodes, nodes, added);
            
            // Confirm the proper order of the replicants via lookupReplicants
            lookup = maskListClass(drm.lookupReplicants("TEST"));
            confirmReplicantList(lookup, replicants, added);
            
            // Confirm the listener got the same list
//            assertEquals("Listener received a correct list", lookup, 
//                         maskListClass(listener.replicants));
         }
         
         // Let the asynchronous notification thread catch up
         sleep(25);
         
         // Confirm all lists presented to the listener were properly ordered
         assertTrue("Listener saw no misordered lists", listener.clean);
         
      }
      finally {
         MBeanServerFactory.releaseMBeanServer(mbeanServer);
      }
   }
   
   private void confirmReplicantList(List current, Object[] all, boolean[] added)
   {
      Iterator iter = current.iterator();
      for (int i = 0; i < added.length; i++)
      {
         if (added[i])
         {
            assertTrue("List has more replicants", iter.hasNext());
            assertEquals("Replicant for node " + i + " is next", 
                         all[i], iter.next());
         }
      }
      assertFalse("List has no extra replicants", iter.hasNext());
   }
   
   /** Converts the given list to an ArrayList, if it isn't already */
   private List maskListClass(List toMask)
   {
      if (toMask instanceof ArrayList)
         return toMask;
      else if (toMask == null)
         return new ArrayList();
      else
         return new ArrayList(toMask);
   }

   private static void sleepThread(long millis)
   {
      try
      {
         Thread.sleep(millis);
      }
      catch (InterruptedException e) {
         e.printStackTrace();
      }
   }
   
   private static void printStackTrace(String threadName, Exception e)
   {
      if (e instanceof InterruptedException)
      {
         System.out.println("Stack trace for " + threadName);
         e.printStackTrace(System.out);
         System.out.println();
      }
   }

}

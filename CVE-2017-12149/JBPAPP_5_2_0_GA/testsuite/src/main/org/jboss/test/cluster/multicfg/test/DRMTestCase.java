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
package org.jboss.test.cluster.multicfg.test;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashSet;
import java.util.List;
import java.util.Vector;

import javax.management.MBeanServerConnection;
import javax.management.MBeanServerInvocationHandler;
import javax.management.Notification;
import javax.management.ObjectName;

import junit.framework.Test;

import org.jboss.jmx.adaptor.rmi.RMIAdaptorExt;
import org.jboss.jmx.adaptor.rmi.RMINotificationListener;
import org.jboss.logging.Logger;
import org.jboss.test.JBossClusteredTestCase;
import org.jboss.test.cluster.hapartition.drm.IReplicants;

/** 
 * Tests of the DistributedReplicantManagerImpl.
 *
 * @author  Scott.Stark@jboss.org
 * @author  Brian.Stansberry@jboss.com
 * @version $Revision: 85945 $
 */
public class DRMTestCase extends JBossClusteredTestCase
{  
   private static final String PARTITION_NAME = System.getProperty("jbosstest.partitionName", "DefaultPartition");
   
   static class TestListener extends UnicastRemoteObject
      implements RMINotificationListener
   {
      private static final long serialVersionUID = 1;
      private Logger log;

      public TestListener(Logger log) throws RemoteException
      {
         this.log = log;
      }
      public void handleNotification(Notification notification, Object handback)
         throws RemoteException
      {
         log.info("handleNotification, "+notification);
      }
   }

   public static Test suite() throws Exception
   {
      Test t1 = getDeploySetup(DRMTestCase.class, "drm-tests.sar");
      return t1;
   }

   public DRMTestCase(String name)
   {
      super(name);
   }

   public void testStateReplication()
      throws Exception
   {
      log.debug("+++ testStateReplication");
      log.info("java.rmi.server.hostname="+System.getProperty("java.rmi.server.hostname"));
      MBeanServerConnection[] adaptors = getAdaptors();
      String[] servers = super.getServers();
      RMIAdaptorExt server0 = (RMIAdaptorExt) adaptors[0];
      log.info("server0: "+server0);
      ObjectName clusterService = new ObjectName("jboss:service=HAPartition,partition=" + PARTITION_NAME);
      Vector view0 = (Vector) server0.getAttribute(clusterService, "CurrentView");
      log.info("server0: CurrentView, "+view0);
      log.debug("+++ testStateReplication 1");
      ObjectName drmService = new ObjectName("jboss.test:service=DRMTestCase");
      IReplicants drm0 = (IReplicants)
         MBeanServerInvocationHandler.newProxyInstance(server0, drmService,
         IReplicants.class, true);
      log.debug("+++ testStateReplication 2");
      log.info(MBeanServerInvocationHandler.class.getProtectionDomain());
      TestListener listener = new TestListener(log);
      server0.addNotificationListener(drmService, listener, null, null);
      log.info("server0 addNotificationListener");
      log.debug("+++ testStateReplication 3");
      String address = (String) drm0.lookupLocalReplicant();
      log.debug("+++ testStateReplication 4");
      log.info("server0: lookupLocalReplicant: "+address);
      assertTrue("server0: address("+address+") == server0("+servers[0]+")",
         address.equals(servers[0]));

      RMIAdaptorExt server1 = (RMIAdaptorExt) adaptors[1];
      log.info("server1: "+server1);
      Vector view1 = (Vector) server1.getAttribute(clusterService, "CurrentView");
      log.info("server1: CurrentView, "+view1);
      IReplicants drm1 = (IReplicants)
         MBeanServerInvocationHandler.newProxyInstance(server1, drmService,
         IReplicants.class, true);
      server1.addNotificationListener(drmService, listener, null, null);
      log.info("server1 addNotificationListener");
      address = (String) drm1.lookupLocalReplicant();
      log.info("server1: lookupLocalReplicant: "+address);
      assertTrue("server1: address("+address+") == server1("+servers[1]+")",
         address.equals(servers[1]));

      List replicants0 = drm0.lookupReplicants();
      List replicants1 = drm1.lookupReplicants();
      assertTrue("size of replicants0 == replicants1)",
         replicants0.size() == replicants1.size());
      HashSet testSet = new HashSet(replicants0);
      for(int n = 0; n < replicants0.size(); n ++)
      {
         Object entry = replicants1.get(n);
         assertTrue("replicants0 contains:"+entry, testSet.contains(entry));
      }

      //
      for(int n = 0; n < 10; n ++)
      {
         drm0.add("key"+n, "data"+n+".0");
         drm1.add("key"+n, "data"+n+".1");
      }
      for(int n = 0; n < 10; n ++)
      {
         String key = "key"+n;
         log.info("key: "+key);
         replicants0 = drm0.lookupReplicants(key);
         replicants1 = drm1.lookupReplicants(key);
         log.info("replicants0: "+replicants0);
         log.info("replicants1: "+replicants1);
         HashSet testSet0 = new HashSet(replicants0);
         HashSet testSet1 = new HashSet(replicants1);
         assertTrue("size of replicants0 == replicants1)",
            replicants0.size() == replicants1.size());
         Object entry = drm0.lookupLocalReplicant(key);
         log.info("drm0.lookupLocalReplicant, key="+key+", entry="+entry);
         assertTrue("replicants0 contains:"+entry, testSet0.contains(entry));
         assertTrue("replicants1 contains:"+entry, testSet1.contains(entry));
         entry = drm1.lookupLocalReplicant(key);
         log.info("drm1.lookupLocalReplicant, key="+key+", entry="+entry);
         assertTrue("replicants0 contains:"+entry, testSet0.contains(entry));
         assertTrue("replicants1 contains:"+entry, testSet1.contains(entry));
      }

      for(int n = 0; n < 10; n ++)
         drm0.remove("key"+n);

      server0.removeNotificationListener(drmService, listener);
      server1.removeNotificationListener(drmService, listener);
   }

}

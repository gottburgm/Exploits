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

import java.util.Vector;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;

import junit.framework.Test;

import org.jboss.test.JBossClusteredTestCase;
import org.jboss.test.cluster.hapartition.state.BadHAPartitionStateException;
import org.jboss.test.cluster.hapartition.state.CustomStateHAPartitionStateTransfer;
import org.jboss.test.cluster.hapartition.state.SimpleHAPartitionStateTransfer;

/**
 * Tests of HAPartitionImpl's state transfer.
 * 
 * @author <a href="mailto://brian.stansberry@jboss.com">Brian Stansberry</a>
 * @version $Revision: 85945 $
 */
public class HAPartitionStateTransferTestCase extends JBossClusteredTestCase
{

   public HAPartitionStateTransferTestCase(String name)
   {
      super(name);
   }        

   public static Test suite() throws Exception
   {
      Test t1 = JBossClusteredTestCase.getDeploySetup(HAPartitionStateTransferTestCase.class, "partitionstatetransfer.sar");
      return t1;
   } 

   protected void setUp() throws Exception
   {
      super.setUp();
   }

   protected void tearDown() throws Exception
   {
      super.tearDown();
   }
   
   public void testFailedStateProvider() throws Exception
   {
      MBeanServerConnection[] adaptors = getAdaptors();
      
      ObjectName recorder = new ObjectName("jboss:service=BadProviderPartitionRecorder");
      
      Exception e = (Exception) adaptors[1].getAttribute(recorder, "StartupException");      

      assertNotNull("Partition caught exception", e);
      
      Throwable parent = e;
      Throwable cause = e.getCause();
      while (!(parent instanceof IllegalStateException) && cause != null)
      {
         parent = cause;
         cause = parent.getCause();
      }
      
      if (!(parent instanceof IllegalStateException))
      {
         log.info("Wrong exception type caught", parent);
         fail(parent.getClass().getName() + " caught; should be IllegalStateException");
      }
      
      // Confirm the bad partition is removed from the current view
      ObjectName partition = new ObjectName("jboss:service=BadProviderPartition");
      Vector view = (Vector) adaptors[0].getAttribute(partition, "CurrentView");
      assertEquals("View size after failure is correct", 1, view.size());
   }

   public void testBadStateIntegration() throws Exception
   {
      MBeanServerConnection[] adaptors = getAdaptors();
      
      ObjectName recorder = new ObjectName("jboss:service=BadStatePartitionRecorder");
      
      Exception e = (Exception) adaptors[1].getAttribute(recorder, "StartupException");

      assertNotNull("Partition caught exception", e);
      
      Throwable parent = e;
      Throwable cause = e.getCause();
      while (!(parent instanceof BadHAPartitionStateException) && cause != null)
      {
         parent = cause;
         cause = parent.getCause();
      }
      
      if (!(parent instanceof BadHAPartitionStateException))
      {
         log.info("Wrong exception type caught", parent);
         fail(parent.getClass().getName() + " caught; should be BadHAPartitionStateException");
      }
      
      // Confirm the bad partition is removed from the current view
      ObjectName partition = new ObjectName("jboss:service=BadStatePartition");
      Vector view = (Vector) adaptors[0].getAttribute(partition, "CurrentView");
      assertEquals("View size after failure is correct", 1, view.size());
   }
   
   public void testNoStateTransfer() throws Exception
   {
      MBeanServerConnection[] adaptors = getAdaptors();
      
      ObjectName partition = new ObjectName("jboss:service=NoStatePartitionRecorder");
      
      Exception e = (Exception) adaptors[1].getAttribute(partition, "StartupException");

      assertNull("Partition started successfully", e);
      
   }
   
   public void testGoodStateTransfer() throws Exception
   {
      MBeanServerConnection[] adaptors = getAdaptors();
      
      ObjectName partition = new ObjectName("jboss:service=GoodStatePartitionRecorder");
      
      Exception e = (Exception) adaptors[1].getAttribute(partition, "StartupException");

      assertNull("Partition started successfully", e);
      
      ObjectName simple = new ObjectName("jboss.test:service=SimpleHAPartitionStateTransfer");
      
      Object simpleState = adaptors[1].getAttribute(simple, "TransferredState");
      
      assertEquals("Got simple state", SimpleHAPartitionStateTransfer.SIMPLE, simpleState);
      
      ObjectName custom = new ObjectName("jboss.test:service=CustomStateHAPartitionStateTransfer");
      
      Object customState = adaptors[1].getAttribute(custom, "TransferredState");
      
      assertNotNull("Got custom state", customState);
      assertEquals("Got correct custom state", CustomStateHAPartitionStateTransfer.CUSTOM, customState.toString());
   }

   /**
    * In this subclass this is a no-op because we are deliberately
    * deploying a sar that will fail in deployment
    */
   public void testServerFound() throws Exception
   {
      // do nothing
   }
   
   
}

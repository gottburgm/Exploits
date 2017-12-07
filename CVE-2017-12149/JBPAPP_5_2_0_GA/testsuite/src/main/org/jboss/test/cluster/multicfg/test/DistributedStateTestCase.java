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
import java.util.Collection;
import java.util.Vector;

import javax.management.MBeanServerConnection;
import javax.management.MBeanServerInvocationHandler;
import javax.management.ObjectName;
import javax.management.Notification;

import junit.framework.Test;

import org.jboss.test.JBossClusteredTestCase;
import org.jboss.test.cluster.hapartition.ds.DistributedStateUser;
import org.jboss.test.cluster.hapartition.ds.IDistributedState;
import org.jboss.test.cluster.hapartition.ds.IDistributedState.NotifyData;
import org.jboss.jmx.adaptor.rmi.RMIAdaptorExt;
import org.jboss.jmx.adaptor.rmi.RMINotificationListener;

/** Tests of http session replication
 *
 * @author  Scott.Stark@jboss.org
 * @version $Revision: 85945 $
 */
public class DistributedStateTestCase extends JBossClusteredTestCase
{
   private static final String PARTITION_NAME = System.getProperty("jbosstest.partitionName", "DefaultPartition");
   
   private static final String NOTIFY_KEY = "NotifyKey";
   private static final String NOTIFY_VALUE = "NotifyValue";
   // note - this static variable must match the category defined in ds-tests.sar
   private static final String NOTIFY_CATEGORY = "DistributedStateTestCase";
   
   class TestListener extends UnicastRemoteObject
      implements RMINotificationListener
   {
      /** The serialVersionUID */
      private static final long serialVersionUID = 3694780966459612453L;
      
      private String type = null;
      private Object data = null;
      
      TestListener() throws RemoteException
      {
      }
      
      public void handleNotification(Notification notification, Object handback)
         throws RemoteException
      {
         System.out.println(notification);
         type = notification.getType();
         data = notification.getUserData();
      }
      
      public String getNotificationType()
      {
         return type;
      }
      
      public Object getNotificationData()
      {
         return data;
      }
   }

   public static Test suite() throws Exception
   {
      Test t1 = getDeploySetup(DistributedStateTestCase.class, "ds-tests.sar");
      return t1;
   }

   public DistributedStateTestCase(String name)
   {
      super(name);
   }

   public void testStateReplication()
      throws Exception
   {
      log.debug("+++ testStateReplication");
      
      MBeanServerConnection[] adaptors = getAdaptors();
      RMIAdaptorExt server0 = (RMIAdaptorExt) adaptors[0];
      log.info("server0: "+server0);
      ObjectName clusterService = new ObjectName("jboss:service=HAPartition,partition=" + PARTITION_NAME);
      Vector view0 = (Vector) server0.getAttribute(clusterService, "CurrentView");
      log.info("server0: CurrentView, "+view0);
      ObjectName dsService = new ObjectName("jboss.test:service=DistributedStateTestCase");
      IDistributedState ds0 = (IDistributedState)
         MBeanServerInvocationHandler.newProxyInstance(server0, dsService,
         IDistributedState.class, true);
      TestListener listener0 = new TestListener();
      server0.addNotificationListener(dsService, listener0, null, null);
      ds0.put("key0", "value0");
      String value = (String) ds0.get("key0");
      log.info("server0: get(key0): "+value);
      assertTrue("server0: value == value0", value.equals("value0"));

      RMIAdaptorExt server1 = (RMIAdaptorExt) adaptors[1];
      log.info("server1: "+server1);
      Vector view1 = (Vector) server1.getAttribute(clusterService, "CurrentView");
      log.info("server1: CurrentView, "+view1);
      IDistributedState ds1 = (IDistributedState)
         MBeanServerInvocationHandler.newProxyInstance(server1, dsService,
         IDistributedState.class, true);
      TestListener listener1 = new TestListener();
      server1.addNotificationListener(dsService, listener1, null, null);
      value = (String) ds1.get("key0");
      log.info("server1: get(key0): "+value);
      assertTrue("server1: value == value0", value.equals("value0"));
      ds1.put("key0", "value1");
      value = (String) ds1.get("key0");
      assertTrue("server1: value == value1("+value+")", value.equals("value1"));
      value = (String) ds0.get("key0");
      assertTrue("server0: value == value1("+value+")", value.equals("value1"));
      
      ds1.put("key1", "value11");
      Collection categories = ds0.listAllCategories();
      log.info("server0: categories: " + categories);
      assertTrue("server0 has category " + NOTIFY_CATEGORY, categories.contains(NOTIFY_CATEGORY));
      categories = ds1.listAllCategories();
      log.info("server1: categories: " + categories);
      assertTrue("server1 has category " + NOTIFY_CATEGORY, categories.contains(NOTIFY_CATEGORY));
      
      Collection keys = ds0.listAllKeys(NOTIFY_CATEGORY);
      log.info("server0: keys: " + keys);
      assertEquals("server0 keys size", 2, keys.size());
      keys = ds1.listAllKeys(NOTIFY_CATEGORY);
      log.info("server1: keys: " + keys);
      assertEquals("server1 keys size", 2, keys.size());
      Collection vals = ds0.listAllValues(NOTIFY_CATEGORY);
      log.info("server0: values: " + vals);
      assertEquals("server0 values size", 2, vals.size());
      vals = ds1.listAllValues(NOTIFY_CATEGORY);
      log.info("server1: values: " + vals);
      assertEquals("server1 values size", 2, vals.size());

      ds0.remove("key0");
      value = (String) ds1.get("key0");
      assertTrue("server1: value == null("+value+")", value == null);
      value = (String) ds0.get("key0");
      assertTrue("server0: value == null("+value+")", value == null);

      // set a key/value on server0 and test its notifications on both servers
      ds0.put(NOTIFY_KEY, NOTIFY_VALUE);
      Thread.sleep(5000);
      
      // check the change notification on server0
      String type = listener0.getNotificationType();
      NotifyData data = (NotifyData)listener0.getNotificationData();
      assertTrue("server0: change notification type = " + type, type.equals(DistributedStateUser.NOTIFY_CHANGE));
      assertNotNull("server0: change notification data is null", data);
      String cat = data.category;
      String key = (String)data.key;
      String val = (String)data.value;
      boolean isLocal = data.locallyModified;
      assertTrue("server0: change notification category = " + cat, cat.equals(NOTIFY_CATEGORY));
      assertTrue("server0: change notification key = " + key, key.equals(NOTIFY_KEY));
      assertTrue("server0: change notification value = " + val, val.equals(NOTIFY_VALUE));
      assertTrue("server0: change notification isLocal = " + isLocal, isLocal == true);
      
      // check the change notification on server1
      type = listener1.getNotificationType();
      data = (NotifyData)listener1.getNotificationData();
      assertTrue("server1: change notification type = " + type, type.equals(DistributedStateUser.NOTIFY_CHANGE));
      assertNotNull("server1: change notification data is null", data);
      cat = data.category;
      key = (String)data.key;
      val = (String)data.value;
      isLocal = data.locallyModified;
      assertTrue("server1: change notification category = " + cat, cat.equals(NOTIFY_CATEGORY));
      assertTrue("server1: change notification key = " + key, key.equals(NOTIFY_KEY));
      assertTrue("server1: change notification value = " + val, val.equals(NOTIFY_VALUE));
      assertTrue("server1: change notification isLocal = " + isLocal, isLocal == false);
      
      // remove the key from server1 and check its notifications
      ds1.remove(NOTIFY_KEY);
      Thread.sleep(5000);
      
      // check the remove notification on server0
      type = listener0.getNotificationType();
      data = (NotifyData)listener0.getNotificationData();
      assertTrue("server0: removal notification type = " + type, type.equals(DistributedStateUser.NOTIFY_REMOVAL));
      assertNotNull("server0: removal notification data is null", data);
      cat = data.category;
      key = (String)data.key;
      val = (String)data.value;
      isLocal = data.locallyModified;
      assertTrue("server0: removal notification category = " + cat, cat.equals(NOTIFY_CATEGORY));
      assertTrue("server0: removal notification key = " + key, key.equals(NOTIFY_KEY));
      assertTrue("server01: removal notification value = " + val, val.equals(NOTIFY_VALUE));
      assertTrue("server0: removal notification isLocal = " + isLocal, isLocal == false);
      
      // check the remove notification on server1
      type = listener1.getNotificationType();
      data = (NotifyData)listener1.getNotificationData();
      assertTrue("server1: removal notification type = " + type, type.equals(DistributedStateUser.NOTIFY_REMOVAL));
      assertNotNull("server1: removal notification data is null", data);
      cat = data.category;
      key = (String)data.key;
      val = (String)data.value;
      isLocal = data.locallyModified;
      assertTrue("server1: removal notification category = " + cat, cat.equals(NOTIFY_CATEGORY));
      assertTrue("server1: removal notification key = " + key, key.equals(NOTIFY_KEY));
      assertTrue("server1: removal notification value = " + val, val.equals(NOTIFY_VALUE));
      assertTrue("server1: removal notification isLocal = " + isLocal, isLocal == true);
      
   }

}

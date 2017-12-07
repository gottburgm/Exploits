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
package org.jboss.test.cluster.hapartition.ds;

import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import javax.management.Notification;

import org.jboss.ha.framework.interfaces.DistributedState;
import org.jboss.ha.framework.interfaces.DistributedState.DSListenerEx;
import org.jboss.ha.framework.interfaces.HAPartition;
import org.jboss.ha.framework.server.HAPartitionLocator;
import org.jboss.logging.Logger;
import org.jboss.mx.util.JBossNotificationBroadcasterSupport;

/** Tests of the DistributedState service  

    @author <a href="mailto:Scott.Stark@jboss.org">Scott Stark</a>.
    @version $Revision: 85945 $
*/
public class DistributedStateUser extends JBossNotificationBroadcasterSupport
   implements DistributedStateUserMBean, DSListenerEx
{
   public static final String NOTIFY_CHANGE = "valueHasChanged";
   public static final String NOTIFY_REMOVAL = "keyHasBeenRemoved";
   
   protected static Logger log = Logger.getLogger(DistributedStateUser.class);

   protected DistributedState entryMap;
   protected String category;
   protected String partitionName;
   protected long sequence;

   public String getPartitionName()
   {
      return partitionName;
   }
   public void setPartitionName(String partitionName)
   {
      this.partitionName = partitionName;
   }

   public String getCategory()
   {
      return category;
   }
   public void setCategory(String category)
   {
      this.category = category;
   }

   public void start() throws Exception
   {
      // Lookup the partition
      HAPartition partition = HAPartitionLocator.getHAPartitionLocator().getHAPartition(partitionName, null);
      this.entryMap = partition.getDistributedStateService();
      log.debug("Obtained DistributedState from partition="+partitionName);
      entryMap.registerDSListenerEx(category, this);
   }
   public void stop()
   {
      entryMap.unregisterDSListenerEx(category, this);
      flush();
   }

   public Serializable get(Serializable key)
   {
      Serializable value = entryMap.get(category, key);
      log.debug("Get: "+key+", value: "+value);
      return value;
   }

   public void put(Serializable key, Serializable value)
      throws Exception
   {
      entryMap.set(category, key, value, false);
      log.debug("Put: "+key+", value: "+value);
   }

   public void remove(Serializable key)
      throws Exception
   {
      Object value = entryMap.remove(category, key, false);
      log.debug("Removed: "+key+", value: "+value);
   }
   
   public Collection listAllCategories()
   {
      // return results as a vector to avoid hashmap.keyset serialization error
      Collection cats = entryMap.getAllCategories();
      if (cats == null || cats.isEmpty()) {
         return new java.util.Vector();
      }
      java.util.Vector vcats = new java.util.Vector();
      java.util.Iterator iter = cats.iterator();
      while (iter.hasNext())
         vcats.add(iter.next());
      return vcats;
   }
   
   public Collection listAllKeys(String category)
   {
      // return results as a vector to avoid hashmap.keyset serialization error
      Collection keys = entryMap.getAllKeys(category);
      if (keys == null || keys.isEmpty()) {
         return new java.util.Vector();
      }
      java.util.Vector vkeys = new java.util.Vector();
      java.util.Iterator iter = keys.iterator();
      while (iter.hasNext())
         vkeys.add(iter.next());
      return vkeys;
   }
   
   public Collection listAllValues(String category)
   {
      // return results as a vector to avoid hashmap.keyset serialization error
      Collection vals = entryMap.getAllKeys(category);
      if (vals == null || vals.isEmpty()) {
         return new java.util.Vector();
      }
      java.util.Vector vvals = new java.util.Vector();
      java.util.Iterator iter = vals.iterator();
      while (iter.hasNext())
         vvals.add(iter.next());
      return vvals;
   }

   /** Remove all entries from the cache.
    */
   public void flush()
   {
      Collection keys = entryMap.getAllKeys(category);
      if(keys == null ) return;
      // Notify the entries of their removal
      Iterator iter = keys.iterator();
      while( iter.hasNext() )
      {
         Serializable key = (Serializable) iter.next();
         try
         {
            entryMap.remove(category, key);
         }
         catch(Exception e)
         {
            log.debug("Failed to remove: "+key, e);
         }
      }
   }

   public int size()
   {
      return entryMap.getAllKeys(category).size();
   }

   public void valueHasChanged(String category, Serializable key,
      Serializable value, boolean locallyModified)
   {
      NotifyData data = new NotifyData();
      data.category = category;
      data.key = key;
      data.value = value;
      data.locallyModified = locallyModified;
      String address = System.getProperty("jboss.bind.address");
      long id = nextSequence();
      Notification msg = new Notification(NOTIFY_CHANGE, this, id, address);
      msg.setUserData(data);
      log.debug("valueHasChanged, "+msg);
      super.sendNotification(msg);
   }

   public void keyHasBeenRemoved(String category, Serializable key,
      Serializable previousContent, boolean locallyModified)
   {
      NotifyData data = new NotifyData();
      data.category = category;
      data.key = key;
      data.value = previousContent;
      data.locallyModified = locallyModified;
      String address = System.getProperty("jboss.bind.address");
      long id = nextSequence();
      Notification msg = new Notification(NOTIFY_REMOVAL, this, id, address);
      msg.setUserData(data);
      log.debug("keyHasBeenRemoved, "+msg);
      super.sendNotification(msg);
   }

   private synchronized long nextSequence()
   {
      return sequence ++;
   }
}

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
package org.jboss.test.cluster.cache.bean;

import java.util.Map;
import java.util.Set;
import java.util.Vector;

import javax.ejb.CreateException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.management.Attribute;
import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.jboss.cache.pojo.PojoCache;
import org.jboss.mx.util.MBeanServerLocator;

/**
 * EJB proxy to the TreeCache MBean service. Used to be able to transport
 * user transactions from a test client to a TreeCache. Note that TreeCache MBean
 * is deployed during a test case run and is persistent throughout that run only.
 *
 * @author Ben Wang
 * @version $Revision: 81036 $
 * @ejb.bean type="Stateful"
 * name="test/TreeCacheMBeanTester"
 * jndi-name="ejb/test/TreeCacheMBeanTester"
 * view-type="remote"
 * @ejb.transaction type="Supports"
 */
public class TreeCacheMBeanTesterBean implements SessionBean
{
   /** The serialVersionUID */
   private static final long serialVersionUID = -8034966753463280684L;
   // Use a different service name so that it won't collide with the regular name.
   static final String OBJECT_NAME = "jboss.cache:service=testTreeCache";
   MBeanServer server;
   ObjectName cacheService;
   PojoCache cache=null;

   /**
    * @throws CreateException
    * @ejb.create-method
    */
   public void ejbCreate() throws CreateException
   {
      log("Creating TreeCache ejb proxy");
      init();
   }

   /**
    * @param name MBean object name.
    * @throws CreateException
    * @ejb.create-method
    */
   public void ejbCreate(String name) throws CreateException
   {
      log("I'm being created");
      init(name);
   }

   private void init() throws CreateException
   {
      init(OBJECT_NAME);
   }

   private void init(String name) throws CreateException
   {
      try {
         cacheService = new ObjectName(name);
         server = MBeanServerLocator.locate();
         cache=(PojoCache)server.getAttribute(new ObjectName("jboss.cache:service=testTreeCacheAop"),
         "PojoCache");
      } catch (Exception ex) {
         throw new CreateException(ex.toString());
      }
   }

   public void ejbActivate()
   {
   }

   public void ejbPassivate()
   {
   }

   public void ejbRemove()
   {
      log("I'm being removed");
   }

   public void setSessionContext(SessionContext ctx)
   {
   }

   /**
    * @return
    * @ejb.interface-method
    */
   public Vector getMembers() throws Exception
   {
      // FIXME restore after Cache exposes getMembers    
//      return cache.getMembers();
      throw new UnsupportedOperationException("See FIXME in bean code");
   }

   /**
    * @return
    * @ejb.interface-method
    */
   public int getCacheMode() throws Exception
   {
      return ((Integer) server.getAttribute(cacheService, "CacheMode")).intValue();
   }

   /**
    * @param mode
    * @ejb.interface-method
    */
   public void setCacheMode(int mode) throws Exception
   {
      server.setAttribute(cacheService, new Attribute("CacheMode",
            new Integer(mode)));
   }

   /**
    * @return
    * @ejb.interface-method
    */
   public boolean getLocking() throws Exception
   {
      return ((Boolean) server.getAttribute(cacheService, "Locking")).booleanValue();
   }

   /**
    * @param flag
    * @ejb.interface-method
    */
   public void setLocking(boolean flag) throws Exception
   {
      server.setAttribute(cacheService, new Attribute("Locking",
            new Boolean(flag)));
   }

   /**
    * @return
    * @ejb.interface-method
    */
   public int getLockingLevel() throws Exception
   {
      return ((Integer) server.getAttribute(cacheService, "LockingLevel")).intValue();
   }

   /**
    * @param level
    * @ejb.interface-method
    */
   public void setLocking(int level) throws Exception
   {
      server.setAttribute(cacheService, new Attribute("LockingLevel",
            new Integer(level)));
   }

   /**
    * @param fqn
    * @return
    * @ejb.interface-method
    */
   public Set getKeys(String fqn) throws Exception {
      return (Set) server.invoke(cacheService, "getKeys",
            new Object[]{fqn},
            new String[]{String.class.getName()});
   }

   /**
    * @param fqn
    * @param key
    * @return
    * @ejb.interface-method
    */
   public Object get(String fqn, String key) throws Exception
   {
      return server.invoke(cacheService, "get",
            new Object[]{fqn, key},
            new String[]{String.class.getName(),
                         Object.class.getName()});
   }

   /**
    * @param fqn
    * @return
    * @ejb.interface-method
    */
   public boolean exists(String fqn) throws Exception
   {
      return ((Boolean) server.invoke(cacheService, "exists",
            new Object[]{fqn},
            new String[]{String.class.getName()})).booleanValue();
   }

   /**
    * @param fqn
    * @param data
    * @throws Exception
    * @ejb.interface-method
    */
   public void put(String fqn, Map data) throws Exception
   {
      server.invoke(cacheService, "put",
            new Object[]{fqn, data},
            new String[]{String.class.getName(),
                         Map.class.getName()});
   }

   /**
    * @param fqn
    * @param key
    * @param value
    * @throws Exception
    * @ejb.interface-method
    */
   public void put(String fqn, String key, Object value) throws Exception
   {
      Object[] args = {fqn, key, value};
      String[] sig = {String.class.getName(),
                      Object.class.getName(),
                      Object.class.getName()};

      server.invoke(cacheService, "put", args, sig);
   }

   /**
    * @param fqn
    * @throws Exception
    * @ejb.interface-method
    */
   public void remove(String fqn) throws Exception
   {
      Object[] args = {fqn};
      String[] sig = {String.class.getName()};

      server.invoke(cacheService, "remove", args, sig);
   }


   /**
    * @param fqn
    * @param key
    * @return
    * @throws Exception
    * @ejb.interface-method
    */
   public Object remove(String fqn, String key) throws Exception
   {
      return server.invoke(cacheService, "remove",
            new Object[]{fqn, key},
            new String[]{String.class.getName(),
                         String.class.getName()});
   }

   /**
    * @param fqn
    * @ejb.interface-method
    */
   public void releaseAllLocks(String fqn) throws Exception
   {
      server.invoke(cacheService, "releaseAllLocks",
            new Object[]{fqn},
            new String[]{String.class.getName()});
   }

   /**
    * @param fqn
    * @return
    * @ejb.interface-method
    */
   public String print(String fqn) throws Exception
   {
      return (String) server.invoke(cacheService, "print",
            new Object[]{fqn},
            new String[]{String.class.getName()});
   }

   /**
    * @param fqn
    * @return
    * @ejb.interface-method
    */
   public Set getChildrenNames(String fqn) throws Exception
   {
      return (Set) server.invoke(cacheService, "getChildrenNames",
            new Object[]{fqn},
            new String[]{String.class.getName()});
   }

   /**
    * @return
    * @ejb.interface-method
    */
   public String printDetails() throws Exception
   {
      return (String) server.invoke(cacheService, "printDetails",
            null,
            null);
   }

   /**
    * @return
    * @ejb.interface-method
    */
   public String printLockInfo() throws Exception
   {
      return (String) server.invoke(cacheService, "printLockInfo",
            null,
            null);
   }

   private void log(String msg)
   {
      System.out.println("-- [" + Thread.currentThread().getName() + "]: " + msg);
   }

}

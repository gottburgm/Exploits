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
package org.jboss.test.cmp2.simple;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.naming.InitialContext;

import junit.framework.Test;

import org.jboss.ejb.EjbModule;
import org.jboss.ejb.plugins.cmp.ejbql.Catalog;
import org.jboss.ejb.plugins.cmp.jdbc.JDBCStoreManager;
import org.jboss.ejb.plugins.cmp.jdbc.ReadAheadCache;
import org.jboss.ejb.plugins.cmp.jdbc.bridge.JDBCEntityBridge;
import org.jboss.mx.util.MBeanServerLocator;
import org.jboss.test.JBossTestCase;
import org.jboss.test.util.ejb.EJBTestCase;

public class PageSizeUnitTestCase extends EJBTestCase
{
   private JDBCStoreManager jdbcStoreManager;
   private List pkList;

   public static Test suite() throws Exception
   {
      return JBossTestCase.getDeploySetup(
            PageSizeUnitTestCase.class, "cmp2-simple.jar");
   }

   public PageSizeUnitTestCase(String name)
   {
      super(name);
   }

   private SimpleHome getSimpleHome()
   {
      try
      {
         InitialContext jndiContext = new InitialContext();
         return (SimpleHome) jndiContext.lookup("cmp2/simple/Simple");
      }
      catch (Exception e)
      {
         fail("Exception in getSimpleHome: " + e.getMessage());
      }
      return null;
   }

   public void testOnLoad() throws Exception
   {
      SimpleHome simpleHome = getSimpleHome();
      Iterator simpleIter = simpleHome.findAll().iterator();
      Simple simple = (Simple) simpleIter.next();
      Object pk = simple.getPrimaryKey();
      ReadAheadCache cache = jdbcStoreManager.getReadAheadCache();
      ReadAheadCache.EntityReadAheadInfo info = cache.getEntityReadAheadInfo(pk);
      assertEquals(pkList.subList(0, 4), info.getLoadKeys());

      for (int i = 0; i < 4; i++)
      {
         Object o = pkList.get(i);
         assertNull(cache.getPreloadDataMap(o, false));
      }

      simple.getStringValue(); // test0

      assertNull(cache.getPreloadDataMap("test0", false));
      for (int i = 1; i < 4; i++)
      {
         Object o = pkList.get(i);
         assertNotNull(cache.getPreloadDataMap(o, false));
      }
      assertNull(cache.getPreloadDataMap("test4", false));

      simple = (Simple) simpleIter.next(); // test1
      simple.getStringValue();
      assertNull(cache.getPreloadDataMap("test1", false));
      simple = (Simple) simpleIter.next(); // test2
      simple.getStringValue();
      simple = (Simple) simpleIter.next(); // test3
      simple.getStringValue();
      for (int i = 0; i < 4; i++)
      {
         Object o = pkList.get(i);
         assertNull(cache.getPreloadDataMap(o, false));
      }

      simple = (Simple) simpleIter.next(); // test4
      simple.getStringValue();
      for (int i = 5; i < 8; i++)
      {
         Object o = pkList.get(i);
         assertNotNull(cache.getPreloadDataMap(o, false));
      }
   }

   public void setUpEJB(Properties props) throws Exception
   {
      super.setUpEJB(props);
      SimpleHome simpleHome = getSimpleHome();
      pkList = new ArrayList();

      for (int i = 0; i < 10; i++)
      {
         Simple simple = simpleHome.create("test" + i);
         pkList.add(simple.getPrimaryKey());
         simple.setIntegerPrimitive(i);
      }

      MBeanServer server = MBeanServerLocator.locateJBoss();
      ObjectName name = new ObjectName("jboss.j2ee:jndiName=cmp2/simple/Simple,service=EJB");
      EjbModule ejbModule = (EjbModule) server.getAttribute(name, "EjbModule");
      Catalog catalog = (Catalog) ejbModule.getModuleData("CATALOG");
      JDBCEntityBridge bridge = (JDBCEntityBridge) catalog.getEntityByEJBName("SimpleEJB");
      jdbcStoreManager = (JDBCStoreManager)bridge.getManager();
   }

   public void tearDownEJB(Properties props) throws Exception
   {
      SimpleHome simpleHome = getSimpleHome();
      Collection c = simpleHome.findAll();
      for (Iterator iterator = c.iterator(); iterator.hasNext();)
      {
         Simple simple = (Simple) iterator.next();
         simple.remove();
      }
      super.tearDownEJB(props);
   }
}


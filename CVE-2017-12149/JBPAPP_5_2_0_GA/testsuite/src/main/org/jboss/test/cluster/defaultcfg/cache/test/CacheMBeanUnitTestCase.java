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
package org.jboss.test.cluster.defaultcfg.cache.test;

import junit.framework.Test;
import org.jboss.test.JBossTestCase;
import org.jboss.test.cluster.cache.bean.TreeCacheMBeanTester;
import org.jboss.test.cluster.cache.bean.TreeCacheMBeanTesterHome;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;
import javax.transaction.UserTransaction;
import java.util.Properties;

/**
 * Tests transactional access to a local TreeCache MBean service.
 *
 * @version $Revision: 85945 $
 */
public class CacheMBeanUnitTestCase extends JBossTestCase
{
   TreeCacheMBeanTesterHome cache_home;
   TreeCacheMBeanTester cache1 = null, cache2 = null;
   Properties p_ = new Properties();


   public CacheMBeanUnitTestCase(String name)
   {
      super(name);
   }

   public void setUp() throws Exception
   {
      super.setUp();
      mySetup();
   }

   public void tearDown() throws Exception
   {
      super.tearDown();
      if (cache2 != null)
         cache2.remove(); // calls stop()
      if (cache1 != null)
         cache1.remove();
   }

   public void mySetup() throws Exception
   {
      Object obj;

      p_.put(Context.INITIAL_CONTEXT_FACTORY, "org.jnp.interfaces.NamingContextFactory");
      p_.put(Context.URL_PKG_PREFIXES, "jboss.naming:org.jnp.interfaces");
      p_.put(Context.PROVIDER_URL, "localhost:1099");
      obj = new InitialContext(p_).lookup(TreeCacheMBeanTesterHome.JNDI_NAME);
      cache_home = (TreeCacheMBeanTesterHome) PortableRemoteObject.narrow(obj, TreeCacheMBeanTesterHome.class);
   }


   public void testSetup()
   {
      assertNotNull("TreeCacheMBeanTesterHome ", cache_home);
   }

   public void testPutTx()
   {
      UserTransaction tx = null;

      try {
         tx = (UserTransaction) new InitialContext(p_).lookup("UserTransaction");
         assertNotNull("UserTransaction should not be null ", tx);
         // Note that to set tree cache properties, you can do it here
         // or go to transient-cache-service.xml.
         cache1 = cache_home.create();

         tx.begin();
         cache1.put("/a/b/c", "age", new Integer(38));
         assertEquals(new Integer(38), cache1.get("/a/b/c", "age"));

         cache1.put("/a/b/c", "age", new Integer(39));
         tx.commit();

         tx.begin();
         assertEquals(new Integer(39), cache1.get("/a/b/c", "age"));
         tx.commit();

         // Need to do cleanup ...
         tx.begin();
         cache1.remove("/a/b/c");
         cache1.remove("/a/b");
         cache1.remove("/a");
         tx.commit();

      } catch (Throwable t) {
         fail(t.toString());
         try {
            tx.rollback();
         } catch (Throwable t2) {
            ;
         }
         fail(t.toString());
      }
   }


   public void testRollbackTx()
   {
      UserTransaction tx = null;

      try {
         tx = (UserTransaction) new InitialContext(p_).lookup("UserTransaction");
         // Note that to set tree cache properties, you can do it here
         // or go to transient-cache-service.xml.
         assertNotNull("UserTransaction should not be null ", tx);
         cache1 = cache_home.create();
         // cache1.setLocking(true);

         tx.begin();
         cache1.put("/a/b/c", "age", new Integer(38));
         cache1.put("/a/b/c", "age", new Integer(39));
         tx.rollback();

         tx.begin();
         Integer val = (Integer) cache1.get("/a/b/c", "age");
         tx.commit();
         assertNull(val);
      } catch (Throwable t) {
//t.printStackTrace();
         fail(t.toString());
         try {
            tx.rollback();
         } catch (Throwable t2) {
            ;
         }
         fail(t.toString());
      }
   }

   public void testReplicatedPutTx()
   {
      UserTransaction tx = null;

      try {
         tx = (UserTransaction) new InitialContext(p_).lookup("UserTransaction");
         assertNotNull("UserTransaction should not be null ", tx);
         // Note that to set tree cache properties, you can do it here
         // or go to transient-cache-service.xml.
         cache1 = cache_home.create();
         cache2 = cache_home.create();

         tx.begin();
         cache1.put("/a/b/c", "age", new Integer(38));
         assertEquals(new Integer(38), cache1.get("/a/b/c", "age"));

         cache1.put("/a/b/c", "age", new Integer(39));
         tx.commit();

         tx.begin();
         assertEquals(new Integer(39), cache2.get("/a/b/c", "age"));
         tx.commit();

         // Need to do cleanup ...
         tx.begin();
         cache1.remove("/a/b/c");
         cache1.remove("/a/b");
         cache1.remove("/a");
         tx.commit();

      } catch (Throwable t) {
         fail(t.toString());
         try {
            tx.rollback();
         } catch (Throwable t2) {
            ;
         }
         fail(t.toString());
      }
   }

   void log(String msg)
   {
      getLog().info("-- [" + Thread.currentThread() + "]: " + msg);
   }


   public static Test suite() throws Exception
   {
//        return getDeploySetup(MBeanUnitTestCase.class, "cachetest.sar");
      // Deploy the package recursively. The jar contains ejb and the sar file contains
      // tree cache MBean service
        return getDeploySetup(getDeploySetup(CacheMBeanUnitTestCase.class, "cachetest.jar"),
            "cachetest.sar");
//      return new TestSuite(MBeanUnitTestCase.class);
   }

   public static void main(String[] args) throws Exception
   {
      junit.textui.TestRunner.run(suite());
   }


}
